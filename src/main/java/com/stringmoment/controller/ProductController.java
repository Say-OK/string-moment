package com.stringmoment.controller;

import com.stringmoment.common.result.Result;
import com.stringmoment.model.request.ProductListQueryDTO;
import com.stringmoment.model.response.ProductPageVO;
import com.stringmoment.model.response.ProductVO;
import com.stringmoment.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product")
@Validated
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 获取商品列表
     */
    @GetMapping("/list")
    public Result<ProductPageVO> getProductList(@Valid ProductListQueryDTO dto) {
        ProductPageVO productPageVO = productService.getProductList(dto);
        return Result.success(productPageVO);
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/detail/{id}")
    public Result<ProductVO> getProductDetail(@PathVariable Long id) {
        ProductVO productVO = productService.getProductDetail(id);
        return Result.success(productVO);
    }
}
