package com.stringmoment.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员用户列表查询参数
 * 管理员可以查询所有用户（包括管理员和普通用户），并可以按状态和角色筛选
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserListQueryDTO {

    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;      // 当前页码，默认1

    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer size = 10;     // 每页数量，默认10

    private String keyword;        // 搜索关键词（用户名/昵称/手机号）

    /**
     * 用户状态筛选
     * null: 查询所有用户（正常+禁用）
     * 0: 只查询禁用用户
     * 1: 只查询正常用户
     */
    private Integer status;        // 状态筛选（可选）

    /**
     * 用户角色筛选
     * null: 查询所有角色（管理员+普通用户）
     * 0: 只查询普通用户
     * 1: 只查询管理员
     */
    private Integer role;          // 角色筛选（可选）
}