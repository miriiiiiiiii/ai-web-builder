package com.miri.aibuilder.common;

import com.miri.aibuilder.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用响应类，用于封装API返回结果
 */
@Data
public class BaseResponse<T> implements Serializable {

    // 响应状态码
    private int code;

    // 响应数据
    private T data;

    // 响应消息，用于描述响应状态或错误信息
    private String message;


    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }


    public BaseResponse(int code, T data) {
        this(code, data, "");
    }


    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
