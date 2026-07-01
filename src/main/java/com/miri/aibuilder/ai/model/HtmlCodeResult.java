package com.miri.aibuilder.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * html代码生成结果
 */
@Description("生成 Html 代码文件的结果")
@Data
public class HtmlCodeResult {

    @Description("Html代码")
    private String htmlCode;

    @Description("代码描述")
    private String description;
}
