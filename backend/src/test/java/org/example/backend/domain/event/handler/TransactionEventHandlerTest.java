// backend/src/test/java/org/example/backend/domain/event/handler/TransactionEventHandlerTest.java
package org.example.backend.domain.event.handler;

import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.event.DomainEventTestBase;
import org.example.backend.domain.event.TransactionCreatedEvent;
import org.example.backend.domain.event.TransactionApprovedEvent;
import org.example.backend.domain.event.TransactionCancelledEvent;
import org.example.backend.service.AuditLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionEventHandler
 * 
 * Tests the handling of transaction domain events including:
 * 1. TransactionCreatedEvent handling
 * 2. TransactionApprovedEvent handling
 * 3. TransactionCancelledEvent handling
 * 4. Audit logging integration
 * 5. Error handling scenarios
 */
public class TransactionEventHandlerTest extends DomainEventTestBase {
    
    @Mock
    private AuditLogService auditLogService;
    
    @InjectMocks
    private TransactionEventHandler transactionEventHandler;
    
    private TransactionCreatedEvent transactionCreatedEvent;
    private TransactionApprovedEvent transactionApprovedEvent;
    private TransactionCancelledEvent transactionCancelledEvent;
    
    @BeforeEach
    void setUpEventHandlerTests() {
        // Create test events
        transactionCreatedEvent = new TransactionCreatedEvent(
            TEST_TRANSACTION_ID,
            TransactionAggregate.TransactionType.INCOME,
            createTestMoney(),
            TEST_COMPANY_ID
        );
        
        transactionApprovedEvent = new TransactionApprovedEvent(
            TEST_TRANSACTION_ID,
            createTestMoney(),
            TEST_COMPANY_ID,
            TEST_USER_ID
        );
        
        transactionCancelledEvent = new TransactionCancelledEvent(
            TEST_TRANSACTION_ID,
            TEST_COMPANY_ID,
            "Duplicate transaction detected"
        );
    }
    
    @Test
    @DisplayName("Should handle TransactionCreatedEvent successfully")
    void shouldHandleTransactionCreatedEventSuccessfully() {
        // When
        transactionEventHandler.handleTransactionCreated(transactionCreatedEvent);
        
        // Then
        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> entityTypeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> entityIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> detailsCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> sourceCaptor = ArgumentCaptor.forClass(String.class);
        
        verify(auditLogService, times(1)).logAction(
            eq(null), // user object
            actionCaptor.capture(),
            entityTypeCaptor.capture(),
            entityIdCaptor.capture(),
            detailsCaptor.capture(),
            sourceCaptor.capture()
        );
        
        assertEquals("TRANSACTION_CREATED", actionCaptor.getValue());
        assertEquals("Transaction", entityTypeCaptor.getValue());
        assertEquals(TEST_TRANSACTION_ID.toString(), entityIdCaptor.getValue());
        assertTrue(detailsCaptor.getValue().contains("Income"));
        assertTrue(detailsCaptor.getValue().contains("1000.00"));
        assertEquals("system", sourceCaptor.getValue());
    }
    
    @Test
    @DisplayName("Should handle TransactionCreatedEvent for expense transaction")
    void shouldHandleTransactionCreatedEventForExpenseTransaction() {
        // Given
        TransactionCreatedEvent expenseEvent = new TransactionCreatedEvent(
            TEST_TRANSACTION_ID,
            TransactionAggregate.TransactionType.EXPENSE,
            createMoney("500.00"),
            TEST_COMPANY_ID
        );
        
        // When
        transactionEventHandler.handleTransactionCreated(expenseEvent);
        
        // Then
        ArgumentCaptor<String> detailsCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditLogService).logAction(
            any(), any(), any(), any(), detailsCaptor.capture(), any()
        );
        
        assertTrue(detailsCaptor.getValue().contains("Expense"));
        assertTrue(detailsCaptor.getValue().contains("500.00"));
    }
    
    @Test
    @DisplayName("Should handle TransactionApprovedEvent successfully")
    void shouldHandleTransactionApprovedEventSuccessfully() {
        // When
        transactionEventHandler.handleTransactionApproved(transactionApprovedEvent);
        
        // Then
        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> detailsCaptor = ArgumentCaptor.forClass(String.class);
        
        verify(auditLogService, times(1)).logAction(
            eq(null),
            actionCaptor.capture(),
            eq("Transaction"),
            eq(TEST_TRANSACTION_ID.toString()),
            detailsCaptor.capture(),
            eq("system")
        );
        
        assertEquals("TRANSACTION_APPROVED", actionCaptor.getValue());
        assertTrue(detailsCaptor.getValue().contains("批准交易"));
        assertTrue(detailsCaptor.getValue().contains("1000.00"));
        assertTrue(detailsCaptor.getValue().contains(TEST_USER_ID.toString()));
    }
    
    @Test
    @DisplayName("Should handle TransactionCancelledEvent with reason")
    void shouldHandleTransactionCancelledEventWithReason() {
        // When
        transactionEventHandler.handleTransactionCancelled(transactionCancelledEvent);
        
        // Then
        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> detailsCaptor = ArgumentCaptor.forClass(String.class);
        
        verify(auditLogService, times(1)).logAction(
            eq(null),
            actionCaptor.capture(),
            eq("Transaction"),
            eq(TEST_TRANSACTION_ID.toString()),
            detailsCaptor.capture(),
            eq("system")
        );
        
        assertEquals("TRANSACTION_CANCELLED", actionCaptor.getValue());
        assertTrue(detailsCaptor.getValue().contains("取消交易"));
        assertTrue(detailsCaptor.getValue().contains("Duplicate transaction detected"));
    }
    
    @Test
    @DisplayName("Should handle TransactionCancelledEvent without reason")
    void shouldHandleTransactionCancelledEventWithoutReason() {
        // Given
        TransactionCancelledEvent eventWithoutReason = new TransactionCancelledEvent(
            TEST_TRANSACTION_ID,
            TEST_COMPANY_ID,
            null
        );
        
        // When
        transactionEventHandler.handleTransactionCancelled(eventWithoutReason);
        
        // Then
        ArgumentCaptor<String> detailsCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditLogService).logAction(
            any(), any(), any(), any(), detailsCaptor.capture(), any()
        );
        
        assertEquals("取消交易", detailsCaptor.getValue());
    }
    
    @Test
    @DisplayName("Should propagate exception when audit logging fails for transaction created")
    void shouldPropagateExceptionWhenAuditLoggingFailsForTransactionCreated() {
        // Given
        RuntimeException auditException = new RuntimeException("Audit logging failed");
        doThrow(auditException).when(auditLogService).logAction(
            any(), any(), any(), any(), any(), any()
        );
        
        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> transactionEventHandler.handleTransactionCreated(transactionCreatedEvent)
        );
        
        assertEquals(auditException, exception);
        verify(auditLogService, times(1)).logAction(any(), any(), any(), any(), any(), any());
    }
    
    @Test
    @DisplayName("Should propagate exception when audit logging fails for transaction approved")
    void shouldPropagateExceptionWhenAuditLoggingFailsForTransactionApproved() {
        // Given
        RuntimeException auditException = new RuntimeException("Audit logging failed");
        doThrow(auditException).when(auditLogService).logAction(
            any(), any(), any(), any(), any(), any()
        );
        
        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> transactionEventHandler.handleTransactionApproved(transactionApprovedEvent)
        );
        
        assertEquals(auditException, exception);
    }
    
    @Test
    @DisplayName("Should propagate exception when audit logging fails for transaction cancelled")
    void shouldPropagateExceptionWhenAuditLoggingFailsForTransactionCancelled() {
        // Given
        RuntimeException auditException = new RuntimeException("Audit logging failed");
        doThrow(auditException).when(auditLogService).logAction(
            any(), any(), any(), any(), any(), any()
        );
        
        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> transactionEventHandler.handleTransactionCancelled(transactionCancelledEvent)
        );
        
        assertEquals(auditException, exception);
    }
    
    @Test
    @DisplayName("Should handle multiple events in sequence")
    void shouldHandleMultipleEventsInSequence() {
        // When
        transactionEventHandler.handleTransactionCreated(transactionCreatedEvent);
        transactionEventHandler.handleTransactionApproved(transactionApprovedEvent);
        transactionEventHandler.handleTransactionCancelled(transactionCancelledEvent);
        
        // Then
        verify(auditLogService, times(3)).logAction(any(), any(), any(), any(), any(), any());
        
        // Verify the sequence of actions
        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditLogService, times(3)).logAction(
            any(), actionCaptor.capture(), any(), any(), any(), any()
        );
        
        assertEquals("TRANSACTION_CREATED", actionCaptor.getAllValues().get(0));
        assertEquals("TRANSACTION_APPROVED", actionCaptor.getAllValues().get(1));
        assertEquals("TRANSACTION_CANCELLED", actionCaptor.getAllValues().get(2));
    }
}