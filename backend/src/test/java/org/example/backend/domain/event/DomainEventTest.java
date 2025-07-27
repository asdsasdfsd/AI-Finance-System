// backend/src/test/java/org/example/backend/domain/event/DomainEventTest.java
package org.example.backend.domain.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DomainEvent base class
 * 
 * Tests the fundamental behavior of domain events including:
 * 1. Event ID generation
 * 2. Timestamp assignment
 * 3. Event type determination
 */
public class DomainEventTest extends DomainEventTestBase {
    
    @Test
    @DisplayName("Should create domain event with valid basic properties")
    void shouldCreateDomainEventWithValidBasicProperties() {
        // When
        TestDomainEvent event = new TestDomainEvent("Test message");
        
        // Then
        assertDomainEventBasics(event);
        assertEquals("TestDomainEvent", event.getEventType());
    }
    
    @Test
    @DisplayName("Should generate unique event IDs for different events")
    void shouldGenerateUniqueEventIds() {
        // When
        TestDomainEvent event1 = new TestDomainEvent("First event");
        TestDomainEvent event2 = new TestDomainEvent("Second event");
        
        // Then
        assertNotEquals(event1.getEventId(), event2.getEventId(), 
            "Event IDs should be unique");
        assertValidEventId(event1.getEventId());
        assertValidEventId(event2.getEventId());
    }
    
    @Test
    @DisplayName("Should set occurred time close to creation time")
    void shouldSetOccurredTimeCloseToCreationTime() {
        // Given
        LocalDateTime beforeCreation = LocalDateTime.now();
        
        // When
        TestDomainEvent event = new TestDomainEvent("Time test");
        
        // Then
        LocalDateTime afterCreation = LocalDateTime.now();
        assertTrue(event.getOccurredOn().isAfter(beforeCreation.minusSeconds(1)), 
            "Event occurrence time should be after creation start");
        assertTrue(event.getOccurredOn().isBefore(afterCreation.plusSeconds(1)), 
            "Event occurrence time should be before creation end");
    }
    
    @Test
    @DisplayName("Should derive event type from class name")
    void shouldDeriveEventTypeFromClassName() {
        // When
        TestDomainEvent event = new TestDomainEvent("Type test");
        AnotherTestEvent anotherEvent = new AnotherTestEvent();
        
        // Then
        assertEquals("TestDomainEvent", event.getEventType());
        assertEquals("AnotherTestEvent", anotherEvent.getEventType());
    }
    
    // Test domain event implementations
    private static class TestDomainEvent extends DomainEvent {
        private final String message;
        
        public TestDomainEvent(String message) {
            super();
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    private static class AnotherTestEvent extends DomainEvent {
        public AnotherTestEvent() {
            super();
        }
    }
}