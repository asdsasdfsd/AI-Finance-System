// backend/src/main/java/org/example/backend/BackendApplication.java
package org.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {
    "org.example.backend.domain.aggregate",  // DDD聚合
    "org.example.backend.model"              // 共享实体(Role等)
})
@EnableJpaRepositories(basePackages = {
    "org.example.backend.domain.aggregate.company",
    "org.example.backend.domain.aggregate.transaction", 
    "org.example.backend.domain.aggregate.user",
    "org.example.backend.repository"  // 共享Repository
})
@ComponentScan(basePackages = {
    "org.example.backend.domain",
    "org.example.backend.application",
    "org.example.backend.infrastructure",
    "org.example.backend.config",
    "org.example.backend.security",
    "org.example.backend.util",
    "org.example.backend.exception",
    "org.example.backend.service.AuditLogService",  // 保留审计服务
    "org.example.backend.service.RoleService"       // 保留角色服务
})
public class BackendApplication {
    public static void main(String[] args) {
        System.out.println("🚀 启动AI财务管理系统 - DDD模式");
        SpringApplication.run(BackendApplication.class, args);
    }
}