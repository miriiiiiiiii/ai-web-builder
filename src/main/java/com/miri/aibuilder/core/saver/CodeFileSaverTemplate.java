package com.miri.aibuilder.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.miri.aibuilder.exception.BusinessException;
import com.miri.aibuilder.exception.ErrorCode;
import com.miri.aibuilder.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 抽象代码保存器（模版方法模式）
 */
public abstract class CodeFileSaverTemplate<T> {
    // 文件保存的根目录
    protected static final String ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 模版方法：保存代码的标准流程
     * @param result
     * @return
     */
    public final File saveCode(T result) {
        // 1.校验入参
        validateInput(result);
        // 2.构建文件保存路径
        String dirPath = buildDirPath();
        // 3.保存文件（具体实现交给子类）
        saveFiles(result, dirPath);
        // 4.返回文件对象
        return new File(dirPath);
    }

    /**
     * 验证输入参数（可由子类覆盖）
     * @param result
     */
    protected void validateInput(T result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码解析结果为空");
        }
    }

    /**
     * 构建文件保存路径 Root_DIR/bizType_雪花ID
     * @return
     */
    protected final String buildDirPath() {
        String bizType = getCodeGenType().getValue();
        String uniqueDirName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextIdStr());
        String dirPath = ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 保存单个文件工具类
     * @param dirPath 文件存放路径
     * @param fileName 文件名
     * @param content  文件内容
     * @return
     */
    protected final void writeToFile(String dirPath, String fileName, String content) {
        if (StrUtil.isBlank(content)) {
            String fullPath = dirPath + File.separator + fileName;
            FileUtil.writeString(content, fullPath, StandardCharsets.UTF_8);
        }
    }

    /**
     * 保存文件（具体实现交给子类）
     * @param result 解析结果对象
     * @param dirPath 文件保存路径
     */
    protected abstract void saveFiles(T result, String dirPath);

    /**
     * 获取代码生成类型（由子类实现）
     * @return 代码生成类型
     */
    protected abstract CodeGenTypeEnum getCodeGenType();
}
