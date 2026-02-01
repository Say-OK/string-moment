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
     * 订单ID
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
     */
    @TableField(value = "seckill_price")
    private BigDecimal seckillPrice;
    
    /**
     * 状态：0-秒杀成功未支付，1-已支付
     */
    @TableField(value = "status")
    private Integer status = 0;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}