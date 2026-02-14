package com.stringmoment.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单列表查询参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderListQueryDTO {
    
    private Integer page = 1;      // 页码，默认1
    private Integer size = 10;     // 每页数量，默认10
    private Integer status;        // 订单状态过滤
    private Integer orderType;     // 订单类型：1-普通订单，2-秒杀订单
}