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
@Component
public class AppNameExtractUtils {
    // 开头创建类前缀
    private static final List<String> CREATE_PREFIX = Arrays.asList(
            "生成一个", "做一个", "写一个", "搭建", "开发", "创建", "制作",
            "搭建一个", "开发一个", "创建一个", "制作一个"
    );

    // 业务后缀列表
    private static final List<String> BUSINESS_SUFFIX = Arrays.asList(
            "网站", "商城", "后台", "平台", "页面", "社区", "小游戏", "系统"
    );

    // 截断分隔标点，遇到任意一个就停止截取
    private static final String[] SPLIT_CHARS = {
            "，", ",", "。", "；", ";", "：", ":", "\n", "（", "(", "【"
    };

    // 匹配「任意字符+的」正则，只匹配开头修饰段
    private static final Pattern DE_PATTERN = Pattern.compile("^.+?的");

    // 名称最大长度
    private static final int MAX_NAME_LENGTH = 12;

    /**
     * 从提示词中提取应用名称
     * @param prompt 提示词
     * @return 应用名称
     */
    public static String extractAppNameFromPrompt(String prompt) {
        // 1. 空值校验
        if (StrUtil.isBlank(prompt)) {
            return "未命名应用";
        }
        String text = prompt.trim();

        // 2. 循环移除开头所有创建前缀
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

        // 3. 移除开头「xxx的」修饰部分
        Matcher deMatcher = DE_PATTERN.matcher(text);
        if (deMatcher.find()) {
            String matchStr = deMatcher.group();
            text = text.substring(matchStr.length()).trim();
        }

        // 新增：遇到标点直接截断，只保留标点前文字
        int splitPoint = text.length();
        for (String symbol : SPLIT_CHARS) {
            int idx = text.indexOf(symbol);
            if (idx != -1 && idx < splitPoint) {
                splitPoint = idx;
            }
        }
        text = text.substring(0, splitPoint).trim();

        // 4. 核心名称+完整后缀
        int endIndex = text.length();
        String hitSuffix = "";
        for (String suffix : BUSINESS_SUFFIX) {
            int idx = text.indexOf(suffix);
            if (idx != -1) {
                endIndex = idx + suffix.length();
                hitSuffix = suffix;
                break;
            }
        }

        String targetName;
        if (StrUtil.isNotBlank(hitSuffix)) {
            targetName = text.substring(0, endIndex).trim();
        } else {
            targetName = text;
        }

        // 5. 清理多余连续空格
        targetName = targetName.replaceAll("\\s+", " ");

        // 6. 提取结果为空，截取原提示词前N位
        if (StrUtil.isBlank(targetName)) {
            return prompt.substring(0, Math.min(prompt.length(), MAX_NAME_LENGTH));
        }

        // 7. 长度截断控制
        return targetName.length() > MAX_NAME_LENGTH
                ? targetName.substring(0, MAX_NAME_LENGTH)
                : targetName;
    }
}

