package com.stringmoment.controller;

import com.stringmoment.common.result.Result;
import com.stringmoment.model.request.UserLoginDTO;
import com.stringmoment.model.request.UserRegisterDTO;
import com.stringmoment.model.response.LoginResultVO;
import com.stringmoment.model.response.UserVO;
import com.stringmoment.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * 用户Controller
 */
@RestController  // 这个注解表示这是Controller，并且返回JSON
@RequestMapping("/user")
@Validated  // 开启Spring的参数校验（类级别）
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody UserRegisterDTO dto) {
        UserVO userVO = userService.register(dto);
        return Result.success("注册成功", userVO);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResultVO> login(@Valid @RequestBody UserLoginDTO dto) {
        LoginResultVO loginResultVO = userService.login(dto);
        return Result.success("登录成功", loginResultVO);
    }

    /**
     * 获取当前用户信息（需要Token）
     * 注意：这里只需要HttpServletRequest，不需要@RequestHeader
     * 因为拦截器已经验证了Token并将userId放入了request
     */
    @GetMapping("/info")
    public Result<UserVO> getUserInfo(HttpServletRequest request) {
        // 从request中获取拦截器存入的userId
        Long userId = (Long) request.getAttribute("userId");
        UserVO userVO = userService.getUserInfo(userId);
        return Result.success(userVO);
    }

}
