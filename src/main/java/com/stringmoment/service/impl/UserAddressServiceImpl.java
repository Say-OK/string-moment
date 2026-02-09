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

import java.time.LocalDateTime;
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

    /**
     * 设置默认地址
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddressVO setDefaultAddress(Long id, Long userId) {
        // 1. 查询地址信息
        UserAddress address = lambdaQuery()
                .eq(UserAddress::getId, id)
                .eq(UserAddress::getUserId, userId)
                .one();

        // 2. 为空检查
        if (address == null) {
            throw new RuntimeException("地址不存在");
        }

        // 3. 状态检查：如果已经是默认地址，直接返回
        if (address.getIsDefault() == 1) {
            return AddressVO.fromEntity(address);
        }

        // 4. 取消其他默认地址
        lambdaUpdate()
                .set(UserAddress::getIsDefault, 0)
                .set(UserAddress::getUpdateTime, LocalDateTime.now())  // 条件更新自动注入不生效
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1)
                .update();

        // 5. 设置当前地址为默认
        address.setIsDefault(1);
        updateById(address);

        // 6. 返回更新后的地址
        return AddressVO.fromEntity(address);
    }

}
