package com.stringmoment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stringmoment.common.exception.BusinessException;
import com.stringmoment.common.util.JwtUtil;
import com.stringmoment.common.util.PasswordUtil;
import com.stringmoment.model.request.UserLoginDTO;
import com.stringmoment.model.request.UserRegisterDTO;
import com.stringmoment.model.response.LoginResultVO;
import com.stringmoment.model.response.UserVO;
import com.stringmoment.entity.User;
import com.stringmoment.mapper.UserMapper;
import com.stringmoment.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service  // 这个注解告诉Spring这是一个Service组件
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordUtil passwordUtil;

    /**
     * 用户注册
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(UserRegisterDTO dto) {
        // 1. 检查用户名是否已存在
        boolean usernameExists = this.lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .exists();
        if (usernameExists) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 检查手机号是否已存在
        boolean phoneExists = this.lambdaQuery()
                .eq(User::getPhone, dto.getPhone())
                .exists();
        if (phoneExists) {
            throw new BusinessException("手机号已被注册");
        }

        // 3. 创建用户
        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordUtil.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .phone(dto.getPhone())
                .build();

        // 4. 保存到数据库
        this.save(user);

        // 5. 返回VO
        return UserVO.fromEntity(user);
    }

    /**
     * 用户登录
     */
    @Override
    public LoginResultVO login(UserLoginDTO dto) {
        // 1. 查询用户（状态正常）
        User user = this.lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .eq(User::getStatus, 1)
                .one();

        // 2. 统一验证
        if (user == null || !passwordUtil.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 生成token
        String token = jwtUtil.generateToken(user.getId());

        // 4. 返回结果
        return LoginResultVO.builder()
                .token(token)
                .user(UserVO.fromEntity(user))
                .build();
    }

    /**
     * 获取当前用户信息
     */
    @Override
    public UserVO getUserInfo(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return UserVO.fromEntity(user);
    }

}