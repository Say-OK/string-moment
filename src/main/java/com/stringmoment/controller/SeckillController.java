package com.stringmoment.controller;

import com.stringmoment.common.result.Result;
import com.stringmoment.model.response.SeckillActivitySimpleVO;
import com.stringmoment.model.response.SeckillActivityVO;
import com.stringmoment.service.SeckillActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seckill")
@Validated
public class SeckillController {

    @Autowired
    private SeckillActivityService seckillActivityService;

    /**
     * 获取秒杀活动列表
     */
    @GetMapping("/activity/list")
    public Result<List<SeckillActivitySimpleVO>> getSeckillActivityList(@RequestParam(required = false) Integer status) {
        List<SeckillActivitySimpleVO> list = seckillActivityService.getSeckillActivityList(status);
        return Result.success(list);
    }

    /**
     * 获取秒杀活动详情
     */
    @GetMapping("/activity/detail/{id}")
    public Result<SeckillActivityVO> getSeckillActivityDetail(@PathVariable Long id) {
         SeckillActivityVO seckillActivityVO = seckillActivityService.getSeckillActivityDetail(id);
        return Result.success(seckillActivityVO);
    }
}
