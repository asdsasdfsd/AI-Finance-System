package org.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {
    "org.example.backend.model"  // 只扫描原始model包
    // 排除 "org.example.backend.domain.aggregate" 避免冲突
})
@EnableJpaRepositories(basePackages = {
    "org.example.backend.repository"  // 只扫描原始repository包
    // 排除 "org.example.backend.domain.aggregate.**.repository"
})
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}