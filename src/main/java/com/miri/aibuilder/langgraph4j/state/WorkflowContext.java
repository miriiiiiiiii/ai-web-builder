package com.miri.aibuilder.langgraph4j.state;

import com.miri.aibuilder.langgraph4j.model.ImageResource;
import com.miri.aibuilder.langgraph4j.model.QualityResult;
import com.miri.aibuilder.model.enums.CodeGenTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 工作流上下文 - 存储所有状态信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowContext implements Serializable {

    /**
     * WorkflowContext 在 MessagesState 中的存储key
     */
    public static final String WORKFLOW_CONTEXT_KEY = "workflowContext";

    /**
     * 流式输出消息回调缓存
     * key：每一轮对话唯一ID streamEmitterId
     * value：用于把文本实时推送给前端的回调函数
     */
    private static final Map<String, Consumer<String>> STREAM_EMITTER_REGISTRY = new ConcurrentHashMap<>();

    /**
     * 当前执行步骤
     */
    private String currentStep;

    /**
     * 用户原始输入的提示词
     */
    private String originalPrompt;

    /**
     * 应用 ID
     */
    private Long appId;

    /**
     * 图片资源字符串
     */
    private String imageListStr;

    /**
     * 图片资源列表
     */
    private List<ImageResource> imageList;

    /**
     * 增强后的提示词
     */
    private String enhancedPrompt;

    /**
     * 代码生成类型
     */
    private CodeGenTypeEnum generationType;

    /**
     * 代码质量检查结果
     */
    private QualityResult qualityResult;

    /**
     * 生成的代码目录
     */
    private String generatedCodeDir;

    /**
     * 构建成功的目录
     */
    private String buildResultDir;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 流式输出回调 ID，用于跨工作流状态拷贝获取实时输出回调
     */
    private String streamEmitterId;


    @Serial
    private static final long serialVersionUID = 1L;

    // ========== 上下文操作方法 ==========

    /**
     * 从 MessagesState 中获取 WorkflowContext
     */
    public static WorkflowContext getContext(MessagesState<String> state) {
        return (WorkflowContext) state.data().get(WORKFLOW_CONTEXT_KEY);
    }

    /**
     * 将 WorkflowContext 保存到 MessagesState 中
     */
    public static Map<String, Object> saveContext(WorkflowContext context) {
        return Map.of(WORKFLOW_CONTEXT_KEY, context);
    }

    /**
     * 注册流式输出回调
     */
    public static String registerStreamEmitter(Consumer<String> streamEmitter) {
        String streamEmitterId = UUID.randomUUID().toString();
        STREAM_EMITTER_REGISTRY.put(streamEmitterId, streamEmitter);
        return streamEmitterId;
    }

    /**
     * 移除流式输出回调
     */
    public static void removeStreamEmitter(String streamEmitterId) {
        if (streamEmitterId != null) {
            STREAM_EMITTER_REGISTRY.remove(streamEmitterId);
        }
    }

    /**
     * 推送流式消息
     */
    public void emitStream(String message) {
        Consumer<String> streamEmitter = STREAM_EMITTER_REGISTRY.get(streamEmitterId);
        if (streamEmitter != null) {
            streamEmitter.accept(message);
        }
    }
}
