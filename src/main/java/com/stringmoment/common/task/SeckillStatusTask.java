package com.stringmoment.common.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stringmoment.common.constant.SeckillConstant;
import com.stringmoment.entity.SeckillActivity;
import com.stringmoment.entity.SeckillOrder;
import com.stringmoment.mapper.SeckillOrderMapper;
import com.stringmoment.service.SeckillActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * 秒杀活动状态定时任务
 * 负责活动状态更新 + Redis缓存生命周期管理
 */
@Slf4j
@Component
public class SeckillStatusTask {

    @Autowired
    private SeckillActivityService seckillActivityService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    /**
     * 每10秒：更新一次活动状态，管理Redis缓存的初始化和清理
     * 查询所有未删除的活动（不限状态），根据时间重新计算状态
     */
    @Scheduled(fixedRate = 10000)
    public void updateActivityStatus() {
        List<SeckillActivity> activities = seckillActivityService.list(new LambdaQueryWrapper<>());

        if (activities.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<SeckillActivity> needUpdate = new ArrayList<>();

        for (SeckillActivity activity : activities) {
            Integer oldStatus = activity.getStatus();
            Integer newStatus = calculateStatus(activity, now);

            if (!oldStatus.equals(newStatus)) {
                activity.setStatus(newStatus);
                needUpdate.add(activity);

                handleStatusChange(activity, oldStatus, newStatus);
            }
        }

        if (!needUpdate.isEmpty()) {
            seckillActivityService.updateBatchById(needUpdate);
        }
    }

    /**
     * 每1分钟：缓存校对任务（库存校对 + 用户缓存检查）
     */
    @Scheduled(fixedRate = 60000)
    public void cacheReconcileTask() {
        // 1. 查询进行中的活动（一次查询，避免重复IO）
        List<SeckillActivity> activities = seckillActivityService.list(
            new LambdaQueryWrapper<SeckillActivity>()
                .eq(SeckillActivity::getStatus, SeckillConstant.SECKILL_ACTIVITY_ON_GOING)
        );

        if (activities.isEmpty()) {
            return;
        }

        // 2. 同时校对库存和用户缓存
        int reconciledStockCount = 0;
        int rebuiltUserCacheCount = 0;

        for (SeckillActivity activity : activities) {
            Long activityId = activity.getId();
            String stockKey = SeckillConstant.SECKILL_STOCK_KEY_PREFIX + activityId;
            String userKey = SeckillConstant.SECKILL_USER_KEY_PREFIX + activityId;

            // 2.1 校对库存：读取MySQL真实库存，检查是否与Redis一致
            Integer mysqlStock = activity.getAvailableStock();
            String redisStockStr = stringRedisTemplate.opsForValue().get(stockKey);
            Integer redisStock = redisStockStr != null ? Integer.parseInt(redisStockStr) : null;

            if (redisStock == null) {
                // 场景1：Redis无key，使用setIfAbsent回填
                Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(stockKey, String.valueOf(mysqlStock));
                if (Boolean.TRUE.equals(success)) {
                    log.info("库存校对：活动[{}] Redis无缓存，已回填MySQL库存({})", activityId, mysqlStock);
                    reconciledStockCount++;
                }
                // 如果setIfAbsent失败，说明其他线程已重建，无需处理
            } else if (!redisStock.equals(mysqlStock)) {
                // 场景2：Redis有key但数值不一致，不主动覆盖（可能正在秒杀）
                // 打印告警，人工介入核对数据
                log.error("库存校对告警：活动[{}] Redis库存({}) 与 MySQL库存({}) 不一致，可能存在并发秒杀，请人工核查",
                    activityId, redisStock, mysqlStock);
            }

            // 2.2 检查用户购买记录缓存是否存在
            Boolean userCacheExists = stringRedisTemplate.hasKey(userKey);

            if (!Boolean.TRUE.equals(userCacheExists)) {
                // 缓存丢失，从MySQL重建
                List<SeckillOrder> purchasedOrders = seckillOrderMapper.selectList(
                    new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getSeckillActivityId, activityId)
                        .select(SeckillOrder::getUserId)
                );

                if (!purchasedOrders.isEmpty()) {
                    String[] userIds = purchasedOrders.stream()
                        .map(order -> String.valueOf(order.getUserId()))
                        .toArray(String[]::new);
                    stringRedisTemplate.opsForSet().add(userKey, userIds);
                    log.info("用户Set缓存丢失：活动[{}]已重建，购买用户数={}", activityId, userIds.length);
                } else {
                    // 初始化空Set
                    stringRedisTemplate.opsForSet().add(userKey, SeckillConstant.REDIS_SET_EMPTY_PLACEHOLDER);
                    stringRedisTemplate.opsForSet().remove(userKey, SeckillConstant.REDIS_SET_EMPTY_PLACEHOLDER);
                    log.info("用户Set缓存丢失：活动[{}]已重建，购买用户数=0（空Set）", activityId);
                }

                rebuiltUserCacheCount++;
            }
        }

        // 3. 输出汇总日志（只在有实际操作时）
        if (reconciledStockCount > 0 || rebuiltUserCacheCount > 0) {
            log.info("缓存校对完成：共检查{}个活动，库存校对{}个，用户缓存重建{}个",
                activities.size(), reconciledStockCount, rebuiltUserCacheCount);
        }
    }

    private Integer calculateStatus(SeckillActivity activity, LocalDateTime now) {
        if (now.isBefore(activity.getStartTime())) {
            return SeckillConstant.SECKILL_ACTIVITY_NOT_STARTED;
        } else if (now.isAfter(activity.getEndTime())) {
            return SeckillConstant.SECKILL_ACTIVITY_ENDED;
        } else {
            return SeckillConstant.SECKILL_ACTIVITY_ON_GOING;
        }
    }

    /**
     * 处理状态变化时的Redis缓存操作
     * <p>
     * 正常流程：
     * - 未开始(0) → 进行中(1)：初始化Redis缓存
     * - 进行中(1) → 已结束(2)：清理Redis缓存
     * <p>
     * 逆向流程（管理员修改时间）：
     * - 已结束(2) → 进行中(1)：重新初始化Redis缓存，清空原有购买记录
     * - 已结束(2) → 未开始(0)：清理Redis缓存
     * - 进行中(1) → 未开始(0)：清理Redis缓存
     */
    private void handleStatusChange(SeckillActivity activity, Integer oldStatus, Integer newStatus) {
        Long activityId = activity.getId();

        // 正常流程：未开始 → 进行中
        if (Objects.equals(oldStatus, SeckillConstant.SECKILL_ACTIVITY_NOT_STARTED) && Objects.equals(newStatus, SeckillConstant.SECKILL_ACTIVITY_ON_GOING)) {
            initRedisCache(activity);
            log.info("秒杀活动[{}]开始，已初始化Redis库存: {}", activityId, activity.getAvailableStock());
        }
        
        // 正常流程：进行中 → 已结束
        else if (Objects.equals(oldStatus, SeckillConstant.SECKILL_ACTIVITY_ON_GOING) && Objects.equals(newStatus, SeckillConstant.SECKILL_ACTIVITY_ENDED)) {
            clearRedisCache(activityId);
            log.info("秒杀活动[{}]结束，已清理Redis缓存", activityId);
        }
        
        // 逆向流程：已结束 → 进行中（管理员修改结束时间往后延）
        else if (Objects.equals(oldStatus, SeckillConstant.SECKILL_ACTIVITY_ENDED) && Objects.equals(newStatus, SeckillConstant.SECKILL_ACTIVITY_ON_GOING)) {
            initRedisCache(activity);
            log.warn("秒杀活动[{}]重新开始（管理员修改时间），已重新初始化Redis缓存，清空原有购买记录", activityId);
        }
        
        // 逆向流程：已结束 → 未开始（管理员修改开始时间往后延）
        else if (Objects.equals(oldStatus, SeckillConstant.SECKILL_ACTIVITY_ENDED) && Objects.equals(newStatus, SeckillConstant.SECKILL_ACTIVITY_NOT_STARTED)) {
            clearRedisCache(activityId);
            log.warn("秒杀活动[{}]改为未开始（管理员修改时间），已清理Redis缓存", activityId);
        }
        
        // 逆向流程：进行中 → 未开始（管理员修改开始时间往后延）
        else if (Objects.equals(oldStatus, SeckillConstant.SECKILL_ACTIVITY_ON_GOING) && Objects.equals(newStatus, SeckillConstant.SECKILL_ACTIVITY_NOT_STARTED)) {
            clearRedisCache(activityId);
            log.warn("秒杀活动[{}]改为未开始（管理员修改时间），已清理Redis缓存，原有购买记录已保留在MySQL", activityId);
        }
    }

    /**
     * 活动开始：预热Redis库存和用户购买记录缓存
     */
    private void initRedisCache(SeckillActivity activity) {
        Long activityId = activity.getId();
        String stockKey = SeckillConstant.SECKILL_STOCK_KEY_PREFIX + activityId;
        String userKey = SeckillConstant.SECKILL_USER_KEY_PREFIX + activityId;

        // 1. 预热库存缓存
        stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(activity.getAvailableStock()));
        
        // 2. 预热用户购买记录缓存（从MySQL读取已购买用户列表）
        List<SeckillOrder> purchasedOrders = seckillOrderMapper.selectList(
            new LambdaQueryWrapper<SeckillOrder>()
                .eq(SeckillOrder::getSeckillActivityId, activityId)
                .select(SeckillOrder::getUserId)
        );
        
        if (!purchasedOrders.isEmpty()) {
            // 批量添加到Redis Set
            String[] userIds = purchasedOrders.stream()
                .map(order -> String.valueOf(order.getUserId()))
                .toArray(String[]::new);
            stringRedisTemplate.opsForSet().add(userKey, userIds);
            log.info("秒杀活动[{}]预热完成：库存={}, 已购买用户={}", activityId, activity.getAvailableStock(), userIds.length);
        } else {
            // 初始化空Set（避免后续重建时查数据库）
            stringRedisTemplate.opsForSet().add(userKey, SeckillConstant.REDIS_SET_EMPTY_PLACEHOLDER);
            stringRedisTemplate.opsForSet().remove(userKey, SeckillConstant.REDIS_SET_EMPTY_PLACEHOLDER);
            log.info("秒杀活动[{}]预热完成：库存={}, 已购买用户=0（空Set）", activityId, activity.getAvailableStock());
        }
    }

    /**
     * 清理Redis缓存（库存key + 用户购买记录key）
     */
    private void clearRedisCache(Long activityId) {
        String stockKey = SeckillConstant.SECKILL_STOCK_KEY_PREFIX + activityId;
        String userKey = SeckillConstant.SECKILL_USER_KEY_PREFIX + activityId;

        stringRedisTemplate.delete(stockKey);
        stringRedisTemplate.delete(userKey);
    }
}
