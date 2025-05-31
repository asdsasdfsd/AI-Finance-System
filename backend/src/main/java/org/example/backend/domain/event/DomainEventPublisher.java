// backend/src/main/java/org/example/backend/domain/event/DomainEventPublisher.java
package org.example.backend.domain.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 领域事件发布器
 * 
 * 职责：
 * 1. 发布领域事件到Spring应用上下文
 * 2. 确保事件发布的可靠性
 * 3. 提供统一的事件发布接口
 */
@Component
public class DomainEventPublisher {
    
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    
    /**
     * 发布领域事件
     * 
     * @param event 要发布的领域事件
     */
    public void publish(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("领域事件不能为null");
        }
        
        try {
            applicationEventPublisher.publishEvent(event);
            System.out.println("领域事件已发布: " + event);
        } catch (Exception e) {
            System.err.println("发布领域事件失败: " + event + ", 错误: " + e.getMessage());
            // 在实际项目中，这里可能需要更复杂的错误处理
            // 比如写入死信队列、重试机制等
            throw new RuntimeException("发布领域事件失败", e);
        }
    }
    
    /**
     * 批量发布领域事件
     * 
     * @param events 要发布的领域事件列表
     */
    public void publishAll(java.util.List<DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        
        for (DomainEvent event : events) {
            publish(event);
        }
    }
}
