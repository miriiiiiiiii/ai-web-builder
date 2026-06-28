package com.miri.aibuilder.controller;

import com.miri.aibuilder.common.BaseResponse;
import com.miri.aibuilder.common.ResultUtils;
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

