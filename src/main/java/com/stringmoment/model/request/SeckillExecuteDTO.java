package com.stringmoment.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 执行秒杀请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillExecuteDTO {
    
    @NotNull(message = "秒杀活动ID不能为空")
    private Long seckillActivityId;
    
    @NotNull(message = "收货地址ID不能为空")
    private Long addressId;
}
