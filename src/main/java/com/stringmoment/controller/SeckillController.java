package com.stringmoment.controller;

import com.stringmoment.common.annotation.RateLimit;
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
     * 检查用户秒杀资格（IP级限流：防止单个IP频繁查询）
     */
    @GetMapping("/qualification/{activityId}")
    @RateLimit(key = "seckill_qualification", limit = 10, period = 1, limitType = RateLimit.LimitType.IP)
    public Result<Integer> checkSeckillQualification(@PathVariable Long activityId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Integer qualification = seckillOrderService.checkSeckillQualification(activityId, userId);
        return Result.success(qualification);
    }

    /**
     * 执行秒杀（三级限流：用户级+IP级+全局）
     * 1. 用户级限流：单个用户每秒最多1次秒杀请求（防止恶意刷接口）
     * 2. IP级限流：单个IP每秒最多5次秒杀请求（防止代理IP批量抢购）
     * 3. 全局限流：秒杀接口整体QPS限制在500（保护系统稳定性）
     */
    @PostMapping("/execute")
    @RateLimit(key = "seckill_execute_user", limit = 1, period = 1, limitType = RateLimit.LimitType.USER)
    @RateLimit(key = "seckill_execute_ip", limit = 5, period = 1, limitType = RateLimit.LimitType.IP)
    @RateLimit(key = "seckill_execute_global", limit = 500, period = 1, limitType = RateLimit.LimitType.GLOBAL)
    public Result<SeckillExecuteVO> executeSeckill(@Valid @RequestBody SeckillExecuteDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        SeckillExecuteVO result = seckillOrderService.executeSeckill(dto, userId);
        return Result.success(result);
    }
}
