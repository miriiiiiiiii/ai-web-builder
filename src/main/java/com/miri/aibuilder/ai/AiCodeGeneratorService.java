package com.miri.aibuilder.ai;

import com.miri.aibuilder.ai.model.HtmlCodeResult;
import com.miri.aibuilder.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;


public interface AiCodeGeneratorService {
    /**
     * 生成html代码
     * @param userMessage 用户提示词
     * @return
     */
    @SystemMessage(fromResource = "prompt/html-codegen-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);


    /**
     * 生成多文件代码
     * @param userMessage 用户提示词
     * @return
     */
    @SystemMessage(fromResource = "prompt/multi-file-codegen-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);
}
