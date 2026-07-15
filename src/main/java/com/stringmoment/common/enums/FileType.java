package com.stringmoment.common.enums;

import lombok.Getter;

/**
 * 文件类型枚举
 * 定义不同类型文件的存储配置
 */
@Getter
public enum FileType {
    /**
     * 头像文件
     */
    AVATAR("avatars", "/api/uploads/avatars", 10 * 1024 * 1024),

    /**
     * 商品图片
     */
    PRODUCT("products", "/api/uploads/products", 10 * 1024 * 1024),

    /**
     * 秒杀活动图片
     */
    SECKILL("seckill", "/api/uploads/seckill", 10 * 1024 * 1024);

    /**
     * 文件夹名称（相对于上传根目录）
     */
    private final String folderName;

    /**
     * URL访问前缀
     */
    private final String urlPrefix;

    /**
     * 最大文件大小（字节）
     */
    private final long maxSize;

    FileType(String folderName, String urlPrefix, long maxSize) {
        this.folderName = folderName;
        this.urlPrefix = urlPrefix;
        this.maxSize = maxSize;
    }
}