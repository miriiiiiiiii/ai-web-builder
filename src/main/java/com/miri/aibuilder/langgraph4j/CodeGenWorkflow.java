package com.miri.aibuilder.langgraph4j;

import com.miri.aibuilder.exception.BusinessException;
import com.miri.aibuilder.exception.ErrorCode;
import com.miri.aibuilder.langgraph4j.model.QualityResult;
import com.miri.aibuilder.langgraph4j.node.*;
import com.miri.aibuilder.langgraph4j.state.WorkflowContext;
import com.miri.aibuilder.model.enums.CodeGenTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

/**
 * 代码生成工作流（实际应用）
 */
@Slf4j
public class CodeGenWorkflow {

    /**
     * 创建完整的工作流
     */
    public CompiledGraph<MessagesState<String>> createWorkflow() {
        try {
            return new MessagesStateGraph<String>()
                    // 添加节点 - 使用完整实现的节点
                    .addNode("image_collector", ImageCollectorNode.create())
                    .addNode("prompt_enhancer", PromptEnhancerNode.create())
                    .addNode("router", RouterNode.create())
                    .addNode("code_generator", CodeGeneratorNode.create())
                    .addNode("project_builder", ProjectBuilderNode.create())
                    .addNode("code_quality_check", CodeQualityCheckNode.create())
                    // 添加边
                    .addEdge(START, "image_collector")
                    .addEdge("image_collector", "prompt_enhancer")
                    .addEdge("prompt_enhancer", "router")
                    .addEdge("router", "code_generator")
                    .addEdge("code_generator", "code_quality_check")
                    // 使用条件边，根据质检结果决定下一步
                    .addConditionalEdges("code_quality_check",
                            edge_async(this::routeAfterQualityCheck),
                            Map.of("build", "project_builder", // 质检通过，且需要构建
                                    "skip_build", END, // 质检通过，但跳过构建直接结束
                                    "fail", "code_generator" // 质检不通过，重新生成代码
                            ))
                    .addEdge("project_builder", END)
                    // 编译工作流
                    .compile();
        } catch (GraphStateException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "工作流创建失败");
        }
    }

    /**
     * 执行工作流
     */
    public WorkflowContext executeWorkflow(String originalPrompt) {
        CompiledGraph<MessagesState<String>> workflow = createWorkflow();

        // 初始化 WorkflowContext
        WorkflowContext initialContext = WorkflowContext.builder()
                .originalPrompt(originalPrompt)
                .currentStep("初始化")
                .build();

        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
        log.info("工作流图:\n{}", graph.content());
        log.info("开始执行代码生成工作流");

        WorkflowContext finalContext = null;
        int stepCounter = 1;
        for (NodeOutput<MessagesState<String>> step : workflow.stream(
                Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
            log.info("--- 第 {} 步完成 ---", stepCounter);
            // 显示当前状态
            WorkflowContext currentContext = WorkflowContext.getContext(step.state());
            if (currentContext != null) {
                finalContext = currentContext;
                log.info("当前步骤上下文: {}", currentContext);
            }
            stepCounter++;
        }
        log.info("代码生成工作流执行完成！");
        return finalContext;
    }

    /**
     * 根据质检结果决定下一步
     *
     * @param state
     * @return
     */
    private String routeAfterQualityCheck(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.getContext(state);
        QualityResult qualityResult = context.getQualityResult();
        // 质检不通过
        if (qualityResult == null || !qualityResult.getIsValid()) {
            log.error("代码质检未通过，需要重新生成代码");
            return "fail";
        }
        // 质检通过，继续下一步：路由检查
        log.error("代码质检通过，继续后续流畅");
        return routeBuildOrSkip(state);
    }

    /**
     * 根据代码生成类型决定下一步
     *
     * @param state
     * @return
     */
    private String routeBuildOrSkip(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.getContext(state);
        CodeGenTypeEnum codeGenTypeEnum = context.getGenerationType();
        log.info("【路由判断】当前代码类型：{}", codeGenTypeEnum);

        // HTML 和 MULTI_FILE 不需要构建
        if (CodeGenTypeEnum.HTML.equals(codeGenTypeEnum) || CodeGenTypeEnum.MULTI_FILE.equals(codeGenTypeEnum)) {
            log.info("【路由分支】匹配无需构建，返回 skip_build");
            return "skip_build";
        }
        log.info("【路由分支】需要执行构建，返回 build");
        // VUE_PROJECT 需要构建
        return "build";
    }
}
