package com.stringmoment.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResultVO {
    
    private String token;
    private UserVO user;
}