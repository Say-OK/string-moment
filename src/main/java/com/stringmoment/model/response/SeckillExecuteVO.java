package com.stringmoment.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 执行秒杀响应参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillExecuteVO {
    
    /**
     * 秒杀订单ID（关联seckill_order表）
     */
    private Long seckillOrderId;
    
    /**
     * 主订单ID（关联orders表）
     */
    private Long orderId;
    
    /**
     * 订单号（业务唯一标识）
     */
    private String orderNo;

    /**
     * 秒杀执行结果状态
     * 参考常量：SeckillConstant.EXECUTE_xxx
     * 0-秒杀成功
     * 1-库存不足
     * 2-重复秒杀
     * 3-活动未开始
     * 4-活动已结束
     */
    private Integer status;
    
    /**
     * 状态详细描述
     */
    private String message;
    
    /**
     * 秒杀价格（快照，来自seckill_order/seckill_activity）
     */
    private BigDecimal seckillPrice;
    
    /**
     * 商品名称（快照，来自order_item）
     */
    private String productName;
    
    /**
     * 商品图片（快照，来自order_item）
     */
    private String productImage;
    
    /**
     * 订单总金额（来自orders表total_amount）
     */
    private BigDecimal totalAmount;
    
    /**
     * 购买数量（来自order_item表quantity）
     */
    private Integer quantity;
    
    /**
     * 支付超时时间（分钟）
     */
    private Integer paymentTimeout;
}