package com.stringmoment.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.stringmoment.entity.UserAddress;
import com.stringmoment.model.request.AddressAddDTO;
import com.stringmoment.model.response.AddressVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 用户地址服务接口
 */
public interface UserAddressService extends IService<UserAddress> {

    /**
     * 添加收货地址
     */
    AddressVO addAddress(Long userId, @Valid AddressAddDTO dto);

    /**
     * 获取地址列表
     */
    List<AddressVO> getAddressList(Long userId);
}
