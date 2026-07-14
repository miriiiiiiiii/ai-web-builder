package com.miri.aibuilder.langgraph4j;

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
 * 工作流应用（模拟状态流转）
 */
@Slf4j
public class WorkflowApp {

    public void main(String[] args) throws GraphStateException {
        // 创建工作流图
        CompiledGraph<MessagesState<String>> workflow = new MessagesStateGraph<String>()
                // 添加节点 - 使用真实的工作节点
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



        // 初始化 WorkflowContext - 只设置基本信息
        WorkflowContext initialContext = WorkflowContext.builder()
                .originalPrompt("创建一个小马的个人博客网站")
                .currentStep("初始化")
                .build();
        log.info("初始输入: {}", initialContext.getOriginalPrompt());
        log.info("开始执行工作流");

        // 显示工作流图
        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
        log.info("工作流图:\n{}", graph.content());

        // 执行工作流
        int stepCounter = 1;
        for (NodeOutput<MessagesState<String>> step : workflow.stream(Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
            log.info("--- 第 {} 步完成 ---", stepCounter);
            // 显示当前状态
            WorkflowContext currentContext = WorkflowContext.getContext(step.state());
            if (currentContext != null) {
                log.info("当前步骤上下文: {}", currentContext);
            }
            stepCounter++;
        }
        log.info("工作流执行完成！");
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
