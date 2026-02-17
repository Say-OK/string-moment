package com.stringmoment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stringmoment.entity.Order;
import com.stringmoment.model.request.OrderCreateDTO;
import com.stringmoment.model.response.OrderVO;
import jakarta.validation.Valid;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     */
    OrderVO createOrder(Long userId, @Valid OrderCreateDTO dto);
}
