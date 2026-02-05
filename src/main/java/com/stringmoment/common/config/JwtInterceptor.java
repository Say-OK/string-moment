package com.stringmoment.common.config;

import com.stringmoment.common.exception.AuthenticationException;
import com.stringmoment.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;



/**
 * JWT拦截器
 * 作用：验证Token，保护需要登录的接口
 * 位置：在Controller之前执行
 * 流程：请求 → 拦截器 → Controller
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        // 1. 从请求头获取Token
        String token = getTokenFromRequest(request);

        // 2. 如果没Token，抛出自定义异常
        if (token == null) {
            throw new AuthenticationException("未提供Token");
        }

        // 3. 验证Token
        if (!jwtUtil.validateToken(token)) {
            throw new AuthenticationException("Token无效或已过期");
        }

        // 4. 从Token中获取用户ID
        Long userId = jwtUtil.getUserIdFromToken(token);

        // 5. 将用户ID存入request，供后续使用
        request.setAttribute("userId", userId);

        return true;
    }

    /**
     * 从请求头获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);  // 去掉"Bearer "前缀
        }
        return null;
    }
}