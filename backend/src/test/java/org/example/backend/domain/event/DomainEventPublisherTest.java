// backend/src/test/java/org/example/backend/domain/event/DomainEventPublisherTest.java
package org.example.backend.domain.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DomainEventPublisher
 * 
 * Tests the domain event publishing functionality including:
 * 1. Single event publishing
 * 2. Multiple events publishing
 * 3. Conditional event publishing
 * 4. Error handling
 */
public class DomainEventPublisherTest extends DomainEventTestBase {
    
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    
    @InjectMocks
    private DomainEventPublisher domainEventPublisher;
    
    private TestDomainEvent testEvent;
    
    @BeforeEach
    void setUpPublisherTests() {
        testEvent = new TestDomainEvent("Test message");
    }
    
    @Test
    @DisplayName("Should publish single domain event successfully")
    void shouldPublishSingleDomainEventSuccessfully() {
        // When
        domainEventPublisher.publish(testEvent);
        
        // Then
        verify(applicationEventPublisher, times(1)).publishEvent(testEvent);
    }
    
    @Test
    @DisplayName("Should publish multiple domain events successfully")
    void shouldPublishMultipleDomainEventsSuccessfully() {
        // Given
        TestDomainEvent event1 = new TestDomainEvent("First event");
        TestDomainEvent event2 = new TestDomainEvent("Second event");
        List<Object> events = Arrays.asList(event1, event2);
        
        // When
        domainEventPublisher.publishAll(events);
        
        // Then
        verify(applicationEventPublisher, times(1)).publishEvent(event1);
        verify(applicationEventPublisher, times(1)).publishEvent(event2);
        verify(applicationEventPublisher, times(2)).publishEvent(any(TestDomainEvent.class));
    }
    
    @Test
    @DisplayName("Should handle empty event list gracefully")
    void shouldHandleEmptyEventListGracefully() {
        // Given
        List<Object> emptyEvents = Collections.emptyList();
        
        // When
        domainEventPublisher.publishAll(emptyEvents);
        
        // Then
        verify(applicationEventPublisher, never()).publishEvent(any());
    }
    
    @Test
    @DisplayName("Should handle null event list gracefully")
    void shouldHandleNullEventListGracefully() {
        // When
        domainEventPublisher.publishAll(null);
        
        // Then
        verify(applicationEventPublisher, never()).publishEvent(any());
    }
    
    @Test
    @DisplayName("Should publish event when condition is true")
    void shouldPublishEventWhenConditionIsTrue() {
        // When
        domainEventPublisher.publishIf(testEvent, true);
        
        // Then
        verify(applicationEventPublisher, times(1)).publishEvent(testEvent);
    }
    
    @Test
    @DisplayName("Should not publish event when condition is false")
    void shouldNotPublishEventWhenConditionIsFalse() {
        // When
        domainEventPublisher.publishIf(testEvent, false);
        
        // Then
        verify(applicationEventPublisher, never()).publishEvent(any());
    }
    
    @Test
    @DisplayName("Should throw exception when publishing null event")
    void shouldThrowExceptionWhenPublishingNullEvent() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> domainEventPublisher.publish(null)
        );
        
        assertEquals("Domain event cannot be null", exception.getMessage());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }
    
    @Test
    @DisplayName("Should handle ApplicationEventPublisher exception")
    void shouldHandleApplicationEventPublisherException() {
        // Given
        RuntimeException publishException = new RuntimeException("Publishing failed");
        doThrow(publishException).when(applicationEventPublisher).publishEvent(testEvent);
        
        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> domainEventPublisher.publish(testEvent)
        );
        
        assertEquals("Failed to publish domain event", exception.getMessage());
        assertEquals(publishException, exception.getCause());
        verify(applicationEventPublisher, times(1)).publishEvent(testEvent);
    }
    
    @Test
    @DisplayName("Should publish different types of domain events")
    void shouldPublishDifferentTypesOfDomainEvents() {
        // Given
        CompanyCreatedEvent companyEvent = new CompanyCreatedEvent(
            TEST_COMPANY_ID, "Test Company", "test@company.com", TEST_USER_ID
        );
        TransactionCreatedEvent transactionEvent = new TransactionCreatedEvent(
            TEST_TRANSACTION_ID, 
            org.example.backend.domain.aggregate.transaction.TransactionAggregate.TransactionType.INCOME,
            createTestMoney(), 
            TEST_COMPANY_ID
        );
        
        // When
        domainEventPublisher.publish(companyEvent);
        domainEventPublisher.publish(transactionEvent);
        
        // Then
        verify(applicationEventPublisher, times(1)).publishEvent(companyEvent);
        verify(applicationEventPublisher, times(1)).publishEvent(transactionEvent);
    }
    
    @Test
    @DisplayName("Should stop publishing when one event fails")
    void shouldStopPublishingWhenOneEventFails() {
        // Given
        TestDomainEvent event1 = new TestDomainEvent("First event");
        TestDomainEvent event2 = new TestDomainEvent("Second event - will fail");
        TestDomainEvent event3 = new TestDomainEvent("Third event");
        
        // Configure mock to throw exception only for event2
        doNothing().when(applicationEventPublisher).publishEvent(event1);
        doThrow(new RuntimeException("Publishing failed")).when(applicationEventPublisher).publishEvent(event2);
        
        List<Object> events = Arrays.asList(event1, event2, event3);
        
        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> domainEventPublisher.publishAll(events)
        );
        
        // Verify that event1 was published successfully
        verify(applicationEventPublisher, times(1)).publishEvent(event1);
        // Verify that event2 caused the exception
        verify(applicationEventPublisher, times(1)).publishEvent(event2);
        // Note: event3 is not published because the exception stops the loop
        verify(applicationEventPublisher, never()).publishEvent(event3);
    }
    
    // Test domain event implementation
    private static class TestDomainEvent extends DomainEvent {
        private final String message;
        
        public TestDomainEvent(String message) {
            super();
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
        
        @Override
        public String toString() {
            return String.format("TestDomainEvent{message='%s', eventId='%s'}", 
                               message, getEventId());
        }
    }
}