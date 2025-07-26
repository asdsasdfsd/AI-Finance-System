// backend/src/test/java/org/example/backend/domain/aggregate/transaction/TransactionAggregateTest.java - 修复版本
package org.example.backend.domain.aggregate.transaction;

import org.example.backend.domain.aggregate.AggregateTestBase;
import org.example.backend.domain.event.TransactionCreatedEvent;
import org.example.backend.domain.event.TransactionApprovedEvent;
import org.example.backend.domain.valueobject.Money;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.TransactionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransactionAggregate - 修复版本
 */
public class TransactionAggregateTest extends AggregateTestBase {
    
    @Test
    @DisplayName("Should create income transaction successfully")
    void shouldCreateIncomeTransactionSuccessfully() {
        // Given
        Money amount = createTestMoney();
        LocalDate transactionDate = TEST_DATE;
        String description = "Service revenue";
        TenantId tenantId = createTestTenantId();
        Integer userId = TEST_USER_ID;
        
        // When
        TransactionAggregate transaction = TransactionAggregate.createIncome(
            amount, transactionDate, description, tenantId, userId
        );
        
        // Then
        assertNotNull(transaction);
        assertEquals(TransactionAggregate.TransactionType.INCOME, transaction.getTransactionType());
        assertEquals(amount, transaction.getMoney());
        assertEquals(transactionDate, transaction.getTransactionDate());
        assertEquals(description, transaction.getDescription());
        assertEquals(tenantId, transaction.getTenantId());
        assertEquals(userId, transaction.getUserId());
        assertEquals(TransactionStatus.Status.DRAFT, transaction.getTransactionStatus().getStatus());
        assertFalse(transaction.getIsRecurring());
        assertFalse(transaction.getIsTaxable());
        
        // Verify audit fields
        assertValidCreationTime(transaction.getCreatedAt());
        assertValidCreationTime(transaction.getUpdatedAt());
        
        // Verify domain event
        assertEventPublished(transaction.getDomainEvents(), TransactionCreatedEvent.class);
    }
    
    @Test
    @DisplayName("Should update transaction details successfully")
    void shouldUpdateTransactionDetailsSuccessfully() {
        // Given
        TransactionAggregate transaction = createTestIncomeTransaction();
        Money newAmount = createMoney("1500.00");
        String newDescription = "Updated description";
        String paymentMethod = "Bank Transfer";
        String referenceNumber = "REF123456";
        
        // When
        transaction.updateTransaction(newAmount, newDescription, paymentMethod, referenceNumber);
        
        // Then
        assertEquals(newAmount, transaction.getMoney());
        assertEquals(newDescription, transaction.getDescription());
        assertEquals(paymentMethod, transaction.getPaymentMethod());
        assertEquals(referenceNumber, transaction.getReferenceNumber());
    }
    
    @Test
    @DisplayName("Should set recurring flag successfully")
    void shouldSetRecurringFlagSuccessfully() {
        // Given
        TransactionAggregate transaction = createTestIncomeTransaction();
        
        // When
        transaction.markAsRecurring();
        
        // Then
        assertTrue(transaction.getIsRecurring());
    }
    
    @Test
    @DisplayName("Should set taxable flag successfully")
    void shouldSetTaxableFlagSuccessfully() {
        // Given
        TransactionAggregate transaction = createTestIncomeTransaction();
        
        // When
        transaction.markAsTaxable();
        
        // Then
        assertTrue(transaction.getIsTaxable());
    }
    
    @Test
    @DisplayName("Should calculate tax amount correctly")
    void shouldCalculateTaxAmountCorrectly() {
        // Given
        TransactionAggregate transaction = createTestIncomeTransaction();
        transaction.markAsTaxable();
        double taxRate = 0.13; // 13% tax
        
        // When
        Money taxAmount = transaction.calculateTax(taxRate);
        
        // Then
        // 使用字符串比较避免精度问题
        assertEquals("130.00", taxAmount.getAmount().toString());
        assertEquals(TEST_CURRENCY, taxAmount.getCurrencyCode());
    }
    
    @Test
    @DisplayName("Should check transaction status correctly")
    void shouldCheckTransactionStatusCorrectly() {
        // Given
        TransactionAggregate transaction = createTestIncomeTransaction();
        
        // When & Then
        assertTrue(transaction.canModify()); // Draft can be modified
        assertFalse(transaction.isCompleted()); // Draft is not completed
        assertTrue(transaction.getTransactionStatus().isDraft());
    }
    
    // Helper method to create test income transaction
    private TransactionAggregate createTestIncomeTransaction() {
        return TransactionAggregate.createIncome(
            createTestMoney(),
            TEST_DATE,
            "Test income transaction",
            createTestTenantId(),
            TEST_USER_ID
        );
    }
}

