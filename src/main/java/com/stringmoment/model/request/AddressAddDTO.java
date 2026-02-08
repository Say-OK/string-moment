package com.stringmoment.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 添加收货地址请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressAddDTO {
    
    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;
    
    @NotBlank(message = "收货人手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String receiverPhone;
    
    @NotBlank(message = "省份不能为空")
    private String province;
    
    @NotBlank(message = "城市不能为空")
    private String city;
    
    @NotBlank(message = "区/县不能为空")
    private String district;
    
    @NotBlank(message = "详细地址不能为空")
    private String detailAddress;
    
    private Integer isDefault = 0;  // 0-否，1-是
}