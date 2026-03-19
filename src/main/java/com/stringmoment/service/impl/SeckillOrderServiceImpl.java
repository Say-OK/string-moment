package com.stringmoment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stringmoment.common.exception.BusinessException;
import com.stringmoment.entity.SeckillActivity;
import com.stringmoment.entity.SeckillOrder;
import com.stringmoment.mapper.SeckillOrderMapper;
import com.stringmoment.service.SeckillActivityService;
import com.stringmoment.service.SeckillOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrder> implements SeckillOrderService {

    @Autowired
    private SeckillActivityService seckillActivityService;

    @Override
    public Integer checkSeckillQualification(Long activityId, Long userId) {
        // 1. 校验活动是否存在
        SeckillActivity activity = seckillActivityService.getById(activityId);
        if (activity == null) {
            throw new BusinessException("秒杀活动不存在");
        }

        // 2. 检查活动状态
        if (activity.getStatus() == 0) {
            return 3; // 活动未开始
        } else if (activity.getStatus() == 2) {
            return 4; // 活动已结束
        }

        // 3. 检查库存
        if (activity.getAvailableStock() <= 0) {
            return 1; // 库存不足
        }

        // 4. 检查是否重复秒杀
        boolean hasParticipated = lambdaQuery()
                .eq(SeckillOrder::getSeckillActivityId, activityId)
                .eq(SeckillOrder::getUserId, userId)
                .exists();
        if (hasParticipated) {
            return 2; // 重复秒杀
        }

        // 5. 所有校验通过
        return 0; // 可秒杀
    }
}
