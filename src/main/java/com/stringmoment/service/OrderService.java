package com.stringmoment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stringmoment.entity.Order;
import com.stringmoment.model.request.AdminOrderListQueryDTO;
import com.stringmoment.model.request.OrderCreateDTO;
import com.stringmoment.model.request.OrderListQueryDTO;
import com.stringmoment.model.response.OrderPageVO;
import com.stringmoment.model.response.OrderVO;
import jakarta.validation.Valid;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建普通订单
     */
    OrderVO createOrder(Long userId, @Valid OrderCreateDTO dto);

    /**
     * 获取订单列表
     */
    OrderPageVO getOrderList(Long userId, @Valid OrderListQueryDTO dto);

    /**
     * 获取订单详情
     */
    OrderVO getOrderDetail(Long id, Long userId);

    /**
     * 取消订单
     */
    void cancelOrder(Long id, Long userId);

    /**
     * 支付订单
     */
    void payOrder(Long id, Long userId);

    /**
     * 确认收货
     */
    void confirmOrder(Long id, Long userId);

    /**
     * 发货（管理员）
     */
    void shipOrder(Long id);

    /**
     * 获取后台订单列表（管理员：多条件筛选）
     */
    OrderPageVO getAdminOrderList(@Valid AdminOrderListQueryDTO dto);

    /**
     * 获取订单详情（管理员：不校验用户权限）
     */
    OrderVO getOrderDetailAdmin(Long id);
}
