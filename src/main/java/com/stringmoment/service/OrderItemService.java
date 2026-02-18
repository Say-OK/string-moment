package com.stringmoment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stringmoment.entity.OrderItem;

import java.util.List;
import java.util.Map;

/**
 * 订单商品明细服务接口
 */
public interface OrderItemService extends IService<OrderItem> {

    /**
     * 根据订单ID列表批量查询订单商品
     */
    Map<Long, List<OrderItem>> getOrderItemsByOrderIds(List<Long> orderIds);
}
