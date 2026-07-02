package com.miri.aibuilder.core.saver;

import com.miri.aibuilder.ai.model.HtmlCodeResult;
import com.miri.aibuilder.ai.model.MultiFileCodeResult;
import com.miri.aibuilder.exception.BusinessException;
import com.miri.aibuilder.exception.ErrorCode;
import com.miri.aibuilder.model.enums.CodeGenTypeEnum;

/**
 * 多文件代码保存器
 */
public class MultiFileCodeSaver extends CodeFileSaverTemplate<MultiFileCodeResult> {

    /**
     * 保存多个代码文件（HTML+CSS+JS）
     * @param result 解析结果对象
     * @param dirPath 文件保存路径
     */
    @Override
    protected void saveFiles(MultiFileCodeResult result, String dirPath) {
        writeToFile(dirPath, "index.html", result.getHtmlCode());
        writeToFile(dirPath, "style.css", result.getCssCode());
        writeToFile(dirPath, "script.js", result.getJsCode());
    }

    @Override
    protected CodeGenTypeEnum getCodeGenType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }

    @Override
    protected void validateInput(MultiFileCodeResult result) {
        super.validateInput(result);
        // 至少要有html代码，Css和js代码可为空
        if (result.getHtmlCode() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "html代码为空");
        }
    }

}
