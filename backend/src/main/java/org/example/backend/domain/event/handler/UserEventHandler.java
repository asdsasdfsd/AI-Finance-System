// backend/src/main/java/org/example/backend/domain/event/handler/UserEventHandler.java
package org.example.backend.domain.event.handler;

import org.example.backend.domain.event.UserCreatedEvent;
import org.example.backend.domain.event.CompanyCreatedEvent;
import org.example.backend.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户和公司事件处理器
 */
@Component

@Transactional
public class UserEventHandler {
    
    @Autowired
    private AuditLogService auditLogService;
    
    /**
     * 处理用户创建事件
     */
    @EventListener
    public void handleUserCreated(UserCreatedEvent event) {
        try {
            // 记录审计日志
            auditLogService.logAction(
                null,
                "USER_CREATED",
                "User",
                event.getUserId().toString(),
                String.format("创建用户：%s (%s)，公司ID：%d", 
                    event.getUsername(), event.getEmail(), event.getCompanyId()),
                "system"
            );
            
            // 其他业务逻辑
            // - 发送欢迎邮件
            // - 初始化用户配置
            // - 分配默认权限
            
            System.out.println("处理用户创建事件完成: " + event);
            
        } catch (Exception e) {
            System.err.println("处理用户创建事件失败: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * 处理公司创建事件
     */
    @EventListener
    public void handleCompanyCreated(CompanyCreatedEvent event) {
        try {
            // 记录审计日志
            auditLogService.logAction(
                null,
                "COMPANY_CREATED",
                "Company",
                event.getCompanyId().toString(),
                String.format("创建公司：%s (%s)，创建人：%d", 
                    event.getCompanyName(), event.getEmail(), event.getCreatedBy()),
                "system"
            );
            
            // 其他业务逻辑
            // - 初始化公司基础数据（科目、部门等）
            // - 创建默认预算
            // - 设置公司配置
            
            System.out.println("处理公司创建事件完成: " + event);
            
        } catch (Exception e) {
            System.err.println("处理公司创建事件失败: " + e.getMessage());
            throw e;
        }
    }
}
