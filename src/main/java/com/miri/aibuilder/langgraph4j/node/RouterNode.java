package com.miri.aibuilder.langgraph4j.node;

import com.miri.aibuilder.ai.AiCodeGenTypeRoutingService;
import com.miri.aibuilder.langgraph4j.state.WorkflowContext;
import com.miri.aibuilder.model.entity.App;
import com.miri.aibuilder.model.enums.CodeGenTypeEnum;
import com.miri.aibuilder.service.AppService;
import com.miri.aibuilder.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 智能路由节点
 */
@Slf4j
public class RouterNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 智能路由");

            CodeGenTypeEnum generationType;
            try {
                // 获取AI路由服务
                AiCodeGenTypeRoutingService routingService = SpringContextUtil.getBean(AiCodeGenTypeRoutingService.class);
                // 根据原始提示词进行智能路由
                generationType = routingService.routeCodeGenType(context.getOriginalPrompt());
                log.info("AI智能路由完成，选择类型: {} ({})", generationType.getValue(), generationType.getText());
            } catch (Exception e) {
                log.error("AI智能路由失败，使用默认HTML类型: {}", e.getMessage());
                generationType = CodeGenTypeEnum.HTML;
            }

            // 更新状态
            context.setCurrentStep("智能路由");
            context.setGenerationType(generationType);

            Long appId = context.getAppId();
            if (appId != null && appId > 0) {
                AppService appService = SpringContextUtil.getBean(AppService.class);
                App app = appService.getById(appId);
                if (app != null) {
                    App updateApp = new App();
                    updateApp.setId(appId);
                    updateApp.setCodeGenType(generationType.getValue());
                    appService.updateById(updateApp);
                    log.info("已回写应用 codeGenType: {} -> {}", appId, generationType.getValue());
                }
            }
            return WorkflowContext.saveContext(context);
        });
    }
}

