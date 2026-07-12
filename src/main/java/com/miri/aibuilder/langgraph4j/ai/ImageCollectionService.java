package com.miri.aibuilder.langgraph4j.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * Ai图片收集服务
 */
public interface ImageCollectionService {

    @SystemMessage(fromResource = "prompt/image-collection-prompt.txt")
    String collectImages(@UserMessage String userMessage);
}
