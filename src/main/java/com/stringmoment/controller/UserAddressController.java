package com.stringmoment.controller;

import com.stringmoment.common.result.Result;
import com.stringmoment.model.request.AddressAddDTO;
import com.stringmoment.model.request.AddressUpdateDTO;
import com.stringmoment.model.response.AddressVO;
import com.stringmoment.service.UserAddressService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址Controller
 */
@RestController
@RequestMapping("/address")
@Validated
public class UserAddressController {

    @Autowired
    private UserAddressService userAddressService;

    /**
     * 添加收货地址
     */
    @PostMapping("/add")
    public Result<AddressVO> addAddress(@RequestBody @Valid AddressAddDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        AddressVO addressVO = userAddressService.addAddress(userId, dto);
        return Result.success("地址添加成功", addressVO);
    }

    /**
     * 更新收货地址
     */
    @PutMapping("/update")
    public Result<AddressVO> updateAddress(@RequestBody @Valid AddressUpdateDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        AddressVO addressVO = userAddressService.updateAddress(userId, dto);
        return Result.success("地址更新成功", addressVO);
    }

    /**
     * 删除收货地址
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userAddressService.deleteAddress(id, userId);
        return Result.success("地址删除成功");
    }

    /**
     * 获取地址列表
     */
    @GetMapping("/list")
    public Result<List<AddressVO>> getAddressList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<AddressVO> addressList = userAddressService.getAddressList(userId);
        return Result.success(addressList);
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/set-default/{id}")
    public Result<AddressVO> setDefaultAddress(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        AddressVO addressVO = userAddressService.setDefaultAddress(id, userId);
        return Result.success("默认地址设置成功", addressVO);
    }
}
