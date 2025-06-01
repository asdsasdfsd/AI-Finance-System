package org.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {
    "org.example.backend.model"  // 只扫描传统ORM实体
    // 暂时排除DDD实体: "org.example.backend.domain.aggregate"
})
@EnableJpaRepositories(basePackages = {
    "org.example.backend.repository"  // 只扫描传统Repository
    // 暂时排除DDD仓库: "org.example.backend.domain.aggregate.*.repository"
})
@ComponentScan(basePackages = {
    "org.example.backend",
    // 排除DDD聚合扫描，避免冲突
    "!org.example.backend.domain.aggregate"
})
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}