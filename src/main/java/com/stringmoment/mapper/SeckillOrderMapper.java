package com.stringmoment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stringmoment.entity.SeckillOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀订单Mapper接口
 */
@Mapper
public interface SeckillOrderMapper extends BaseMapper<SeckillOrder> {
}
