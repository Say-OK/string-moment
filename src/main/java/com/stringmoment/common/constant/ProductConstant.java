package com.stringmoment.common.constant;

/**
 * 商品相关常量
 */
public final class ProductConstant {
    private ProductConstant() {}

    // 商品状态
    public static final Integer PRODUCT_STATUS_OFF = 0;     // 下架
    public static final Integer PRODUCT_STATUS_ON = 1;      // 上架

    // Redis缓存（区分用户和管理员）
    public static final String PRODUCT_CATEGORY_CACHE_KEY = "product:categories";           // 用户分类缓存（上架商品）
    public static final String PRODUCT_ALL_CATEGORY_CACHE_KEY = "product:all:categories";   // 管理员分类缓存（所有商品）
    public static final Long PRODUCT_CATEGORY_CACHE_TTL = 3600L;                            // 分类缓存过期时间（1小时）

    // 商品详情缓存
    public static final String PRODUCT_DETAIL_CACHE_KEY_PREFIX = "product:detail:";         // 商品详情缓存键前缀
    public static final Long PRODUCT_DETAIL_CACHE_TTL = 1800L;                              // 商品详情缓存过期时间（30分钟）

    // 商品排序字段（数据库字段名）
    public static final String SORT_FIELD_PRICE = "price";
    public static final String SORT_FIELD_SALE_COUNT = "sale_count";
    public static final String SORT_FIELD_CREATE_TIME = "create_time";

    // 排序方式
    public static final String SORT_ORDER_ASC = "asc";
    public static final String SORT_ORDER_DESC = "desc";
}