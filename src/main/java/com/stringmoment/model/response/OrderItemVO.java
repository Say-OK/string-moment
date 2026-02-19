package com.stringmoment.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单商品响应对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemVO {
    
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
    
    /**
     * 从OrderItem实体转换为OrderItemVO
     */
    public static OrderItemVO fromEntity(com.stringmoment.entity.OrderItem item) {
        if (item == null) {
            return null;
        }
        
        OrderItemVO vo = new OrderItemVO();
        vo.setId(item.getId());
        vo.setProductId(item.getProductId());
        vo.setProductName(item.getProductName());
        vo.setProductImage(item.getProductImage());
        vo.setUnitPrice(item.getUnitPrice());
        vo.setQuantity(item.getQuantity());
        vo.setTotalPrice(item.getTotalPrice());
        
        return vo;
    }
}