package com.miri.aibuilder.manager;

import com.miri.aibuilder.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Cos对象存储管理器
 */
@Slf4j
@Component
public class CosManager {

    /**
     * cos配置
     */
    @Resource
    private CosClientConfig cosClientConfig;

    /**
     * cos客户端
     */
    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     * @param key 文件上传到cos上的路径
     * @param file 要上传的文件
     * @return 上传结果
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传文件到 COS 并返回图片路径
     * @param key
     * @param file 要上传的文件
     * @return 文件的访问路径
     */
    public String uploadFile(String key, File file) {
        PutObjectResult result = putObject(key, file);
        if (result != null) {
            String url = String.format("%s%s", cosClientConfig.getHost(), key);
            log.info("文件上传到 COS 成功：{} -> {}", file.getName(), url);
            return url;
        }
        log.error("文件上传到 COS 失败：{}，返回结果为空", file.getName());
        return null;
    }
}
