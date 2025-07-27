// backend/src/test/java/org/example/backend/domain/event/config/DomainEventTestConfig.java
package org.example.backend.domain.event.config;

import org.example.backend.service.AuditLogService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

/**
 * Test configuration for domain event testing
 * 
 * Provides test-specific beans and configurations for domain event tests including:
 * 1. Mock beans for external dependencies
 * 2. Simplified audit logging for testing
 * 3. Event handling verification utilities
 */
@TestConfiguration
@Profile("test")
public class DomainEventTestConfig {
    
    /**
     * Mock audit log service for testing
     * Prevents actual database calls during unit tests
     */
    @Bean
    @Primary
    public AuditLogService auditLogService() {
        AuditLogService mockService = Mockito.mock(AuditLogService.class);

        // Configure default behavior to avoid NullPointerExceptions
        doNothing().when(mockService).logAction(
            any(), any(), any(), any(), any(), any()
        );

        return mockService;
    }
    
    /**
     * Test event verification helper
     * Provides utilities for verifying event publishing in tests
     */
    @Bean
    public TestEventVerifier testEventVerifier() {
        return new TestEventVerifier();
    }
    
    /**
     * Helper class for verifying events in tests
     */
    public static class TestEventVerifier {
        
        /**
         * Verify that an event has the required basic properties
         */
        public void verifyBasicEventProperties(Object event, String expectedEventType) {
            if (event instanceof org.example.backend.domain.event.DomainEvent) {
                org.example.backend.domain.event.DomainEvent domainEvent = 
                    (org.example.backend.domain.event.DomainEvent) event;
                
                assert domainEvent.getEventId() != null : "Event ID should not be null";
                assert domainEvent.getOccurredOn() != null : "Occurred time should not be null";
                assert expectedEventType.equals(domainEvent.getEventType()) : 
                    "Event type should match expected type";
            }
        }
        
        /**
         * Verify that event timing is reasonable (occurred recently)
         */
        public void verifyEventTiming(org.example.backend.domain.event.DomainEvent event) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDateTime fiveSecondsAgo = now.minusSeconds(5);
            
            assert event.getOccurredOn().isAfter(fiveSecondsAgo) : 
                "Event should have occurred recently";
            assert !event.getOccurredOn().isAfter(now.plusSeconds(1)) : 
                "Event should not be in the future";
        }
        
        /**
         * Verify event ID format (basic UUID validation)
         */
        public void verifyEventIdFormat(String eventId) {
            assert eventId != null : "Event ID should not be null";
            assert eventId.contains("-") : "Event ID should be in UUID format";
            assert eventId.length() >= 32 : "Event ID should be of appropriate length";
        }
    }
}