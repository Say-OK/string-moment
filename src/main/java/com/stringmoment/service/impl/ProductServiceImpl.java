package com.stringmoment.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.stringmoment.common.cache.TwoLevelCacheUtil;
import com.stringmoment.common.constant.ProductConstant;
import com.stringmoment.common.exception.BusinessException;
import com.stringmoment.entity.Product;
import com.stringmoment.mapper.ProductMapper;
import com.stringmoment.model.request.AdminProductListQueryDTO;
import com.stringmoment.model.request.ProductAddDTO;
import com.stringmoment.model.request.ProductListQueryDTO;
import com.stringmoment.model.request.ProductUpdateDTO;
import com.stringmoment.model.response.ProductPageVO;
import com.stringmoment.model.response.ProductVO;
import com.stringmoment.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private Cache<Long, ProductVO> productDetailCache;

    @Autowired
    private TwoLevelCacheUtil twoLevelCacheUtil;

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

        // 3. 价格区间筛选
        if (dto.getMinPrice() != null) {
            query.ge(Product::getPrice, dto.getMinPrice());
        }
        if (dto.getMaxPrice() != null) {
            query.le(Product::getPrice, dto.getMaxPrice());
        }

        // 4. 排序逻辑
        if (StringUtils.hasText(dto.getSortBy())) {
            boolean isAsc = ProductConstant.SORT_ORDER_ASC.equalsIgnoreCase(dto.getSortOrder());

            switch (dto.getSortBy().toLowerCase()) {
                case ProductConstant.SORT_FIELD_PRICE:
                    query.orderBy(true, isAsc, Product::getPrice);
                    break;
                case ProductConstant.SORT_FIELD_SALE_COUNT:
                    query.orderBy(true, isAsc, Product::getSaleCount);
                    break;
                case ProductConstant.SORT_FIELD_CREATE_TIME:
                    query.orderBy(true, isAsc, Product::getCreateTime);
                    break;
                default:
                    // 默认按创建时间倒序
                    query.orderByDesc(Product::getCreateTime);
                    break;
            }
        } else {
            // 默认按创建时间倒序
            query.orderByDesc(Product::getCreateTime);
        }

        // 5. 分页查询 + 参数校验
        IPage<Product> page = query.page(new Page<>(
                Math.max(dto.getPage(), 1),
                Math.max(Math.min(dto.getSize(), 100), 1)
        ));

        // 6. 转换为VO
        ProductPageVO result = new ProductPageVO();
        result.setPage(dto.getPage());
        result.setSize(dto.getSize());
        result.setTotal(page.getTotal());
        result.setPages((int) page.getPages());

        // 7. 转换商品列表
        result.setList(
                page.getRecords().stream()
                        .map(ProductVO::fromEntity)
                        .toList()
        );

        return result;
    }

    /**
     * 获取商品详情（多级缓存：Caffeine本地缓存 + Redis分布式缓存）
     */
    @Override
    public ProductVO getProductDetail(Long id) {
        String cacheKey = ProductConstant.PRODUCT_DETAIL_CACHE_KEY_PREFIX + id;

        // 使用二级缓存工具类查询（防止穿透、雪崩）
        ProductVO productVO = twoLevelCacheUtil.getWithCache(
                cacheKey,
                productDetailCache,
                id,
                ProductVO.class,
                () -> {
                    Product product = lambdaQuery()
                            .eq(Product::getId, id)
                            .eq(Product::getStatus, ProductConstant.PRODUCT_STATUS_ON)
                            .one();
                    return product == null ? null : ProductVO.fromEntity(product);
                },
                ProductConstant.PRODUCT_DETAIL_CACHE_TTL.intValue(),  // 基础TTL（30分钟）
                ProductConstant.PRODUCT_DETAIL_CACHE_RANDOM_RANGE,
                ProductConstant.PRODUCT_DETAIL_CACHE_EMPTY_TTL
        );

        if (productVO == null) {
            throw new BusinessException("商品不存在");
        }

        return productVO;
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

    /**
     * 添加商品
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO addProduct(ProductAddDTO dto) {
        // 1. 创建商品对象（默认下架状态）
        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .price(dto.getPrice())
                .stock(dto.getStock() != null ? dto.getStock() : 0)
                .imageUrl(dto.getImageUrl())
                .status(ProductConstant.PRODUCT_STATUS_OFF)  // 默认下架，需要手动上架
                .saleCount(0)  // 初始销量为0
                .build();

        // 2. 保存到数据库
        save(product);

        // 3. 返回VO
        return ProductVO.fromEntity(product);
    }

    /**
     * 更新商品信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO updateProduct(Long id, ProductUpdateDTO dto) {
        // 1. 查询商品是否存在
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 2. 上架状态禁止编辑
        if (product.getStatus().equals(ProductConstant.PRODUCT_STATUS_ON)) {
            throw new BusinessException("上架商品无法编辑，请先下架");
        }

        // 3. 记录原分类（用于判断是否需要清除缓存）
        String oldCategory = product.getCategory();
        boolean isCategoryChanged = false;

        // 4. 更新非空字段（下架状态可以编辑所有字段）
        if (StringUtils.hasText(dto.getName())) {
            product.setName(dto.getName());
        }
        if (StringUtils.hasText(dto.getDescription())) {
            product.setDescription(dto.getDescription());
        }
        if (StringUtils.hasText(dto.getCategory())) {
            product.setCategory(dto.getCategory());
            // 判断分类是否变化
            if (!dto.getCategory().equals(oldCategory)) {
                isCategoryChanged = true;
            }
        }
        if (dto.getPrice() != null) {
            product.setPrice(dto.getPrice());
        }
        if (dto.getStock() != null) {
            product.setStock(dto.getStock());
        }
        if (StringUtils.hasText(dto.getImageUrl())) {
            product.setImageUrl(dto.getImageUrl());
        }

        // 5. 保存更新
        updateById(product);

        // 6. 清除商品详情缓存
        clearProductDetailCache(id);

        // 7. 只有分类变化且商品上架时才清除分类缓存
        if (isCategoryChanged && product.getStatus().equals(ProductConstant.PRODUCT_STATUS_ON)) {
            clearCategoryCache();
        }

        // 8. 返回更新后的商品信息
        return ProductVO.fromEntity(product);
    }

    /**
     * 删除商品
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id) {
        // 1. 查询商品是否存在
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 2. 上架状态禁止删除
        if (product.getStatus().equals(ProductConstant.PRODUCT_STATUS_ON)) {
            throw new BusinessException("上架商品无法删除，请先下架");
        }

        // 3. 执行软删除（is_deleted=1）
        removeById(id); // MyBatis-Plus会自动执行软删除

        // 4. 清除商品详情缓存
        clearProductDetailCache(id);

        // 5. 清除分类缓存
        clearCategoryCache();
    }

    /**
     * 商品上架
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onProduct(Long id) {
        // 1. 查询商品是否存在
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 2. 检查商品状态
        if (product.getStatus().equals(ProductConstant.PRODUCT_STATUS_ON)) {
            throw new BusinessException("商品已上架，无需重复操作");
        }

        // 3. 检查库存
        if (product.getStock() == null || product.getStock() <= 0) {
            throw new BusinessException("商品库存不足，无法上架");
        }

        // 4. 检查该分类是否已存在于上架商品列表中
        boolean categoryExists = lambdaQuery()
                .eq(Product::getCategory, product.getCategory())
                .eq(Product::getStatus, ProductConstant.PRODUCT_STATUS_ON)
                .exists();

        // 5. 更新状态为上架
        product.setStatus(ProductConstant.PRODUCT_STATUS_ON);
        updateById(product);

        // 6. 清除商品详情缓存
        clearProductDetailCache(id);

        // 7. 只有新分类上架时才清除分类缓存
        if (!categoryExists) {
            clearCategoryCache();
        }
    }

    /**
     * 商品下架
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offProduct(Long id) {
        // 1. 查询商品是否存在
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 2. 检查商品状态
        if (product.getStatus().equals(ProductConstant.PRODUCT_STATUS_OFF)) {
            throw new BusinessException("商品已下架，无需重复操作");
        }

        // 3. 检查该分类下是否还有其他上架商品
        boolean hasOtherProductsInCategory = lambdaQuery()
                .eq(Product::getCategory, product.getCategory())
                .eq(Product::getStatus, ProductConstant.PRODUCT_STATUS_ON)
                .ne(Product::getId, id)
                .exists();

        // 4. 更新状态为下架
        product.setStatus(ProductConstant.PRODUCT_STATUS_OFF);
        updateById(product);

        // 5. 清除商品详情缓存
        clearProductDetailCache(id);

        // 6. 只有分类下最后一个商品下架时才清除分类缓存
        if (!hasOtherProductsInCategory) {
            clearCategoryCache();
        }
    }

    // ==================== 管理员端查询功能 ====================

    /**
     * 获取商品列表（管理员端：查询所有商品，可按状态筛选）
     */
    @Override
    public ProductPageVO getAllProductList(AdminProductListQueryDTO dto) {
        // 1. 创建查询对象
        LambdaQueryChainWrapper<Product> query = lambdaQuery();

        // 2. 状态筛选（管理员可以选择查询所有、上架、下架）
        if (dto.getStatus() != null) {
            query.eq(Product::getStatus, dto.getStatus());
        }

        // 3. 拼接动态条件
        if (StringUtils.hasText(dto.getCategory())) {
            query.eq(Product::getCategory, dto.getCategory());
        }

        if (StringUtils.hasText(dto.getKeyword())) {
            query.and(w -> w.like(Product::getName, dto.getKeyword())
                    .or()
                    .like(Product::getDescription, dto.getKeyword()));
        }

        // 4. 价格区间筛选
        if (dto.getMinPrice() != null) {
            query.ge(Product::getPrice, dto.getMinPrice());
        }
        if (dto.getMaxPrice() != null) {
            query.le(Product::getPrice, dto.getMaxPrice());
        }

        // 5. 排序逻辑
        if (StringUtils.hasText(dto.getSortBy())) {
            boolean isAsc = ProductConstant.SORT_ORDER_ASC.equalsIgnoreCase(dto.getSortOrder());

            switch (dto.getSortBy().toLowerCase()) {
                case ProductConstant.SORT_FIELD_PRICE:
                    query.orderBy(true, isAsc, Product::getPrice);
                    break;
                case ProductConstant.SORT_FIELD_SALE_COUNT:
                    query.orderBy(true, isAsc, Product::getSaleCount);
                    break;
                case ProductConstant.SORT_FIELD_CREATE_TIME:
                    query.orderBy(true, isAsc, Product::getCreateTime);
                    break;
                default:
                    query.orderByDesc(Product::getCreateTime);
                    break;
            }
        } else {
            query.orderByDesc(Product::getCreateTime);
        }

        // 6. 分页查询
        IPage<Product> page = query.page(new Page<>(
                Math.max(dto.getPage(), 1),
                Math.max(Math.min(dto.getSize(), 100), 1)
        ));

        // 7. 转换为VO
        ProductPageVO result = new ProductPageVO();
        result.setPage(dto.getPage());
        result.setSize(dto.getSize());
        result.setTotal(page.getTotal());
        result.setPages((int) page.getPages());
        result.setList(page.getRecords().stream().map(ProductVO::fromEntity).toList());

        return result;
    }

    /**
     * 获取商品详情（管理员端：查询所有商品，包括下架商品）
     */
    @Override
    public ProductVO getProductDetailAdmin(Long id) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        return ProductVO.fromEntity(product);
    }

    /**
     * 获取商品分类列表（管理员端：返回所有商品的分类）
     */
    @Override
    public List<String> getAllCategoryList() {
        String cacheKey = ProductConstant.PRODUCT_ALL_CATEGORY_CACHE_KEY;
        String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);

        if (StringUtils.hasText(cacheValue)) {
            return JSONUtil.toList(cacheValue, String.class);
        }

        // 查询所有商品的分类（包括下架商品）
        List<String> categories = lambdaQuery()
                .select(Product::getCategory)
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

    // ==================== 缓存清除 ====================

    /**
     * 清除商品详情缓存（延时双删策略）
     */
    private void clearProductDetailCache(Long productId) {
        String cacheKey = ProductConstant.PRODUCT_DETAIL_CACHE_KEY_PREFIX + productId;
        twoLevelCacheUtil.evictWithDelayDelete(
                cacheKey,
                productDetailCache,
                productId,
                ProductConstant.CACHE_DELAY_DELETE_MS
        );
    }

    /**
     * 清除商品分类缓存（用户和管理员）
     */
    private void clearCategoryCache() {
        // 清除用户分类缓存
        stringRedisTemplate.delete(ProductConstant.PRODUCT_CATEGORY_CACHE_KEY);
        // 清除管理员分类缓存
        stringRedisTemplate.delete(ProductConstant.PRODUCT_ALL_CATEGORY_CACHE_KEY);
    }
}