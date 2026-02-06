package com.stringmoment.model.response;

import lombok.Data;
import java.util.List;

@Data
public class ProductPageVO {
    
    private List<ProductVO> list;  // 商品列表
    private Long total;            // 总记录数
    private Integer page;          // 当前页码
    private Integer size;          // 每页数量
    private Integer pages;         // 总页数
}