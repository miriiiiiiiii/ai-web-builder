package com.miri.aibuilder.core.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.miri.aibuilder.ai.model.message.*;
import com.miri.aibuilder.ai.tools.BaseTool;
import com.miri.aibuilder.ai.tools.ToolManager;
import com.miri.aibuilder.constant.AppConstant;
import com.miri.aibuilder.core.builder.VueProjectBuilder;
import com.miri.aibuilder.model.entity.User;
import com.miri.aibuilder.model.enums.ChatHistoryMessageTypeEnum;
import com.miri.aibuilder.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.Set;

/**
 * JSON 消息流处理器
 * 处理 VUE_PROJECT 类型的复杂流式响应，包含工具调用信息
 */
@Slf4j
@Component

public class JsonMessageStreamHandler {

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ToolManager toolManager;

    /**
     * 处理 TokenStream（VUE_PROJECT）
     * 解析 JSON 消息并重组为完整的响应格式
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @return 处理后的流
     */
    public Flux<String> handle(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               long appId, User loginUser) {
        StringBuilder chatHistoryStringBuilder = new StringBuilder();
        Set<String> seenToolIds = new HashSet<>();
        return originFlux
                .map(chunk -> parseChunkToDisplayText(chunk, seenToolIds, chatHistoryStringBuilder))
                .filter(StrUtil::isNotEmpty)
                .doOnComplete(() -> {
                    String aiResponse = chatHistoryStringBuilder.toString();
                    chatHistoryService.addChatHistory(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                    String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
                    vueProjectBuilder.buildProjectAsync(projectPath);
                })
                .doOnError(error -> {
                    String errorMessage = "AI回复失败: " + error.getMessage();
                    chatHistoryService.addChatHistory(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                });
    }

    /**
     * 将 VUE_PROJECT 流式 JSON 消息转换为前端可展示文本
     */
    public String parseChunkToDisplayText(String chunk, Set<String> seenToolIds) {
        return parseChunkToDisplayText(chunk, seenToolIds, null);
    }

    /**
     * 将 VUE_PROJECT 流式 JSON 消息转换为前端可展示文本，并可选拼接历史内容
     */
    public String parseChunkToDisplayText(String chunk,
                                          Set<String> seenToolIds,
                                          StringBuilder chatHistoryStringBuilder) {
        StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
        StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getEnumByValue(streamMessage.getType());
        if (typeEnum == null) {
            return "";
        }
        return switch (typeEnum) {
            case AI_RESPONSE -> {
                AiResponseMessage aiMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
                String data = aiMessage.getData();
                if (chatHistoryStringBuilder != null) {
                    chatHistoryStringBuilder.append(data);
                }
                yield data;
            }
            case TOOL_REQUEST -> {
                ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
                String toolId = toolRequestMessage.getId();
                String toolName = toolRequestMessage.getName();
                if (toolId == null || seenToolIds.contains(toolId)) {
                    yield "";
                }
                seenToolIds.add(toolId);
                BaseTool tool = toolManager.getTool(toolName);
                yield tool.generateToolRequestResponse();
            }
            case TOOL_EXECUTED -> {
                ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);
                String toolName = toolExecutedMessage.getName();
                JSONObject jsonObject = JSONUtil.parseObj(toolExecutedMessage.getArguments());
                BaseTool tool = toolManager.getTool(toolName);
                String output = String.format("\n\n%s\n\n", tool.generateToolExecutedResult(jsonObject));
                if (chatHistoryStringBuilder != null) {
                    chatHistoryStringBuilder.append(output);
                }
                yield output;
            }
        };
    }
}
