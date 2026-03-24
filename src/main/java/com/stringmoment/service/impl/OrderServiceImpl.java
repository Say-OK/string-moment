package com.stringmoment.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stringmoment.common.constant.OrderConstant;
import com.stringmoment.common.constant.ProductConstant;
import com.stringmoment.common.exception.BusinessException;
import com.stringmoment.common.util.OrderNoGenerator;
import com.stringmoment.entity.Order;
import com.stringmoment.entity.OrderItem;
import com.stringmoment.entity.Product;
import com.stringmoment.mapper.OrderMapper;
import com.stringmoment.model.request.OrderCreateDTO;
import com.stringmoment.model.request.OrderListQueryDTO;
import com.stringmoment.model.response.*;
import com.stringmoment.service.OrderItemService;
import com.stringmoment.service.OrderService;
import com.stringmoment.service.ProductService;
import com.stringmoment.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserAddressService userAddressService;

    @Autowired
    private OrderNoGenerator orderNoGenerator;

    /**
     * 创建普通订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(Long userId, OrderCreateDTO dto) {
        // 1. 获取并验证地址
        AddressVO address = userAddressService.getAddressByIdAndUser(dto.getAddressId(), userId);
        if (address == null) {
            throw new BusinessException("收货地址不存在");
        }

        // 2. 获取商品信息
        List<Long> productIds = dto.getItems().stream()
                .map(OrderCreateDTO.OrderItemDTO::getProductId)
                .distinct()  // 去重
                .collect(Collectors.toList());

        List<Product> products = productService.listByIds(productIds);
        if (products.size() != productIds.size()) {
            throw new BusinessException("部分商品不存在");
        }

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 3. 合并相同商品的数量
        Map<Long, Integer> productQuantityMap = new HashMap<>();
        for (OrderCreateDTO.OrderItemDTO item : dto.getItems()) {
            Long productId = item.getProductId();
            Integer quantity = item.getQuantity();
            if (quantity <= 0) {
                throw new BusinessException("商品数量必须大于0");
            }

            productQuantityMap.put(productId,
                    productQuantityMap.getOrDefault(productId, 0) + quantity);
        }

        // 4. 验证库存
        for (Map.Entry<Long, Integer> entry : productQuantityMap.entrySet()) {
            Long productId = entry.getKey();
            Integer totalQuantity = entry.getValue();
            Product product = productMap.get(productId);

            if (Objects.equals(product.getStatus(), ProductConstant.PRODUCT_STATUS_OFF)) {
                throw new BusinessException("商品已下架: " + product.getName());
            }
            if (product.getStock() < totalQuantity) {
                throw new BusinessException("库存不足: " + product.getName());
            }
        }

        // 5. 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderCreateDTO.OrderItemDTO item : dto.getItems()) {
            Product product = productMap.get(item.getProductId());
            BigDecimal itemTotal = product.getPrice().multiply(
                    BigDecimal.valueOf(item.getQuantity())
            );
            totalAmount = totalAmount.add(itemTotal);
        }


        // 6. 生成订单号
        String orderNo = orderNoGenerator.generateOrderNo(userId);

        // 7. 创建订单
        Order order = Order.builder()
                .userId(userId)
                .orderNo(orderNo)
                .totalAmount(totalAmount)
                .payAmount(totalAmount)
                .orderType(OrderConstant.ORDER_TYPE_NORMAL)
                .addressId(dto.getAddressId())
                .receiverName(address.getReceiverName())
                .receiverPhone(address.getReceiverPhone())
                .receiverProvince(address.getProvince())
                .receiverCity(address.getCity())
                .receiverDistrict(address.getDistrict())
                .receiverDetailAddress(address.getDetailAddress())
                .status(OrderConstant.ORDER_STATUS_PENDING_PAY)
                .build();

        save(order);

        // 8. 创建订单商品记录
        List<OrderItemVO> orderItemVOList = new ArrayList<>();

        for (OrderCreateDTO.OrderItemDTO item : dto.getItems()) {
            Product product = productMap.get(item.getProductId());

            OrderItem orderItem = OrderItem.builder()
                    .orderId(order.getId())
                    .productId(product.getId())
                    .productName(product.getName())
                    .productImage(product.getImageUrl())
                    .unitPrice(product.getPrice())
                    .quantity(item.getQuantity())
                    .totalPrice(product.getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity())))
                    .build();

            orderItemService.save(orderItem);
            orderItemVOList.add(OrderItemVO.fromEntity(orderItem));
        }

        // 9. 扣减库存
        for (Map.Entry<Long, Integer> entry : productQuantityMap.entrySet()) {
            Long productId = entry.getKey();
            Integer totalQuantity = entry.getValue();

            boolean updated = productService.lambdaUpdate()
                    .eq(Product::getId, productId)
                    .ge(Product::getStock, totalQuantity)
                    .setSql("stock = stock - " + totalQuantity)
                    .setSql("sale_count = sale_count + " + totalQuantity)
                    .update();

            if (!updated) {
                throw new BusinessException("库存扣减失败: " + productMap.get(productId).getName());
            }
        }

        OrderVO orderVO = OrderVO.fromEntity(order);
        orderVO.setItems(orderItemVOList);
        return orderVO;
    }

    /**
     * 获取订单列表
     */
    @Override
    public OrderPageVO getOrderList(Long userId, OrderListQueryDTO dto) {
        // 1. 创建查询对象
        LambdaQueryChainWrapper<Order> query = lambdaQuery()
                .eq(Order::getUserId, userId);

        // 2. 拼接动态条件
        if (dto.getStatus() != null) {
            query.eq(Order::getStatus, dto.getStatus());
        }
        if (dto.getOrderType() != null) {
            query.eq(Order::getOrderType, dto.getOrderType());
        }
        query.orderByDesc(Order::getCreateTime);

        // 3. 分页查询 + 参数校验
        IPage<Order> page = query.page(new Page<>(
                Math.max(dto.getPage(), 1),
                Math.max(Math.min(dto.getSize(), 100), 1)
        ));


        // 4. 处理空订单情况
        List<Long> orderIds = page.getRecords().stream()
                .map(Order::getId)
                .toList();

        if (orderIds.isEmpty()) {
            return OrderPageVO.builder()
                    .list(Collections.emptyList())
                    .total(page.getTotal())
                    .page((int) page.getCurrent())
                    .size((int) page.getSize())
                    .pages((int) page.getPages())
                    .build();
        }

        // 5. 批量查询订单商品项
        Map<Long, List<OrderItem>> orderItemsMap = orderItemService.getOrderItemsByOrderIds(orderIds);

        // 6. 组装订单简略信息列表
        List<OrderSimpleVO> orderSimpleVOList = page.getRecords().stream()
                .map(order -> {
                    OrderSimpleVO vo = OrderSimpleVO.fromEntity(order);
                    List<OrderItem> items = orderItemsMap.getOrDefault(order.getId(), Collections.emptyList());
                    vo.setProductCount(items.size());

                    if (!items.isEmpty()) {
                        OrderItem firstItem = items.get(0);
                        vo.setFirstProductName(firstItem.getProductName());
                        vo.setFirstProductImage(firstItem.getProductImage());
                        vo.setFirstProductQuantity(firstItem.getQuantity());
                    }
                    return vo;
                })
                .toList();


        return OrderPageVO.builder()
                .list(orderSimpleVOList)
                .total(page.getTotal())
                .page((int) page.getCurrent())
                .size((int) page.getSize())
                .pages((int) page.getPages())
                .build();
    }

    /**
     * 获取订单详情
     */
    @Override
    public OrderVO getOrderDetail(Long id, Long userId) {
        // 1. 获取订单
        Order order = lambdaQuery()
                .eq(Order::getId, id)
                .eq(Order::getUserId, userId)
                .one();

        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 2. 获取订单商品详情
        List<OrderItem> itemList = orderItemService.lambdaQuery()
                .eq(OrderItem::getOrderId, order.getId())
                .list();

        List<OrderItemVO> itemVOList = itemList.stream().map(OrderItemVO::fromEntity).toList();

        // 3. 创建vo
        OrderVO orderVO = OrderVO.fromEntity(order);
        orderVO.setItems(itemVOList);
        return orderVO;
    }

    /**
     * 取消订单
     */
    @Override
    @Transactional
    public void cancelOrder(Long id, Long userId) {
        // 1. 检查权限、订单状态
        Order order = lambdaQuery()
                .eq(Order::getId, id)
                .eq(Order::getUserId, userId)
                .one();

        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!Objects.equals(order.getStatus(), OrderConstant.ORDER_STATUS_PENDING_PAY)) {
            throw new BusinessException("当前状态无法取消订单");
        }

        // 2. 软删除订单
        boolean success = lambdaUpdate()
                .set(Order::getStatus, OrderConstant.ORDER_STATUS_CANCELED)
                .set(Order::getCloseTime, LocalDateTime.now())
                .eq(Order::getId, id)
                .eq(Order::getUserId, userId)
                .eq(Order::getStatus, OrderConstant.ORDER_STATUS_PENDING_PAY)
                .update();

        if (!success) {
            throw new BusinessException("订单状态已发生变化，取消失败");
        }

        // 3. 回滚商品库存、售出数量
        List<OrderItem> items = orderItemService.lambdaQuery()
                .eq(OrderItem::getOrderId, order.getId())
                .list();

        for (OrderItem item : items) {
            Long productId = item.getProductId();
            Integer quantity = item.getQuantity();

            if (productId == null || productId <= 0) {
                throw new BusinessException("商品ID异常，无法回滚库存");
            }
            if (quantity == null || quantity <= 0) {
                throw new BusinessException("商品数量异常");
            }

            LambdaUpdateWrapper<Product> wrapper = new LambdaUpdateWrapper<>();
            wrapper.setSql("stock = stock + " + quantity)
                    .setSql("sale_count = sale_count - " + quantity)
                    .eq(Product::getId, productId)
                    .ge(Product::getSaleCount, quantity);  // 防止销量为负

            boolean update = productService.update(wrapper);
            if (!update) {
                throw new BusinessException("商品库存回滚失败");
            }
        }
    }

    /**
     * 支付订单
     * 注意：当前为模拟支付
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long id, Long userId) {
        // 1. 检查权限、订单状态
        Order order = lambdaQuery()
                .eq(Order::getId, id)
                .eq(Order::getUserId, userId)
                .one();

        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!Objects.equals(order.getStatus(), OrderConstant.ORDER_STATUS_PENDING_PAY)) {
            throw new BusinessException("当前状态无法支付订单");
        }

        // 2. 更新订单状态
        boolean success = lambdaUpdate()
                .set(Order::getStatus, OrderConstant.ORDER_STATUS_PAID)
                .set(Order::getPaymentTime, LocalDateTime.now())
                .eq(Order::getId, id)
                .eq(Order::getUserId, userId)
                .eq(Order::getStatus, OrderConstant.ORDER_STATUS_PENDING_PAY)
                .update();

        if (!success) {
            throw new BusinessException("支付订单失败");
        }
    }
}
