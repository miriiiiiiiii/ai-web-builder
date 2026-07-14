package com.miri.aibuilder.utils;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 应用名称提取工具
 */
@Slf4j
@Component
public class AppNameExtractUtils {
    // 开头创建类前缀【重要！长词优先！长短语放前面】
    private static final List<String> CREATE_PREFIX = Arrays.asList(
            "搭建一个", "开发一个", "创建一个", "制作一个", "生成一个", "做一个", "写一个",
            "生成", "创建", "搭建", "开发",  "制作",
            "一个", "一款", "一套"
    );

    // 业务后缀列表【长词优先！】
    private static final List<String> BUSINESS_SUFFIX = Arrays.asList(
            "小游戏", "管理系统", "网站", "商城", "后台", "平台", "页面", "社区", "系统"
    );

    // 截断分隔标点（主体结束分隔符）
    private static final String[] SPLIT_CHARS = {
            "，", ",", "。", "；", ";", "：", ":", "\n", "（", "(", "【"
    };

    // 匹配开头「xxx的」修饰部分
    private static final Pattern DE_PATTERN = Pattern.compile("^.+?的");

    // 名称最大长度
    private static final int MAX_NAME_LENGTH = 12;

    /**
     * 从提示词中提取应用名称
     * @param prompt 提示词
     * @return 应用名称
     */
    public static String extractAppNameFromPrompt(String prompt) {
        if (StrUtil.isBlank(prompt)) {
            return "未命名应用";
        }
        String originText = prompt.trim();
        String text = originText;

        // 1. 循环移除开头创建前缀（长词优先）
        boolean matchPrefix;
        do {
            matchPrefix = false;
            for (String prefix : CREATE_PREFIX) {
                if (text.startsWith(prefix)) {
                    text = text.substring(prefix.length()).trim();
                    matchPrefix = true;
                    break;
                }
            }
        } while (matchPrefix);

        // 2. 移除开头「xxx的」修饰
        Matcher deMatcher = DE_PATTERN.matcher(text);
        if (deMatcher.find()) {
            text = text.substring(deMatcher.group().length()).trim();
        }

        // ============关键修复============
        // 第一步：先以第一个标点截断，只保留前半段主体，杜绝匹配后半段描述里的业务后缀！
        int firstSplitIdx = text.length();
        for (String symbol : SPLIT_CHARS) {
            int idx = text.indexOf(symbol);
            if (idx != -1 && idx < firstSplitIdx) {
                firstSplitIdx = idx;
            }
        }
        // 只保留标点前的主体文本
        String mainText = text.substring(0, firstSplitIdx).trim();

        // 第二步：在主体文本内匹配业务后缀
        int splitPoint = mainText.length();
        boolean foundSuffix = false;
        for (String suffix : BUSINESS_SUFFIX) {
            int idx = mainText.indexOf(suffix);
            if (idx != -1) {
                splitPoint = idx + suffix.length();
                foundSuffix = true;
                break;
            }
        }
        String targetName = mainText.substring(0, splitPoint).trim();

        // 清理连续空格
        targetName = targetName.replaceAll("\\s+", " ");

        // 兜底：提取为空则截取原始文本前N字符
        if (StrUtil.isBlank(targetName)) {
            targetName = originText.substring(0, Math.min(originText.length(), MAX_NAME_LENGTH));
        }

        // 长度截断
        String finalName = targetName.length() > MAX_NAME_LENGTH
                ? targetName.substring(0, MAX_NAME_LENGTH)
                : targetName;

        log.debug("[应用名称提取] 原始prompt:{} | 主体文本:{} | 最终名称:{}", prompt, mainText, finalName);
        return finalName;
    }
}