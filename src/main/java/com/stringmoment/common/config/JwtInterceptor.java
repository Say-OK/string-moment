package com.stringmoment.common.config;

import com.stringmoment.common.constant.UserConstant;
import com.stringmoment.common.exception.AuthenticationException;
import com.stringmoment.common.util.JwtUtil;
import com.stringmoment.entity.User;
import com.stringmoment.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
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

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        // 1. 跳过静态资源和非Controller请求
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 2. 从请求头获取Token
        String token = getTokenFromRequest(request);

        // 3. 如果没Token，抛出自定义异常
        if (token == null) {
            throw new AuthenticationException("未提供Token");
        }

        // 4. 验证Token
        if (!jwtUtil.validateToken(token)) {
            throw new AuthenticationException("Token无效或已过期");
        }

        // 5. 从Token中获取用户ID和角色
        Long userId = jwtUtil.getUserIdFromToken(token);
        Integer role = jwtUtil.getRoleFromToken(token);

        // 6. 检查用户是否被禁用（实时检查数据库状态）
        User user = userService.getById(userId);
        if (user == null) {
            throw new AuthenticationException("用户不存在");
        }
        if (UserConstant.USER_STATUS_DISABLE.equals(user.getStatus())) {
            throw new AuthenticationException("您的账号已被禁用，请联系管理员");
        }

        // 7. 将用户ID和角色存入request，供后续使用
        request.setAttribute("userId", userId);
        request.setAttribute("role", role);

        // 8. 对/admin/**路径进行管理员权限校验
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/admin/")) {
            if (!UserConstant.USER_ROLE_ADMIN.equals(role)) {
                throw new AuthenticationException("无管理员权限");
            }
        }

        return true;
    }

    /**
     * 从请求头获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ") && authHeader.length() > 7) {
            return authHeader.substring(7);  // 去掉"Bearer "前缀
        }
        return null;
    }
}