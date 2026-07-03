package com.stringmoment.common.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.stringmoment.common.constant.OrderConstant;
import com.stringmoment.common.constant.SeckillConstant;
import com.stringmoment.entity.Order;
import com.stringmoment.entity.OrderItem;
import com.stringmoment.entity.Product;
import com.stringmoment.entity.SeckillActivity;
import com.stringmoment.service.OrderItemService;
import com.stringmoment.service.OrderService;
import com.stringmoment.service.ProductService;
import com.stringmoment.service.SeckillActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OrderTimeoutTask {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SeckillActivityService seckillActivityService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Scheduled(fixedDelay = 60000)
    public void closeTimeoutOrders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime normalOrderTimeout = now.minusMinutes(OrderConstant.ORDER_PAY_TIMEOUT_MINUTES);
        LocalDateTime seckillOrderTimeout = now.minusMinutes(SeckillConstant.SECKILL_PAY_TIMEOUT_MINUTES);

        List<Order> timeoutOrders = orderService.list(
            new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, OrderConstant.ORDER_STATUS_PENDING_PAY)
                .and(wrapper -> wrapper
                    .and(w -> w.eq(Order::getOrderType, OrderConstant.ORDER_TYPE_NORMAL)
                            .lt(Order::getCreateTime, normalOrderTimeout))
                    .or(w -> w.eq(Order::getOrderType, OrderConstant.ORDER_TYPE_SECKILL)
                            .lt(Order::getCreateTime, seckillOrderTimeout))
                )
        );

        if (timeoutOrders.isEmpty()) {
            return;
        }

        log.info("发现{}个超时未支付订单，开始自动关闭", timeoutOrders.size());

        List<Long> orderIds = timeoutOrders.stream()
                .map(Order::getId)
                .collect(Collectors.toList());

        Map<Long, List<OrderItem>> orderItemMap = orderItemService.getOrderItemsByOrderIds(orderIds);

        for (Order order : timeoutOrders) {
            try {
                closeOrderWithTransaction(order, orderItemMap.getOrDefault(order.getId(), Collections.emptyList()));
                log.info("订单[{}]已自动关闭并返还库存", order.getOrderNo());
            } catch (Exception e) {
                log.error("关闭订单[{}]失败: {}", order.getOrderNo(), e.getMessage(), e);
            }
        }
    }

    private void closeOrderWithTransaction(Order order, List<OrderItem> items) {
        transactionTemplate.execute(status -> {
            boolean updateSuccess = orderService.lambdaUpdate()
                    .set(Order::getStatus, OrderConstant.ORDER_STATUS_CANCELED)
                    .set(Order::getCloseTime, LocalDateTime.now())
                    .eq(Order::getId, order.getId())
                    .eq(Order::getStatus, OrderConstant.ORDER_STATUS_PENDING_PAY)
                    .update();

            if (!updateSuccess) {
                status.setRollbackOnly();
                throw new RuntimeException("订单状态更新失败，可能已被其他操作修改");
            }

            for (OrderItem item : items) {
                Long productId = item.getProductId();
                Integer quantity = item.getQuantity();

                if (productId == null || productId <= 0 || quantity == null || quantity <= 0) {
                    continue;
                }

                if (OrderConstant.ORDER_TYPE_NORMAL.equals(order.getOrderType())) {
                    LambdaUpdateWrapper<Product> wrapper = new LambdaUpdateWrapper<>();
                    wrapper.setSql("stock = stock + " + quantity)
                            .setSql("sale_count = sale_count - " + quantity)
                            .eq(Product::getId, productId)
                            .ge(Product::getSaleCount, quantity);
                    boolean success = productService.update(wrapper);
                    if (!success) {
                        status.setRollbackOnly();
                        throw new RuntimeException("商品库存返还失败: productId=" + productId);
                    }
                } else if (OrderConstant.ORDER_TYPE_SECKILL.equals(order.getOrderType())) {
                    if (order.getSeckillActivityId() != null) {
                        boolean success = seckillActivityService.lambdaUpdate()
                                .eq(SeckillActivity::getId, order.getSeckillActivityId())
                                .setSql("available_stock = available_stock + " + quantity)
                                .update();
                        if (!success) {
                            status.setRollbackOnly();
                            throw new RuntimeException("秒杀活动库存返还失败: activityId=" + order.getSeckillActivityId());
                        }
                    }
                }
            }

            return null;
        });
    }
}