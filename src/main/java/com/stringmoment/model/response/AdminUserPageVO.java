package com.stringmoment.model.response;

import lombok.Data;

import java.util.List;

/**
 * 管理员用户列表分页返回数据
 */
@Data
public class AdminUserPageVO {

    private List<UserVO> list;     // 用户列表
    private Long total;            // 总记录数
    private Integer page;          // 当前页码
    private Integer size;          // 每页数量
    private Integer pages;         // 总页数
}