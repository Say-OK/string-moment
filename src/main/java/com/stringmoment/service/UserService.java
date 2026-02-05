package com.stringmoment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stringmoment.model.request.UserLoginDTO;
import com.stringmoment.model.request.UserRegisterDTO;
import com.stringmoment.model.response.LoginResultVO;
import com.stringmoment.model.response.UserVO;
import com.stringmoment.entity.User;
import jakarta.validation.Valid;


/**
 * 用户服务接口
 * 继承IService，获得MyBatis-Plus提供的增强服务方法
 */
public interface UserService extends IService<User> {
    
    /**
     * 用户注册
     */
    UserVO register(@Valid UserRegisterDTO dto);
    
    /**
     * 用户登录
     */
    LoginResultVO login(@Valid UserLoginDTO dto);


    /**
     * 获取当前用户信息
     */
    UserVO getUserInfo(Long userId);
}