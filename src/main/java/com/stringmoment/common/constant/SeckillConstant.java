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
    public static final Integer SECKILL_ACTIVITY_NOT_STARTED = 0;
    public static final Integer SECKILL_ACTIVITY_ON_GOING = 1;
    public static final Integer SECKILL_ACTIVITY_ENDED = 2;

    // 秒杀资格校验码
    public static final Integer QUALIFY_CAN_SECKILL = 0;         // 可以秒杀
    public static final Integer QUALIFY_STOCK_LACK = 1;          // 库存不足
    public static final Integer QUALIFY_REPEAT_PURCHASE = 2;     // 已参与过
    public static final Integer QUALIFY_ACTIVITY_NOT_START = 3;  // 活动未开始
    public static final Integer QUALIFY_ACTIVITY_ENDED = 4;      // 活动已结束

    // 秒杀执行结果状态
    public static final Integer SECKILL_RESULT_SUCCESS = 0;     // 秒杀成功
    public static final Integer SECKILL_RESULT_STOCK_LACK = 1;  // 库存不足
    public static final Integer SECKILL_RESULT_REPEAT = 2;      // 重复秒杀
    public static final Integer SECKILL_RESULT_NOT_START = 3;   // 活动未开始
    public static final Integer SECKILL_RESULT_END = 4;         // 活动已结束

    // 秒杀超时时间
    public static final Integer SECKILL_PAY_TIMEOUT_MINUTES = 30;
}
