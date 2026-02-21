package com.stringmoment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stringmoment.common.exception.BusinessException;
import com.stringmoment.entity.SeckillActivity;
import com.stringmoment.mapper.SeckillActivityMapper;
import com.stringmoment.model.response.ProductVO;
import com.stringmoment.model.response.SeckillActivityVO;
import com.stringmoment.service.ProductService;
import com.stringmoment.service.SeckillActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SeckillActivityServiceImpl extends ServiceImpl<SeckillActivityMapper, SeckillActivity> implements SeckillActivityService {

    @Autowired
    private ProductService productService;

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

        // 2. 更新活动状态
        updateActivityStatus(activity);

        // 3. 查询秒杀商品
        ProductVO productVO = productService.getProductDetail(activity.getProductId());

        return SeckillActivityVO.fromEntity(activity, productVO);
    }

    /**
     * 更新活动状态
     */
    private void updateActivityStatus(SeckillActivity activity) {
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(activity.getStartTime())) {
            activity.setStatus(0); // 未开始
        } else if (now.isAfter(activity.getEndTime())) {
            activity.setStatus(2); // 已结束
        } else {
            activity.setStatus(1); // 进行中
        }

        updateById(activity);
    }
}
