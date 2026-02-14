package com.stringmoment.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 订单分页响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPageVO {
    
    private List<OrderVO> list;  // 订单列表
    private Long total;          // 总记录数
    private Integer page;        // 当前页码
    private Integer size;        // 每页数量
    private Integer pages;       // 总页数
}