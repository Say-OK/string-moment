package com.stringmoment.model.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * 订单简略信息（列表展示）
 */
@Data
public class OrderSimpleVO {
    
    private Long id;
    private String orderNo;
    private BigDecimal totalAmount;
    private Integer status;
    private Integer orderType;
    private String createTime;
    private Integer productCount;  // 商品总数
    
    // 第一个商品信息
    private String firstProductName;
    private String firstProductImage;
    private Integer firstProductQuantity;
    
    public static OrderSimpleVO fromEntity(com.stringmoment.entity.Order order) {
        if (order == null) {
            return null;
        }
        
        OrderSimpleVO vo = new OrderSimpleVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setOrderType(order.getOrderType());
        
        if (order.getCreateTime() != null) {
            vo.setCreateTime(order.getCreateTime().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ));
        }
        
        return vo;
    }
}