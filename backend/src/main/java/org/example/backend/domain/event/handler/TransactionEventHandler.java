
// backend/src/main/java/org/example/backend/domain/event/handler/TransactionEventHandler.java
package org.example.backend.domain.event.handler;

import org.example.backend.domain.event.TransactionCreatedEvent;
import org.example.backend.domain.event.TransactionApprovedEvent;
import org.example.backend.domain.event.TransactionCancelledEvent;
import org.example.backend.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 交易事件处理器
 * 
 * 职责：
 * 1. 处理交易相关的领域事件
 * 2. 执行事件驱动的业务逻辑
 * 3. 维护数据一致性
 */
@Component
@Profile("ddd")
@Transactional
public class TransactionEventHandler {
    
    @Autowired
    private AuditLogService auditLogService;
    
    // 注意：这些服务可能还不存在，这里作为示例
    // @Autowired
    // private NotificationService notificationService;
    
    // @Autowired
    // private ReportingService reportingService;
    
    /**
     * 处理交易创建事件
     */
    @EventListener
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        try {
            // 1. 记录审计日志
            auditLogService.logAction(
                null, // 需要用户对象，这里简化处理
                "TRANSACTION_CREATED",
                "Transaction",
                event.getTransactionId().toString(),
                String.format("创建%s交易，金额：%s", 
                    event.getTransactionType().getDisplayName(), 
                    event.getAmount()),
                "system"
            );
            
            // 2. 发送通知（如果需要）
            // notificationService.notifyTransactionCreated(event);
            
            // 3. 更新统计数据
            // reportingService.updateTransactionStatistics(event);
            
            System.out.println("处理交易创建事件完成: " + event);
            
        } catch (Exception e) {
            System.err.println("处理交易创建事件失败: " + e.getMessage());
            // 在实际项目中，这里可能需要补偿机制
            throw e;
        }
    }
    
    /**
     * 处理交易批准事件
     */
    @EventListener
    public void handleTransactionApproved(TransactionApprovedEvent event) {
        try {
            // 1. 记录审计日志
            auditLogService.logAction(
                null,
                "TRANSACTION_APPROVED",
                "Transaction",
                event.getTransactionId().toString(),
                String.format("批准交易，金额：%s，批准人：%d", 
                    event.getAmount(), 
                    event.getApprovedBy()),
                "system"
            );
            
            // 2. 触发会计分录生成
            // accountingService.generateJournalEntry(event);
            
            // 3. 更新预算执行情况
            // budgetService.updateBudgetExecution(event);
            
            // 4. 发送批准通知
            // notificationService.notifyTransactionApproved(event);
            
            System.out.println("处理交易批准事件完成: " + event);
            
        } catch (Exception e) {
            System.err.println("处理交易批准事件失败: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * 处理交易取消事件
     */
    @EventListener
    public void handleTransactionCancelled(TransactionCancelledEvent event) {
        try {
            // 1. 记录审计日志
            String details = event.getReason() != null ? 
                "取消交易，原因：" + event.getReason() : "取消交易";
                
            auditLogService.logAction(
                null,
                "TRANSACTION_CANCELLED",
                "Transaction",
                event.getTransactionId().toString(),
                details,
                "system"
            );
            
            // 2. 撤销相关的会计分录
            // accountingService.reverseJournalEntry(event);
            
            // 3. 更新预算执行情况
            // budgetService.reverseBudgetExecution(event);
            
            // 4. 发送取消通知
            // notificationService.notifyTransactionCancelled(event);
            
            System.out.println("处理交易取消事件完成: " + event);
            
        } catch (Exception e) {
            System.err.println("处理交易取消事件失败: " + e.getMessage());
            throw e;
        }
    }
}
