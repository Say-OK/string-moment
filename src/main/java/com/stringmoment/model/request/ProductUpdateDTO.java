package com.stringmoment.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 更新商品DTO
 * 注意：商品ID通过路径参数传递，不在DTO中
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateDTO {

    /**
     * 商品名称
     */
    @Size(max = 200, message = "商品名称长度不能超过200个字符")
    private String name;

    /**
     * 商品描述
     */
    @Size(max = 2000, message = "商品描述长度不能超过2000个字符")
    private String description;

    /**
     * 商品分类
     */
    @Size(max = 100, message = "商品分类长度不能超过100个字符")
    private String category;

    /**
     * 商品价格
     */
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal price;

    /**
     * 商品库存
     */
    @Min(value = 0, message = "商品库存不能为负数")
    private Integer stock;

    /**
     * 商品主图URL
     */
    @Size(max = 500, message = "图片URL长度不能超过500个字符")
    private String imageUrl;
}