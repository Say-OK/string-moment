package com.stringmoment.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

/**
 * 收货地址响应对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressVO {
    
    private Long id;
    private Long userId;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private Integer isDefault;
    private String createTime;
    private String updateTime;
    
    /**
     * 从UserAddress实体转换为AddressVO
     */
    public static AddressVO fromEntity(com.stringmoment.entity.UserAddress address) {
        if (address == null) {
            return null;
        }
        
        AddressVO vo = new AddressVO();
        vo.setId(address.getId());
        vo.setUserId(address.getUserId());
        vo.setReceiverName(address.getReceiverName());
        vo.setReceiverPhone(address.getReceiverPhone());
        vo.setProvince(address.getProvince());
        vo.setCity(address.getCity());
        vo.setDistrict(address.getDistrict());
        vo.setDetailAddress(address.getDetailAddress());
        vo.setIsDefault(address.getIsDefault());
        
        // 格式化时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (address.getCreateTime() != null) {
            vo.setCreateTime(address.getCreateTime().format(formatter));
        }
        if (address.getUpdateTime() != null) {
            vo.setUpdateTime(address.getUpdateTime().format(formatter));
        }
        
        return vo;
    }
}