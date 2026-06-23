package com.miri.aibuilderbackend.controller;

import com.miri.aibuilderbackend.common.BaseResponse;
import com.miri.aibuilderbackend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class HelloController {
    @GetMapping("/")
    public BaseResponse<String> hello() {
        return ResultUtils.success("Hello!");
    }
}

