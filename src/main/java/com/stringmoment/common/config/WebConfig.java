package com.stringmoment.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web应用的配置类：负责拦截器的注册和路径配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                // 1. 拦截所有接口
                .addPathPatterns("/**")

                // 2. 排除无需token的公开接口
                .excludePathPatterns(
                        // 用户模块公开接口
                        "/user/register",     // 注册
                        "/user/login",        // 登录

                        // 管理员模块公开接口
                        "/admin/login",       // 管理员登录

                        // 商品模块公开接口（用户端）
                        "/product/list",      // 商品列表
                        "/product/detail/*",  // 商品详情
                        "/product/categories", // 商品分类列表

                        // 秒杀模块公开接口
                        "/seckill/activity/list",     // 秒杀活动列表
                        "/seckill/activity/detail/*",  // 秒杀活动详情

                        // 静态资源访问路径
                        "/uploads/**"
                );
    }

    /**
     * 配置静态资源路径
     * 让上传的文件可以通过HTTP访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根目录的绝对路径
        String projectRoot = System.getProperty("user.dir");
        String uploadsPath = projectRoot + "/uploads/";

        // 映射 /uploads/** 到项目根目录下的 uploads 文件夹
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadsPath);
    }
}
