// backend/src/test/java/org/example/backend/infrastructure/repository/TransactionAggregateRepositoryTest.java
package org.example.backend.infrastructure.repository;

import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.aggregate.transaction.TransactionAggregateRepository;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.Money;
import org.example.backend.domain.valueobject.TransactionStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Integration tests for TransactionAggregateRepository
 * Tests data access patterns and native SQL queries
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TransactionAggregateRepository Tests")
class TransactionAggregateRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionAggregateRepository transactionRepository;

    // Test data constants
    private static final Integer TEST_COMPANY_ID = 999;
    private static final Integer TEST_COMPANY_ID_2 = 998;
    private static final String TEST_CURRENCY = "CNY";
    private static final LocalDate TEST_DATE = LocalDate.of(2024, 6, 15);

    private TransactionAggregate savedIncomeTransaction;
    private TransactionAggregate savedExpenseTransaction;
    private TransactionAggregate savedPendingTransaction;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudOperationsTests {

        @Test
        @DisplayName("Should save and find transaction by ID")
        void shouldSaveAndFindTransactionById() {
            // Given
            TransactionAggregate newTransaction = createTestTransaction(
                    TEST_COMPANY_ID, 
                    new BigDecimal("1500.00"), 
                    TransactionAggregate.TransactionType.INCOME,
                    TransactionStatus.PENDING
            );

            // When
            TransactionAggregate saved = transactionRepository.save(newTransaction);
            Optional<TransactionAggregate> found = transactionRepository.findById(saved.getTransactionId());

            // Then
            assertThat(result).isEmpty();
                        assertThat(found).isPresent();
            assertThat(found.get().getTransactionId()).isEqualTo(saved.getTransactionId());
            assertThat(found.get().getAmount().getValue()).isEqualTo(new BigDecimal("1500.00"));
            assertThat(found.get().getTenantId().getValue()).isEqualTo(TEST_COMPANY_ID);
        }

        @Test
        @DisplayName("Should update transaction successfully")
        void shouldUpdateTransactionSuccessfully() {
            // Given
            String newDescription = "Updated transaction description";
            savedIncomeTransaction.updateDetails(
                    Money.of(new BigDecimal("2500.00"), TEST_CURRENCY),
                    newDescription,
                    1
            );

            // When
            TransactionAggregate updated = transactionRepository.save(savedIncomeTransaction);

            // Then
            entityManager.flush();
            entityManager.clear();

            Optional<TransactionAggregate> found = transactionRepository.findById(updated.getTransactionId());
            assertThat(found).isPresent();
            assertThat(found.get().getAmount().getValue()).isEqualTo(new BigDecimal("2500.00"));
            assertThat(found.get().getDescription()).isEqualTo(newDescription);
        }

        @Test
        @DisplayName("Should delete transaction successfully")
        void shouldDeleteTransactionSuccessfully() {
            // Given
            Integer transactionId = savedIncomeTransaction.getTransactionId();

            // When
            transactionRepository.delete(savedIncomeTransaction);
            entityManager.flush();

            // Then
            Optional<TransactionAggregate> found = transactionRepository.findById(transactionId);
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tenant Boundary Tests")
    class TenantBoundaryTests {

        @Test
        @DisplayName("Should find transaction by ID and tenant")
        void shouldFindTransactionByIdAndTenant() {
            // When
            Optional<TransactionAggregate> found = transactionRepository.findByIdAndTenant(
                    savedIncomeTransaction.getTransactionId(), TEST_COMPANY_ID);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getTenantId().getValue()).isEqualTo(TEST_COMPANY_ID);
        }

        @Test
        @DisplayName("Should not find transaction with wrong tenant ID")
        void shouldNotFindTransactionWithWrongTenantId() {
            // When
            Optional<TransactionAggregate> found = transactionRepository.findByIdAndTenant(
                    savedIncomeTransaction.getTransactionId(), 888);

            // Then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should find all transactions for tenant ordered by date")
        void shouldFindAllTransactionsForTenantOrderedByDate() {
            // When
            List<TransactionAggregate> transactions = transactionRepository
                    .findByTenantIdOrderByTransactionDateDesc(TEST_COMPANY_ID);

            // Then
            assertThat(transactions).hasSize(3); // income, expense, pending
            assertThat(transactions)
                    .extracting(t -> t.getTenantId().getValue())
                    .allMatch(tenantId -> tenantId.equals(TEST_COMPANY_ID));
            
            // Verify ordering (most recent first)
            for (int i = 0; i < transactions.size() - 1; i++) {
                LocalDate current = transactions.get(i).getTransactionDate();
                LocalDate next = transactions.get(i + 1).getTransactionDate();
                assertThat(current).isAfterOrEqualTo(next);
            }
        }

        @Test
        @DisplayName("Should isolate transactions between different tenants")
        void shouldIsolateTransactionsBetweenDifferentTenants() {
            // Given - create transaction for different company
            TransactionAggregate otherCompanyTransaction = createTestTransaction(
                    TEST_COMPANY_ID_2,
                    new BigDecimal("500.00"),
                    TransactionAggregate.TransactionType.EXPENSE,
                    TransactionStatus.APPROVED
            );
            transactionRepository.save(otherCompanyTransaction);
            entityManager.flush();

            // When
            List<TransactionAggregate> company1Transactions = transactionRepository
                    .findByTenantIdOrderByTransactionDateDesc(TEST_COMPANY_ID);
            List<TransactionAggregate> company2Transactions = transactionRepository
                    .findByTenantIdOrderByTransactionDateDesc(TEST_COMPANY_ID_2);

            // Then
            assertThat(company1Transactions).hasSize(3);
            assertThat(company2Transactions).hasSize(1);
            
            assertThat(company1Transactions)
                    .extracting(t -> t.getTenantId().getValue())
                    .allMatch(tenantId -> tenantId.equals(TEST_COMPANY_ID));
            
            assertThat(company2Transactions)
                    .extracting(t -> t.getTenantId().getValue())
                    .allMatch(tenantId -> tenantId.equals(TEST_COMPANY_ID_2));
        }
    }

    @Nested
    @DisplayName("Transaction Type and Status Filtering")
    class TransactionTypeAndStatusFilteringTests {

        @Test
        @DisplayName("Should find transactions by tenant and type")
        void shouldFindTransactionsByTenantAndType() {
            // When
            List<TransactionAggregate> incomeTransactions = transactionRepository
                    .findByTenantIdAndTransactionType(TEST_COMPANY_ID, "INCOME");
            List<TransactionAggregate> expenseTransactions = transactionRepository
                    .findByTenantIdAndTransactionType(TEST_COMPANY_ID, "EXPENSE");

            // Then
            assertThat(incomeTransactions).hasSize(1);
            assertThat(expenseTransactions).hasSize(1);
            
            assertThat(incomeTransactions.get(0).getTransactionType())
                    .isEqualTo(TransactionAggregate.TransactionType.INCOME);
            assertThat(expenseTransactions.get(0).getTransactionType())
                    .isEqualTo(TransactionAggregate.TransactionType.EXPENSE);
        }

        @Test
        @DisplayName("Should find transactions by tenant and status")
        void shouldFindTransactionsByTenantAndStatus() {
            // When
            List<TransactionAggregate> approvedTransactions = transactionRepository
                    .findByTenantIdAndStatus(TEST_COMPANY_ID, "APPROVED");
            List<TransactionAggregate> pendingTransactions = transactionRepository
                    .findByTenantIdAndStatus(TEST_COMPANY_ID, "PENDING");

            // Then
            assertThat(approvedTransactions).hasSize(2); // income and expense are approved
            assertThat(pendingTransactions).hasSize(1);
            
            assertThat(approvedTransactions)
                    .extracting(TransactionAggregate::getStatus)
                    .allMatch(status -> status == TransactionStatus.APPROVED);
            assertThat(pendingTransactions)
                    .extracting(TransactionAggregate::getStatus)
                    .allMatch(status -> status == TransactionStatus.PENDING);
        }

    }

    @Nested
    @DisplayName("Date Range Filtering Tests")
    class DateRangeFilteringTests {

        @Test
        @DisplayName("Should find transactions within date range")
        void shouldFindTransactionsWithinDateRange() {
            // Given
            LocalDate startDate = TEST_DATE.minusDays(1);
            LocalDate endDate = TEST_DATE.plusDays(1);

            // When
            List<TransactionAggregate> transactions = transactionRepository
                    .findByTenantIdAndDateRange(TEST_COMPANY_ID, startDate, endDate);

            // Then
            assertThat(transactions).hasSize(3);
            assertThat(transactions)
                    .extracting(TransactionAggregate::getTransactionDate)
                    .allMatch(date -> !date.isBefore(startDate) && !date.isAfter(endDate));
        }

        @Test
        @DisplayName("Should find approved transactions within date range")
        void shouldFindApprovedTransactionsWithinDateRange() {
            // Given
            LocalDate startDate = TEST_DATE.minusDays(1);
            LocalDate endDate = TEST_DATE.plusDays(1);

            // When
            List<TransactionAggregate> transactions = transactionRepository
                    .findByTenantIdAndStatusAndDateRange(
                            TenantId.of(TEST_COMPANY_ID), 
                            TransactionStatus.APPROVED, 
                            startDate, 
                            endDate
                    );

            // Then
            assertThat(transactions).hasSize(2); // Only approved transactions
            assertThat(transactions)
                    .extracting(TransactionAggregate::getStatus)
                    .allMatch(status -> status == TransactionStatus.APPROVED);
            assertThat(transactions)
                    .extracting(TransactionAggregate::getTransactionDate)
                    .allMatch(date -> !date.isBefore(startDate) && !date.isAfter(endDate));
        }

        @Test
        @DisplayName("Should return empty list for future date range")
        void shouldReturnEmptyListForFutureDateRange() {
            // Given
            LocalDate futureStart = LocalDate.now().plusDays(10);
            LocalDate futureEnd = LocalDate.now().plusDays(20);

            // When
            List<TransactionAggregate> transactions = transactionRepository
                    .findByTenantIdAndDateRange(TEST_COMPANY_ID, futureStart, futureEnd);

            // Then
            assertThat(transactions).isEmpty();
        }

        @Test
        @DisplayName("Should handle same start and end date")
        void shouldHandleSameStartAndEndDate() {
            // When
            List<TransactionAggregate> transactions = transactionRepository
                    .findByTenantIdAndDateRange(TEST_COMPANY_ID, TEST_DATE, TEST_DATE);

            // Then
            assertThat(transactions).hasSize(3);
            assertThat(transactions)
                    .extracting(TransactionAggregate::getTransactionDate)
                    .allMatch(date -> date.equals(TEST_DATE));
        }
    }

    @Nested
    @DisplayName("Amount Aggregation Tests")
    class AmountAggregationTests {

        @Test
        @DisplayName("Should calculate total amount by tenant and type")
        void shouldCalculateTotalAmountByTenantAndType() {
            // When
            BigDecimal totalIncome = transactionRepository
                    .getTotalAmountByTenantAndType(TEST_COMPANY_ID, "INCOME");
            BigDecimal totalExpense = transactionRepository
                    .getTotalAmountByTenantAndType(TEST_COMPANY_ID, "EXPENSE");

            // Then
            assertThat(totalIncome).isEqualTo(new BigDecimal("1000.00"));
            assertThat(totalExpense).isEqualTo(new BigDecimal("500.00"));
        }

        @Test
        @DisplayName("Should calculate total amount by tenant and status")
        void shouldCalculateTotalAmountByTenantAndStatus() {
            // When
            BigDecimal totalApproved = transactionRepository
                    .getTotalAmountByTenantAndStatus(TEST_COMPANY_ID, "APPROVED");
            BigDecimal totalPending = transactionRepository
                    .getTotalAmountByTenantAndStatus(TEST_COMPANY_ID, "PENDING");

            // Then
            assertThat(totalApproved).isEqualTo(new BigDecimal("1500.00")); // 1000 + 500
            assertThat(totalPending).isEqualTo(new BigDecimal("750.00"));
        }

        @Test
        @DisplayName("Should return zero for tenant with no transactions")
        void shouldReturnZeroForTenantWithNoTransactions() {
            // When
            BigDecimal total = transactionRepository
                    .getTotalAmountByTenantAndType(888, "INCOME");

            // Then
            assertThat(total).isEqualTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Complex Query Tests")
    class ComplexQueryTests {

        @Test
        @DisplayName("Should find transactions by multiple criteria")
        void shouldFindTransactionsByMultipleCriteria() {
            // When - Find approved income transactions in date range
            List<TransactionAggregate> transactions = transactionRepository
                    .findByTenantIdAndTypeAndStatusAndDateRange(
                            TEST_COMPANY_ID,
                            "INCOME",
                            "APPROVED",
                            TEST_DATE.minusDays(1),
                            TEST_DATE.plusDays(1)
                    );

            // Then
            assertThat(transactions).hasSize(1);
            TransactionAggregate transaction = transactions.get(0);
            assertThat(transaction.getTransactionType()).isEqualTo(TransactionAggregate.TransactionType.INCOME);
            assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.APPROVED);
            assertThat(transaction.getTenantId().getValue()).isEqualTo(TEST_COMPANY_ID);
        }

        @Test
        @DisplayName("Should count transactions by tenant and criteria")
        void shouldCountTransactionsByTenantAndCriteria() {
            // When
            Long totalCount = transactionRepository.countByTenantId(TEST_COMPANY_ID);
            Long approvedCount = transactionRepository.countByTenantIdAndStatus(TEST_COMPANY_ID, "APPROVED");
            Long incomeCount = transactionRepository.countByTenantIdAndType(TEST_COMPANY_ID, "INCOME");

            // Then
            assertThat(totalCount).isEqualTo(3L);
            assertThat(approvedCount).isEqualTo(2L);
            assertThat(incomeCount).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("Performance and Edge Cases")
    class PerformanceAndEdgeCasesTests {

        @Test
        @DisplayName("Should handle large amounts correctly")
        void shouldHandleLargeAmountsCorrectly() {
            // Given
            TransactionAggregate largeTransaction = createTestTransaction(
                    TEST_COMPANY_ID,
                    new BigDecimal("999999999.99"),
                    TransactionAggregate.TransactionType.INCOME,
                    TransactionStatus.APPROVED
            );

            // When
            TransactionAggregate saved = transactionRepository.save(largeTransaction);
            entityManager.flush();
            entityManager.clear();

            // Then
            Optional<TransactionAggregate> found = transactionRepository.findById(saved.getTransactionId());
            assertThat(found).isPresent();
            assertThat(found.get().getAmount().getValue()).isEqualTo(new BigDecimal("999999999.99"));
        }

        @Test
        @DisplayName("Should handle null values gracefully")
        void shouldHandleNullValuesGracefully() {
            // When & Then - These should not throw exceptions
            List<TransactionAggregate> result1 = transactionRepository
                    .findByTenantIdAndTransactionType(TEST_COMPANY_ID, null);
            List<TransactionAggregate> result2 = transactionRepository
                    .findByTenantIdAndStatus(TEST_COMPANY_ID, null);

            assertThat(result1).isEmpty();
            assertThat(result2).isEmpty();
        }

        @Test
        @DisplayName("Should maintain data consistency across operations")
        void shouldMaintainDataConsistencyAcrossOperations() {
            // Given
            long initialCount = transactionRepository.count();

            // When - Perform multiple operations
            TransactionAggregate newTransaction = createTestTransaction(
                    TEST_COMPANY_ID,
                    new BigDecimal("100.00"),
                    TransactionAggregate.TransactionType.EXPENSE,
                    TransactionStatus.PENDING
            );
            
            TransactionAggregate saved = transactionRepository.save(newTransaction);
            entityManager.flush();
            
            long afterSaveCount = transactionRepository.count();
            
            transactionRepository.delete(saved);
            entityManager.flush();
            
            long afterDeleteCount = transactionRepository.count();

            // Then
            assertThat(afterSaveCount).isEqualTo(initialCount + 1);
            assertThat(afterDeleteCount).isEqualTo(initialCount);
        }
    }

    // Helper methods for test data setup
    private void setupTestData() {
        // Create and save test transactions
        savedIncomeTransaction = createTestTransaction(
                TEST_COMPANY_ID,
                new BigDecimal("1000.00"),
                TransactionAggregate.TransactionType.INCOME,
                TransactionStatus.APPROVED
        );
        savedIncomeTransaction = transactionRepository.save(savedIncomeTransaction);

        savedExpenseTransaction = createTestTransaction(
                TEST_COMPANY_ID,
                new BigDecimal("500.00"),
                TransactionAggregate.TransactionType.EXPENSE,
                TransactionStatus.APPROVED
        );
        savedExpenseTransaction = transactionRepository.save(savedExpenseTransaction);

        savedPendingTransaction = createTestTransaction(
                TEST_COMPANY_ID,
                new BigDecimal("750.00"),
                TransactionAggregate.TransactionType.INCOME,
                TransactionStatus.PENDING
        );
        savedPendingTransaction = transactionRepository.save(savedPendingTransaction);

        entityManager.flush();
        entityManager.clear();
    }

    private TransactionAggregate createTestTransaction(
            Integer companyId,
            BigDecimal amount,
            TransactionAggregate.TransactionType type,
            TransactionStatus status) {
        
        return TransactionAggregate.builder()
                .tenantId(TenantId.of(companyId))
                .amount(Money.of(amount, TEST_CURRENCY))
                .description("Test transaction - " + type.name())
                .transactionType(type)
                .status(status)
                .transactionDate(TEST_DATE)
                .categoryId(1)
                .departmentId(1)
                .createdBy(1)
                .build();
    }
}
