package com.miri.aibuilder.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.reasoning-chat-model")
@Data
public class ReasoningStreamingChatModelConfig {
    private String baseUrl;
    private String apiKey;

    /**
     * 流式推理模型（用于生成 Vue 项目，带工具调用）
     * @return
     */
    @Bean
    public StreamingChatModel reasoningStreamingChatModel() {
        final String modelName = "deepseek-reasoner";
        final int maxTokens = 32768;
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
