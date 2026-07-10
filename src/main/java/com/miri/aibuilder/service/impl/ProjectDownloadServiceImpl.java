package com.miri.aibuilder.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.miri.aibuilder.exception.BusinessException;
import com.miri.aibuilder.exception.ErrorCode;
import com.miri.aibuilder.exception.ThrowUtils;
import com.miri.aibuilder.service.ProjectDownloadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

@Slf4j
@Service
public class ProjectDownloadServiceImpl implements ProjectDownloadService {
    /**
     * 需要过滤的文件和目录名称
     */
    public static final Set<String> IGNORED_NAMES = Set.of(
            "node_modules",
            ".git",
            "dist",
            "build",
            ".DS_Store",
            ".idea",
            ".vscode"
    );
    /**
     * 需要过滤的文件扩展名
     */
    public static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".log",
            ".tmp",
            ".cache"
    );

    /**
     * 下载项目为ZIP文件
     * @param projectPath 项目路径
     * @param downloadFileName 下载文件名
     * @param response HTTP响应
     */
    @Override
    public void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response) {
        // 1.基础校验
        ThrowUtils.throwIf(StrUtil.isBlank(projectPath), ErrorCode.PARAMS_ERROR,"项目路径不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(downloadFileName), ErrorCode.PARAMS_ERROR,"下载文件名不能为空");
        File projectDir = new File(projectPath);
        ThrowUtils.throwIf(!projectDir.exists(), ErrorCode.NOT_FOUND_ERROR,"项目路径不存在");
        ThrowUtils.throwIf(!projectDir.isDirectory(), ErrorCode.PARAMS_ERROR,"指定路径不是一个目录");
        log.info("开始打包下载项目：{} -> {}.zip", projectPath, downloadFileName);
        // 2.设置 HTTP 响应头
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/zip");
        response.addHeader("Content-Disposition", String.format("attachment; filename=\"%s.zip\"", downloadFileName));
        // 3.定义文件过滤器
        FileFilter filter = file -> isPathAllowed(projectDir.toPath(), file.toPath());
        // 4.流式压缩打包
        try {
            // 使用 Hutool 的 ZipUtil 直接将过滤后的目录压缩写入响应输出流
            ZipUtil.zip(response.getOutputStream(), StandardCharsets.UTF_8, false, filter, projectDir);
            log.info("项目 {}.zip 打包下载完成", downloadFileName);
        } catch (Exception e) {
            log.error("项目下载异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "项目下载失败");
        }
    }

    /**
     * 检查路径是否允许包含在压缩包中
     * @param projectRoot 项目根路径
     * @param fullPath 需要校验的文件完整路径
     * @return true-允许保留该文件；false-匹配过滤规则，直接忽略该文件
     */
    private boolean isPathAllowed(Path projectRoot, Path fullPath) {
        // 获取相对路径
        Path relativize = projectRoot.relativize(fullPath);
        // 检查路径的每一部分
        for (Path part : relativize) {
            String partName = part.toString();
            // 检查是否在要过滤的名称列表中
            if (IGNORED_NAMES.contains(partName)) {
                return false;
            }
            // 检查是否在要过滤的扩展名列表中
            if (IGNORED_EXTENSIONS.stream().anyMatch(partName::endsWith)) {
                return false;
            }
        }
        return true;
    }
}
