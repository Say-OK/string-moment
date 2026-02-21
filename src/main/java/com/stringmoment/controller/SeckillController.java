package com.stringmoment.controller;

import com.stringmoment.common.result.Result;
import com.stringmoment.model.response.SeckillActivityVO;
import com.stringmoment.service.SeckillActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seckill")
@Validated
public class SeckillController {

    @Autowired
    private SeckillActivityService seckillActivityService;

    /**
     * 获取秒杀活动详情
     */
    @GetMapping("/activity/detail/{id}")
    public Result<SeckillActivityVO> getSeckillActivityDetail(@PathVariable Long id) {
         SeckillActivityVO seckillActivityVO = seckillActivityService.getSeckillActivityDetail(id);
        return Result.success(seckillActivityVO);
    }
}
