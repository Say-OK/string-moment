-- ============================================
-- 数据库：string_moment
-- 描述：弦上一刻 - 乐器垂直电商秒杀系统
-- 版本：1.0
-- 创建时间：2026-01-25
-- ============================================

-- 1. 创建数据库
DROP DATABASE IF EXISTS string_moment;
CREATE DATABASE string_moment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE string_moment;

-- 2. 用户表
CREATE TABLE `user` (
                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID，自增主键',
                        `username` varchar(50) NOT NULL COMMENT '用户名，唯一登录标识',
                        `password` varchar(255) NOT NULL COMMENT '加密后的密码',
                        `nickname` varchar(50) DEFAULT NULL COMMENT '用户昵称',
                        `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
                        `avatar` varchar(500) DEFAULT '/default-avatar.jpg' COMMENT '头像URL',
                        `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
                        `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                        `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_username` (`username`),
                        UNIQUE KEY `uk_phone` (`phone`),
                        KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 3. 商品表
CREATE TABLE `product` (
                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID，自增主键',
                           `name` varchar(200) NOT NULL COMMENT '商品名称',
                           `description` text COMMENT '商品描述',
                           `category` varchar(50) DEFAULT NULL COMMENT '商品分类：guitar-吉他, piano-钢琴',
                           `price` decimal(10,2) NOT NULL COMMENT '原价',
                           `stock` int NOT NULL DEFAULT 0 COMMENT '总库存',
                           `image_url` varchar(500) DEFAULT NULL COMMENT '商品主图URL',
                           `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-下架，1-上架',
                           `sale_count` int NOT NULL DEFAULT 0 COMMENT '销量统计',
                           `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`id`),
                           KEY `idx_category` (`category`),
                           KEY `idx_status` (`status`),
                           KEY `idx_sale_count` (`sale_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 4. 秒杀活动表
CREATE TABLE `seckill_activity` (
                                    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动ID',
                                    `name` varchar(200) NOT NULL COMMENT '活动名称',
                                    `product_id` bigint NOT NULL COMMENT '关联的商品ID',
                                    `seckill_price` decimal(10,2) NOT NULL COMMENT '秒杀价格',
                                    `total_stock` int NOT NULL COMMENT '总秒杀库存',
                                    `available_stock` int NOT NULL COMMENT '可用库存（实时更新）',
                                    `start_time` datetime NOT NULL COMMENT '开始时间',
                                    `end_time` datetime NOT NULL COMMENT '结束时间',
                                    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-未开始，1-进行中，2-已结束',
                                    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    PRIMARY KEY (`id`),
                                    KEY `idx_product_id` (`product_id`),
                                    KEY `idx_time_range` (`start_time`, `end_time`),
                                    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀活动表';

-- 5. 收货地址表
CREATE TABLE `user_address` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '地址ID',
                                `user_id` bigint NOT NULL COMMENT '用户ID',
                                `receiver_name` varchar(50) NOT NULL COMMENT '收货人姓名',
                                `receiver_phone` varchar(20) NOT NULL COMMENT '收货人手机号',
                                `province` varchar(50) NOT NULL COMMENT '省份',
                                `city` varchar(50) NOT NULL COMMENT '城市',
                                `district` varchar(50) NOT NULL COMMENT '区/县',
                                `detail_address` varchar(200) NOT NULL COMMENT '详细地址',
                                `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认地址：0-否，1-是',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                PRIMARY KEY (`id`),
                                KEY `idx_user_id` (`user_id`),
                                KEY `idx_user_default` (`user_id`, `is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收货地址表';

-- 6. 订单表
CREATE TABLE `orders` (
                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
                          `order_no` varchar(32) NOT NULL COMMENT '订单号，业务唯一',
                          `user_id` bigint NOT NULL COMMENT '用户ID',
                          `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
                          `pay_amount` decimal(10,2) NOT NULL DEFAULT 0 COMMENT '实付金额',
                          `order_type` tinyint NOT NULL DEFAULT 1 COMMENT '订单类型：1-普通订单，2-秒杀订单',
                          `seckill_activity_id` bigint DEFAULT NULL COMMENT '秒杀活动ID，仅秒杀订单有值',
                          `address_id` bigint NOT NULL COMMENT '收货地址ID',
                          `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-待支付，1-已支付，2-已发货，3-已完成，4-已取消',
                          `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
                          `delivery_time` datetime DEFAULT NULL COMMENT '发货时间',
                          `receive_time` datetime DEFAULT NULL COMMENT '确认收货时间',
                          `close_time` datetime DEFAULT NULL COMMENT '订单关闭时间',
                          `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `uk_order_no` (`order_no`),
                          KEY `idx_user_id` (`user_id`),
                          KEY `idx_create_time` (`create_time`),
                          KEY `idx_status` (`status`),
                          KEY `idx_seckill_activity` (`seckill_activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 7. 订单商品表（一对多设计）
CREATE TABLE `order_item` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                              `order_id` bigint NOT NULL COMMENT '订单ID',
                              `product_id` bigint NOT NULL COMMENT '商品ID',
                              `product_name` varchar(200) NOT NULL COMMENT '商品名称（快照）',
                              `product_image` varchar(500) DEFAULT NULL COMMENT '商品图片（快照）',
                              `unit_price` decimal(10,2) NOT NULL COMMENT '商品单价（快照）',
                              `quantity` int NOT NULL COMMENT '购买数量',
                              `total_price` decimal(10,2) NOT NULL COMMENT '商品总价 = 单价 * 数量',
                              `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              PRIMARY KEY (`id`),
                              KEY `idx_order_id` (`order_id`),
                              KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品明细表';

-- 8. 秒杀订单表
CREATE TABLE `seckill_order` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `user_id` bigint NOT NULL COMMENT '用户ID',
                                 `order_id` bigint NOT NULL COMMENT '订单ID',
                                 `seckill_activity_id` bigint NOT NULL COMMENT '秒杀活动ID',
                                 `seckill_price` decimal(10,2) NOT NULL COMMENT '秒杀价格（快照）',
                                 `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-秒杀成功未支付，1-已支付',
                                 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_user_activity` (`user_id`, `seckill_activity_id`) COMMENT '防止同一用户重复秒杀',
                                 UNIQUE KEY `uk_order_id` (`order_id`),
                                 KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀订单表';