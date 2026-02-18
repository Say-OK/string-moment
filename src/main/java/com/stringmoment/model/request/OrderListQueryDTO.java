package com.stringmoment.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单列表查询参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderListQueryDTO {

    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;

    @NotNull(message = "每页数量不能为空")
    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer size = 10;

    @Min(value = 0, message = "订单状态必须在0-4之间")
    @Max(value = 4, message = "订单状态必须在0-4之间")
    private Integer status;

    @Min(value = 1, message = "订单类型必须在1-2之间")
    @Max(value = 2, message = "订单类型必须在1-2之间")
    private Integer orderType;
}