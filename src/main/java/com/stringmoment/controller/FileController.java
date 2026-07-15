package com.stringmoment.controller;

import com.stringmoment.common.enums.FileType;
import com.stringmoment.common.result.Result;
import com.stringmoment.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传Controller
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * 上传头像
     */
    @PostMapping("/upload/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String url = fileService.uploadFile(file, FileType.AVATAR);
        return Result.success("文件上传成功", url);
    }

    /**
     * 上传商品图片
     */
    @PostMapping("/upload/product")
    public Result<String> uploadProduct(@RequestParam("file") MultipartFile file) {
        String url = fileService.uploadFile(file, FileType.PRODUCT);
        return Result.success("文件上传成功", url);
    }

    /**
     * 上传秒杀活动图片
     */
    @PostMapping("/upload/seckill")
    public Result<String> uploadSeckill(@RequestParam("file") MultipartFile file) {
        String url = fileService.uploadFile(file, FileType.SECKILL);
        return Result.success("文件上传成功", url);
    }
}