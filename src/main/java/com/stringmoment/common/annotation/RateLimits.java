package com.stringmoment.common.annotation;

import java.lang.annotation.*;

/**
 * 限流注解容器（支持多个限流规则）
 * 用于同一个方法上应用多个 @RateLimit 注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimits {
    RateLimit[] value();
}