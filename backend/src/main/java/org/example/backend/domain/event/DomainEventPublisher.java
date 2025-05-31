// backend/src/main/java/org/example/backend/domain/event/DomainEventPublisher.java
package org.example.backend.domain.event;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Domain Event Publisher - Fixed for type compatibility
 * 
 * Responsibilities:
 * 1. Publish domain events to Spring application context
 * 2. Ensure event publishing reliability
 * 3. Provide unified event publishing interface
 */
@Component
public class DomainEventPublisher {
    
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    
    /**
     * Publish single domain event
     * 
     * @param event Domain event to publish
     */
    public void publish(Object event) {
        if (event == null) {
            throw new IllegalArgumentException("Domain event cannot be null");
        }
        
        try {
            applicationEventPublisher.publishEvent(event);
            System.out.println("Domain event published: " + event);
        } catch (Exception e) {
            System.err.println("Failed to publish domain event: " + event + ", error: " + e.getMessage());
            // In production, this might need more complex error handling
            // such as dead letter queue, retry mechanism, etc.
            throw new RuntimeException("Failed to publish domain event", e);
        }
    }
    
    /**
     * Publish multiple domain events
     * 
     * @param events List of domain events to publish
     */
    public void publishAll(List<Object> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        
        for (Object event : events) {
            publish(event);
        }
    }
    
    /**
     * Publish domain event if condition is met
     * 
     * @param event Domain event to publish
     * @param condition Condition to check before publishing
     */
    public void publishIf(Object event, boolean condition) {
        if (condition) {
            publish(event);
        }
    }
}