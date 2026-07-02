package com.stringmoment.controller;

import com.stringmoment.common.result.Result;
import com.stringmoment.model.request.AdminProductListQueryDTO;
import com.stringmoment.model.request.ProductAddDTO;
import com.stringmoment.model.request.ProductUpdateDTO;
import com.stringmoment.model.response.ProductPageVO;
import com.stringmoment.model.response.ProductVO;
import com.stringmoment.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员后台Controller
 * 统一管理所有管理员操作：商品管理、秒杀活动管理、订单管理等
 */
@RestController
@RequestMapping("/admin")
@Validated
public class AdminController {

    @Autowired
    private ProductService productService;

    // ==================== 商品查询（管理员端） ====================

    /**
     * 获取商品列表（管理员：查询所有商品，可按状态筛选）
     */
    @GetMapping("/product/list")
    public Result<ProductPageVO> getAllProductList(@Valid AdminProductListQueryDTO dto) {
        ProductPageVO productPageVO = productService.getAllProductList(dto);
        return Result.success(productPageVO);
    }

    /**
     * 获取商品详情（管理员：可以查看下架商品）
     */
    @GetMapping("/product/detail/{id}")
    public Result<ProductVO> getProductDetailAdmin(@PathVariable Long id) {
        ProductVO productVO = productService.getProductDetailAdmin(id);
        return Result.success(productVO);
    }

    /**
     * 获取商品分类列表（管理员：返回所有商品的分类）
     */
    @GetMapping("/product/categories")
    public Result<List<String>> getAllCategoryList() {
        List<String> categories = productService.getAllCategoryList();
        return Result.success(categories);
    }

    // ==================== 商品管理 ====================

    /**
     * 添加商品
     */
    @PostMapping("/product/add")
    public Result<ProductVO> addProduct(@Valid @RequestBody ProductAddDTO dto) {
        ProductVO productVO = productService.addProduct(dto);
        return Result.success("商品添加成功", productVO);
    }

    /**
     * 更新商品信息
     */
    @PutMapping("/product/update/{id}")
    public Result<ProductVO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductUpdateDTO dto) {
        ProductVO productVO = productService.updateProduct(id, dto);
        return Result.success("商品信息更新成功", productVO);
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/product/delete/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success("商品删除成功");
    }

    /**
     * 商品上架
     */
    @PutMapping("/product/on/{id}")
    public Result<Void> onProduct(@PathVariable Long id) {
        productService.onProduct(id);
        return Result.success("商品上架成功");
    }

    /**
     * 商品下架
     */
    @PutMapping("/product/off/{id}")
    public Result<Void> offProduct(@PathVariable Long id) {
        productService.offProduct(id);
        return Result.success("商品下架成功");
    }
}