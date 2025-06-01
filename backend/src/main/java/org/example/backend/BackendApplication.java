package org.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {
    "org.example.backend.model",  // 原始 model 包
    "org.example.backend.domain.aggregate"  // DDD 聚合实体
})
@EnableJpaRepositories(basePackages = {
    "org.example.backend.repository",  // 原始 repository 包
    "org.example.backend.domain.aggregate.*.repository",  // DDD 聚合仓库，虽然不存在但为了保险
    "org.example.backend.domain.aggregate.**"  // 扫描所有 DDD 聚合仓库
})
@ComponentScan(basePackages = {
    "org.example.backend"  // 扫描所有包确保 DDD 组件被发现
})
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}