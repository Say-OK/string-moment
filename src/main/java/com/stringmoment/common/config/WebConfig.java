package com.stringmoment.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web应用的配置类：负责拦截器的注册和路径配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private JwtInterceptor jwtInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 配置需要Token验证的接口
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns(
                    "/user/info",          // 用户信息
                    "/address/**",         // 地址相关（所有接口）
                    "/order/**",           // 订单相关（所有接口）
                    "/seckill/**"          // 秒杀相关（所有接口）
                )
                .excludePathPatterns(
                    "/user/register",      // 注册（不需要Token）
                    "/user/login"          // 登录（不需要Token）
                );
    }
}
