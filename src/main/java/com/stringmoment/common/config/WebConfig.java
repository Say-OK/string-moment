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
        registry.addInterceptor(jwtInterceptor)
                // 1. 拦截所有接口
                .addPathPatterns("/**")

                // 2. 排除无需token的公开接口
                .excludePathPatterns(
                        // 用户模块公开接口
                        "/user/register",    // 注册
                        "/user/login",       // 登录

                        // 商品模块公开接口
                        "/product/list",     // 商品列表
                        "/product/detail/**" // 商品详情
                );
    }
}
