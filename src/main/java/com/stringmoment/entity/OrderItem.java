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
 * 订单商品明细实体类
 * 对应数据库表：order_item
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("order_item")
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 订单ID
     */
    @TableField(value = "order_id")
    private Long orderId;
    
    /**
     * 商品ID
     */
    @TableField(value = "product_id")
    private Long productId;
    
    /**
     * 商品名称（快照）
     */
    @TableField(value = "product_name")
    private String productName;
    
    /**
     * 商品图片（快照）
     */
    @TableField(value = "product_image")
    private String productImage;
    
    /**
     * 商品单价（快照）
     */
    @TableField(value = "unit_price")
    private BigDecimal unitPrice;
    
    /**
     * 购买数量
     */
    @TableField(value = "quantity")
    private Integer quantity;
    
    /**
     * 商品总价 = 单价 * 数量
     */
    @TableField(value = "total_price")
    private BigDecimal totalPrice;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}