package com.miri.aibuilder.langgraph4j.ai;

import com.miri.aibuilder.langgraph4j.model.QualityResult;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * Ai代码质量检查服务
 */
public interface CodeQualityCheckService {

    @SystemMessage(fromResource = "prompt/code-quality-check-prompt.txt")
    QualityResult checkCodeQuality(@UserMessage String codeConten);
}
