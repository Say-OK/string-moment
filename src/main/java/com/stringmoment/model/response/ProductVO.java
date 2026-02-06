package com.stringmoment.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVO {
    
    private Long id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private Integer saleCount;
    private Integer status;
    private String createTime;  // 创建时间（字符串格式）
    
    /**
     * 从Product实体转换为ProductVO
     */
    public static ProductVO fromEntity(com.stringmoment.entity.Product product) {
        if (product == null) {
            return null;
        }
        
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setName(product.getName());
        vo.setDescription(product.getDescription());
        vo.setCategory(product.getCategory());
        vo.setPrice(product.getPrice());
        vo.setStock(product.getStock());
        vo.setImageUrl(product.getImageUrl());
        vo.setSaleCount(product.getSaleCount());
        vo.setStatus(product.getStatus());
        
        // 格式化时间
        if (product.getCreateTime() != null) {
            vo.setCreateTime(product.getCreateTime().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ));
        }
        
        return vo;
    }
}