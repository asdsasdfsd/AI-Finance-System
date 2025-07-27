// backend/src/test/java/org/example/backend/domain/event/TransactionEventTest.java
package org.example.backend.domain.event;

import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.valueobject.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for transaction-related domain events
 * 
 * Tests the creation and properties of:
 * 1. TransactionCreatedEvent
 * 2. TransactionApprovedEvent
 * 3. TransactionCancelledEvent
 */
public class TransactionEventTest extends DomainEventTestBase {
    
    @Test
    @DisplayName("Should create TransactionCreatedEvent with valid properties")
    void shouldCreateTransactionCreatedEventWithValidProperties() {
        // Given
        Integer transactionId = TEST_TRANSACTION_ID;
        TransactionAggregate.TransactionType transactionType = TransactionAggregate.TransactionType.INCOME;
        Money amount = createTestMoney();
        Integer companyId = TEST_COMPANY_ID;
        
        // When
        TransactionCreatedEvent event = new TransactionCreatedEvent(
            transactionId, transactionType, amount, companyId
        );
        
        // Then
        assertDomainEventBasics(event);
        assertEquals("TransactionCreatedEvent", event.getEventType());
        assertEquals(transactionId, event.getTransactionId());
        assertEquals(transactionType, event.getTransactionType());
        assertEquals(amount, event.getAmount());
        assertEquals(companyId, event.getCompanyId());
        
        // Validate individual components
        assertValidTransactionId(event.getTransactionId());
        assertValidMoney(event.getAmount());
        assertValidCompanyId(event.getCompanyId());
    }
    
    @Test
    @DisplayName("Should create TransactionCreatedEvent for expense transaction")
    void shouldCreateTransactionCreatedEventForExpenseTransaction() {
        // Given
        Integer transactionId = TEST_TRANSACTION_ID;
        TransactionAggregate.TransactionType transactionType = TransactionAggregate.TransactionType.EXPENSE;
        Money amount = createMoney("500.00");
        Integer companyId = TEST_COMPANY_ID;
        
        // When
        TransactionCreatedEvent event = new TransactionCreatedEvent(
            transactionId, transactionType, amount, companyId
        );
        
        // Then
        assertEquals(TransactionAggregate.TransactionType.EXPENSE, event.getTransactionType());
        assertEquals(amount, event.getAmount());
    }
    
    @Test
    @DisplayName("Should create TransactionApprovedEvent with valid properties")
    void shouldCreateTransactionApprovedEventWithValidProperties() {
        // Given
        Integer transactionId = TEST_TRANSACTION_ID;
        Money amount = createTestMoney();
        Integer companyId = TEST_COMPANY_ID;
        Integer approvedBy = TEST_USER_ID;
        
        // When
        TransactionApprovedEvent event = new TransactionApprovedEvent(
            transactionId, amount, companyId, approvedBy
        );
        
        // Then
        assertDomainEventBasics(event);
        assertEquals("TransactionApprovedEvent", event.getEventType());
        assertEquals(transactionId, event.getTransactionId());
        assertEquals(amount, event.getAmount());
        assertEquals(companyId, event.getCompanyId());
        assertEquals(approvedBy, event.getApprovedBy());
        
        // Validate individual components
        assertValidTransactionId(event.getTransactionId());
        assertValidMoney(event.getAmount());
        assertValidCompanyId(event.getCompanyId());
        assertValidUserId(event.getApprovedBy());
    }
    
    @Test
    @DisplayName("Should create TransactionCancelledEvent with valid properties")
    void shouldCreateTransactionCancelledEventWithValidProperties() {
        // Given
        Integer transactionId = TEST_TRANSACTION_ID;
        Integer companyId = TEST_COMPANY_ID;
        String reason = "Duplicate transaction detected";
        
        // When
        TransactionCancelledEvent event = new TransactionCancelledEvent(
            transactionId, companyId, reason
        );
        
        // Then
        assertDomainEventBasics(event);
        assertEquals("TransactionCancelledEvent", event.getEventType());
        assertEquals(transactionId, event.getTransactionId());
        assertEquals(companyId, event.getCompanyId());
        assertEquals(reason, event.getReason());
        
        // Validate individual components
        assertValidTransactionId(event.getTransactionId());
        assertValidCompanyId(event.getCompanyId());
        assertNotNull(event.getReason());
        assertFalse(event.getReason().trim().isEmpty());
    }
    
    @Test
    @DisplayName("Should create TransactionCancelledEvent without reason")
    void shouldCreateTransactionCancelledEventWithoutReason() {
        // Given
        Integer transactionId = TEST_TRANSACTION_ID;
        Integer companyId = TEST_COMPANY_ID;
        String reason = null;
        
        // When
        TransactionCancelledEvent event = new TransactionCancelledEvent(
            transactionId, companyId, reason
        );
        
        // Then
        assertDomainEventBasics(event);
        assertEquals(transactionId, event.getTransactionId());
        assertEquals(companyId, event.getCompanyId());
        assertNull(event.getReason());
    }
    
    @Test
    @DisplayName("Should have proper toString representation for TransactionCreatedEvent")
    void shouldHaveProperToStringForTransactionCreatedEvent() {
        // Given
        Integer transactionId = TEST_TRANSACTION_ID;
        TransactionAggregate.TransactionType transactionType = TransactionAggregate.TransactionType.INCOME;
        Money amount = createTestMoney();
        Integer companyId = TEST_COMPANY_ID;
        
        // When
        TransactionCreatedEvent event = new TransactionCreatedEvent(
            transactionId, transactionType, amount, companyId
        );
        String toString = event.toString();
        
        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("TransactionCreatedEvent"));
        assertTrue(toString.contains(transactionId.toString()));
        assertTrue(toString.contains(transactionType.toString()));
        assertTrue(toString.contains(companyId.toString()));
    }
    
    @Test
    @DisplayName("Should have proper toString representation for TransactionApprovedEvent")
    void shouldHaveProperToStringForTransactionApprovedEvent() {
        // Given
        Integer transactionId = TEST_TRANSACTION_ID;
        Money amount = createTestMoney();
        Integer companyId = TEST_COMPANY_ID;
        Integer approvedBy = TEST_USER_ID;
        
        // When
        TransactionApprovedEvent event = new TransactionApprovedEvent(
            transactionId, amount, companyId, approvedBy
        );
        String toString = event.toString();
        
        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("TransactionApprovedEvent"));
        assertTrue(toString.contains(transactionId.toString()));
        assertTrue(toString.contains(companyId.toString()));
        assertTrue(toString.contains(approvedBy.toString()));
    }
}