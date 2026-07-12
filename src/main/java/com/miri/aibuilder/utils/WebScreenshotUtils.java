package com.miri.aibuilder.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.miri.aibuilder.exception.BusinessException;
import com.miri.aibuilder.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

/**
 * 截图工具类
 */
@Slf4j
@Component
public class WebScreenshotUtils {
    public static final WebDriver webDriver;

    static {
        final int DEFAULT_WIDTH = 1600;
        final int DEFAULT_HEIGHT= 900;
        webDriver = initChromeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 生成网页截图
     * @param webUrl 网页地址
     * @return 压缩后的图片路径
     */
    public static String saveWebPageScreenshot(String webUrl) {
        // 1.参数校验
        if (StrUtil.isBlank(webUrl)) {
            log.error("网页地址不能为空");
             return null;
        }
        try {
            // 2.构建原始图片保存路径
            // 创建临时目录
            String rootPath = System.getProperty("user.dir") + "/tmp/screenshots/" + UUID.randomUUID().toString().substring(0,8);
            FileUtil.mkdir(rootPath);
            // 图片后缀
            final String IMAGE_SUFFIX = ".png";
            // 完整保存路径
            String saveImagePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + IMAGE_SUFFIX;
            // 3.访问网页
            webDriver.get(webUrl);
            // 4.等待网页加载完成
            waitForPageLoad(webDriver);
            // 5.截图并保存图片
            byte[] screenshotBytes = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
            saveImage(screenshotBytes, saveImagePath);
            log.info("截图保存成功，路径：{}", saveImagePath);
            // 6.构建压缩图片路径，执行压缩图片
            final String COMPRESSED_SUFFIX = ".png";
            String compressedImagePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + COMPRESSED_SUFFIX;
            compressImage(saveImagePath, compressedImagePath);
            log.info("压缩图片成功，路径：{}", compressedImagePath);
            // 7.清理原始图片
            FileUtil.del(saveImagePath);
            return compressedImagePath;
        } catch (Exception e) {
            log.error("网页截图失败，网页地址：{}", webUrl, e);
            return null;
        }
    }

    @PreDestroy
    public void destroy() {
        webDriver.quit();
    }
    /**
     * 初始化 Chrome 浏览器驱动
     */
    private static WebDriver initChromeDriver(int width, int height) {
        try {
            // 自动管理 ChromeDriver
            WebDriverManager.chromedriver().setup();
            // 配置 Chrome 选项
            ChromeOptions options = new ChromeOptions();
            // 无头模式
            options.addArguments("--headless");
            // 阻止空白窗口弹出
            options.addArguments("--no-first-run");
            options.addArguments("--no-default-browser-check");
            options.addArguments("--window-position=-2000,-2000");
            options.addArguments("--disable-background-networking");
            // 禁用GPU（在某些环境下避免问题）
            options.addArguments("--disable-gpu");
            // 禁用沙盒模式（Docker环境需要）
            options.addArguments("--no-sandbox");
            // 禁用开发者shm使用
            options.addArguments("--disable-dev-shm-usage");
            // 设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d", width, height));
            // 禁用扩展
            options.addArguments("--disable-extensions");
            // 设置用户代理
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            // 创建驱动
            WebDriver driver = new ChromeDriver(options);
            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }


    /**
     * 保存图片到指定路径
     * @param imageBytes 图片字节数组
     * @param imagePath 图片保存的完整路径
     */
    private static void saveImage(byte[] imageBytes, String imagePath) {
        try {
            FileUtil.writeBytes(imageBytes, imagePath);
        } catch (IORuntimeException e) {
            log.error("保存图片失败，图片路径：{}", imagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    /**
     * 压缩图片
     * @param originalImagePath 原始图片路径
     * @param compressedImagePath 压缩后的图片路径
     */
    private static void compressImage(String originalImagePath, String compressedImagePath) {
        // 图片压缩质量
        final float COMPRESS_QUALITY = 0.3f;
        try {
            ImgUtil.compress(
                    FileUtil.file(originalImagePath),
                    FileUtil.file(compressedImagePath),
                    COMPRESS_QUALITY
            );
        } catch (IORuntimeException e) {
            log.error("保存图片失败，{} -> {}", originalImagePath, compressedImagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }

    /**
     * 等待页面加载完成
     * @param driver WebDriver实例
     */
    private static void waitForPageLoad(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            // 等待 document.readyState 为 complete
            wait.until(webDriver -> (
                    (JavascriptExecutor)webDriver).executeScript("return document.readyState").equals("complete")
            );
            // 额外等待一段时间，确保动态内容加载完成
            Thread.sleep(2000);
            log.info("页面加载完成");
        } catch (InterruptedException e) {
            log.error("等待页面加载出现异常，跳过等待继续执行截图", e);
        }
    }
}

