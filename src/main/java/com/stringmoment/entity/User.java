package com.stringmoment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表：user
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")  // 指定表名，如果不加，默认用类名的小写
public class User implements Serializable {

    /**
     * 序列化版本
     */
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     * @TableId 表示这是主键
     * value = "id" 指定数据库字段名（如果字段名与属性名一致可省略）
     * type = IdType.AUTO 表示自增主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "username")
    private String username;

    @TableField(value = "password")
    private String password;

    @TableField(value = "nickname")
    private String nickname;

    @TableField(value = "phone")
    private String phone;
    
    /**
     * 头像URL
     * defaultValue 可以设置默认值
     */
    @TableField(value = "avatar")
    private String avatar = "/default-avatar.jpg";
    
    /**
     * 状态：0-禁用，1-正常
     */
    @TableField(value = "status")
    private Integer status = 1;
    
    /**
     * 注册时间
     * 使用LocalDateTime代替Date，是Java8的新时间API
     * 数据库字段：create_time，通过@TableField映射
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 最后更新时间
     * update = false 表示不是更新字段，由数据库自动维护
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}