package com.miri.aibuilder.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.miri.aibuilder.constant.AppConstant;
import com.miri.aibuilder.core.AiCodeGeneratorFacade;
import com.miri.aibuilder.core.builder.VueProjectBuilder;
import com.miri.aibuilder.core.handler.StreamHandlerExecutor;
import com.miri.aibuilder.exception.BusinessException;
import com.miri.aibuilder.exception.ErrorCode;
import com.miri.aibuilder.exception.ThrowUtils;
import com.miri.aibuilder.mapper.AppMapper;
import com.miri.aibuilder.model.dto.app.AppQueryRequest;
import com.miri.aibuilder.model.entity.App;
import com.miri.aibuilder.model.entity.User;
import com.miri.aibuilder.model.enums.ChatHistoryMessageTypeEnum;
import com.miri.aibuilder.model.enums.CodeGenTypeEnum;
import com.miri.aibuilder.model.vo.AppVO;
import com.miri.aibuilder.model.vo.UserVO;
import com.miri.aibuilder.service.AppService;
import com.miri.aibuilder.service.ChatHistoryService;
import com.miri.aibuilder.service.ScreenshotService;
import com.miri.aibuilder.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.SqlUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/miriiiiiiiii">程序员小马</a>
 */
@Slf4j
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {
    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ScreenshotService screenshotService;

    @Override
    public Flux<String> chatToGenCode(String userMessage, Long appId, User loginUser) {
        // 1.参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(userMessage), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        // 2.查询应用是否存在
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR, "应用不存在");
        // 3.权限校验，仅本人能和自己的应用对话
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 4.获取应用的代码生成类型
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        // 5.调用ai前，保存用户消息到对话历史
        chatHistoryService.addChatHistory(appId, userMessage, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());
        // 6.调用接口生成并保存代码
        Flux<String> contentFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream(userMessage, codeGenTypeEnum, appId);
        // 7.拼接ai的响应内容，完成后保存到对话历史
        return streamHandlerExecutor.doExecute(contentFlux, chatHistoryService, appId, loginUser, codeGenTypeEnum);
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1.参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        // 2.查询应用是否存在
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR, "应用不存在");
        // 3.权限校验，仅本人能部署自己的应用
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        // 4.检查应用是否已有deployKey，没有则随机生成6位（字母+数字）
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 5.获取应用生成类型，构建源文件访问路径（CODE_OUTPUT_ROOT_DIR/bizType_appId）
        String bizType = app.getCodeGenType();
        String sourceFileName = bizType + "_" + appId;
        String sourceFilePath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceFileName;
        // 6.检测源文件目录是否存在
        File sourceDir = new File(sourceFilePath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }
        // 7.Vue 项目特殊处理：执行构建
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(bizType);
        if (CodeGenTypeEnum.VUE_PROJECT == codeGenTypeEnum) {
            boolean buildSuccess = vueProjectBuilder.buildProject(sourceFilePath);
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败");
            File distDir = new File(sourceFilePath, "dist");
            ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "Vue 项目构建完成单未生成 dist 目录");
            // 将 dist 目录作为部署源
            sourceDir = distDir;
            log.info("Vue 项目构建成功，将部署到 dist 目录：{}", distDir.getAbsolutePath());
        }
        // 8.构建部署目录，复制文件到部署目录
        String deployFilePath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        FileUtil.copyContent(sourceDir, new File(deployFilePath), true);
        // 9.更新应用deployKey和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean result = this.updateById(updateApp);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "应用部署失败");
        // 10.返回可访问的部署路径
        String deployUrl =  String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
        // 11.异步生成截图并更新应用封面
        generateAppScreenshotAsync(appId, deployUrl);
        return deployUrl;
    }

    @Override
    public void generateAppScreenshotAsync(Long appId, String appUrl) {
        Thread.startVirtualThread(() -> {
           // 调用截图服务：生成截图并上传COS对象存储
            String screenshotUrl = screenshotService.generateAndUploadScreenshot(appUrl);
            // 更新应用封面
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCover(screenshotUrl);
            boolean updated = this.updateById(updateApp);
            ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "应用封面更新失败");
        });
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }


    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long id = appQueryRequest.getId();
        Long userId = appQueryRequest.getUserId();
        // 如果传了用户名，则先查用户
        String userName = appQueryRequest.getUserName();
        List<Long> userIds = null;
        if (StrUtil.isNotBlank(userName)) {

            List<User> userList = userService.list(
                    QueryWrapper.create().like("nickName", userName)
            );

            if (CollUtil.isEmpty(userList)) {
                return QueryWrapper.create().eq("id", -1);
            }

            userIds = userList.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
        }

        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();


        QueryWrapper wrapper = QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .orderBy(sortField, "ascend".equals(sortOrder));

        if (CollUtil.isNotEmpty(userIds)) {
            wrapper.in("userId", userIds);
        } else {
            wrapper.eq("userId", userId);
        }
        return wrapper;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean removeById(Serializable id) {
        if (id == null) {
            return false;
        }
        long appId = Long.valueOf(id.toString());
        if (appId <= 0) {
            return false;
        }
        // 先删除关联的对话历史
        try {
            chatHistoryService.removeById(id);
        } catch (Exception e) {
            log.error("删除应用关联的对话历史失败：{}", e.getMessage());
        }
        // 再删除应用
        return super.removeById(appId);
    }


}
