package com.miri.aibuilder.core;

import cn.hutool.json.JSONUtil;
import com.miri.aibuilder.ai.AiCodeGeneratorService;
import com.miri.aibuilder.ai.AiCodeGeneratorServiceFactory;
import com.miri.aibuilder.ai.model.HtmlCodeResult;
import com.miri.aibuilder.ai.model.MultiFileCodeResult;
import com.miri.aibuilder.ai.model.message.AiResponseMessage;
import com.miri.aibuilder.ai.model.message.ToolExecutedMessage;
import com.miri.aibuilder.ai.model.message.ToolRequestMessage;
import com.miri.aibuilder.core.parser.CodeParserExecutor;
import com.miri.aibuilder.core.saver.CodeFileSaverExecutor;
import com.miri.aibuilder.exception.BusinessException;
import com.miri.aibuilder.exception.ErrorCode;
import com.miri.aibuilder.model.enums.CodeGenTypeEnum;
import com.miri.aibuilder.service.ChatHistoryService;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;


/**
 * Ai代码生成门面类，组合代码生成和保存功能
 */
@Slf4j
@Service
public class AiCodeGeneratorFacade {
    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 统一入口：根据类型生成并保存代码
     * @param userMessage
     * @param codeGenTypeEnum
     * @param appId 应用ID
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码生成类型为空");
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(htmlCodeResult, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(multiFileCodeResult, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一入口：根据类型生成并保存代码（流式）
     * @param userMessage 用户消息
     * @param codeGenTypeEnum 代码生成类型
     * @param appId 应用ID
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码生成类型为空");
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(result, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(result, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            case VUE_PROJECT -> {
                TokenStream result = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processTokenStream(result);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMessage);
            }
        };
    }

    /**
     * 将 TokenStream 转化为 Flux<String>，并传递工具调用信息
     * @param tokenStream TokenStream 对象
     * @return 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream) {
        return Flux.create(sink -> {
            tokenStream.onPartialResponse((String partialResponse) -> {
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    .onCompleteResponse((ChatResponse response) -> {
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();
        });
    }



    /**
     * 通用流式代码处理方法
     * @param codeStream 代码流
     * @param codeGenTypeEnum 代码生成类型
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        // 字符串拼接器，用于当流式返回的代码生成完成后，再保存
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream
                // 实时收集代码片段
                .doOnNext(chunk -> codeBuilder.append(chunk))
                // 代码生成完成后，解析并保存代码
                .doOnComplete(() -> {
                    try {
                        String completeCode = codeBuilder.toString();
                        // 使用执行器解析代码
                        Object result = CodeParserExecutor.executeParser(completeCode, codeGenTypeEnum);
                        // 使用执行器保存代码
                        File saveDir = CodeFileSaverExecutor.executeSaver(result, codeGenTypeEnum, appId);
                        log.info("代码生成并保存成功，文件路径：{}", saveDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("文件保存失败：{}", e.getMessage());
                    }
                });
    }
}
