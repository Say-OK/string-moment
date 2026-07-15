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

    // 分布式锁 Key前缀
    public static final String SECKILL_USER_LOCK_KEY_PREFIX = "lock:seckill:user:";

    // 分布式锁配置
    public static final long LOCK_EXPIRE_SECONDS = 10;      // 锁过期时间（秒）
    public static final long LOCK_WAIT_MILLISECONDS = 30;  // 未获取锁时的等待时间（毫秒）
    public static final int LOCK_RETRY_TIMES = 2;           // 未获取锁时的重试次数

    // Redis Set初始化占位符（用于初始化空Set）
    public static final String REDIS_SET_EMPTY_PLACEHOLDER = "init_placeholder";

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
    public static final Integer SECKILL_RESULT_ERROR = 5;       // 参数错误/未知错误

    // 秒杀下单支付超时时长（单位：分钟）
    public static final int SECKILL_PAY_TIMEOUT_MINUTES = 15;

    // 秒杀 Lua 脚本路径
    public static final String SECKILL_LUA_PATH = "lua/seckill.lua";
}
