package com.stringmoment.common.aspect;

import com.stringmoment.common.annotation.RateLimit;
import com.stringmoment.common.exception.BusinessException;
import com.stringmoment.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 限流切面
 * 基于Redis滑动窗口算法实现多维度限流
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 限流前缀
     */
    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";

    @Before("@annotation(com.stringmoment.common.annotation.RateLimit)")
    public void doBefore(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        if (rateLimit == null) {
            return;
        }

        // 获取限流key
        String key = buildLimitKey(rateLimit);
        if (key == null) {
            throw new BusinessException("限流key生成失败，请检查请求头或登录状态");
        }

        // 执行滑动窗口限流
        boolean allowed = checkRateLimit(key, rateLimit.limit(), rateLimit.period());

        if (!allowed) {
            log.warn("触发限流: key={}, limit={}, period={}s", key, rateLimit.limit(), rateLimit.period());
            throw new BusinessException("系统繁忙，请稍后重试");
        }
    }

    /**
     * 构建限流key（根据限流类型）
     */
    private String buildLimitKey(RateLimit rateLimit) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }

        HttpServletRequest request = attributes.getRequest();
        String keyPrefix = RATE_LIMIT_KEY_PREFIX + rateLimit.key() + ":";

        switch (rateLimit.limitType()) {
            case USER:
                // 用户级限流：根据用户ID
                String token = request.getHeader("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    if (userId != null) {
                        return keyPrefix + "user:" + userId;
                    }
                }
                return null;

            case IP:
                // IP级限流：根据客户端IP
                String ip = getClientIp(request);
                return keyPrefix + "ip:" + ip;

            case GLOBAL:
                // 全局限流：所有请求共享配额
                return keyPrefix + "global";

            default:
                return null;
        }
    }

    /**
     * 获取客户端真实IP（支持代理场景）
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果存在多个IP（代理场景），取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 滑动窗口算法判断是否超过限流阈值
     * <p>
     * 原理：
     * 1. 使用Redis ZSET存储请求时间戳
     * 2. 每次请求时，先删除窗口外的旧请求（score < 当前时间-窗口大小）
     * 3. 统计当前窗口内的请求数量
     * 4. 如果超过阈值，返回false；否则添加当前请求时间戳，返回true
     *
     * @param key    限流key
     * @param limit  限流次数
     * @param period 时间窗口（秒）
     * @return true-允许通过，false-触发限流
     */
    private boolean checkRateLimit(String key, int limit, int period) {
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - (long) period * 1000;

        try {
            // 1. 删除窗口外的旧请求（滑动窗口核心）
            stringRedisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

            // 2. 统计当前窗口内的请求数量
            Long count = stringRedisTemplate.opsForZSet().zCard(key);

            if (count != null && count >= limit) {
                // 超过限流阈值
                return false;
            }

            // 3. 添加当前请求时间戳
            stringRedisTemplate.opsForZSet().add(key, String.valueOf(currentTime), currentTime);

            // 4. 设置过期时间（避免冷key长期占用内存）
            stringRedisTemplate.expire(key, period, TimeUnit.SECONDS);

            return true;
        } catch (Exception e) {
            log.error("限流检查异常: key={}", key, e);
            // 异常情况放行，避免影响正常业务
            return true;
        }
    }
}