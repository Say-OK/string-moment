package com.stringmoment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stringmoment.common.constant.SeckillConstant;
import com.stringmoment.common.exception.BusinessException;
import com.stringmoment.entity.Product;
import com.stringmoment.entity.SeckillActivity;
import com.stringmoment.mapper.SeckillActivityMapper;
import com.stringmoment.model.response.ProductVO;
import com.stringmoment.model.response.SeckillActivitySimpleVO;
import com.stringmoment.model.response.SeckillActivityVO;
import com.stringmoment.service.ProductService;
import com.stringmoment.service.SeckillActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SeckillActivityServiceImpl extends ServiceImpl<SeckillActivityMapper, SeckillActivity> implements SeckillActivityService {

    @Autowired
    private ProductService productService;


    /**
     * 获取秒杀活动列表
     */
    @Override
    public List<SeckillActivitySimpleVO> getSeckillActivityList(Integer status) {
        // 1. 创建查询对象
        LambdaQueryWrapper<SeckillActivity> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(SeckillActivity::getStatus, status);
        } else {
            wrapper.in(SeckillActivity::getStatus,
                    SeckillConstant.SECKILL_ACTIVITY_NOT_STARTED,
                    SeckillConstant.SECKILL_ACTIVITY_ON_GOING
            );
        }

        wrapper.orderByAsc(SeckillActivity::getStartTime);

        // 2. 查询活动 + 非空校验
        List<SeckillActivity> activityList = list(wrapper);
        if (CollectionUtils.isEmpty(activityList)) {
            return Collections.emptyList();
        }

        // 3. 查询对应商品
        List<Long> productIds = activityList.stream()
                .map(SeckillActivity::getProductId)
                .distinct()
                .toList();

        Map<Long, Product> productMap = productService.listByIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 4. 转换为VO
        return activityList.stream()
                .map(activity -> {
                    Product product = productMap.get(activity.getProductId());
                    String productName = product != null ? product.getName() : "商品已下架";
                    String productImage = product != null ? product.getImageUrl() : "";
                    BigDecimal originalPrice = product != null ? product.getPrice() : BigDecimal.ZERO;

                    return SeckillActivitySimpleVO.fromEntity(activity, productName, productImage, originalPrice);
                })
                .toList();
    }

    /**
     * 获取秒杀活动详情
     */
    @Override
    public SeckillActivityVO getSeckillActivityDetail(Long id) {
        // 1. 查询秒杀活动
        SeckillActivity activity = getById(id);
        if (activity == null) {
            throw new BusinessException("秒杀活动不存在");
        }

        // 2. 查询秒杀商品
        ProductVO productVO = productService.getProductDetail(activity.getProductId());

        return SeckillActivityVO.fromEntity(activity, productVO);
    }
}
