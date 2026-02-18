package com.stringmoment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stringmoment.entity.OrderItem;
import com.stringmoment.mapper.OrderItemMapper;
import com.stringmoment.service.OrderItemService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {

    /**
     * 根据订单ID列表批量查询订单商品
     */
    @Override
    public Map<Long, List<OrderItem>> getOrderItemsByOrderIds(List<Long> orderIds) {
        if (orderIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<OrderItem> allItems = lambdaQuery()
                .in(OrderItem::getOrderId, orderIds)
                .orderByAsc(OrderItem::getId)  // 主键索引
                .list();

        return allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));
    }
}
