package com.stringmoment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stringmoment.common.constant.UserConstant;
import com.stringmoment.common.exception.BusinessException;
import com.stringmoment.common.util.JwtUtil;
import com.stringmoment.common.util.PasswordUtil;
import com.stringmoment.entity.User;
import com.stringmoment.mapper.UserMapper;
import com.stringmoment.model.request.*;
import com.stringmoment.model.response.AdminUserPageVO;
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
        // 1. 查询用户
        User user = lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .one();

        // 2. 用户不存在或密码错误
        if (user == null || !passwordUtil.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 检查用户是否被禁用
        if (UserConstant.USER_STATUS_DISABLE.equals(user.getStatus())) {
            throw new BusinessException("您的账号已被禁用，请联系管理员");
        }

        // 4. 禁止管理员用用户登录接口
        if (UserConstant.USER_ROLE_ADMIN.equals(user.getRole())) {
            throw new BusinessException("管理员账号请使用管理后台登录");
        }

        // 5. 生成token（携带角色信息）
        String token = jwtUtil.generateToken(user.getId(), user.getRole() != null ? user.getRole() : UserConstant.USER_ROLE_NORMAL);

        // 6. 返回结果
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
        // 1. 查询用户
        User user = lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .one();

        // 2. 用户不存在或密码错误
        if (user == null || !passwordUtil.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 检查用户是否被禁用
        if (UserConstant.USER_STATUS_DISABLE.equals(user.getStatus())) {
            throw new BusinessException("您的账号已被禁用，请联系系统管理员");
        }

        // 4. 校验管理员角色
        if (!UserConstant.USER_ROLE_ADMIN.equals(user.getRole())) {
            throw new BusinessException("非管理员账户，无法登录管理后台");
        }

        // 5. 生成token（携带角色信息）
        String token = jwtUtil.generateToken(user.getId(), user.getRole());

        // 6. 返回结果
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

    // ==================== 管理员端用户管理 ====================

    /**
     * 获取用户列表（管理员端）
     */
    @Override
    public AdminUserPageVO getAdminUserList(AdminUserListQueryDTO dto) {
        // 1. 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        // 搜索关键词（用户名/昵称/手机号）
        if (StringUtils.hasText(dto.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(User::getUsername, dto.getKeyword())
                    .or()
                    .like(User::getNickname, dto.getKeyword())
                    .or()
                    .like(User::getPhone, dto.getKeyword())
            );
        }

        // 状态筛选
        if (dto.getStatus() != null) {
            queryWrapper.eq(User::getStatus, dto.getStatus());
        }

        // 角色筛选
        if (dto.getRole() != null) {
            queryWrapper.eq(User::getRole, dto.getRole());
        }

        // 按创建时间降序排序
        queryWrapper.orderByDesc(User::getCreateTime);

        // 2. 分页查询
        Page<User> page = new Page<>(dto.getPage(), dto.getSize());
        Page<User> result = page(page, queryWrapper);

        // 3. 构建返回对象
        AdminUserPageVO pageVO = new AdminUserPageVO();
        pageVO.setList(result.getRecords().stream().map(UserVO::fromEntity).toList());
        pageVO.setTotal(result.getTotal());
        pageVO.setPage(dto.getPage());
        pageVO.setSize(dto.getSize());
        pageVO.setPages((int) result.getPages());

        return pageVO;
    }

    /**
     * 获取用户详情（管理员端）
     */
    @Override
    public UserVO getUserDetailAdmin(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return UserVO.fromEntity(user);
    }

    /**
     * 禁用/启用用户（管理员端）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long id, Integer status) {
        // 1. 查询用户
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 校验管理员不能禁用其他管理员
        if (UserConstant.USER_ROLE_ADMIN.equals(user.getRole()) 
                && UserConstant.USER_STATUS_DISABLE.equals(status)) {
            throw new BusinessException("管理员不能禁用其他管理员账号");
        }

        // 3. 更新状态
        user.setStatus(status);
        updateById(user);
    }

}