package com.stringmoment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stringmoment.entity.Product;
import com.stringmoment.model.request.AdminProductListQueryDTO;
import com.stringmoment.model.request.ProductAddDTO;
import com.stringmoment.model.request.ProductListQueryDTO;
import com.stringmoment.model.request.ProductUpdateDTO;
import com.stringmoment.model.response.ProductPageVO;
import com.stringmoment.model.response.ProductVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService extends IService<Product> {

    // ==================== 用户端查询功能 ====================

    /**
     * 获取商品列表（用户端：只查询上架商品）
     */
    ProductPageVO getProductList(@Valid ProductListQueryDTO dto);

    /**
     * 获取商品详情（用户端：只查询上架商品）
     */
    ProductVO getProductDetail(Long id);

    /**
     * 获取商品分类列表（用户端：只返回上架商品的分类）
     */
    List<String> getCategoryList();

    // ==================== 管理员端查询功能 ====================

    /**
     * 获取商品列表（管理员端：查询所有商品，可按状态筛选）
     */
    ProductPageVO getAllProductList(@Valid AdminProductListQueryDTO dto);

    /**
     * 获取商品详情（管理员端：查询所有商品，包括下架商品）
     */
    ProductVO getProductDetailAdmin(Long id);

    /**
     * 获取商品分类列表（管理员端：返回所有商品的分类）
     */
    List<String> getAllCategoryList();

    // ==================== 管理员端管理功能 ====================

    /**
     * 添加商品
     */
    ProductVO addProduct(@Valid ProductAddDTO dto);

    /**
     * 更新商品信息
     */
    ProductVO updateProduct(Long id, @Valid ProductUpdateDTO dto);

    /**
     * 删除商品
     */
    void deleteProduct(Long id);

    /**
     * 商品上架
     */
    void onProduct(Long id);

    /**
     * 商品下架
     */
    void offProduct(Long id);
}