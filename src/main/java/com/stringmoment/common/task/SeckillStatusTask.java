package com.stringmoment.common.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stringmoment.entity.SeckillActivity;
import com.stringmoment.service.SeckillActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class SeckillStatusTask {
    
    @Autowired
    private SeckillActivityService seckillActivityService;
    
    /**
     * 每分钟更新一次活动状态
     */
    @Scheduled(fixedRate = 60000)  // 60秒
    @Transactional
    public void updateActivityStatus() {
        // 查询所有非已结束的活动
        List<SeckillActivity> activities = seckillActivityService.list(
            new LambdaQueryWrapper<SeckillActivity>()
                .le(SeckillActivity::getStatus, 1)  // 未开始或进行中
        );
        
        if (activities.isEmpty()) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        List<SeckillActivity> needUpdate = new ArrayList<>();
        
        for (SeckillActivity activity : activities) {
            Integer newStatus = calculateStatus(activity, now);
            
            if (!activity.getStatus().equals(newStatus)) {
                activity.setStatus(newStatus);
                needUpdate.add(activity);
            }
        }
        
        if (!needUpdate.isEmpty()) {
            seckillActivityService.updateBatchById(needUpdate);
        }
    }
    
    private Integer calculateStatus(SeckillActivity activity, LocalDateTime now) {
        if (now.isBefore(activity.getStartTime())) {
            return 0;  // 未开始
        } else if (now.isAfter(activity.getEndTime())) {
            return 2;  // 已结束
        } else {
            return 1;  // 进行中
        }
    }
}