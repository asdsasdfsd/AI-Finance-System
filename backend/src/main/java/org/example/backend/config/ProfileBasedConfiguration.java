// backend/src/main/java/org/example/backend/config/ProfileBasedConfiguration.java
package org.example.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Profile-based Configuration - 简化版本
 * 
 * 解决EntityManagerFactory冲突问题
 */
@Configuration
public class ProfileBasedConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfileBasedConfiguration.class);
    
    /**
     * ORM模式配置 - 传统ORM实体和服务
     */
    @Configuration
    @Profile("orm")
    @EntityScan(basePackages = {
        "org.example.backend.model" // 只扫描传统ORM实体
    })
    @EnableJpaRepositories(basePackages = {
        "org.example.backend.repository" // 扫描传统Repository
    })
    @ComponentScan(basePackages = {
        "org.example.backend.service",           // 传统服务层
        "org.example.backend.controller",        // 传统控制器
        "org.example.backend.config",            // 配置类
        "org.example.backend.security",          // 安全组件
        "org.example.backend.util",              // 工具类
        "org.example.backend.dto",               // DTO类
        "org.example.backend.exception"          // 异常处理
    })
    static class OrmConfiguration {
        
        @Bean
        public ProfileCheck ormProfileCheck() {
            logger.info("✅ ORM模式已激活 - 使用传统ORM架构");
            logger.info("📊 实体扫描: org.example.backend.model");
            logger.info("🗃️ Repository扫描: org.example.backend.repository");
            return new ProfileCheck("orm");
        }
    }
    
    /**
     * DDD模式配置 - 只使用DDD聚合，排除传统实体
     */
    @Configuration
    @Profile("ddd")
    @EntityScan(basePackages = {
        "org.example.backend.domain.aggregate" // 只扫描DDD聚合
        // 注意：不再包含org.example.backend.model，避免冲突
    })
    @EnableJpaRepositories(
        basePackages = {
            "org.example.backend.domain.aggregate.company",     
            "org.example.backend.domain.aggregate.transaction", 
            "org.example.backend.domain.aggregate.user",        
            "org.example.backend.repository" // 保留Role等共享Repository
        },
        excludeFilters = @ComponentScan.Filter(
            pattern = {
                "org.example.backend.repository.CompanyRepository",
                "org.example.backend.repository.TransactionRepository", 
                "org.example.backend.repository.UserRepository"
            }
        )
    )
    @ComponentScan(basePackages = {
        "org.example.backend.domain",                    // 领域层
        "org.example.backend.application",               // 应用服务层
        "org.example.backend.infrastructure",            // 基础设施层
        "org.example.backend.config",                    // 配置类
        "org.example.backend.security",                  // 安全组件（共享）
        "org.example.backend.util",                      // 工具类（共享）
        "org.example.backend.dto",                       // DTO类（共享）
        "org.example.backend.exception",                 // 异常处理（共享）
        "org.example.backend.service.AuditLogService",   // 保留审计服务
        "org.example.backend.service.RoleService"        // 保留角色服务
    })
    static class DddConfiguration {
        
        @Bean
        public ProfileCheck dddProfileCheck() {
            logger.info("✅ DDD模式已激活 - 使用领域驱动设计架构");
            logger.info("🏗️ 聚合扫描: org.example.backend.domain.aggregate");
            logger.info("📱 应用服务: org.example.backend.application");
            logger.info("🔧 基础设施: org.example.backend.infrastructure");
            logger.info("⚠️ 排除传统实体以避免映射冲突");
            return new ProfileCheck("ddd");
        }
    }
    
    /**
     * Profile检查工具类
     */
    public static class ProfileCheck {
        private final String activeProfile;
        
        public ProfileCheck(String activeProfile) {
            this.activeProfile = activeProfile;
            logger.info("🎯 当前激活Profile: {}", activeProfile);
        }
        
        public String getActiveProfile() {
            return activeProfile;
        }
    }
}