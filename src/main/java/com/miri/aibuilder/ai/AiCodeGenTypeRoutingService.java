package com.miri.aibuilder.ai;

import com.miri.aibuilder.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;

/**
 * Ai代码生成类型智能路由服务
 */
public interface AiCodeGenTypeRoutingService {

    /**
     * 根据用户提示词智能选择代码生成类型
     * @param userMessage 用户提示词
     * @return ai选择的代码生成类型
     */
    @SystemMessage(fromResource = "prompt/codegen-routing-prompt.txt")
    CodeGenTypeEnum routeCodeGenType(String userMessage);

}
