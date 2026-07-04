package com.stringmoment.controller;

import com.stringmoment.common.result.Result;
import com.stringmoment.model.request.*;
import com.stringmoment.model.response.*;
import com.stringmoment.service.OrderService;
import com.stringmoment.service.ProductService;
import com.stringmoment.service.SeckillActivityService;
import com.stringmoment.service.UserService;
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

    @Autowired
    private SeckillActivityService seckillActivityService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    // ==================== 管理员登录 ====================

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<LoginResultVO> adminLogin(@Valid @RequestBody UserLoginDTO dto) {
        LoginResultVO result = userService.adminLogin(dto);
        return Result.success("管理员登录成功", result);
    }

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

    // ==================== 秒杀活动查询（管理员端） ====================

    /**
     * 获取秒杀活动列表（管理员：查询所有活动，可按状态筛选）
     */
    @GetMapping("/seckill/list")
    public Result<SeckillActivityPageVO> getAllSeckillActivityList(@Valid AdminSeckillActivityListQueryDTO dto) {
        SeckillActivityPageVO pageVO = seckillActivityService.getAllSeckillActivityList(dto);
        return Result.success(pageVO);
    }

    /**
     * 获取秒杀活动详情（管理员：可以查看所有状态的活动）
     */
    @GetMapping("/seckill/detail/{id}")
    public Result<SeckillActivityVO> getSeckillActivityDetailAdmin(@PathVariable Long id) {
        SeckillActivityVO activityVO = seckillActivityService.getSeckillActivityDetailAdmin(id);
        return Result.success(activityVO);
    }

    // ==================== 秒杀活动管理 ====================

    /**
     * 添加秒杀活动
     */
    @PostMapping("/seckill/add")
    public Result<SeckillActivityVO> addSeckillActivity(@Valid @RequestBody SeckillActivityAddDTO dto) {
        SeckillActivityVO activityVO = seckillActivityService.addSeckillActivity(dto);
        return Result.success("秒杀活动创建成功", activityVO);
    }

    /**
     * 更新秒杀活动信息
     */
    @PutMapping("/seckill/update/{id}")
    public Result<SeckillActivityVO> updateSeckillActivity(@PathVariable Long id, @Valid @RequestBody SeckillActivityUpdateDTO dto) {
        SeckillActivityVO activityVO = seckillActivityService.updateSeckillActivity(id, dto);
        return Result.success("秒杀活动更新成功", activityVO);
    }

    /**
     * 删除秒杀活动
     */
    @DeleteMapping("/seckill/delete/{id}")
    public Result<Void> deleteSeckillActivity(@PathVariable Long id) {
        seckillActivityService.deleteSeckillActivity(id);
        return Result.success("秒杀活动删除成功");
    }

    // ==================== 订单管理（管理员端） ====================

    /**
     * 获取后台订单列表（多条件筛选）
     */
    @GetMapping("/order/list")
    public Result<OrderPageVO> getAdminOrderList(@Valid AdminOrderListQueryDTO dto) {
        OrderPageVO orderPageVO = orderService.getAdminOrderList(dto);
        return Result.success(orderPageVO);
    }

    /**
     * 获取订单详情（管理员）
     */
    @GetMapping("/order/detail/{id}")
    public Result<OrderVO> getOrderDetailAdmin(@PathVariable Long id) {
        OrderVO orderVO = orderService.getOrderDetailAdmin(id);
        return Result.success(orderVO);
    }

    /**
     * 订单发货（管理员独有）
     */
    @PutMapping("/order/ship/{id}")
    public Result<Void> shipOrder(@PathVariable Long id) {
        orderService.shipOrder(id);
        return Result.success("订单发货成功");
    }
}