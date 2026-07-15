package com.stringmoment.service;

import com.stringmoment.common.enums.FileType;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 */
public interface FileService {
    /**
     * 通用文件上传方法
     *
     * @param file 上传的文件
     * @param fileType 文件类型
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, FileType fileType);
}