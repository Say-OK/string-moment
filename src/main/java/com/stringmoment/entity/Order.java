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
 * 订单实体类
 * 对应数据库表：orders（注意表名是复数）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("orders")  // 指定表名为orders，因为order是SQL关键字
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 订单号，业务唯一
     */
    @TableField(value = "order_no")
    private String orderNo;
    
    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;
    
    /**
     * 订单总金额
     */
    @TableField(value = "total_amount")
    private BigDecimal totalAmount;
    
    /**
     * 实付金额
     */
    @TableField(value = "pay_amount")
    private BigDecimal payAmount = BigDecimal.ZERO;
    
    /**
     * 订单类型：1-普通订单，2-秒杀订单
     */
    @TableField(value = "order_type")
    private Integer orderType = 1;
    
    /**
     * 秒杀活动ID，仅秒杀订单有值
     */
    @TableField(value = "seckill_activity_id")
    private Long seckillActivityId;
    
    /**
     * 收货地址ID
     */
    @TableField(value = "address_id")
    private Long addressId;
    
    /**
     * 状态：0-待支付，1-已支付，2-已发货，3-已完成，4-已取消
     */
    @TableField(value = "status")
    private Integer status = 0;
    
    /**
     * 支付时间
     */
    @TableField(value = "payment_time")
    private LocalDateTime paymentTime;
    
    /**
     * 发货时间
     */
    @TableField(value = "delivery_time")
    private LocalDateTime deliveryTime;
    
    /**
     * 确认收货时间
     */
    @TableField(value = "receive_time")
    private LocalDateTime receiveTime;
    
    /**
     * 订单关闭时间
     */
    @TableField(value = "close_time")
    private LocalDateTime closeTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}