package com.stringmoment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀活动实体类
 * 对应数据库表：seckill_activity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("seckill_activity")
public class SeckillActivity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "name")
    private String name;
    
    /**
     * 关联的商品ID
     */
    @TableField(value = "product_id")
    private Long productId;
    
    /**
     * 秒杀价格
     */
    @TableField(value = "seckill_price")
    private BigDecimal seckillPrice;
    
    /**
     * 总秒杀库存
     */
    @TableField(value = "total_stock")
    private Integer totalStock;
    
    /**
     * 可用库存（实时更新）
     */
    @TableField(value = "available_stock")
    private Integer availableStock;

    @TableField(value = "start_time")
    private LocalDateTime startTime;

    @TableField(value = "end_time")
    private LocalDateTime endTime;
    
    /**
     * 状态：0-未开始，1-进行中，2-已结束
     */
    @TableField(value = "status")
    private Integer status = 0;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}