package com.miri.aibuilder;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.miri.aibuilder.mapper")
public class AibuilderBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AibuilderBackendApplication.class, args);
    }

}
