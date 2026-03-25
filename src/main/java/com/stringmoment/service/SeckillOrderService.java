package com.stringmoment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stringmoment.entity.SeckillOrder;
import com.stringmoment.model.request.SeckillExecuteDTO;
import com.stringmoment.model.response.SeckillExecuteVO;

/**
 * 秒杀订单服务接口
 */
public interface SeckillOrderService extends IService<SeckillOrder> {

    /**
     * 检查用户秒杀资格（前端展示用）
     * @param activityId 秒杀活动ID
     * @param userId 用户ID
     * @return 0-可秒杀，1-库存不足，2-重复秒杀，3-活动未开始，4-活动已结束
     */
    Integer checkSeckillQualification(Long activityId, Long userId);

    /**
     * 执行秒杀（一人一单）
     * 使用Redis + Lua脚本实现原子性库存扣减和防重复购买
     *
     * @param dto 秒杀请求参数
     * @param userId 当前登录用户ID
     * @return 秒杀结果
     */
    SeckillExecuteVO executeSeckill(SeckillExecuteDTO dto, Long userId);
}
