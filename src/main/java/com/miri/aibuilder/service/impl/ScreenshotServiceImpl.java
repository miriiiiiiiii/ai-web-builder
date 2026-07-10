package com.miri.aibuilder.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.miri.aibuilder.exception.ErrorCode;
import com.miri.aibuilder.exception.ThrowUtils;
import com.miri.aibuilder.manager.CosManager;
import com.miri.aibuilder.service.ScreenshotService;
import com.miri.aibuilder.utils.WebScreenshotUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private CosManager cosManager;

    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        // 1.参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(webUrl), ErrorCode.PARAMS_ERROR, "截图网址不能为空");
        log.info("开始生成网页截图，url：{}", webUrl);
        // 2.生成截图
        String localScreenshotPath = WebScreenshotUtils.saveWebPageScreenshot(webUrl);
        ThrowUtils.throwIf(StrUtil.isBlank(localScreenshotPath), ErrorCode.OPERATION_ERROR, "生成网页截图失败");
        // 3.上传截图到 COS
        try {
            String cosUrl = uploadScreenshotToCos(localScreenshotPath);
            ThrowUtils.throwIf(StrUtil.isBlank(cosUrl), ErrorCode.OPERATION_ERROR, "上传截图到 COS 失败");
            log.info("网页截图生成并上传成功：{} -> {}", webUrl, cosUrl);
            return cosUrl;
        } finally {
            // 4.清理本地文件
            cleanUpLocalFile(localScreenshotPath);
        }
    }

    private String uploadScreenshotToCos(String localScreenshotPath) {
        // 1.参数校验
        if (StrUtil.isBlank(localScreenshotPath)) {
            return null;
        }
        // 2.构建文件对象
        File screenshotFile = new File(localScreenshotPath);
        if (!screenshotFile.exists()) {
            log.error("截图文件不存在: {}", localScreenshotPath);
            return null;
        }
        // 3.构造 COS 键
        String cosKey = generateCosKey();
        return cosManager.uploadFile(cosKey, screenshotFile);
    }

    /**
     * 生成 COS对象键（即文件上传到cos上的路径）
     * 格式：/screenshots/2026/06/01/filename.jpg
     */
    private String generateCosKey() {
        String fileName = UUID.randomUUID().toString().substring(0, 8) + "_compressed.jpg";
        String dataPath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("/screenshots/%s/%s", dataPath, fileName);
    }

    /**
     * 清理本地文件
     * @param localFilePath 本地文件路径
     */
    private void cleanUpLocalFile(String localFilePath) {
        File localFile = new File(localFilePath);
        if(localFile.exists()) {
            FileUtil.del(localFile);
            log.info("清理本地文件成功: {}", localFilePath);
        }
    }

}
