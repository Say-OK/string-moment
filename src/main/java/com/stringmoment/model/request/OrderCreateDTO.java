package com.stringmoment.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建订单请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDTO {
    
    @NotNull(message = "收货地址ID不能为空")
    private Long addressId;
    
    @Valid
    @Size(min = 1, message = "至少需要一个商品")
    private List<OrderItemDTO> items;
    
    @Data
    public static class OrderItemDTO {
        @NotNull(message = "商品ID不能为空")
        private Long productId;
        
        @NotNull(message = "商品数量不能为空")
        private Integer quantity;
    }
}