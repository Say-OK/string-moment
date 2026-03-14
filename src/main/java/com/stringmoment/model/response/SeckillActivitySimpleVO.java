package com.stringmoment.model.response;

import com.stringmoment.entity.SeckillActivity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 秒杀活动简略信息（列表展示）
 */
@Data
public class SeckillActivitySimpleVO {
    
    private Long id;
    private String name;
    private String productName;
    private String productImage;
    private BigDecimal originalPrice;
    private BigDecimal seckillPrice;
    private Integer stockStatus;  // 0-已售罄，1-有库存
    private Integer stockPercent; // 库存百分比
    private String startTime;
    private String endTime;
    private Integer status;       // 0-未开始，1-进行中，2-已结束
    private Long timeLeft;        // 剩余时间（秒）
    
    /**
     * 从SeckillActivity实体转换为简略VO
     */
    public static SeckillActivitySimpleVO fromEntity(
            SeckillActivity activity,
            String productName,
            String productImage,
            BigDecimal originalPrice) {
        
        if (activity == null) {
            return null;
        }
        
        SeckillActivitySimpleVO vo = new SeckillActivitySimpleVO();
        vo.setId(activity.getId());
        vo.setName(activity.getName());
        vo.setProductName(productName);
        vo.setProductImage(productImage);
        vo.setOriginalPrice(originalPrice);
        vo.setSeckillPrice(activity.getSeckillPrice());
        vo.setStatus(activity.getStatus());
        
        // 计算库存状态和百分比
        if (activity.getAvailableStock() <= 0) {
            vo.setStockStatus(0);  // 已售罄
            vo.setStockPercent(0);
        } else {
            vo.setStockStatus(1);  // 有库存
            if (activity.getTotalStock() > 0) {
                int percent = (int) ((activity.getAvailableStock() * 100.0) / activity.getTotalStock());
                vo.setStockPercent(percent);
            } else {
                vo.setStockPercent(0);
            }
        }
        
        // 格式化时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (activity.getStartTime() != null) {
            vo.setStartTime(activity.getStartTime().format(formatter));
        }
        if (activity.getEndTime() != null) {
            vo.setEndTime(activity.getEndTime().format(formatter));
        }
        
        // 计算剩余时间
        if (activity.getStatus() == 1 && activity.getEndTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(now, activity.getEndTime());
            if (duration.toSeconds() > 0) {
                vo.setTimeLeft(duration.toSeconds());
            } else {
                vo.setTimeLeft(0L);
            }
        }
        
        return vo;
    }
}