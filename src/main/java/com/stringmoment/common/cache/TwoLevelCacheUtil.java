package com.stringmoment.common.cache;

import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 二级缓存工具类（Caffeine本地缓存 + Redis分布式缓存）
 * <p>
 * 核心功能：
 * 1. 查询：本地缓存 → Redis缓存 → 数据库查询
 * 2. 写入：支持随机TTL防止雪崩、空值缓存防止穿透
 * 3. 清除：延时双删策略防止读写竞态和分布式缓存不一致
 * </p>
 */
@Slf4j
@Component
public class TwoLevelCacheUtil {

    private final StringRedisTemplate stringRedisTemplate;

    public TwoLevelCacheUtil(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 查询二级缓存（支持空值缓存）
     *
     * @param cacheKey       Redis缓存键
     * @param localCache     本地缓存实例
     * @param cacheKeyObj    本地缓存键（通常是ID）
     * @param clazz          返回值类型
     * @param dbSupplier     数据库查询逻辑（当缓存不存在时调用）
     * @param baseTtlSec     Redis基础TTL（秒）
     * @param randomRangeSec Redis TTL随机偏移范围（秒）
     * @param emptyTtlSec    空值缓存TTL（秒）
     * @param <K>            本地缓存键类型
     * @param <V>            返回值类型
     * @return 查询结果，如果不存在返回null（需自行处理空值异常）
     */
    public <K, V> V getWithCache(
            String cacheKey,
            Cache<K, V> localCache,
            K cacheKeyObj,
            Class<V> clazz,
            Supplier<V> dbSupplier,
            int baseTtlSec,
            int randomRangeSec,
            int emptyTtlSec) {

        // 1. 尝试从本地缓存获取
        V localCached = localCache.getIfPresent(cacheKeyObj);
        if (localCached != null) {
            return localCached;
        }

        // 2. 尝试从Redis缓存获取
        String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);

        if (StringUtils.hasText(cacheValue)) {
            V value = JSONUtil.toBean(cacheValue, clazz);
            // 回填本地缓存
            localCache.put(cacheKeyObj, value);
            return value;
        }

        // 3. 缓存不存在，查询数据库
        V value = dbSupplier.get();

        if (value == null) {
            // 空值缓存，防止穿透
            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    "",
                    emptyTtlSec,
                    TimeUnit.SECONDS
            );
            log.debug("数据不存在，写入空值缓存: key={}", cacheKeyObj);
            return null;
        }

        // 4. 写入缓存（随机TTL防止雪崩）
        int randomTtl = baseTtlSec
                + ThreadLocalRandom.current().nextInt(-randomRangeSec, randomRangeSec);

        stringRedisTemplate.opsForValue().set(
                cacheKey,
                JSONUtil.toJsonStr(value),
                randomTtl,
                TimeUnit.SECONDS
        );
        localCache.put(cacheKeyObj, value);
        log.debug("缓存重建完成: key={}, ttl={}秒", cacheKeyObj, randomTtl);

        return value;
    }

    /**
     * 清除二级缓存（延时双删策略）
     *
     * @param cacheKey       Redis缓存键
     * @param localCache     本地缓存实例
     * @param cacheKeyObj    本地缓存键
     * @param delayDeleteMs  延时删除时间（毫秒）
     * @param <K>            本地缓存键类型
     */
    public <K> void evictWithDelayDelete(
            String cacheKey,
            Cache<K, ?> localCache,
            K cacheKeyObj,
            int delayDeleteMs) {

        // 1. 立即清除本地缓存和Redis缓存
        localCache.invalidate(cacheKeyObj);
        stringRedisTemplate.delete(cacheKey);

        // 2. 异步延时二次删除Redis（解决读写并发脏写窗口）
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(delayDeleteMs);
                stringRedisTemplate.delete(cacheKey);
            } catch (InterruptedException e) {
                log.error("延时删除缓存失败: key={}", cacheKey, e);
                Thread.currentThread().interrupt();
            }
        });
    }
}