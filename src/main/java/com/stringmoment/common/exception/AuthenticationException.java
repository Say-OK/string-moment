package com.stringmoment.common.exception;

/**
 * 认证异常
 */
public class AuthenticationException extends RuntimeException {
    
    private Integer code = 401;  // 默认401未授权
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    
    public Integer getCode() {
        return code;
    }
}