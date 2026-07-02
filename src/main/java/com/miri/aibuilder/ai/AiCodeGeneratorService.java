package com.miri.aibuilder.ai;

import com.miri.aibuilder.ai.model.HtmlCodeResult;
import com.miri.aibuilder.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;
import reactor.core.publisher.Flux;


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

    /**
     * 生成html代码（流式）
     * @param userMessage 用户提示词
     * @return
     */
    @SystemMessage(fromResource = "prompt/html-codegen-prompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);


    /**
     * 生成多文件代码（流式）
     * @param userMessage 用户提示词
     * @return
     */
    @SystemMessage(fromResource = "prompt/multi-file-codegen-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);
}
