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
import com.stringmoment.model.response.SeckillActivityPageVO;
import com.stringmoment.model.response.SeckillActivitySimpleVO;
import com.stringmoment.model.response.SeckillActivityVO;
import com.stringmoment.service.ProductService;
import com.stringmoment.service.SeckillActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class SeckillActivityServiceImpl extends ServiceImpl<SeckillActivityMapper, SeckillActivity> implements SeckillActivityService {

    @Autowired
    private ProductService productService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


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

        // 3. 转换为VO（使用快照信息）
        return activityList.stream()
                .map(SeckillActivitySimpleVO::fromEntityWithSnapshot)
                .toList();
    }

    /**
     * 获取秒杀活动详情（用户端：只查询未开始和进行中的活动）
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

        // 3. 返回VO（使用快照信息）
        return SeckillActivityVO.fromEntityWithSnapshot(activity);
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

        // 4. 创建秒杀活动，复制商品快照信息
        SeckillActivity activity = SeckillActivity.builder()
                .name(dto.getName())
                .productId(dto.getProductId())
                .seckillProductName(product.getName())  // 商品名称快照
                .seckillProductImage(product.getImageUrl())  // 商品图片快照
                .seckillProductPrice(product.getPrice())  // 商品原价快照
                .seckillPrice(dto.getSeckillPrice())
                .totalStock(dto.getTotalStock())
                .availableStock(dto.getTotalStock())  // 初始可用库存等于总库存
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .status(SeckillConstant.SECKILL_ACTIVITY_NOT_STARTED)  // 默认未开始
                .build();

        // 5. 保存到数据库
        save(activity);

        // 6. 返回VO（使用快照信息）
        return SeckillActivityVO.fromEntityWithSnapshot(activity);
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

        // 2. 进行中的活动禁止编辑
        if (activity.getStatus().equals(SeckillConstant.SECKILL_ACTIVITY_ON_GOING)) {
            throw new BusinessException("进行中的秒杀活动无法编辑");
        }

        // 3. 校验起止时间合法性（前置校验）
        if (dto.getStartTime() != null && dto.getEndTime() != null) {
            if (dto.getEndTime().isBefore(dto.getStartTime())) {
                throw new BusinessException("结束时间不能早于开始时间");
            }
        }
        // 如果只传了startTime，用数据库endTime校验
        if (dto.getStartTime() != null && dto.getEndTime() == null) {
            if (activity.getEndTime().isBefore(dto.getStartTime())) {
                throw new BusinessException("结束时间不能早于开始时间");
            }
        }
        // 如果只传了endTime，用数据库startTime校验
        if (dto.getEndTime() != null && dto.getStartTime() == null) {
            if (dto.getEndTime().isBefore(activity.getStartTime())) {
                throw new BusinessException("结束时间不能早于开始时间");
            }
        }

        // 4. 如果修改了秒杀价格，需要校验（使用快照原价）
        if (dto.getSeckillPrice() != null) {
            if (dto.getSeckillPrice().compareTo(activity.getSeckillProductPrice()) >= 0) {
                throw new BusinessException("秒杀价格必须低于商品原价");
            }
            activity.setSeckillPrice(dto.getSeckillPrice());
        }

        // 5. 如果修改了总库存，需要同步更新可用库存
        if (dto.getTotalStock() != null) {
            int newTotalStock = dto.getTotalStock();
            int oldTotalStock = activity.getTotalStock();
            int stockDiff = newTotalStock - oldTotalStock;

            // 校验库存边界：防止可用库存变为负数
            if (activity.getAvailableStock() + stockDiff < 0) {
                throw new BusinessException("库存不足，无法减少库存。当前可用库存：" + activity.getAvailableStock() +
                        "，尝试减少：" + Math.abs(stockDiff));
            }

            activity.setTotalStock(newTotalStock);
            activity.setAvailableStock(activity.getAvailableStock() + stockDiff);
            
            // 同步更新Redis缓存（如果缓存已存在）
            syncRedisStockAfterUpdate(activity.getId(), activity.getAvailableStock());
        }

        // 6. 更新时间字段
        if (dto.getStartTime() != null) {
            activity.setStartTime(dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            activity.setEndTime(dto.getEndTime());
        }

        // 7. 更新其他字段
        if (StringUtils.hasText(dto.getName())) {
            activity.setName(dto.getName());
        }

        // 8. 保存更新
        updateById(activity);

        // 9. 返回VO（使用快照信息）
        return SeckillActivityVO.fromEntityWithSnapshot(activity);
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

        // 2. 进行中禁止删除
        if (activity.getStatus().equals(SeckillConstant.SECKILL_ACTIVITY_ON_GOING)) {
            throw new BusinessException("进行中的秒杀活动无法删除");
        }

        // 3. 执行软删除（is_deleted=1）
        removeById(id); // MyBatis-Plus会自动执行软删除
    }

    /**
     * 停止秒杀活动
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stopActivity(Long id) {
        // 1. 查询秒杀活动是否存在
        SeckillActivity activity = getById(id);
        if (activity == null) {
            throw new BusinessException("秒杀活动不存在");
        }

        // 2. 只能停止进行中的活动
        if (!activity.getStatus().equals(SeckillConstant.SECKILL_ACTIVITY_ON_GOING)) {
            throw new BusinessException("只能停止进行中的秒杀活动");
        }

        // 3. 将状态改为已结束
        activity.setStatus(SeckillConstant.SECKILL_ACTIVITY_ENDED);
        activity.setEndTime(LocalDateTime.now()); // 更新结束时间为当前时间
        updateById(activity);
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

        // 7. 获取活动列表
        List<SeckillActivity> activityList = page.getRecords();
        if (CollectionUtils.isEmpty(activityList)) {
            result.setList(Collections.emptyList());
            return result;
        }

        // 8. 转换秒杀活动列表（使用快照信息）
        result.setList(
                activityList.stream()
                        .map(SeckillActivitySimpleVO::fromEntityWithSnapshot)
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

        // 2. 返回VO（使用快照信息）
        return SeckillActivityVO.fromEntityWithSnapshot(activity);
    }

    /**
     * 编辑活动后同步更新Redis库存缓存
     * 场景：管理员修改未开始活动的库存，Redis缓存可能已存在（预热），需要同步更新
     */
    private void syncRedisStockAfterUpdate(Long activityId, Integer newStock) {
        String stockKey = SeckillConstant.SECKILL_STOCK_KEY_PREFIX + activityId;
        Boolean exists = stringRedisTemplate.hasKey(stockKey);
        if (Boolean.TRUE.equals(exists)) {
            stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(newStock));
            log.info("编辑活动后同步Redis缓存：活动ID={}, 新库存={}", activityId, newStock);
        }
    }
}
