package com.miri.aibuilder.core.saver;

import com.miri.aibuilder.ai.model.HtmlCodeResult;
import com.miri.aibuilder.exception.BusinessException;
import com.miri.aibuilder.exception.ErrorCode;
import com.miri.aibuilder.model.enums.CodeGenTypeEnum;

/**
 * Html代码保存器
 */
public class HtmlCodeSaver extends CodeFileSaverTemplate<HtmlCodeResult> {

    /**
     * 保存单个html代码文件
     * @param result 解析结果对象
     * @param dirPath 文件保存路径
     */
    @Override
    protected void saveFiles(HtmlCodeResult result, String dirPath) {
        writeToFile(dirPath, "index.html", result.getHtmlCode());
    }

    @Override
    protected CodeGenTypeEnum getCodeGenType() {
        return CodeGenTypeEnum.HTML;
    }

    @Override
    protected void validateInput(HtmlCodeResult result) {
        super.validateInput(result);
        if (result.getHtmlCode() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "html代码为空");
        }
    }


}
