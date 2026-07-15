package com.stringmoment.service.impl;

import com.stringmoment.common.enums.FileType;
import com.stringmoment.common.exception.BusinessException;
import com.stringmoment.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件服务实现类
 */
@Service
public class FileServiceImpl implements FileService {

    // 文件存储路径（从application.yml读取）
    @Value("${file.upload.path}")
    private String uploadPathConfig;

    @Override
    public String uploadFile(MultipartFile file, FileType fileType) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        // 检查文件类型（只允许图片）
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("只能上传图片文件");
        }

        // 检查文件大小
        if (file.getSize() > fileType.getMaxSize()) {
            long maxSizeMB = fileType.getMaxSize() / (1024 * 1024);
            throw new BusinessException("文件大小不能超过" + maxSizeMB + "MB");
        }

        // 生成文件名：日期 + UUID + 原文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = date + "_" + UUID.randomUUID().toString().replace("-", "") + extension;

        // 获取项目根目录的绝对路径
        String projectRoot = System.getProperty("user.dir");
        String folderPath = projectRoot + File.separator + uploadPathConfig + File.separator + fileType.getFolderName();

        // 创建目录
        File dir = new File(folderPath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new BusinessException("无法创建文件存储目录：" + folderPath);
            }
        }

        // 保存文件
        File destFile = new File(folderPath + File.separator + fileName);
        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            throw new BusinessException("文件保存失败：" + e.getMessage());
        }

        // 返回文件访问URL（相对路径）
        return fileType.getUrlPrefix() + "/" + fileName;
    }
}