package com.stringmoment.common.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.stringmoment.common.constant.ProductConstant;
import com.stringmoment.model.response.ProductVO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine本地缓存配置
 * 用于商品详情页热点数据缓存，减少Redis网络IO
 */
@Configuration
public class CacheConfig {

    /**
     * 商品详情本地缓存
     * - 最大缓存1000个热点商品
     * - 写入后5分钟过期（比Redis的30分钟短，保证数据相对新鲜）
     * - 访问后3分钟不被淘汰（热点数据常驻内存）
     */
    @Bean
    public Cache<Long, ProductVO> productDetailCache() {
        return Caffeine.newBuilder()
                .maximumSize(ProductConstant.LOCAL_CACHE_MAX_SIZE)
                .expireAfterWrite(ProductConstant.LOCAL_CACHE_EXPIRE_WRITE_MINUTES, TimeUnit.MINUTES)
                .expireAfterAccess(ProductConstant.LOCAL_CACHE_EXPIRE_ACCESS_MINUTES, TimeUnit.MINUTES)
                .build();
    }
}