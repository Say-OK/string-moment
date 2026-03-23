package com.stringmoment.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

/**
 * 秒杀业务专用配置类
 * 管理秒杀相关的所有配置Bean（Lua脚本、限流器、常量等）
 */
@Configuration
public class SeckillConfig {

    /**
     * 秒杀核心Lua脚本Bean
     * 功能：扣减Redis库存 + 防重复秒杀（一人一单）
     * 返回值说明：
     * 0 - 秒杀成功
     * 1 - 库存不足
     * 2 - 重复秒杀（用户已购买过）
     * 3 - 活动未开始
     * 4 - 活动已结束
     */
    @Bean
    public DefaultRedisScript<Long> seckillLuaScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/seckill.lua"));
        script.setResultType(Long.class);
        return script;
    }
}
