package com.stringmoment.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 订单响应对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO {
    
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private Integer orderType;      // 1-普通订单，2-秒杀订单
    private Long seckillActivityId; // 秒杀活动ID
    private Long addressId;
    private Integer status;         // 0-待支付，1-已支付，2-已发货，3-已完成，4-已取消
    private String paymentTime;
    private String deliveryTime;
    private String receiveTime;
    private String closeTime;
    private String createTime;
    private String updateTime;
    private AddressVO address;       // 收货地址
    private List<OrderItemVO> items; // 订单商品
    
    /**
     * 从Order实体转换为OrderVO
     */
    public static OrderVO fromEntity(com.stringmoment.entity.Order order) {
        if (order == null) {
            return null;
        }
        
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setOrderType(order.getOrderType());
        vo.setSeckillActivityId(order.getSeckillActivityId());
        vo.setAddressId(order.getAddressId());
        vo.setStatus(order.getStatus());
        
        // 格式化时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (order.getPaymentTime() != null) {
            vo.setPaymentTime(order.getPaymentTime().format(formatter));
        }
        if (order.getDeliveryTime() != null) {
            vo.setDeliveryTime(order.getDeliveryTime().format(formatter));
        }
        if (order.getReceiveTime() != null) {
            vo.setReceiveTime(order.getReceiveTime().format(formatter));
        }
        if (order.getCloseTime() != null) {
            vo.setCloseTime(order.getCloseTime().format(formatter));
        }
        if (order.getCreateTime() != null) {
            vo.setCreateTime(order.getCreateTime().format(formatter));
        }
        if (order.getUpdateTime() != null) {
            vo.setUpdateTime(order.getUpdateTime().format(formatter));
        }
        
        return vo;
    }
}