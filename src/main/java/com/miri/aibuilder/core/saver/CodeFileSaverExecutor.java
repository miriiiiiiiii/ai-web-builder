package com.miri.aibuilder.core.saver;

import com.miri.aibuilder.ai.model.HtmlCodeResult;
import com.miri.aibuilder.ai.model.MultiFileCodeResult;
import com.miri.aibuilder.exception.BusinessException;
import com.miri.aibuilder.exception.ErrorCode;
import com.miri.aibuilder.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 代码保存执行器
 */
public class CodeFileSaverExecutor {
    public static final HtmlCodeSaver htmlCodeSaver = new HtmlCodeSaver();
    public static final MultiFileCodeSaver multiFileCodeSaver = new MultiFileCodeSaver();

    public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenTypeEnum) {
        return switch(codeGenTypeEnum) {
            case HTML -> htmlCodeSaver.saveCode((HtmlCodeResult) codeResult);
            case MULTI_FILE -> multiFileCodeSaver.saveCode((MultiFileCodeResult) codeResult);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码保存类型：" + codeGenTypeEnum.getValue());
        };
    }
}
