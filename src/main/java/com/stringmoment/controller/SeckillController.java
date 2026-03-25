package com.stringmoment.controller;

import com.stringmoment.common.result.Result;
import com.stringmoment.model.request.SeckillExecuteDTO;
import com.stringmoment.model.response.SeckillActivitySimpleVO;
import com.stringmoment.model.response.SeckillActivityVO;
import com.stringmoment.model.response.SeckillExecuteVO;
import com.stringmoment.service.SeckillActivityService;
import com.stringmoment.service.SeckillOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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

    @Autowired
    private SeckillOrderService seckillOrderService;

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
        Integer qualification = seckillOrderService.checkSeckillQualification(activityId, userId);
        return Result.success(qualification);
    }

    /**
     * 执行秒杀
     */
    @PostMapping("/execute")
    public Result<SeckillExecuteVO> executeSeckill(@Valid @RequestBody SeckillExecuteDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        SeckillExecuteVO result = seckillOrderService.executeSeckill(dto, userId);
        return Result.success(result);
    }
}
