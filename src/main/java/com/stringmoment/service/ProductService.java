package com.stringmoment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stringmoment.entity.Product;
import com.stringmoment.model.request.ProductListQueryDTO;
import com.stringmoment.model.response.ProductPageVO;
import com.stringmoment.model.response.ProductVO;
import jakarta.validation.Valid;

/**
 * 商品服务接口
 */
public interface ProductService extends IService<Product> {

    /**
     * 获取商品列表
     */
    ProductPageVO getProductList(@Valid ProductListQueryDTO dto);

    /**
     * 获取商品详情
     */
    ProductVO getProductDetail(Long id);
}