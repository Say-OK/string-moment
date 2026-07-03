package com.stringmoment.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀活动更新DTO
 * 注意：秒杀活动ID通过路径参数传递，不在DTO中
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillActivityUpdateDTO {

    @Size(max = 200, message = "活动名称长度不能超过200字符")
    private String name;

    @DecimalMin(value = "0.01", message = "秒杀价格必须大于0")
    private BigDecimal seckillPrice;

    /**
     * 秒杀总库存
     */
    @Min(value = 0, message = "秒杀库存不能为负数")
    private Integer totalStock;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}