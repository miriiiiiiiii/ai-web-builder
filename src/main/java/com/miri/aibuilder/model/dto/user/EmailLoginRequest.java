package com.miri.aibuilder.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmailLoginRequest implements Serializable {
    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 用户邮箱
     */
    private String userEmail;

    /**
     * 验证码
     */
    private String code;
}