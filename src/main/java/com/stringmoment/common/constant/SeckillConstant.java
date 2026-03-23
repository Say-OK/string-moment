package com.stringmoment.common.constant;

/**
 * 秒杀业务通用常量
 */
public final class SeckillConstant {
    // 私有化构造器，防止实例化
    private SeckillConstant() {}

    // Redis Key前缀
    public static final String SECKILL_STOCK_KEY_PREFIX = "seckill:stock:";
    public static final String SECKILL_USER_KEY_PREFIX = "seckill:user:";

    // 秒杀活动状态
    public static final Integer SECKILL_STATUS_NOT_STARTED = 0;
    public static final Integer SECKILL_STATUS_ON_GOING = 1;
    public static final Integer SECKILL_STATUS_ENDED = 2;

    // 秒杀超时时间
    public static final Integer SECKILL_PAY_TIMEOUT_MINUTES = 30;
}
