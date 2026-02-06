package com.stringmoment.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品列表查询参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListQueryDTO {

    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;      // 当前页码，默认1

    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer size = 10;     // 每页数量，默认10

    private String category;       // 商品分类
    private String keyword;        // 搜索关键词
    private Integer status = 1;    // 状态，默认只查询上架商品
}