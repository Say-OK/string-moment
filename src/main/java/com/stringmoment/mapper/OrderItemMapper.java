package com.stringmoment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stringmoment.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单商品明细Mapper接口
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

}
