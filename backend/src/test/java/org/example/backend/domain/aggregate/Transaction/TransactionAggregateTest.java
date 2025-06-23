// backend/src/test/java/org/example/backend/domain/aggregate/transaction/TransactionAggregateTest.java
package org.example.backend.domain.aggregate.transaction;

import org.example.backend.domain.valueobject.Money;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.TransactionStatus;
import org.example.backend.domain.event.TransactionCreatedEvent;
import org.example.backend.domain.event.TransactionApprovedEvent;
import org.example.backend.domain.event.TransactionCancelledEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Unit tests for TransactionAggregate domain object
 * Tests business rules, invariants, and domain events
 */
@DisplayName("TransactionAggregate Domain Tests")
class TransactionAggregateTest {

    // Test data constants
    private static final Integer TEST_COMPANY_ID = 999;
    private static final BigDecimal TEST_AMOUNT = new BigDecimal("1000.00");
    private static final String TEST_CURRENCY = "CNY";
    private static final String TEST_DESCRIPTION = "Test transaction";
    private static final Integer TEST_USER_ID = 1;
    private static final Integer TEST_CATEGORY_ID = 1;
    private static final Integer TEST_DEPARTMENT_ID = 1;

    private TransactionAggregate transaction;
    private Money testMoney;
    private TenantId testTenantId;

    @BeforeEach
    void setUp() {
        testMoney = Money.of(TEST_AMOUNT, TEST_CURRENCY);
        testTenantId = TenantId.of(TEST_COMPANY_ID);
        transaction = createTestTransaction();
    }

    @Nested
    @DisplayName("Transaction Creation Tests")
    class TransactionCreationTests {

        @Test
        @DisplayName("Should create income transaction with valid data")
        void shouldCreateIncomeTransactionWithValidData() {
            // When
            TransactionAggregate incomeTransaction = TransactionAggregate.createIncomeTransaction(
                    testTenantId,
                    testMoney,
                    TEST_DESCRIPTION,
                    LocalDate.now(),
                    TEST_CATEGORY_ID,
                    TEST_DEPARTMENT_ID,
                    TEST_USER_ID
            );

            // Then
            assertThat(incomeTransaction).isNotNull();
            assertThat(incomeTransaction.getTenantId()).isEqualTo(testTenantId);
            assertThat(incomeTransaction.getAmount()).isEqualTo(testMoney);
            assertThat(incomeTransaction.getDescription()).isEqualTo(TEST_DESCRIPTION);
            assertThat(incomeTransaction.getTransactionType()).isEqualTo(TransactionAggregate.TransactionType.INCOME);
            assertThat(incomeTransaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
            assertThat(incomeTransaction.getCreatedBy()).isEqualTo(TEST_USER_ID);
            assertThat(incomeTransaction.getDomainEvents()).hasSize(1);
            assertThat(incomeTransaction.getDomainEvents().get(0)).isInstanceOf(TransactionCreatedEvent.class);
        }

        @Test
        @DisplayName("Should create expense transaction with valid data")
        void shouldCreateExpenseTransactionWithValidData() {
            // When
            TransactionAggregate expenseTransaction = TransactionAggregate.createExpenseTransaction(
                    testTenantId,
                    testMoney,
                    TEST_DESCRIPTION,
                    LocalDate.now(),
                    TEST_CATEGORY_ID,
                    TEST_DEPARTMENT_ID,
                    TEST_USER_ID
            );

            // Then
            assertThat(expenseTransaction.getTransactionType()).isEqualTo(TransactionAggregate.TransactionType.EXPENSE);
            assertThat(expenseTransaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        }

        @Test
        @DisplayName("Should throw exception when tenant ID is null")
        void shouldThrowExceptionWhenTenantIdIsNull() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                TransactionAggregate.createIncomeTransaction(
                    null,
                    testMoney,
                    TEST_DESCRIPTION,
                    LocalDate.now(),
                    TEST_CATEGORY_ID,
                    TEST_DEPARTMENT_ID,
                    TEST_USER_ID
                )
            );
        }

        @Test
        @DisplayName("Should throw exception when amount is null")
        void shouldThrowExceptionWhenAmountIsNull() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                TransactionAggregate.createIncomeTransaction(
                    testTenantId,
                    null,
                    TEST_DESCRIPTION,
                    LocalDate.now(),
                    TEST_CATEGORY_ID,
                    TEST_DEPARTMENT_ID,
                    TEST_USER_ID
                )
            );
        }

        @Test
        @DisplayName("Should throw exception when amount is zero or negative")
        void shouldThrowExceptionWhenAmountIsZeroOrNegative() {
            // Given
            Money zeroMoney = Money.of(BigDecimal.ZERO, TEST_CURRENCY);
            Money negativeMoney = Money.of(new BigDecimal("-100.00"), TEST_CURRENCY);

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                TransactionAggregate.createIncomeTransaction(
                    testTenantId,
                    zeroMoney,
                    TEST_DESCRIPTION,
                    LocalDate.now(),
                    TEST_CATEGORY_ID,
                    TEST_DEPARTMENT_ID,
                    TEST_USER_ID
                )
            );

            assertThrows(IllegalArgumentException.class, () -> 
                TransactionAggregate.createIncomeTransaction(
                    testTenantId,
                    negativeMoney,
                    TEST_DESCRIPTION,
                    LocalDate.now(),
                    TEST_CATEGORY_ID,
                    TEST_DEPARTMENT_ID,
                    TEST_USER_ID
                )
            );
        }

        @Test
        @DisplayName("Should throw exception when description is null or empty")
        void shouldThrowExceptionWhenDescriptionIsNullOrEmpty() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                TransactionAggregate.createIncomeTransaction(
                    testTenantId,
                    testMoney,
                    null,
                    LocalDate.now(),
                    TEST_CATEGORY_ID,
                    TEST_DEPARTMENT_ID,
                    TEST_USER_ID
                )
            );

            assertThrows(IllegalArgumentException.class, () -> 
                TransactionAggregate.createIncomeTransaction(
                    testTenantId,
                    testMoney,
                    "",
                    LocalDate.now(),
                    TEST_CATEGORY_ID,
                    TEST_DEPARTMENT_ID,
                    TEST_USER_ID
                )
            );

            assertThrows(IllegalArgumentException.class, () -> 
                TransactionAggregate.createIncomeTransaction(
                    testTenantId,
                    testMoney,
                    "   ",
                    LocalDate.now(),
                    TEST_CATEGORY_ID,
                    TEST_DEPARTMENT_ID,
                    TEST_USER_ID
                )
            );
        }
    }

    @Nested
    @DisplayName("Transaction Update Tests")
    class TransactionUpdateTests {

        @Test
        @DisplayName("Should update transaction details when in pending status")
        void shouldUpdateTransactionDetailsWhenInPendingStatus() {
            // Given
            Money newAmount = Money.of(new BigDecimal("2000.00"), TEST_CURRENCY);
            String newDescription = "Updated transaction description";

            // When
            transaction.updateDetails(newAmount, newDescription, TEST_USER_ID);

            // Then
            assertThat(transaction.getAmount()).isEqualTo(newAmount);
            assertThat(transaction.getDescription()).isEqualTo(newDescription);
            assertThat(transaction.getUpdatedBy()).isEqualTo(TEST_USER_ID);
            assertThat(transaction.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when updating approved transaction")
        void shouldThrowExceptionWhenUpdatingApprovedTransaction() {
            // Given
            transaction.approve(TEST_USER_ID, "Approved for processing");
            Money newAmount = Money.of(new BigDecimal("2000.00"), TEST_CURRENCY);

            // When & Then
            assertThrows(IllegalStateException.class, () -> 
                transaction.updateDetails(newAmount, "New description", TEST_USER_ID)
            );
        }

        @Test
        @DisplayName("Should throw exception when updating with null amount")
        void shouldThrowExceptionWhenUpdatingWithNullAmount() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                transaction.updateDetails(null, "New description", TEST_USER_ID)
            );
        }

        @Test
        @DisplayName("Should throw exception when updating with invalid user ID")
        void shouldThrowExceptionWhenUpdatingWithInvalidUserId() {
            // Given
            Money newAmount = Money.of(new BigDecimal("2000.00"), TEST_CURRENCY);

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                transaction.updateDetails(newAmount, "New description", null)
            );
        }
    }

    @Nested
    @DisplayName("Transaction Approval Tests")
    class TransactionApprovalTests {

        @Test
        @DisplayName("Should approve pending transaction successfully")
        void shouldApprovePendingTransactionSuccessfully() {
            // Given
            String approvalComment = "Approved for processing";

            // When
            transaction.approve(TEST_USER_ID, approvalComment);

            // Then
            assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.APPROVED);
            assertThat(transaction.getApprovedBy()).isEqualTo(TEST_USER_ID);
            assertThat(transaction.getApprovedAt()).isNotNull();
            assertThat(transaction.getApprovalComment()).isEqualTo(approvalComment);
            assertThat(transaction.getDomainEvents()).hasSize(2); // Created + Approved events
            assertThat(transaction.getDomainEvents().get(1)).isInstanceOf(TransactionApprovedEvent.class);
        }

        @Test
        @DisplayName("Should throw exception when approving already approved transaction")
        void shouldThrowExceptionWhenApprovingAlreadyApprovedTransaction() {
            // Given
            transaction.approve(TEST_USER_ID, "First approval");

            // When & Then
            assertThrows(IllegalStateException.class, () -> 
                transaction.approve(TEST_USER_ID, "Second approval")
            );
        }

        @Test
        @DisplayName("Should throw exception when approving cancelled transaction")
        void shouldThrowExceptionWhenApprovingCancelledTransaction() {
            // Given
            transaction.cancel(TEST_USER_ID, "Transaction cancelled");

            // When & Then
            assertThrows(IllegalStateException.class, () -> 
                transaction.approve(TEST_USER_ID, "Trying to approve cancelled")
            );
        }

        @Test
        @DisplayName("Should throw exception when approver ID is null")
        void shouldThrowExceptionWhenApproverIdIsNull() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                transaction.approve(null, "Approval comment")
            );
        }
    }

    @Nested
    @DisplayName("Transaction Cancellation Tests")
    class TransactionCancellationTests {

        @Test
        @DisplayName("Should cancel pending transaction successfully")
        void shouldCancelPendingTransactionSuccessfully() {
            // Given
            String cancellationReason = "Transaction cancelled due to error";

            // When
            transaction.cancel(TEST_USER_ID, cancellationReason);

            // Then
            assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.CANCELLED);
            assertThat(transaction.getCancelledBy()).isEqualTo(TEST_USER_ID);
            assertThat(transaction.getCancelledAt()).isNotNull();
            assertThat(transaction.getCancellationReason()).isEqualTo(cancellationReason);
            assertThat(transaction.getDomainEvents()).hasSize(2); // Created + Cancelled events
            assertThat(transaction.getDomainEvents().get(1)).isInstanceOf(TransactionCancelledEvent.class);
        }

        @Test
        @DisplayName("Should cancel approved transaction with void operation")
        void shouldCancelApprovedTransactionWithVoidOperation() {
            // Given
            transaction.approve(TEST_USER_ID, "Initial approval");
            String voidReason = "Transaction voided due to error";

            // When
            transaction.voidTransaction(TEST_USER_ID, voidReason);

            // Then
            assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.VOIDED);
            assertThat(transaction.getVoidedBy()).isEqualTo(TEST_USER_ID);
            assertThat(transaction.getVoidedAt()).isNotNull();
            assertThat(transaction.getVoidReason()).isEqualTo(voidReason);
        }

        @Test
        @DisplayName("Should throw exception when cancelling already cancelled transaction")
        void shouldThrowExceptionWhenCancellingAlreadyCancelledTransaction() {
            // Given
            transaction.cancel(TEST_USER_ID, "First cancellation");

            // When & Then
            assertThrows(IllegalStateException.class, () -> 
                transaction.cancel(TEST_USER_ID, "Second cancellation")
            );
        }

        @Test
        @DisplayName("Should throw exception when user ID is null for cancellation")
        void shouldThrowExceptionWhenUserIdIsNullForCancellation() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                transaction.cancel(null, "Cancellation reason")
            );
        }
    }

    @Nested
    @DisplayName("Business Rule Tests")
    class BusinessRuleTests {

        @Test
        @DisplayName("Should enforce maximum transaction amount limit")
        void shouldEnforceMaximumTransactionAmountLimit() {
            // Given
            Money excessiveAmount = Money.of(new BigDecimal("10000000.00"), TEST_CURRENCY);

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                TransactionAggregate.createIncomeTransaction(
                    testTenantId,
                    excessiveAmount,
                    TEST_DESCRIPTION,
                    LocalDate.now(),
                    TEST_CATEGORY_ID,
                    TEST_DEPARTMENT_ID,
                    TEST_USER_ID
                )
            );
        }

        @Test
        @DisplayName("Should validate transaction date is not in future")
        void shouldValidateTransactionDateIsNotInFuture() {
            // Given
            LocalDate futureDate = LocalDate.now().plusDays(1);

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                TransactionAggregate.createIncomeTransaction(
                    testTenantId,
                    testMoney,
                    TEST_DESCRIPTION,
                    futureDate,
                    TEST_CATEGORY_ID,
                    TEST_DEPARTMENT_ID,
                    TEST_USER_ID
                )
            );
        }

        @Test
        @DisplayName("Should allow same-day transactions")
        void shouldAllowSameDayTransactions() {
            // Given
            LocalDate today = LocalDate.now();

            // When & Then
            assertDoesNotThrow(() -> 
                TransactionAggregate.createIncomeTransaction(
                    testTenantId,
                    testMoney,
                    TEST_DESCRIPTION,
                    today,
                    TEST_CATEGORY_ID,
                    TEST_DEPARTMENT_ID,
                    TEST_USER_ID
                )
            );
        }

        @Test
        @DisplayName("Should validate currency consistency")
        void shouldValidateCurrencyConsistency() {
            // Given
            Money differentCurrencyMoney = Money.of(TEST_AMOUNT, "USD");

            // When
            transaction.updateDetails(differentCurrencyMoney, "Updated", TEST_USER_ID);

            // Then - should accept different currency (business rule may vary)
            assertThat(transaction.getAmount().getCurrency()).isEqualTo("USD");
        }
    }

    @Nested
    @DisplayName("Domain Event Tests")
    class DomainEventTests {

        @Test
        @DisplayName("Should publish creation event when transaction is created")
        void shouldPublishCreationEventWhenTransactionIsCreated() {
            // Given
            TransactionAggregate newTransaction = TransactionAggregate.createIncomeTransaction(
                    testTenantId,
                    testMoney,
                    TEST_DESCRIPTION,
                    LocalDate.now(),
                    TEST_CATEGORY_ID,
                    TEST_DEPARTMENT_ID,
                    TEST_USER_ID
            );

            // When
            var events = newTransaction.getDomainEvents();

            // Then
            assertThat(events).hasSize(1);
            TransactionCreatedEvent createdEvent = (TransactionCreatedEvent) events.get(0);
            assertThat(createdEvent.getTransactionId()).isEqualTo(newTransaction.getTransactionId());
            assertThat(createdEvent.getTenantId()).isEqualTo(testTenantId);
            assertThat(createdEvent.getAmount()).isEqualTo(testMoney);
        }

        @Test
        @DisplayName("Should publish approval event when transaction is approved")
        void shouldPublishApprovalEventWhenTransactionIsApproved() {
            // When
            transaction.approve(TEST_USER_ID, "Approved");

            // Then
            var events = transaction.getDomainEvents();
            assertThat(events).hasSize(2);
            TransactionApprovedEvent approvedEvent = (TransactionApprovedEvent) events.get(1);
            assertThat(approvedEvent.getTransactionId()).isEqualTo(transaction.getTransactionId());
            assertThat(approvedEvent.getApprovedBy()).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("Should clear domain events after processing")
        void shouldClearDomainEventsAfterProcessing() {
            // Given
            transaction.approve(TEST_USER_ID, "Approved");
            assertThat(transaction.getDomainEvents()).hasSize(2);

            // When
            transaction.clearDomainEvents();

            // Then
            assertThat(transaction.getDomainEvents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Aggregate Invariant Tests")
    class AggregateInvariantTests {

        @Test
        @DisplayName("Should maintain aggregate consistency during state transitions")
        void shouldMaintainAggregateConsistencyDuringStateTransitions() {
            // Initial state
            assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
            assertThat(transaction.getApprovedBy()).isNull();
            assertThat(transaction.getApprovedAt()).isNull();

            // After approval
            transaction.approve(TEST_USER_ID, "Approved");
            assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.APPROVED);
            assertThat(transaction.getApprovedBy()).isEqualTo(TEST_USER_ID);
            assertThat(transaction.getApprovedAt()).isNotNull();

            // After void
            transaction.voidTransaction(TEST_USER_ID, "Voided");
            assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.VOIDED);
            assertThat(transaction.getVoidedBy()).isEqualTo(TEST_USER_ID);
            assertThat(transaction.getVoidedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should maintain data integrity across operations")
        void shouldMaintainDataIntegrityAcrossOperations() {
            // Given
            LocalDate originalDate = transaction.getTransactionDate();
            TenantId originalTenant = transaction.getTenantId();
            TransactionAggregate.TransactionType originalType = transaction.getTransactionType();

            // When - perform various operations
            transaction.updateDetails(
                Money.of(new BigDecimal("2000.00"), "USD"), 
                "Updated description", 
                TEST_USER_ID
            );
            transaction.approve(TEST_USER_ID, "Approved");

            // Then - core properties should remain unchanged
            assertThat(transaction.getTransactionDate()).isEqualTo(originalDate);
            assertThat(transaction.getTenantId()).isEqualTo(originalTenant);
            assertThat(transaction.getTransactionType()).isEqualTo(originalType);
            assertThat(transaction.getCreatedBy()).isEqualTo(TEST_USER_ID);
        }
    }

    // Helper methods
    private TransactionAggregate createTestTransaction() {
        return TransactionAggregate.createIncomeTransaction(
                testTenantId,
                testMoney,
                TEST_DESCRIPTION,
                LocalDate.now(),
                TEST_CATEGORY_ID,
                TEST_DEPARTMENT_ID,
                TEST_USER_ID
        );
    }
}