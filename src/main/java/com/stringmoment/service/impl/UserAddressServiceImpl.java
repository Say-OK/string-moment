package com.stringmoment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stringmoment.common.constant.UserConstant;
import com.stringmoment.common.exception.BusinessException;
import com.stringmoment.entity.UserAddress;
import com.stringmoment.mapper.UserAddressMapper;
import com.stringmoment.model.request.AddressAddDTO;
import com.stringmoment.model.request.AddressUpdateDTO;
import com.stringmoment.model.response.AddressVO;
import com.stringmoment.service.UserAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {

    /**
     * 取消用户的默认地址
     * @param userId 用户ID
     */
    private void clearOtherDefaultAddresses(Long userId) {
        lambdaUpdate()
                .set(UserAddress::getIsDefault, UserConstant.ADDRESS_NOT_DEFAULT)
                .set(UserAddress::getUpdateTime, LocalDateTime.now())
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, UserConstant.ADDRESS_DEFAULT)
                .update();
    }

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

        if (addressCount >= UserConstant.MAX_ADDRESS_COUNT) {
            throw new BusinessException("最多只能有10个收货地址");
        }

        // 2. 如果设置为默认地址，要先取消其他默认地址
        if (Objects.equals(dto.getIsDefault(), UserConstant.ADDRESS_DEFAULT)) {
            clearOtherDefaultAddresses(userId);
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
                .isDefault(dto.getIsDefault() != null ? dto.getIsDefault() : UserConstant.ADDRESS_NOT_DEFAULT)
                .build();

        // 4. 保存到数据库
        save(address);

        // 5. 返回VO
        return AddressVO.fromEntity(address);
    }

    /**
     * 更新收货地址
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddressVO updateAddress(Long userId, AddressUpdateDTO dto) {
        // 1. 查询地址是否存在且属于当前用户
        UserAddress address = lambdaQuery()
                .eq(UserAddress::getId, dto.getId())
                .eq(UserAddress::getUserId, userId)
                .one();

        if (address == null) {
            throw new BusinessException("地址不存在或无权修改");
        }

        // 2. 如果设置为默认地址，要先取消其他默认地址
        if (Objects.equals(dto.getIsDefault(), UserConstant.ADDRESS_DEFAULT)) {
            clearOtherDefaultAddresses(userId);
        }

        // 3. 更新地址信息
        address.setReceiverName(dto.getReceiverName());
        address.setReceiverPhone(dto.getReceiverPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetailAddress(dto.getDetailAddress());
        address.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : UserConstant.ADDRESS_NOT_DEFAULT);

        // 4. 保存更新
        updateById(address);

        // 5. 返回更新后的地址
        return AddressVO.fromEntity(address);
    }

    /**
     * 删除收货地址
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(Long id, Long userId) {
        // 1. 查询地址是否存在且属于当前用户
        UserAddress address = lambdaQuery()
                .eq(UserAddress::getId, id)
                .eq(UserAddress::getUserId, userId)
                .one();

        if (address == null) {
            throw new BusinessException("地址不存在或无权删除");
        }

        // 2. 如果删除的是默认地址，需要取消默认状态
        if (Objects.equals(address.getIsDefault(), UserConstant.ADDRESS_DEFAULT)) {
            // 删除后，将最新的一个地址设置为默认地址（如果还有其他地址）
            lambdaUpdate()
                    .set(UserAddress::getIsDefault, UserConstant.ADDRESS_NOT_DEFAULT)
                    .eq(UserAddress::getId, id)
                    .update();

            // 查询用户的其他地址
            List<UserAddress> otherAddresses = lambdaQuery()
                    .eq(UserAddress::getUserId, userId)
                    .ne(UserAddress::getId, id)
                    .orderByDesc(UserAddress::getCreateTime)
                    .list();

            // 如果还有其他地址，将最新的一个设置为默认
            if (!otherAddresses.isEmpty()) {
                UserAddress newDefault = otherAddresses.get(0);
                newDefault.setIsDefault(UserConstant.ADDRESS_DEFAULT);
                updateById(newDefault);
            }
        }

        // 3. 删除地址
        removeById(id);
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
            throw new BusinessException("地址不存在");
        }

        // 3. 状态检查：如果已经是默认地址，直接返回
        if (Objects.equals(address.getIsDefault(), UserConstant.ADDRESS_DEFAULT)) {
            return AddressVO.fromEntity(address);
        }

        // 4. 取消其他默认地址
        clearOtherDefaultAddresses(userId);

        // 5. 设置当前地址为默认
        address.setIsDefault(UserConstant.ADDRESS_DEFAULT);
        updateById(address);

        // 6. 返回更新后的地址
        return AddressVO.fromEntity(address);
    }

    /**
     * 根据地址ID和用户ID查询地址
     */
    @Override
    public AddressVO getAddressByIdAndUser(Long id, Long userId) {
        UserAddress address = lambdaQuery()
                .eq(UserAddress::getId, id)
                .eq(UserAddress::getUserId, userId)
                .one();

        return AddressVO.fromEntity(address);
    }
}
