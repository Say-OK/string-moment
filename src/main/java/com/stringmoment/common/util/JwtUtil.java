package com.stringmoment.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Component
public class JwtUtil {
    
    @Value("${jwt.secret:string-moment-secret}")
    private String secret;  // 密钥
    
    @Value("${jwt.expire:604800000}") // 默认7天
    private Long expire;  // 过期时间

    /**
     * 生成密钥对象
     */
    private SecretKey getSecretKey() {
        // 确保密钥长度至少32个字符（256位）
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT密钥不能为空");
        }

        // 如果密钥太短，进行填充
        String validSecret = secret;
        if (validSecret.length() < 32) {
            StringBuilder sb = new StringBuilder(validSecret);
            while (sb.length() < 32) {
                sb.append("0");
            }
            validSecret = sb.toString();
        } else if (validSecret.length() > 32) {
            // 如果太长，截取前32个字符
            validSecret = validSecret.substring(0, 32);
        }

        return Keys.hmacShaKeyFor(validSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成token（携带用户角色）
     */
    public String generateToken(Long userId, Integer role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)  // 内容
                .setIssuedAt(new Date())  // 发行时间
                .setExpiration(new Date(System.currentTimeMillis() + expire))  // 过期时间
                .signWith(getSecretKey(), io.jsonwebtoken.SignatureAlgorithm.HS256)  // 使用SecretKey对象
                .compact();
    }
    
    /**
     * 从token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        // 1. 获取userId
        Claims claims = getClaimsFromToken(token);
        Object userId = claims.get("userId");

        // 2. 处理类型转换（有些情况下数字可能是Integer）
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        } else if (userId instanceof Long) {
            return (Long) userId;
        } else if (userId instanceof String) {
            return Long.parseLong((String) userId);
        } else {
            throw new IllegalArgumentException("用户ID格式不正确");
        }
    }

    /**
     * 从token中获取用户角色
     */
    public Integer getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object role = claims.get("role");

        if (role == null) {
            return 0; // 默认普通用户
        }

        if (role instanceof Integer) {
            return (Integer) role;
        } else if (role instanceof Long) {
            return ((Long) role).intValue();
        } else if (role instanceof String) {
            return Integer.parseInt((String) role);
        } else {
            return 0;
        }
    }
    
    /**
     * 验证token是否有效
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 从token中获取claims
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())  // 使用SecretKey对象
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}