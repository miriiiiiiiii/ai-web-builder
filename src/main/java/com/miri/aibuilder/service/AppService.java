package com.miri.aibuilder.service;

import com.miri.aibuilder.model.dto.app.AppQueryRequest;
import com.miri.aibuilder.model.entity.App;
import com.miri.aibuilder.model.entity.User;
import com.miri.aibuilder.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/miriiiiiiiii">程序员小马</a>
 */
public interface AppService extends IService<App> {

    /**
     * 获取应用封装类
     * @param app
     * @return
     */
    AppVO getAppVO(App app);

    /**
     * 构造应用查询条件
     * @param appQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取应用封装类列表
     * @param appList
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 聊天生成代码
     * @param userMessage 用户消息
     * @param appId 应用ID（流式SSE）
     * @param loginUser 当前登录用户
     * @return
     */
    Flux<String> chatToGenCode(String userMessage, Long appId, User loginUser);

    /**
     * 部署应用
     * @param appId 应用ID
     * @param loginUser 当前登录用户
     * @return 可访问的部署路径
     */
    String deployApp(Long appId, User loginUser);
}
