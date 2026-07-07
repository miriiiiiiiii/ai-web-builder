package com.miri.aibuilder.service;



import com.miri.aibuilder.model.dto.chathistory.ChatHistoryQueryRequest;
import com.miri.aibuilder.model.entity.ChatHistory;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author <a href="https://github.com/miriiiiiiiii">程序员小马</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {
    /**
     * 添加对话历史
     * @param appId 应用ID
     * @param message 消息
     * @param messageType 消息类型
     * @param userId 用户ID
     * @return
     */
    boolean addChatHistory(Long appId, String message, String messageType, Long userId);

    /**
     * 关联删除对话历史
     * @param appId
     * @return
     */
    boolean deleteByAppId(Long appId);

    /**
     * 构造查询条件
     * @param chatHistoryQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    Page<ChatHistory> listChatHistory(Long appId,int pageSize, LocalDateTime  lastCreateTime);
}
