package com.stringmoment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stringmoment.entity.SeckillOrder;

/**
 * 秒杀订单服务接口
 */
public interface SeckillOrderService extends IService<SeckillOrder> {

    /**
     * 检查用户是否已经参与过指定的秒杀活动
     * @param activityId 秒杀活动ID
     * @param userId 用户ID
     * @return true-已参与过，false-未参与过
     */
    boolean existsByActivityIdAndUserId(Long activityId, Long userId);
}
