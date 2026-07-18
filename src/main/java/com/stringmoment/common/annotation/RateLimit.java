package com.stringmoment.common.annotation;

import java.lang.annotation.*;

/**
 * 限流注解
 * 基于Redis滑动窗口算法实现多维度限流
 * <p>
 * 使用示例：
 * {@code @RateLimit(key = "seckill", limit = 1, period = 1, limitType = LimitType.USER)}
 * 表示：单个用户每秒最多1次请求
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(RateLimits.class)
public @interface RateLimit {

    /**
     * 限流key前缀
     */
    String key() default "";

    /**
     * 限流次数
     */
    int limit() default 10;

    /**
     * 限流时间窗口（秒）
     */
    int period() default 1;

    /**
     * 限流类型
     */
    LimitType limitType() default LimitType.USER;

    /**
     * 限流类型枚举
     */
    enum LimitType {
        USER,   // 用户级限流（根据用户ID）
        IP,     // IP级限流（根据IP地址）
        GLOBAL  // 全局限流（所有请求共享配额）
    }
}