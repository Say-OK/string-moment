package com.stringmoment.common.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 订单号生成工具类
 */
@Component
public class OrderNoGenerator {
    
    /**
     * 生成订单号
     */
    public String generateOrderNo(Long userId) {
        // 1. 时间戳
        String timestamp = DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss");
        // 2. 随机数
        String random = RandomUtil.randomNumbers(6);
        // 3. 用户标识
        String userIdStr = String.format("%04d", userId % 10000);
        return "O" + timestamp + userIdStr + random;
    }

    /**
     * 生成秒杀订单号
     */
    public String generateSeckillOrderNo(Long userId) {
        String timestamp = DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss");
        String random = RandomUtil.randomNumbers(6);
        String userIdStr = String.format("%04d", userId % 10000);
        return "S" + timestamp + userIdStr + random;
    }
}