package com.miri.aibuilder.model.dto.user;

import com.miri.aibuilder.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户搜索请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    /**
     * 关键词（账号 or 邮箱）
     */
    private String keyword;

    private static final long serialVersionUID = 1L;
}
