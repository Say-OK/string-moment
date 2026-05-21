package com.stringmoment.common.constant;

public final class ProductConstant {
    private ProductConstant() {}

    public static final Integer PRODUCT_STATUS_OFF = 0;     // 下架
    public static final Integer PRODUCT_STATUS_ON = 1;      // 上架

    public static final String PRODUCT_CATEGORY_CACHE_KEY = "product:categories";
    public static final Long PRODUCT_CATEGORY_CACHE_TTL = 3600L;
}