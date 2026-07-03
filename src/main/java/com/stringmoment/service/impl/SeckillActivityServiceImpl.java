package com.stringmoment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stringmoment.common.constant.ProductConstant;
import com.stringmoment.common.constant.SeckillConstant;
import com.stringmoment.common.exception.BusinessException;
import com.stringmoment.entity.Product;
import com.stringmoment.entity.SeckillActivity;
import com.stringmoment.mapper.SeckillActivityMapper;
import com.stringmoment.model.request.AdminSeckillActivityListQueryDTO;
import com.stringmoment.model.request.SeckillActivityAddDTO;
import com.stringmoment.model.request.SeckillActivityUpdateDTO;
import com.stringmoment.model.response.ProductVO;
import com.stringmoment.model.response.SeckillActivityPageVO;
import com.stringmoment.model.response.SeckillActivitySimpleVO;
import com.stringmoment.model.response.SeckillActivityVO;
import com.stringmoment.service.ProductService;
import com.stringmoment.service.SeckillActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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

        // 3. 查询对应商品（只查询上架商品）
        List<Long> productIds = activityList.stream()
                .map(SeckillActivity::getProductId)
                .distinct()
                .toList();

        // 只查询上架商品
        Map<Long, Product> productMap = productService.lambdaQuery()
                .in(Product::getId, productIds)
                .eq(Product::getStatus, ProductConstant.PRODUCT_STATUS_ON)
                .list()
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 4. 转换为VO（过滤掉商品已下架的活动）
        return activityList.stream()
                .filter(activity -> productMap.containsKey(activity.getProductId()))  // 过滤掉下架商品的活动
                .map(activity -> {
                    Product product = productMap.get(activity.getProductId());
                    String productName = product.getName();
                    String productImage = product.getImageUrl();
                    BigDecimal originalPrice = product.getPrice();

                    return SeckillActivitySimpleVO.fromEntity(activity, productName, productImage, originalPrice);
                })
                .toList();
    }

    /**
     * 获取秒杀活动详情（用户端：只查询未开始和进行中的活动，且商品必须上架）
     */
    @Override
    public SeckillActivityVO getSeckillActivityDetail(Long id) {
        // 1. 查询秒杀活动
        SeckillActivity activity = getById(id);
        if (activity == null) {
            throw new BusinessException("秒杀活动不存在");
        }

        // 2. 校验活动状态（用户只能查看未开始和进行中的活动）
        if (!activity.getStatus().equals(SeckillConstant.SECKILL_ACTIVITY_NOT_STARTED) &&
            !activity.getStatus().equals(SeckillConstant.SECKILL_ACTIVITY_ON_GOING)) {
            throw new BusinessException("秒杀活动已结束");
        }

        // 3. 查询秒杀商品（用户只能查看上架商品）
        ProductVO productVO = productService.getProductDetail(activity.getProductId());

        return SeckillActivityVO.fromEntity(activity, productVO);
    }

    // ==================== 管理员端管理功能 ====================

    /**
     * 添加秒杀活动
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SeckillActivityVO addSeckillActivity(SeckillActivityAddDTO dto) {
        // 1. 校验商品是否存在且上架
        Product product = productService.getById(dto.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!product.getStatus().equals(ProductConstant.PRODUCT_STATUS_ON)) {
            throw new BusinessException("只能为上架商品创建秒杀活动");
        }

        // 2. 校验秒杀价格必须低于原价
        if (dto.getSeckillPrice().compareTo(product.getPrice()) >= 0) {
            throw new BusinessException("秒杀价格必须低于商品原价");
        }

        // 3. 校验时间逻辑
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }

        // 4. 创建秒杀活动（默认状态为未开始）
        SeckillActivity activity = SeckillActivity.builder()
                .name(dto.getName())
                .productId(dto.getProductId())
                .seckillPrice(dto.getSeckillPrice())
                .totalStock(dto.getTotalStock())
                .availableStock(dto.getTotalStock())  // 初始可用库存等于总库存
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .status(SeckillConstant.SECKILL_ACTIVITY_NOT_STARTED)  // 默认未开始
                .build();

        // 5. 保存到数据库
        save(activity);

        // 6. 返回VO
        ProductVO productVO = ProductVO.fromEntity(product);
        return SeckillActivityVO.fromEntity(activity, productVO);
    }

    /**
     * 更新秒杀活动信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SeckillActivityVO updateSeckillActivity(Long id, SeckillActivityUpdateDTO dto) {
        // 1. 查询秒杀活动是否存在
        SeckillActivity activity = getById(id);
        if (activity == null) {
            throw new BusinessException("秒杀活动不存在");
        }

        // 2. 校验活动状态（只有未开始的活动可以修改）
        if (!activity.getStatus().equals(SeckillConstant.SECKILL_ACTIVITY_NOT_STARTED)) {
            throw new BusinessException("只能修改未开始的秒杀活动");
        }

        // 3. 如果修改了秒杀价格，需要校验
        if (dto.getSeckillPrice() != null) {
            Product product = productService.getById(activity.getProductId());
            if (dto.getSeckillPrice().compareTo(product.getPrice()) >= 0) {
                throw new BusinessException("秒杀价格必须低于商品原价");
            }
            activity.setSeckillPrice(dto.getSeckillPrice());
        }

        // 4. 如果修改了总库存，需要同步更新可用库存
        if (dto.getTotalStock() != null) {
            int stockDiff = dto.getTotalStock() - activity.getTotalStock();
            activity.setTotalStock(dto.getTotalStock());
            activity.setAvailableStock(activity.getAvailableStock() + stockDiff);

            // 校验库存不能为负数
            if (activity.getAvailableStock() < 0) {
                throw new BusinessException("可用库存不能为负数，请增加总库存");
            }
        }

        // 5. 更新时间字段
        if (dto.getStartTime() != null) {
            activity.setStartTime(dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            activity.setEndTime(dto.getEndTime());
        }

        // 6. 校验时间逻辑
        if (activity.getEndTime().isBefore(activity.getStartTime())) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }

        // 7. 更新其他字段
        if (StringUtils.hasText(dto.getName())) {
            activity.setName(dto.getName());
        }

        // 9. 保存更新
        updateById(activity);

        // 10. 返回VO
        ProductVO productVO = productService.getProductDetailAdmin(activity.getProductId());
        return SeckillActivityVO.fromEntity(activity, productVO);
    }

    /**
     * 删除秒杀活动
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSeckillActivity(Long id) {
        // 1. 查询秒杀活动是否存在
        SeckillActivity activity = getById(id);
        if (activity == null) {
            throw new BusinessException("秒杀活动不存在");
        }

        // 2. 校验活动状态（只有未开始的活动可以删除）
        if (!activity.getStatus().equals(SeckillConstant.SECKILL_ACTIVITY_NOT_STARTED)) {
            throw new BusinessException("只能删除未开始的秒杀活动");
        }

        // 3. 删除秒杀活动
        removeById(id);
    }

    /**
     * 查询所有秒杀活动（管理员端）
     */
    @Override
    public SeckillActivityPageVO getAllSeckillActivityList(AdminSeckillActivityListQueryDTO dto) {
        // 1. 创建查询对象
        LambdaQueryChainWrapper<SeckillActivity> query = lambdaQuery();

        // 2. 状态筛选（管理员可以选择查询所有、未开始、进行中、已结束）
        if (dto.getStatus() != null) {
            query.eq(SeckillActivity::getStatus, dto.getStatus());
        }

        // 3. 关键词搜索（活动名称）
        if (StringUtils.hasText(dto.getKeyword())) {
            query.like(SeckillActivity::getName, dto.getKeyword());
        }

        // 4. 按开始时间倒序排序
        query.orderByDesc(SeckillActivity::getStartTime);

        // 5. 分页查询 + 参数校验
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SeckillActivity> page = query.page(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                        Math.max(dto.getPage(), 1),
                        Math.max(Math.min(dto.getSize(), 100), 1)
                )
        );

        // 6. 转换为VO
        SeckillActivityPageVO result = new SeckillActivityPageVO();
        result.setPage(dto.getPage());
        result.setSize(dto.getSize());
        result.setTotal(page.getTotal());
        result.setPages((int) page.getPages());

        // 7. 查询对应商品
        List<SeckillActivity> activityList = page.getRecords();
        if (CollectionUtils.isEmpty(activityList)) {
            result.setList(Collections.emptyList());
            return result;
        }

        List<Long> productIds = activityList.stream()
                .map(SeckillActivity::getProductId)
                .distinct()
                .toList();

        Map<Long, Product> productMap = productService.listByIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 8. 转换秒杀活动列表
        result.setList(
                activityList.stream()
                        .map(activity -> {
                            Product product = productMap.get(activity.getProductId());
                            String productName = product != null ? product.getName() : "商品已下架";
                            String productImage = product != null ? product.getImageUrl() : "";
                            BigDecimal originalPrice = product != null ? product.getPrice() : BigDecimal.ZERO;

                            return SeckillActivitySimpleVO.fromEntity(activity, productName, productImage, originalPrice);
                        })
                        .toList()
        );

        return result;
    }

    /**
     * 查询秒杀活动详情（管理员端：可以查看所有状态的活动）
     */
    @Override
    public SeckillActivityVO getSeckillActivityDetailAdmin(Long id) {
        // 1. 查询秒杀活动
        SeckillActivity activity = getById(id);
        if (activity == null) {
            throw new BusinessException("秒杀活动不存在");
        }

        // 2. 查询秒杀商品（管理员可以查看所有商品，包括下架的）
        ProductVO productVO = productService.getProductDetailAdmin(activity.getProductId());

        return SeckillActivityVO.fromEntity(activity, productVO);
    }
}
