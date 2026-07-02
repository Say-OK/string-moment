package com.stringmoment.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 管理员商品列表查询参数
 * 管理员可以查询所有商品（上架+下架），并可以按状态筛选
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductListQueryDTO {

    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;      // 当前页码，默认1

    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer size = 10;     // 每页数量，默认10

    private String category;       // 商品分类
    private String keyword;        // 搜索关键词

    /**
     * 商品状态筛选（管理员特有）
     * null: 查询所有商品（上架+下架）
     * 0: 只查询下架商品
     * 1: 只查询上架商品
     */
    private Integer status;        // 状态筛选（可选）

    /**
     * 价格区间筛选
     */
    private BigDecimal minPrice;   // 最低价格
    private BigDecimal maxPrice;   // 最高价格

    /**
     * 排序字段
     */
    private String sortBy;         // 排序字段：price, sale_count, create_time
    private String sortOrder;      // 排序方式：asc, desc
}