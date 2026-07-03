package com.stringmoment.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 秒杀活动分页VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillActivityPageVO {

    private Integer page;           // 当前页码
    private Integer size;           // 每页数量
    private Long total;             // 总记录数
    private Integer pages;          // 总页数

    private List<SeckillActivitySimpleVO> list;  // 秒杀活动列表
}