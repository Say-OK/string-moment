package com.stringmoment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stringmoment.common.constant.OrderConstant;
import com.stringmoment.common.constant.SeckillConstant;
import com.stringmoment.common.exception.BusinessException;
import com.stringmoment.common.util.OrderNoGenerator;
import com.stringmoment.entity.*;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Objects;


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
    private ProductService productService;

    @Autowired
    private UserAddressService userAddressService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private OrderNoGenerator orderNoGenerator;

    /**
     * 检查用户秒杀资格（前端展示用）
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

        // 3. 检查库存
        if (activity.getAvailableStock() <= 0) {
            return SeckillConstant.QUALIFY_STOCK_LACK;
        }

        // 4. 检查是否重复秒杀
        boolean hasParticipated = lambdaQuery()
                .eq(SeckillOrder::getSeckillActivityId, activityId)
                .eq(SeckillOrder::getUserId, userId)
                .exists();
        if (hasParticipated) {
            return SeckillConstant.QUALIFY_REPEAT_PURCHASE;
        }

        // 5. 校验通过，可以秒杀
        return SeckillConstant.QUALIFY_CAN_SECKILL;
    }

    /**
     * 执行秒杀
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SeckillExecuteVO executeSeckill(SeckillExecuteDTO dto, Long userId) {
        Long activityId = dto.getSeckillActivityId();
        Long addressId = dto.getAddressId();

        // 1. 前置校验（查活动、商品、地址）
        SeckillActivity activity = seckillActivityService.getById(activityId);
        if (activity == null) {
            return buildFailVO(SeckillConstant.SECKILL_RESULT_ERROR, "秒杀活动不存在");
        }

        Product product = productService.getById(activity.getProductId());
        if (product == null) {
            return buildFailVO(SeckillConstant.SECKILL_RESULT_ERROR, "商品不存在");
        }

        AddressVO address = userAddressService.getAddressByIdAndUser(addressId, userId);
        if (address == null) {
            return buildFailVO(SeckillConstant.SECKILL_RESULT_ERROR, "收货地址不存在");
        }

        ensureRedisCache(activity);

        Long result = null;
        try {
            // 2. 执行Lua脚本扣Redis库存
            result = executeLuaScript(activityId, userId, activity);
            if (result.intValue() != SeckillConstant.SECKILL_RESULT_SUCCESS) {
                return handleLuaResult(result);
            }

            // 3. 创建订单
            Order order = createOrder(userId, activity, product, address);
            Long seckillOrderId = createSeckillOrder(userId, order.getId(), activityId, activity.getSeckillPrice());

            return buildSuccessVO(order, seckillOrderId, activity.getSeckillPrice(), product);

        } catch (Exception e) {
            log.error("秒杀失败，开始回滚", e);
            // 只有Lua扣Redis成功，才需要回滚Redis
            if (result != null && result.intValue() == SeckillConstant.SECKILL_RESULT_SUCCESS) {
                String stockKey = SeckillConstant.SECKILL_STOCK_KEY_PREFIX + activityId;
                String userKey = SeckillConstant.SECKILL_USER_KEY_PREFIX + activityId;
                stringRedisTemplate.opsForValue().increment(stockKey); // 库存回滚
                stringRedisTemplate.opsForSet().remove(userKey, userId.toString()); // 用户记录回滚
            }
            // 重新抛异常，触发事务回滚（如果是创建订单失败，回滚MySQL；如果是Lua执行失败，MySQL没操作，回滚无影响）
            throw new BusinessException("秒杀失败：" + e.getMessage());
        }
    }

    /**
     * 从数据库恢复缓存
     */
    private void ensureRedisCache(SeckillActivity activity) {
        String stockKey = SeckillConstant.SECKILL_STOCK_KEY_PREFIX + activity.getId();
        Boolean stockExists = stringRedisTemplate.hasKey(stockKey);

        if (!Boolean.TRUE.equals(stockExists)) {
            log.warn("活动[{}] Redis缓存丢失，仅恢复库存（用户Set需从MySQL订单表重建）: {}", activity.getId(), activity.getAvailableStock());
            stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(activity.getAvailableStock()));
        }
    }

    private Long executeLuaScript(Long activityId, Long userId, SeckillActivity activity) {
        String stockKey = SeckillConstant.SECKILL_STOCK_KEY_PREFIX + activityId;
        String userKey = SeckillConstant.SECKILL_USER_KEY_PREFIX + activityId;

        long currentTime = System.currentTimeMillis() / 1000;
        long startTime = activity.getStartTime().toEpochSecond(ZoneOffset.ofHours(8));
        long endTime = activity.getEndTime().toEpochSecond(ZoneOffset.ofHours(8));

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

    private Order createOrder(Long userId, SeckillActivity activity, Product product, AddressVO address) {
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

        // 2. 创建订单项
        OrderItem orderItem = OrderItem.builder()
                .orderId(order.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productImage(product.getImageUrl())
                .unitPrice(activity.getSeckillPrice())
                .quantity(1)
                .totalPrice(activity.getSeckillPrice())
                .build();
        orderItemService.save(orderItem);
        return order;
    }

    private Long createSeckillOrder(Long userId, Long orderId, Long activityId, BigDecimal seckillPrice) {
        SeckillOrder seckillOrder = SeckillOrder.builder()
            .userId(userId)
            .orderId(orderId)
            .seckillActivityId(activityId)
            .seckillPrice(seckillPrice)
            .build();
        save(seckillOrder);
        return seckillOrder.getId();
    }


    private SeckillExecuteVO buildSuccessVO(Order order, Long seckillOrderId, BigDecimal seckillPrice, Product product) {
        return SeckillExecuteVO.builder()
            .seckillOrderId(seckillOrderId)
            .orderId(order.getId())
            .orderNo(order.getOrderNo())
            .status(SeckillConstant.SECKILL_RESULT_SUCCESS)
            .message("秒杀成功，请在" + SeckillConstant.SECKILL_PAY_TIMEOUT_MINUTES + "分钟内完成支付，超时订单将自动取消")
            .seckillPrice(seckillPrice)
            .productName(product.getName())
            .productImage(product.getImageUrl())
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
