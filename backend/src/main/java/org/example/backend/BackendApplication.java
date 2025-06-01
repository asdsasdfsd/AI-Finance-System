// backend/src/main/java/org/example/backend/BackendApplication.java
package org.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring Boot Application Main Class
 * 
 * 重要：移除了静态的EntityScan和EnableJpaRepositories注解
 * 改为通过ProfileBasedConfiguration动态配置，避免Profile间冲突
 */
@SpringBootApplication
// 移除静态扫描配置，改为Profile动态配置
@ComponentScan(basePackages = {
    "org.example.backend.config" // 只扫描配置包，其他由Profile配置决定
})
public class BackendApplication {

    public static void main(String[] args) {
        // 启动前显示Profile信息
        System.out.println("🚀 启动AI财务管理系统...");
        System.out.println("📋 支持的Profile模式:");
        System.out.println("   - orm: 传统ORM架构模式");
        System.out.println("   - ddd: 领域驱动设计模式");
        System.out.println("⚙️ 当前Profile: " + System.getProperty("spring.profiles.active", "orm(默认)"));
        
        SpringApplication.run(BackendApplication.class, args);
    }
}