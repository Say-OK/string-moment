package com.stringmoment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收货地址实体类
 * 对应数据库表：user_address
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_address")
public class UserAddress implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;
    
    /**
     * 收货人姓名
     */
    @TableField(value = "receiver_name")
    private String receiverName;
    
    /**
     * 收货人手机号
     */
    @TableField(value = "receiver_phone")
    private String receiverPhone;
    
    /**
     * 省份
     */
    @TableField(value = "province")
    private String province;
    
    /**
     * 城市
     */
    @TableField(value = "city")
    private String city;
    
    /**
     * 区/县
     */
    @TableField(value = "district")
    private String district;
    
    /**
     * 详细地址
     */
    @TableField(value = "detail_address")
    private String detailAddress;
    
    /**
     * 是否默认地址：0-否，1-是
     */
    @TableField(value = "is_default")
    private Integer isDefault = 0;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}