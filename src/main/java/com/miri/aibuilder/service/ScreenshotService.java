package com.miri.aibuilder.service;

/**
 * 截图服务
 */
public interface ScreenshotService {

    /**
     * 生成并上传截图到 COS
     * @param webUrl 网页地址
     * @return 可访问的地址
     */
    public String generateAndUploadScreenshot(String webUrl);
}
