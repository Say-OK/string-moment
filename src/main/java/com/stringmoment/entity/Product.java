package com.stringmoment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 * 对应数据库表：product
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("product")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField(value = "name")
    private String name;
    
    /**
     * 商品描述
     * 使用String类型对应数据库的text类型
     */
    @TableField(value = "description")
    private String description;
    
    /**
     * 商品分类
     */
    @TableField(value = "category")
    private String category;
    
    /**
     * 原价
     * 使用BigDecimal处理金额，避免精度问题
     */
    @TableField(value = "price")
    private BigDecimal price;
    
    /**
     * 总库存
     */
    @TableField(value = "stock")
    private Integer stock;
    
    /**
     * 商品主图URL
     */
    @TableField(value = "image_url")
    private String imageUrl;
    
    /**
     * 状态：0-下架，1-上架
     */
    @TableField(value = "status")
    private Integer status = 1;
    
    /**
     * 销量统计
     */
    @TableField(value = "sale_count")
    private Integer saleCount = 0;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}