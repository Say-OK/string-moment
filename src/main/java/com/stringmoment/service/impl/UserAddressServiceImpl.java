package com.stringmoment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stringmoment.common.exception.BusinessException;
import com.stringmoment.entity.UserAddress;
import com.stringmoment.mapper.UserAddressMapper;
import com.stringmoment.model.request.AddressAddDTO;
import com.stringmoment.model.response.AddressVO;
import com.stringmoment.service.UserAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {

    /**
     * 添加收货地址
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddressVO addAddress(Long userId, AddressAddDTO dto) {
        // 1. 检查：用户最多有10个地址
        long addressCount = lambdaQuery()
                .eq(UserAddress::getUserId, userId)
                .count();

        if (addressCount >= 10) {
            throw new BusinessException("最多只能有10个收货地址");
        }

        // 2. 如果设置为默认地址，要先取消其他默认地址
        if (dto.getIsDefault() == 1) {
            lambdaUpdate()
                    .set(UserAddress::getIsDefault, 0)
                    .eq(UserAddress::getUserId, userId)
                    .eq(UserAddress::getIsDefault, 1)
                    .update();
        }

        // 3. 创建地址
        UserAddress address = UserAddress.builder()
                .userId(userId)
                .receiverName(dto.getReceiverName())
                .receiverPhone(dto.getReceiverPhone())
                .province(dto.getProvince())
                .city(dto.getCity())
                .district(dto.getDistrict())
                .detailAddress(dto.getDetailAddress())
                .isDefault(dto.getIsDefault() != null ? dto.getIsDefault() : 0)
                .build();

        // 4. 保存到数据库
        save(address);

        // 5. 返回VO
        return AddressVO.fromEntity(address);
    }

    /**
     * 获取地址列表
     */
    @Override
    public List<AddressVO> getAddressList(Long userId) {
        // 1. 查询并按创建时间倒序
        List<UserAddress> addressList = lambdaQuery()
                .eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getCreateTime)
                .list();

        // 2. 转换为VO
        return addressList.stream()
                .map(AddressVO::fromEntity)
                .collect(Collectors.toList());
    }

}
