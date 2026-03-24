package com.stringmoment.common.constant;

public final class OrderConstant {
    private OrderConstant() {}

    // 订单状态
    public static final Integer ORDER_STATUS_PENDING_PAY = 0;    // 待支付
    public static final Integer ORDER_STATUS_PAID = 1;           // 已支付
    public static final Integer ORDER_STATUS_SHIPPED = 2;       // 已发货
    public static final Integer ORDER_STATUS_COMPLETED = 3;     // 已完成
    public static final Integer ORDER_STATUS_CANCELED = 4;      // 已取消

    // 订单类型
    public static final Integer ORDER_TYPE_NORMAL = 1;           // 普通订单
    public static final Integer ORDER_TYPE_SECKILL = 2;         // 秒杀订单
}