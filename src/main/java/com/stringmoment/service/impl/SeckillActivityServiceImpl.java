package com.stringmoment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stringmoment.common.exception.BusinessException;
import com.stringmoment.entity.Product;
import com.stringmoment.entity.SeckillActivity;
import com.stringmoment.mapper.SeckillActivityMapper;
import com.stringmoment.model.response.ProductVO;
import com.stringmoment.model.response.SeckillActivitySimpleVO;
import com.stringmoment.model.response.SeckillActivityVO;
import com.stringmoment.service.ProductService;
import com.stringmoment.service.SeckillActivityService;
import com.stringmoment.service.SeckillOrderService;
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
    
    @Autowired
    private SeckillOrderService seckillOrderService;

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
            wrapper.le(SeckillActivity::getStatus, 1);
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

    /**
     * 检查用户秒杀资格
     */
    @Override
    public Integer checkSeckillQualification(Long activityId, Long userId) {
        // 1. 查询秒杀活动
        SeckillActivity activity = getById(activityId);
        if (activity == null) {
            throw new BusinessException("秒杀活动不存在");
        }

        // 2. 检查活动状态
        if (activity.getStatus() == 0) {
            return 3; // 活动未开始
        } else if (activity.getStatus() == 2) {
            return 4; // 活动已结束
        }

        // 3. 检查库存
        if (activity.getAvailableStock() <= 0) {
            return 1; // 库存不足
        }

        // 4. 检查是否重复秒杀
        boolean hasParticipated = seckillOrderService.existsByActivityIdAndUserId(activityId, userId);
        if (hasParticipated) {
            return 2; // 重复秒杀
        }

        // 5. 所有检查通过，返回可秒杀状态
        return 0; // 可秒杀
    }
}
