package com.stringmoment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stringmoment.entity.SeckillActivity;
import com.stringmoment.model.request.AdminSeckillActivityListQueryDTO;
import com.stringmoment.model.request.SeckillActivityAddDTO;
import com.stringmoment.model.request.SeckillActivityUpdateDTO;
import com.stringmoment.model.response.SeckillActivityPageVO;
import com.stringmoment.model.response.SeckillActivitySimpleVO;
import com.stringmoment.model.response.SeckillActivityVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 秒杀活动服务接口
 */
public interface SeckillActivityService extends IService<SeckillActivity> {

    // ==================== 用户端查询功能 ====================

    /**
     * 获取秒杀活动列表（用户端：只查询未开始和进行中的活动）
     */
    List<SeckillActivitySimpleVO> getSeckillActivityList(Integer status);

    /**
     * 获取秒杀活动详情（用户端：只查询未开始和进行中的活动）
     */
    SeckillActivityVO getSeckillActivityDetail(Long id);

    // ==================== 管理员端管理功能 ====================

    /**
     * 添加秒杀活动（管理员）
     */
    SeckillActivityVO addSeckillActivity(@Valid SeckillActivityAddDTO dto);

    /**
     * 更新秒杀活动信息（管理员）
     */
    SeckillActivityVO updateSeckillActivity(Long id, @Valid SeckillActivityUpdateDTO dto);

    /**
     * 删除秒杀活动（管理员）
     */
    void deleteSeckillActivity(Long id);

    /**
     * 查询所有秒杀活动（管理员：可按状态筛选）
     */
    SeckillActivityPageVO getAllSeckillActivityList(AdminSeckillActivityListQueryDTO dto);

    /**
     * 查询秒杀活动详情（管理员：可以查看所有状态的活动）
     */
    SeckillActivityVO getSeckillActivityDetailAdmin(Long id);
}
