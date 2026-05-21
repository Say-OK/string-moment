package com.stringmoment.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stringmoment.common.constant.ProductConstant;
import com.stringmoment.common.exception.BusinessException;
import com.stringmoment.entity.Product;
import com.stringmoment.mapper.ProductMapper;
import com.stringmoment.model.request.ProductListQueryDTO;
import com.stringmoment.model.response.ProductPageVO;
import com.stringmoment.model.response.ProductVO;
import com.stringmoment.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 获取商品列表
     */
    @Override
    public ProductPageVO getProductList(ProductListQueryDTO dto) {
       // 1. 创建查询对象
        LambdaQueryChainWrapper<Product> query = lambdaQuery();
        query.eq(Product::getStatus, ProductConstant.PRODUCT_STATUS_ON);

        // 2. 拼接动态条件
        if (StringUtils.hasText(dto.getCategory())) {
            query.eq(Product::getCategory, dto.getCategory());
        }

        if (StringUtils.hasText(dto.getKeyword())) {
            query.and(w -> w.like(Product::getName, dto.getKeyword())
                    .or()
                    .like(Product::getDescription, dto.getKeyword()));
        }

        // 3. 分页查询 + 参数校验
        IPage<Product> page = query.page(new Page<>(
                Math.max(dto.getPage(), 1),
                Math.max(Math.min(dto.getSize(), 100), 1)
        ));

        // 4. 转换为VO
        ProductPageVO result = new ProductPageVO();
        result.setPage(dto.getPage());
        result.setSize(dto.getSize());
        result.setTotal(page.getTotal());
        result.setPages((int) page.getPages());

        // 5. 转换商品列表
        result.setList(
                page.getRecords().stream()
                        .map(ProductVO::fromEntity)
                        .toList()
        );

        return result;
    }

    /**
     * 获取商品详情
     */
    @Override
    public ProductVO getProductDetail(Long id) {
        Product product = lambdaQuery()
                .eq(Product::getId, id)
                .eq(Product::getStatus, ProductConstant.PRODUCT_STATUS_ON)
                .one();

        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        return ProductVO.fromEntity(product);
    }

    /**
     * 获取所有商品分类列表
     */
    @Override
    public List<String> getCategoryList() {
        String cacheKey = ProductConstant.PRODUCT_CATEGORY_CACHE_KEY;
        String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);

        if (StringUtils.hasText(cacheValue)) {
            return JSONUtil.toList(cacheValue, String.class);
        }

        List<String> categories = lambdaQuery()
                .select(Product::getCategory)
                .eq(Product::getStatus, ProductConstant.PRODUCT_STATUS_ON)
                .groupBy(Product::getCategory)
                .list()
                .stream()
                .map(Product::getCategory)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());

        stringRedisTemplate.opsForValue().set(
                cacheKey,
                JSONUtil.toJsonStr(categories),
                ProductConstant.PRODUCT_CATEGORY_CACHE_TTL,
                TimeUnit.SECONDS
        );

        return categories;
    }
}