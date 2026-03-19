package com.stringmoment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stringmoment.entity.SeckillActivity;
import com.stringmoment.model.response.SeckillActivitySimpleVO;
import com.stringmoment.model.response.SeckillActivityVO;

import java.util.List;

/**
 * 秒杀活动服务接口
 */
public interface SeckillActivityService extends IService<SeckillActivity> {

    /**
     * 获取秒杀活动列表
     */
    List<SeckillActivitySimpleVO> getSeckillActivityList(Integer status);

    /**
     * 获取秒杀活动详情
     */
    SeckillActivityVO getSeckillActivityDetail(Long id);
}
