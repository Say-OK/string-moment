package com.stringmoment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stringmoment.entity.SeckillOrder;
import com.stringmoment.mapper.SeckillOrderMapper;
import com.stringmoment.service.SeckillOrderService;
import org.springframework.stereotype.Service;

@Service
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrder> implements SeckillOrderService {

    /**
     * 检查用户是否已经参与过指定的秒杀活动
     */
    @Override
    public boolean existsByActivityIdAndUserId(Long activityId, Long userId) {
        return lambdaQuery()
            .eq(SeckillOrder::getSeckillActivityId, activityId)
            .eq(SeckillOrder::getUserId, userId)
            .exists();
    }
}

