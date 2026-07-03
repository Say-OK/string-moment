package com.stringmoment.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员秒杀活动列表查询参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSeckillActivityListQueryDTO {

    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;      // 当前页码，默认1

    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer size = 10;     // 每页数量，默认10

    /**
     * 秒杀活动状态筛选（管理员特有）
     * null: 查询所有活动（未开始+进行中+已结束）
     * 0: 只查询未开始的活动
     * 1: 只查询进行中的活动
     * 2: 只查询已结束的活动
     */
    private Integer status;        // 状态筛选（可选）

    private String keyword;        // 搜索关键词（活动名称）
}