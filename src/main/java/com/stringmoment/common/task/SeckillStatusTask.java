package com.stringmoment.common.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stringmoment.common.constant.SeckillConstant;
import com.stringmoment.entity.SeckillActivity;
import com.stringmoment.service.SeckillActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 每10秒：更新一次活动状态，管理Redis缓存的初始化和清理
     */
    @Scheduled(fixedRate = 10000)
    @Transactional
    public void updateActivityStatus() {
        List<SeckillActivity> activities = seckillActivityService.list(
            new LambdaQueryWrapper<SeckillActivity>()
                .in(SeckillActivity::getStatus,
                        SeckillConstant.SECKILL_STATUS_NOT_STARTED,
                        SeckillConstant.SECKILL_STATUS_ON_GOING
                )
        );

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

    private Integer calculateStatus(SeckillActivity activity, LocalDateTime now) {
        if (now.isBefore(activity.getStartTime())) {
            return SeckillConstant.SECKILL_STATUS_NOT_STARTED;
        } else if (now.isAfter(activity.getEndTime())) {
            return SeckillConstant.SECKILL_STATUS_ENDED;
        } else {
            return SeckillConstant.SECKILL_STATUS_ON_GOING;
        }
    }

    /**
     * 处理状态变化时的Redis缓存操作
     */
    private void handleStatusChange(SeckillActivity activity, Integer oldStatus, Integer newStatus) {
        Long activityId = activity.getId();

        if (Objects.equals(oldStatus, SeckillConstant.SECKILL_STATUS_NOT_STARTED) && Objects.equals(newStatus, SeckillConstant.SECKILL_STATUS_ON_GOING)) {
            initRedisCache(activity);
            log.info("秒杀活动[{}]开始，已初始化Redis库存: {}", activityId, activity.getAvailableStock());
        } else if (Objects.equals(oldStatus, SeckillConstant.SECKILL_STATUS_ON_GOING) && Objects.equals(newStatus, SeckillConstant.SECKILL_STATUS_ENDED)) {
            syncAndClearCache(activity);
            log.info("秒杀活动[{}]结束，已同步库存并清理缓存", activityId);
        }
    }

    /**
     * 活动开始：初始化Redis库存和用户购买记录Set
     */
    private void initRedisCache(SeckillActivity activity) {
        String stockKey = SeckillConstant.SECKILL_STOCK_KEY_PREFIX + activity.getId();
        String userKey = SeckillConstant.SECKILL_USER_KEY_PREFIX + activity.getId();

        stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(activity.getAvailableStock()));
        stringRedisTemplate.delete(userKey);
    }

    /**
     * 活动结束：同步剩余库存回DB，清理Redis缓存
     */
    private void syncAndClearCache(SeckillActivity activity) {
        String stockKey = SeckillConstant.SECKILL_STOCK_KEY_PREFIX+ activity.getId();
        String userKey = SeckillConstant.SECKILL_USER_KEY_PREFIX + activity.getId();

        String stockStr = stringRedisTemplate.opsForValue().get(stockKey);
        if (stockStr != null) {
            int remainStock = Integer.parseInt(stockStr);
            activity.setAvailableStock(remainStock);
            log.info("活动[{}]剩余库存同步回DB: {}", activity.getId(), remainStock);
        }

        stringRedisTemplate.delete(stockKey);
        stringRedisTemplate.delete(userKey);
    }
}
