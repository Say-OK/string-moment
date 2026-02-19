package com.stringmoment.controller;

import com.stringmoment.common.result.Result;
import com.stringmoment.model.request.OrderCreateDTO;
import com.stringmoment.model.request.OrderListQueryDTO;
import com.stringmoment.model.response.OrderPageVO;
import com.stringmoment.model.response.OrderVO;
import com.stringmoment.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 订单Controller
 */
@RestController
@RequestMapping("/order")
@Validated
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建普通订单
     */
    @PostMapping("/create")
    public Result<OrderVO> createOrder(@RequestBody @Valid OrderCreateDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        OrderVO orderVO = orderService.createOrder(userId, dto);
        return Result.success("订单创建成功", orderVO);
    }

    /**
     * 获取订单列表
     */
    @GetMapping("/list")
    public Result<OrderPageVO> getOrderList(@Valid OrderListQueryDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        OrderPageVO result = orderService.getOrderList(userId, dto);
        return Result.success(result);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/detail/{id}")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        OrderVO orderVO = orderService.getOrderDetail(id, userId);
        return Result.success(orderVO);
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel/{id}")
    public Result<Void> cancelOrder(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        orderService.cancelOrder(id, userId);
        return Result.success("订单取消成功");
    }
}
