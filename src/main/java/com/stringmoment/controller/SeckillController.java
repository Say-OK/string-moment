package com.stringmoment.controller;

import com.stringmoment.common.result.Result;
import com.stringmoment.model.response.SeckillActivitySimpleVO;
import com.stringmoment.model.response.SeckillActivityVO;
import com.stringmoment.service.SeckillActivityService;
import jakarta.servlet.http.HttpServletRequest;
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

    /**
     * 检查用户秒杀资格
     */
    @GetMapping("/qualification/{activityId}")
    public Result<Integer> checkSeckillQualification(@PathVariable Long activityId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Integer qualification = seckillActivityService.checkSeckillQualification(activityId, userId);
        return Result.success(qualification);
    }
}
