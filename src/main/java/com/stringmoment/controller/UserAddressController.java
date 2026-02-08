package com.stringmoment.controller;

import com.stringmoment.common.result.Result;
import com.stringmoment.model.request.AddressAddDTO;
import com.stringmoment.model.response.AddressVO;
import com.stringmoment.service.UserAddressService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
}
