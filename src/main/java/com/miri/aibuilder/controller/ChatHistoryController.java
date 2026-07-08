package com.miri.aibuilder.controller;


import com.miri.aibuilder.annotation.AuthCheck;
import com.miri.aibuilder.common.BaseResponse;
import com.miri.aibuilder.common.ResultUtils;
import com.miri.aibuilder.constant.UserConstant;
import com.miri.aibuilder.exception.ErrorCode;
import com.miri.aibuilder.exception.ThrowUtils;
import com.miri.aibuilder.model.dto.chathistory.ChatHistoryQueryRequest;
import com.miri.aibuilder.model.entity.ChatHistory;
import com.miri.aibuilder.model.entity.User;
import com.miri.aibuilder.service.ChatHistoryService;
import com.miri.aibuilder.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 对话历史 控制层。
 *
 * @author <a href="https://github.com/miriiiiiiiii">程序员小马</a>
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private UserService userService;

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * 分页查询应用对话历史（游标查询）
     * @param appId 应用ID
     * @param pageSize 页面大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @param request
     * @return
     */
    @GetMapping("/app/{appId}")
    public BaseResponse<Page<ChatHistory>> listAppChatHistory(@PathVariable Long appId,
                                                                  @RequestParam(defaultValue = "10") int pageSize,
                                                                  @RequestParam(required = false)LocalDateTime  lastCreateTime,
                                                                  HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 分页查询
        Page<ChatHistory> chatHistoryPage = chatHistoryService.listChatHistory(appId, pageSize, lastCreateTime);
        return ResultUtils.success(chatHistoryPage);
    }


    /**
     * 管理员分页查询所有对话历史
     * @param chatHistoryQueryRequest 查询请求
     * @return 对话历史分页
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> listAllChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = chatHistoryQueryRequest.getPageNum();
        long pageSize = chatHistoryQueryRequest.getPageSize();
        // 查询数据
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> result = chatHistoryService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(result);
    }

}
