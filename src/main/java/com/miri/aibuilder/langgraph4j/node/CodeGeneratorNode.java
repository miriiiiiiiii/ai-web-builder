package com.miri.aibuilder.langgraph4j.node;

import com.miri.aibuilder.constant.AppConstant;
import com.miri.aibuilder.core.AiCodeGeneratorFacade;
import com.miri.aibuilder.core.handler.JsonMessageStreamHandler;
import com.miri.aibuilder.langgraph4j.model.QualityResult;
import com.miri.aibuilder.langgraph4j.state.WorkflowContext;
import com.miri.aibuilder.model.enums.CodeGenTypeEnum;
import com.miri.aibuilder.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 代码生成节点
 */
@Slf4j
public class CodeGeneratorNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            context.emitStream("正在生成代码...  \n\n");
            log.info("执行节点: 代码生成");

            // 构造用户消息，包含原始提示词和可能的错误修复信息
            String userMessage = buildUserMessage(context);
            CodeGenTypeEnum generationType = context.getGenerationType();
            // 获取 AI 代码生成外观服务
            AiCodeGeneratorFacade codeGeneratorFacade = SpringContextUtil.getBean(AiCodeGeneratorFacade.class);
            log.info("开始生成代码，类型: {} ({})", generationType.getValue(), generationType.getText());
            Long appId = context.getAppId();
            if (appId == null || appId <= 0) {
                throw new IllegalArgumentException("应用 ID 无效，无法保存生成代码");
            }
            // 调用流式代码生成，HTML / MULTI_FILE 会在流完成后解析并保存，VUE_PROJECT 会通过工具调用保存文件
            Flux<String> codeStream = codeGeneratorFacade.generateAndSaveCodeStream(userMessage, generationType, appId);
            Set<String> seenToolIds = new HashSet<>();
            // 同步等待流式输出完成，同时把 AI 生成内容实时转发给前端
            codeStream.doOnNext(chunk -> emitDisplayChunk(context, chunk, generationType, seenToolIds))
                    .blockLast(Duration.ofMinutes(10));
            // 根据类型设置生成目录
            String generatedCodeDir = String.format("%s/%s_%s", AppConstant.CODE_OUTPUT_ROOT_DIR, generationType.getValue(), appId);
            log.info("AI 代码生成完成，生成目录: {}", generatedCodeDir);

            // 更新状态
            context.setCurrentStep("代码生成");
            context.setGeneratedCodeDir(generatedCodeDir);
            return WorkflowContext.saveContext(context);
        });
    }

    /**
     * 输出前端可展示的流式内容
     */
    private static void emitDisplayChunk(WorkflowContext context, String chunk, CodeGenTypeEnum generationType, Set<String> seenToolIds) {
        if (!CodeGenTypeEnum.VUE_PROJECT.equals(generationType)) {
            context.emitStream(chunk);
            return;
        }
        String displayText = handleJsonMessageStream(chunk, seenToolIds);
        if (StrUtil.isNotBlank(displayText)) {
            context.emitStream(displayText);
        }
    }

    /**
     * 处理 VUE_PROJECT 类型的复杂流式响应
     */
    private static String handleJsonMessageStream(String chunk, Set<String> seenToolIds) {
        try {
            StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
            StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getEnumByValue(streamMessage.getType());
            if (typeEnum == null) {
                return "";
            }
            return switch (typeEnum) {
                case AI_RESPONSE -> JSONUtil.toBean(chunk, AiResponseMessage.class).getData();
                case TOOL_REQUEST -> {
                    ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
                    String toolId = toolRequestMessage.getId();
                    if (StrUtil.isBlank(toolId) || seenToolIds.contains(toolId)) {
                        yield "";
                    }
                    seenToolIds.add(toolId);
                    ToolManager toolManager = SpringContextUtil.getBean(ToolManager.class);
                    BaseTool tool = toolManager.getTool(toolRequestMessage.getName());
                    yield tool.generateToolRequestResponse();
                }
                case TOOL_EXECUTED -> {
                    ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);
                    ToolManager toolManager = SpringContextUtil.getBean(ToolManager.class);
                    BaseTool tool = toolManager.getTool(toolExecutedMessage.getName());
                    JSONObject jsonObject = JSONUtil.parseObj(toolExecutedMessage.getArguments());
                    yield String.format("\n\n%s\n\n", tool.generateToolExecutedResult(jsonObject));
                }
            };
        } catch (Exception e) {
            log.warn("Vue 项目流式消息解析失败，已忽略当前片段: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 构造用户消息，如果存在质检失败结果则添加错误修复信息
     */
    private static String buildUserMessage(WorkflowContext context) {
        String userMessage = context.getEnhancedPrompt();
        // 检查是否存在质检失败结果
        QualityResult qualityResult = context.getQualityResult();
        if (isQualityCheckFailed(qualityResult)) {
            // 直接将错误修复信息作为新的提示词（起到了修改的作用）
            userMessage = buildErrorFixPrompt(qualityResult);
        }
        return userMessage;
    }

    /**
     * 判断质检是否失败
     */
    private static boolean isQualityCheckFailed(QualityResult qualityResult) {
        return qualityResult != null &&
                !qualityResult.getIsValid() &&
                qualityResult.getErrors() != null &&
                !qualityResult.getErrors().isEmpty();
    }

    /**
     * 构造错误修复提示词
     */
    private static String buildErrorFixPrompt(QualityResult qualityResult) {
        StringBuilder errorInfo = new StringBuilder();
        errorInfo.append("\n\n## 上次生成的代码存在以下问题，请修复：\n");
        // 添加错误列表
        qualityResult.getErrors().forEach(error ->
                errorInfo.append("- ").append(error).append("\n"));
        // 添加修复建议（如果有）
        if (qualityResult.getSuggestions() != null && !qualityResult.getSuggestions().isEmpty()) {
            errorInfo.append("\n## 修复建议：\n");
            qualityResult.getSuggestions().forEach(suggestion ->
                    errorInfo.append("- ").append(suggestion).append("\n"));
        }
        errorInfo.append("\n请根据上述问题和建议重新生成代码，确保修复所有提到的问题。");
        return errorInfo.toString();
    }

}

