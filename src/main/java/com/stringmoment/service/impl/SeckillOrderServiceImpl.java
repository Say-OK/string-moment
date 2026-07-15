package com.stringmoment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stringmoment.common.constant.OrderConstant;
import com.stringmoment.common.constant.SeckillConstant;
import com.stringmoment.common.exception.BusinessException;
import com.stringmoment.common.util.OrderNoGenerator;
import com.stringmoment.entity.Order;
import com.stringmoment.entity.OrderItem;
import com.stringmoment.entity.SeckillActivity;
import com.stringmoment.entity.SeckillOrder;
import com.stringmoment.mapper.SeckillOrderMapper;
import com.stringmoment.model.request.SeckillExecuteDTO;
import com.stringmoment.model.response.AddressVO;
import com.stringmoment.model.response.SeckillExecuteVO;
import com.stringmoment.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrder> implements SeckillOrderService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private DefaultRedisScript<Long> seckillLuaScript;

    @Autowired
    private SeckillActivityService seckillActivityService;

    @Autowired
    private UserAddressService userAddressService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private OrderNoGenerator orderNoGenerator;

    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * 检查用户秒杀资格
     */
    @Override
    public Integer checkSeckillQualification(Long activityId, Long userId) {
        // 1. 校验活动是否存在
        SeckillActivity activity = seckillActivityService.getById(activityId);
        if (activity == null) {
            throw new BusinessException("秒杀活动不存在");
        }

        // 2. 检查活动状态
        if (Objects.equals(activity.getStatus(), SeckillConstant.SECKILL_ACTIVITY_NOT_STARTED)) {
            return SeckillConstant.QUALIFY_ACTIVITY_NOT_START;
        } else if (Objects.equals(activity.getStatus(), SeckillConstant.SECKILL_ACTIVITY_ENDED)) {
            return SeckillConstant.QUALIFY_ACTIVITY_ENDED;
        }

        // 3. 检查库存（优先Redis缓存）
        String stockKey = SeckillConstant.SECKILL_STOCK_KEY_PREFIX + activityId;
        String stockStr = stringRedisTemplate.opsForValue().get(stockKey);
        int stock;
        if (stockStr != null) {
            stock = Integer.parseInt(stockStr);
        } else {
            // Redis缓存不存在，从MySQL读取并回填Redis
            stock = activity.getAvailableStock();
            stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(stock));
            log.info("活动[{}]库存缓存丢失，从MySQL回填: {}", activityId, stock);
        }
        if (stock <= 0) {
            return SeckillConstant.QUALIFY_STOCK_LACK;
        }

        // 4. 检查是否重复秒杀（优先Redis Set）
        String userKey = SeckillConstant.SECKILL_USER_KEY_PREFIX + activityId;
        Boolean userExists = stringRedisTemplate.opsForSet().isMember(userKey, userId.toString());
        if (Boolean.TRUE.equals(userExists)) {
            return SeckillConstant.QUALIFY_REPEAT_PURCHASE;
        }
        
        // Redis Set不存在，查MySQL兜底并回填Redis
        Boolean userKeyExists = stringRedisTemplate.hasKey(userKey);
        if (!Boolean.TRUE.equals(userKeyExists)) {
            // 使用分布式锁重建用户Set缓存
            rebuildUserCacheWithLock(activityId, userKey);

            // 重建后再次检查用户是否已购买
            Boolean userExistsAfterRebuild = stringRedisTemplate.opsForSet().isMember(userKey, userId.toString());
            if (Boolean.TRUE.equals(userExistsAfterRebuild)) {
                return SeckillConstant.QUALIFY_REPEAT_PURCHASE;
            }
        }

        // 5. 校验通过，可以秒杀
        return SeckillConstant.QUALIFY_CAN_SECKILL;
    }

    /**
     * 执行秒杀（Redis操作在事务外，MySQL操作在事务内）
     */
    @Override
    public SeckillExecuteVO executeSeckill(SeckillExecuteDTO dto, Long userId) {
        Long activityId = dto.getSeckillActivityId();
        Long addressId = dto.getAddressId();

        // 1. 前置校验（查活动、地址）
        SeckillActivity activity = seckillActivityService.getById(activityId);
        if (activity == null) {
            return buildFailVO(SeckillConstant.SECKILL_RESULT_ERROR, "秒杀活动不存在");
        }

        AddressVO address = userAddressService.getAddressByIdAndUser(addressId, userId);
        if (address == null) {
            return buildFailVO(SeckillConstant.SECKILL_RESULT_ERROR, "收货地址不存在");
        }

        // 2. 确保Redis缓存存在
        ensureRedisCache(activity);

        // 3. 执行Lua脚本扣Redis库存
        Long result = executeLuaScript(activityId, userId, activity);
        if (result.intValue() != SeckillConstant.SECKILL_RESULT_SUCCESS) {
            return handleLuaResult(result);
        }

        // 4. 执行MySQL事务：创建订单 + 扣减库存（使用编程式事务）
        try {
            return transactionTemplate.execute(status -> {
                // 1. 创建订单
                Order order = createOrder(userId, activity, address);
                Long seckillOrderId = createSeckillOrder(userId, order.getId(), activity.getId(), activity.getSeckillPrice());

                // 2. 扣减MySQL库存
                decreaseMySQLStock(activity.getId());

                return buildSuccessVO(order, seckillOrderId, activity);
            });
        } catch (Exception e) {
            log.error("MySQL操作失败，同步回滚Redis，定时任务作为最终兜底校对库存", e);
            // 加回Redis库存、删除用户标记
            if (result.intValue() == SeckillConstant.SECKILL_RESULT_SUCCESS) {
                String stockKey = SeckillConstant.SECKILL_STOCK_KEY_PREFIX + activityId;
                String userKey = SeckillConstant.SECKILL_USER_KEY_PREFIX + activityId;
                stringRedisTemplate.opsForValue().increment(stockKey);
                stringRedisTemplate.opsForSet().remove(userKey, userId.toString());
            }
            throw new BusinessException("秒杀失败：" + e.getMessage());
        }
    }

    /**
     * 从数据库恢复缓存（库存 + 用户购买记录）
     * 库存缓存：从内存对象读取，无需锁
     * 用户购买记录缓存：需要查数据库，使用分布式锁避免并发查询
     */
    private void ensureRedisCache(SeckillActivity activity) {
        Long activityId = activity.getId();
        String stockKey = SeckillConstant.SECKILL_STOCK_KEY_PREFIX + activityId;
        String userKey = SeckillConstant.SECKILL_USER_KEY_PREFIX + activityId;

        // 检查缓存是否已存在
        Boolean stockExists = stringRedisTemplate.hasKey(stockKey);
        Boolean userExists = stringRedisTemplate.hasKey(userKey);

        // 库存缓存丢失：使用setIfAbsent避免并发写入
        if (!Boolean.TRUE.equals(stockExists)) {
            Boolean firstWrite = stringRedisTemplate.opsForValue()
                    .setIfAbsent(stockKey, String.valueOf(activity.getAvailableStock()));
            if (Boolean.TRUE.equals(firstWrite)) {
                log.info("活动[{}] Redis库存缓存重建完成: {}", activityId, activity.getAvailableStock());
            }
        }

        // 用户购买记录缓存丢失：需要查数据库，使用分布式锁
        if (!Boolean.TRUE.equals(userExists)) {
            rebuildUserCacheWithLock(activityId, userKey);
        }
    }

    /**
     * 使用分布式锁重建用户购买记录缓存
     */
    private void rebuildUserCacheWithLock(Long activityId, String userKey) {
        String lockKey = SeckillConstant.SECKILL_USER_LOCK_KEY_PREFIX + activityId;

        try {
            // 尝试获取锁
            Boolean locked = stringRedisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "1", SeckillConstant.LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(locked)) {
                try {
                    // 双重检查：获取锁后再次检查缓存是否存在
                    Boolean exists = stringRedisTemplate.hasKey(userKey);
                    if (!Boolean.TRUE.equals(exists)) {
                        rebuildUserSetFromDB(activityId, userKey);
                    }
                } finally {
                    // 释放锁
                    stringRedisTemplate.delete(lockKey);
                }
            } else {
                // 未获取到锁，等待其他线程完成重建
                boolean cacheReady = false;

                for (int retry = 0; retry < SeckillConstant.LOCK_RETRY_TIMES; retry++) {
                    try {
                        Thread.sleep(SeckillConstant.LOCK_WAIT_MILLISECONDS);
                        Boolean exists = stringRedisTemplate.hasKey(userKey);
                        if (Boolean.TRUE.equals(exists)) {
                            cacheReady = true;
                            break;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                // 缓存未就绪，抛出异常阻断请求，避免重复下单漏洞
                if (!cacheReady) {
                    log.debug("活动[{}] 用户缓存重建超时，拒绝请求", activityId);
                    throw new BusinessException("系统繁忙，请稍后重试");
                }
            }
        } catch (BusinessException e) {
            // 等待缓存超时手动throw的业务异常，属于预期限流，直接透传
            throw e;
        } catch (Exception e) {
            // Redis宕机、数据库超时、网络IO异常等非预期系统故障
            log.error("活动[{}] 用户缓存重建失败", activityId, e);
            throw new BusinessException("系统繁忙，请稍后重试");
        }
    }

    /**
     * 从MySQL订单表重建用户购买记录缓存
     */
    private void rebuildUserSetFromDB(Long activityId, String userKey) {
        // 查询已购买的用户ID列表
        List<Long> userIds = lambdaQuery()
                .select(SeckillOrder::getUserId)
                .eq(SeckillOrder::getSeckillActivityId, activityId)
                .list()
                .stream()
                .map(SeckillOrder::getUserId)
                .toList();

        if (!userIds.isEmpty()) {
            // 批量添加到Redis Set
            String[] userIdStrs = userIds.stream()
                    .map(String::valueOf)
                    .toArray(String[]::new);
            stringRedisTemplate.opsForSet().add(userKey, userIdStrs);
            log.info("活动[{}] Redis用户Set缓存重建完成: {} 位用户", activityId, userIds.size());
        } else {
            // 确保Set存在（即使为空）
            stringRedisTemplate.opsForSet().add(userKey, SeckillConstant.REDIS_SET_EMPTY_PLACEHOLDER);
            stringRedisTemplate.opsForSet().remove(userKey, SeckillConstant.REDIS_SET_EMPTY_PLACEHOLDER);
            log.info("活动[{}] Redis用户Set缓存初始化为空Set", activityId);
        }
    }

    private Long executeLuaScript(Long activityId, Long userId, SeckillActivity activity) {
        String stockKey = SeckillConstant.SECKILL_STOCK_KEY_PREFIX + activityId;
        String userKey = SeckillConstant.SECKILL_USER_KEY_PREFIX + activityId;

        long currentTime = System.currentTimeMillis() / 1000;
        // 使用系统默认时区，避免时区硬编码导致的跨环境问题
        ZoneOffset zoneOffset = ZonedDateTime.now().getOffset();
        long startTime = activity.getStartTime().toEpochSecond(zoneOffset);
        long endTime = activity.getEndTime().toEpochSecond(zoneOffset);

        return stringRedisTemplate.execute(
            seckillLuaScript,
            Arrays.asList(stockKey, userKey),
            String.valueOf(userId),
            String.valueOf(activity.getStatus()),
            String.valueOf(currentTime),
            String.valueOf(startTime),
            String.valueOf(endTime)
        );
    }

    private SeckillExecuteVO handleLuaResult(Long result) {
        return switch (result.intValue()) {
            case 1 -> buildFailVO(SeckillConstant.SECKILL_RESULT_STOCK_LACK, "库存不足");
            case 2 -> buildFailVO(SeckillConstant.SECKILL_RESULT_REPEAT, "您已参与过本次秒杀活动");
            case 3 -> buildFailVO(SeckillConstant.SECKILL_RESULT_NOT_START, "活动未开始");
            case 4 -> buildFailVO(SeckillConstant.SECKILL_RESULT_END, "活动已结束");
            default -> buildFailVO(SeckillConstant.SECKILL_RESULT_ERROR, "秒杀失败：未知错误（Lua返回码：" + result + "）");
        };
    }

    private Order createOrder(Long userId, SeckillActivity activity, AddressVO address) {
        // 1. 创建主订单
        Order order = Order.builder()
            .orderNo(orderNoGenerator.generateSeckillOrderNo(userId))
            .userId(userId)
            .totalAmount(activity.getSeckillPrice())
            .orderType(OrderConstant.ORDER_TYPE_SECKILL)
            .seckillActivityId(activity.getId())
            .addressId(address.getId())
            .receiverName(address.getReceiverName())
            .receiverPhone(address.getReceiverPhone())
            .receiverProvince(address.getProvince())
            .receiverCity(address.getCity())
            .receiverDistrict(address.getDistrict())
            .receiverDetailAddress(address.getDetailAddress())
            .status(OrderConstant.ORDER_STATUS_PENDING_PAY)
            .build();
        orderService.save(order);

        // 2. 创建订单项（使用快照信息）
        OrderItem orderItem = OrderItem.builder()
                .orderId(order.getId())
                .productId(activity.getProductId())
                .productName(activity.getSeckillProductName())
                .productImage(activity.getSeckillProductImage())
                .unitPrice(activity.getSeckillPrice())
                .quantity(1)
                .totalPrice(activity.getSeckillPrice())
                .build();
        orderItemService.save(orderItem);
        return order;
    }

    /**
     * 创建秒杀订单
     */
    private Long createSeckillOrder(Long userId, Long orderId, Long activityId, BigDecimal seckillPrice) {
        // MySQL物理防重兜底：再次检查是否已购买
        boolean hasPurchased = lambdaQuery()
                .eq(SeckillOrder::getUserId, userId)
                .eq(SeckillOrder::getSeckillActivityId, activityId)
                .exists();
        if (hasPurchased) {
            throw new BusinessException("您已购买过该秒杀商品，无法重复下单");
        }

        try {
            SeckillOrder seckillOrder = SeckillOrder.builder()
                .userId(userId)
                .orderId(orderId)
                .seckillActivityId(activityId)
                .seckillPrice(seckillPrice)
                .build();
            save(seckillOrder);
            return seckillOrder.getId();
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 捕获唯一键冲突异常（高并发下极端情况：两条请求同时通过exists检查）
            log.warn("用户[{}]活动[{}]触发唯一键冲突，已成功防止重复下单", userId, activityId);
            throw new BusinessException("您已购买过该秒杀商品，无法重复下单");
        }
    }

    /**
     * 扣减MySQL库存
     */
    private void decreaseMySQLStock(Long activityId) {
        boolean success = seckillActivityService.lambdaUpdate()
                .eq(SeckillActivity::getId, activityId)
                .gt(SeckillActivity::getAvailableStock, 0)
                .setSql("available_stock = available_stock - 1")
                .update();

        if (!success) {
            throw new BusinessException("秒杀库存扣减失败");
        }
    }


    private SeckillExecuteVO buildSuccessVO(Order order, Long seckillOrderId, SeckillActivity activity) {
        return SeckillExecuteVO.builder()
            .seckillOrderId(seckillOrderId)
            .orderId(order.getId())
            .orderNo(order.getOrderNo())
            .status(SeckillConstant.SECKILL_RESULT_SUCCESS)
            .message("秒杀成功，请在" + SeckillConstant.SECKILL_PAY_TIMEOUT_MINUTES + "分钟内完成支付，超时订单将自动取消")
            .seckillPrice(activity.getSeckillPrice())
            .productName(activity.getSeckillProductName())
            .productImage(activity.getSeckillProductImage())
            .totalAmount(order.getTotalAmount())
            .quantity(1)
            .paymentTimeout(SeckillConstant.SECKILL_PAY_TIMEOUT_MINUTES)
            .build();
    }

    private SeckillExecuteVO buildFailVO(Integer status, String message) {
        return SeckillExecuteVO.builder()
            .status(status)
            .message(message)
            .build();
    }
}
