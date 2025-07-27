// backend/src/test/java/org/example/backend/domain/event/DomainEventTestBase.java
package org.example.backend.domain.event;

import org.example.backend.domain.valueobject.Money;
import org.example.backend.domain.valueobject.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base class for domain event unit tests
 * 
 * Provides common utilities for testing domain events, including:
 * 1. Event creation helpers
 * 2. Event verification utilities
 * 3. Async event handling support
 * 4. Test data factories
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public abstract class DomainEventTestBase {
    
    // Common test constants
    protected static final Integer TEST_COMPANY_ID = 1;
    protected static final Integer TEST_USER_ID = 100;
    protected static final Integer TEST_TRANSACTION_ID = 200;
    protected static final String TEST_CURRENCY = "CNY";
    protected static final LocalDate TEST_DATE = LocalDate.of(2024, 1, 15);
    protected static final LocalDateTime TEST_DATETIME = LocalDateTime.of(2024, 1, 15, 10, 30);
    
    // Event handling test utilities
    protected CountDownLatch eventLatch;
    protected AtomicBoolean eventReceived;
    protected AtomicReference<Object> capturedEvent;
    
    @BeforeEach
    void setUpEventTests() {
        // Initialize event testing utilities
        eventLatch = new CountDownLatch(1);
        eventReceived = new AtomicBoolean(false);
        capturedEvent = new AtomicReference<>();
    }
    
    // Factory methods for test data
    protected TenantId createTestTenantId() {
        return TenantId.of(TEST_COMPANY_ID);
    }
    
    protected Money createTestMoney() {
        return Money.of(new BigDecimal("1000.00"), TEST_CURRENCY);
    }
    
    protected Money createMoney(String amount) {
        return Money.of(new BigDecimal(amount), TEST_CURRENCY);
    }
    
    // Event verification utilities
    protected void assertEventReceived(String eventType) {
        assertTrue(eventReceived.get(), 
            String.format("Event of type %s should have been received", eventType));
    }
    
    protected void assertEventNotReceived() {
        assertFalse(eventReceived.get(), "No event should have been received");
    }
    
    protected void assertEventType(Object event, Class<?> expectedType) {
        assertNotNull(event, "Event should not be null");
        assertEquals(expectedType, event.getClass(), 
            String.format("Event should be of type %s", expectedType.getSimpleName()));
    }
    
    protected void assertDomainEventBasics(DomainEvent event) {
        assertNotNull(event, "Domain event should not be null");
        assertNotNull(event.getEventId(), "Event ID should not be null");
        assertNotNull(event.getOccurredOn(), "Event occurrence time should not be null");
        assertNotNull(event.getEventType(), "Event type should not be null");
        
        // Verify event occurred recently (within last 5 seconds)
        LocalDateTime fiveSecondsAgo = LocalDateTime.now().minusSeconds(5);
        assertTrue(event.getOccurredOn().isAfter(fiveSecondsAgo),
            "Event should have occurred recently");
    }
    
    protected void assertValidCreationTime(LocalDateTime timestamp) {
        assertNotNull(timestamp, "Creation timestamp should not be null");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fiveSecondsAgo = now.minusSeconds(5);
        assertTrue(timestamp.isAfter(fiveSecondsAgo) && !timestamp.isAfter(now),
            "Creation timestamp should be recent and not in the future");
    }
    
    // Async event handling support
    protected void waitForEvent(long timeoutSeconds) throws InterruptedException {
        boolean eventReceived = eventLatch.await(timeoutSeconds, TimeUnit.SECONDS);
        assertTrue(eventReceived, 
            String.format("Event should be received within %d seconds", timeoutSeconds));
    }
    
    protected void signalEventReceived(Object event) {
        capturedEvent.set(event);
        eventReceived.set(true);
        eventLatch.countDown();
    }
    
    @SuppressWarnings("unchecked")
    protected <T> T getCapturedEvent(Class<T> eventType) {
        Object event = capturedEvent.get();
        assertNotNull(event, "No event was captured");
        assertInstanceOf(eventType, event, 
            String.format("Captured event should be of type %s", eventType.getSimpleName()));
        return (T) event;
    }
    
    // Common validation methods
    protected void assertValidEventId(String eventId) {
        assertNotNull(eventId, "Event ID should not be null");
        assertFalse(eventId.trim().isEmpty(), "Event ID should not be empty");
        // UUID format check (simplified)
        assertTrue(eventId.contains("-"), "Event ID should be in UUID format");
    }
    
    protected void assertValidCompanyId(Integer companyId) {
        assertNotNull(companyId, "Company ID should not be null");
        assertTrue(companyId > 0, "Company ID should be positive");
    }
    
    protected void assertValidUserId(Integer userId) {
        assertNotNull(userId, "User ID should not be null");
        assertTrue(userId > 0, "User ID should be positive");
    }
    
    protected void assertValidTransactionId(Integer transactionId) {
        assertNotNull(transactionId, "Transaction ID should not be null");
        assertTrue(transactionId > 0, "Transaction ID should be positive");
    }
    
    protected void assertValidMoney(Money money) {
        assertNotNull(money, "Money should not be null");
        assertNotNull(money.getAmount(), "Money amount should not be null");
        assertNotNull(money.getCurrency(), "Money currency should not be null");
        assertTrue(money.getAmount().compareTo(BigDecimal.ZERO) >= 0, 
            "Money amount should not be negative");
    }
}