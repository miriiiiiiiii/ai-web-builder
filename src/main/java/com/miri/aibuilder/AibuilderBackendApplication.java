package com.miri.aibuilder;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.miri.aibuilder.mapper")
public class AibuilderBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AibuilderBackendApplication.class, args);
    }

}
