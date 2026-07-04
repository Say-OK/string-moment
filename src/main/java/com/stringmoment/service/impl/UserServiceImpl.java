package com.stringmoment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stringmoment.common.constant.UserConstant;
import com.stringmoment.common.exception.BusinessException;
import com.stringmoment.common.util.JwtUtil;
import com.stringmoment.common.util.PasswordUtil;
import com.stringmoment.entity.User;
import com.stringmoment.mapper.UserMapper;
import com.stringmoment.model.request.PasswordUpdateDTO;
import com.stringmoment.model.request.UserLoginDTO;
import com.stringmoment.model.request.UserRegisterDTO;
import com.stringmoment.model.request.UserUpdateDTO;
import com.stringmoment.model.response.LoginResultVO;
import com.stringmoment.model.response.UserVO;
import com.stringmoment.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
        boolean usernameExists = lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .exists();
        if (usernameExists) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 检查手机号是否已存在
        boolean phoneExists = lambdaQuery()
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
                .avatar(UserConstant.DEFAULT_AVATAR)
                .status(UserConstant.USER_STATUS_NORMAL)
                .role(UserConstant.USER_ROLE_NORMAL)
                .build();

        // 4. 保存到数据库
        save(user);

        // 5. 返回VO
        return UserVO.fromEntity(user);
    }

    /**
     * 用户登录
     */
    @Override
    public LoginResultVO login(UserLoginDTO dto) {
        // 1. 查询用户（状态正常）
        User user = lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .eq(User::getStatus, UserConstant.USER_STATUS_NORMAL)
                .one();

        // 2. 统一验证
        if (user == null || !passwordUtil.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 禁止管理员用用户登录接口
        if (UserConstant.USER_ROLE_ADMIN.equals(user.getRole())) {
            throw new BusinessException("管理员账号请使用管理后台登录");
        }

        // 4. 生成token（携带角色信息）
        String token = jwtUtil.generateToken(user.getId(), user.getRole() != null ? user.getRole() : UserConstant.USER_ROLE_NORMAL);

        // 5. 返回结果
        return LoginResultVO.builder()
                .token(token)
                .user(UserVO.fromEntity(user))
                .build();
    }

    /**
     * 管理员登录
     */
    @Override
    public LoginResultVO adminLogin(UserLoginDTO dto) {
        // 1. 查询用户（状态正常）
        User user = lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .eq(User::getStatus, UserConstant.USER_STATUS_NORMAL)
                .one();

        // 2. 统一验证
        if (user == null || !passwordUtil.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 校验管理员角色
        if (!UserConstant.USER_ROLE_ADMIN.equals(user.getRole())) {
            throw new BusinessException("非管理员账户，无法登录管理后台");
        }

        // 4. 生成token（携带角色信息）
        String token = jwtUtil.generateToken(user.getId(), user.getRole());

        // 5. 返回结果
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
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return UserVO.fromEntity(user);
    }

    /**
     * 更新用户信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateUserInfo(Long userId, UserUpdateDTO dto) {
        // 1. 查询用户
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 检查用户状态
        if (!user.getStatus().equals(UserConstant.USER_STATUS_NORMAL)) {
            throw new BusinessException("用户已被禁用，无法修改信息");
        }

        // 3. 如果修改了手机号，需要检查手机号是否已被其他用户使用
        if (StringUtils.hasText(dto.getPhone()) && !dto.getPhone().equals(user.getPhone())) {
            boolean phoneExists = lambdaQuery()
                    .eq(User::getPhone, dto.getPhone())
                    .ne(User::getId, userId)
                    .exists();
            if (phoneExists) {
                throw new BusinessException("手机号已被其他用户使用");
            }
            user.setPhone(dto.getPhone());
        }

        // 4. 更新其他字段（只更新非空字段）
        if (StringUtils.hasText(dto.getNickname())) {
            user.setNickname(dto.getNickname());
        }
        if (StringUtils.hasText(dto.getAvatar())) {
            user.setAvatar(dto.getAvatar());
        }

        // 5. 保存更新
        updateById(user);

        // 6. 返回更新后的用户信息
        return UserVO.fromEntity(user);
    }

    /**
     * 修改密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, PasswordUpdateDTO dto) {
        // 1. 查询用户
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 检查用户状态
        if (!user.getStatus().equals(UserConstant.USER_STATUS_NORMAL)) {
            throw new BusinessException("用户已被禁用，无法修改密码");
        }

        // 3. 验证旧密码
        if (!passwordUtil.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }

        // 4. 检查新密码不能与旧密码相同
        if (passwordUtil.matches(dto.getNewPassword(), user.getPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }

        // 5. 更新密码
        user.setPassword(passwordUtil.encode(dto.getNewPassword()));
        updateById(user);
    }

}