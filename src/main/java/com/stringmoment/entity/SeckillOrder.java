package com.stringmoment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀订单实体类
 * 对应数据库表：seckill_order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("seckill_order")
public class SeckillOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;
    
    /**
     * 关联的订单ID
     * 通过orderId关联Order表，获取订单状态、支付时间等完整信息
     */
    @TableField(value = "order_id")
    private Long orderId;
    
    /**
     * 秒杀活动ID
     */
    @TableField(value = "seckill_activity_id")
    private Long seckillActivityId;
    
    /**
     * 秒杀价格（快照）
     * 保存秒杀时的价格，防止后续活动价格变化影响已生成的订单
     */
    @TableField(value = "seckill_price")
    private BigDecimal seckillPrice;
    
    /**
     * 秒杀参与时间
     * 记录用户参与秒杀的具体时间
     */
    @TableField(value = "participate_time", fill = FieldFill.INSERT)
    private LocalDateTime participateTime;
}