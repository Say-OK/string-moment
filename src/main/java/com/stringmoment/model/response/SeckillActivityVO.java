package com.stringmoment.model.response;

import com.stringmoment.entity.SeckillActivity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * 秒杀活动响应对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillActivityVO {

    private Long id;
    private String name;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal originalPrice;
    private BigDecimal seckillPrice;
    private Integer totalStock;
    private Integer availableStock;
    private String startTime;
    private String endTime;
    private Integer status;  // 0-未开始，1-进行中，2-已结束
    private String createTime;

    /**
     * 从SeckillActivity实体转换为VO（使用快照信息）
     */
    public static SeckillActivityVO fromEntityWithSnapshot(SeckillActivity activity) {

        if (activity == null) {
            return null;
        }

        SeckillActivityVO vo = new SeckillActivityVO();
        vo.setId(activity.getId());
        vo.setName(activity.getName());
        vo.setProductId(activity.getProductId());

        // 使用快照字段
        vo.setProductName(activity.getSeckillProductName());
        vo.setProductImage(activity.getSeckillProductImage());
        vo.setOriginalPrice(activity.getSeckillProductPrice());

        vo.setSeckillPrice(activity.getSeckillPrice());
        vo.setTotalStock(activity.getTotalStock());
        vo.setAvailableStock(activity.getAvailableStock());
        vo.setStatus(activity.getStatus());

        if (activity.getStartTime() != null) {
            vo.setStartTime(activity.getStartTime().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ));
        }

        if (activity.getEndTime() != null) {
            vo.setEndTime(activity.getEndTime().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ));
        }

        if (activity.getCreateTime() != null) {
            vo.setCreateTime(activity.getCreateTime().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ));
        }

        return vo;
    }
}