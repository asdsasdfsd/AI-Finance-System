// backend/src/test/java/org/example/backend/application/service/TransactionApplicationServiceTest.java
package org.example.backend.application.service;

import org.example.backend.application.dto.CreateTransactionCommand;
import org.example.backend.application.dto.UpdateTransactionCommand;
import org.example.backend.application.dto.TransactionDTO;
import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.aggregate.transaction.TransactionAggregateRepository;
import org.example.backend.domain.valueobject.Money;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.event.DomainEventPublisher;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.exception.UnauthorizedException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import java.util.Arrays;

/**
 * Unit tests for TransactionApplicationService
 * Tests business logic and coordination between domain aggregates
 */
@DisplayName("TransactionApplicationService Tests")
class TransactionApplicationServiceTest {

    @Mock
    private TransactionAggregateRepository transactionRepository;
    
    @Mock
    private DomainEventPublisher eventPublisher;
    
    private TransactionApplicationService transactionService;
    
    // Test data constants
    private static final Integer TEST_COMPANY_ID = 999;
    private static final Integer TEST_TRANSACTION_ID = 1;
    private static final Integer TEST_USER_ID = 1;
    private static final BigDecimal TEST_AMOUNT = new BigDecimal("1000.00");
    private static final String TEST_CURRENCY = "CNY";
    private static final String TEST_DESCRIPTION = "Test transaction";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transactionService = new TransactionApplicationService(transactionRepository, eventPublisher);
    }

    @Nested
    @DisplayName("Create Income Transaction Tests")
    class CreateIncomeTransactionTests {

        @Test
        @DisplayName("Should create income transaction successfully")
        void shouldCreateIncomeTransactionSuccessfully() {
            // Given
            CreateTransactionCommand command = CreateTransactionCommand.builder()
                    .companyId(TEST_COMPANY_ID)
                    .amount(TEST_AMOUNT)
                    .currency(TEST_CURRENCY)
                    .description(TEST_DESCRIPTION)
                    .transactionDate(LocalDate.now())
                    .categoryId(1)
                    .departmentId(1)
                    .build();

            TransactionAggregate savedTransaction = createMockTransactionAggregate();
            when(transactionRepository.save(any(TransactionAggregate.class)))
                    .thenReturn(savedTransaction);

            // When
            TransactionDTO result = transactionService.createIncomeTransaction(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTransactionId()).isEqualTo(TEST_TRANSACTION_ID);
            assertThat(result.getAmount()).isEqualTo(TEST_AMOUNT);
            assertThat(result.getTransactionType()).isEqualTo("INCOME");

            verify(transactionRepository).save(any(TransactionAggregate.class));
            verify(eventPublisher).publishAll(any());
        }

        @Test
        @DisplayName("Should throw exception when command is null")
        void shouldThrowExceptionWhenCommandIsNull() {
            // When & Then
            assertThrows(IllegalArgumentException.class, 
                    () -> transactionService.createIncomeTransaction(null));
        }

        @Test
        @DisplayName("Should throw exception when amount is null or negative")
        void shouldThrowExceptionWhenAmountIsInvalid() {
            // Given - null amount
            CreateTransactionCommand commandWithNullAmount = CreateTransactionCommand.builder()
                    .companyId(TEST_COMPANY_ID)
                    .amount(null)
                    .currency(TEST_CURRENCY)
                    .description(TEST_DESCRIPTION)
                    .build();

            // Given - negative amount
            CreateTransactionCommand commandWithNegativeAmount = CreateTransactionCommand.builder()
                    .companyId(TEST_COMPANY_ID)
                    .amount(new BigDecimal("-100.00"))
                    .currency(TEST_CURRENCY)
                    .description(TEST_DESCRIPTION)
                    .build();

            // When & Then
            assertThrows(IllegalArgumentException.class,
                    () -> transactionService.createIncomeTransaction(commandWithNullAmount));
            assertThrows(IllegalArgumentException.class,
                    () -> transactionService.createIncomeTransaction(commandWithNegativeAmount));
        }

        @Test
        @DisplayName("Should throw exception when company ID is null")
        void shouldThrowExceptionWhenCompanyIdIsNull() {
            // Given
            CreateTransactionCommand command = CreateTransactionCommand.builder()
                    .companyId(null)
                    .amount(TEST_AMOUNT)
                    .currency(TEST_CURRENCY)
                    .description(TEST_DESCRIPTION)
                    .build();

            // When & Then
            assertThrows(IllegalArgumentException.class,
                    () -> transactionService.createIncomeTransaction(command));
        }
    }

    @Nested
    @DisplayName("Update Transaction Tests")
    class UpdateTransactionTests {

        @Test
        @DisplayName("Should update transaction successfully")
        void shouldUpdateTransactionSuccessfully() {
            // Given
            UpdateTransactionCommand command = UpdateTransactionCommand.builder()
                    .companyId(TEST_COMPANY_ID)
                    .amount(new BigDecimal("2000.00"))
                    .description("Updated description")
                    .build();

            TransactionAggregate existingTransaction = createMockTransactionAggregate();
            TransactionAggregate updatedTransaction = createMockUpdatedTransactionAggregate();

            when(transactionRepository.findByIdAndTenant(TEST_TRANSACTION_ID, TEST_COMPANY_ID))
                    .thenReturn(Optional.of(existingTransaction));
            when(transactionRepository.save(any(TransactionAggregate.class)))
                    .thenReturn(updatedTransaction);

            // When
            TransactionDTO result = transactionService.updateTransaction(TEST_TRANSACTION_ID, command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getAmount()).isEqualTo(new BigDecimal("2000.00"));
            assertThat(result.getDescription()).isEqualTo("Updated description");

            verify(transactionRepository).findByIdAndTenant(TEST_TRANSACTION_ID, TEST_COMPANY_ID);
            verify(transactionRepository).save(any(TransactionAggregate.class));
            verify(eventPublisher).publishAll(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when transaction not found")
        void shouldThrowResourceNotFoundExceptionWhenTransactionNotFound() {
            // Given
            UpdateTransactionCommand command = UpdateTransactionCommand.builder()
                    .companyId(TEST_COMPANY_ID)
                    .amount(TEST_AMOUNT)
                    .build();

            when(transactionRepository.findByIdAndTenant(999, TEST_COMPANY_ID))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class,
                    () -> transactionService.updateTransaction(999, command));
        }
    }

    @Nested
    @DisplayName("Approve Transaction Tests")
    class ApproveTransactionTests {

        @Test
        @DisplayName("Should approve pending transaction successfully")
        void shouldApprovePendingTransactionSuccessfully() {
            // Given
            TransactionAggregate pendingTransaction = createMockPendingTransactionAggregate();
            TransactionAggregate approvedTransaction = createMockApprovedTransactionAggregate();

            when(transactionRepository.findByIdAndTenant(TEST_TRANSACTION_ID, TEST_COMPANY_ID))
                    .thenReturn(Optional.of(pendingTransaction));
            when(transactionRepository.save(any(TransactionAggregate.class)))
                    .thenReturn(approvedTransaction);

            // When
            TransactionDTO result = transactionService.approveTransaction(
                    TEST_TRANSACTION_ID, "Approved for processing");

            // Then
            assertThat(result).isNotNull();

            verify(transactionRepository).findByIdAndTenant(TEST_TRANSACTION_ID, TEST_COMPANY_ID);
            verify(transactionRepository).save(any(TransactionAggregate.class));
            verify(eventPublisher).publishAll(any());
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when approver ID is null")
        void shouldThrowUnauthorizedExceptionWhenApproverIdIsNull() {
            // When & Then
            assertThrows(UnauthorizedException.class,
                    () -> transactionService.approveTransaction(
                            TEST_TRANSACTION_ID, null));
        }
    }

    @Nested
    @DisplayName("Query Transaction Tests")
    class QueryTransactionTests {

        @Test
        @DisplayName("Should get transaction by ID successfully")
        void shouldGetTransactionByIdSuccessfully() {
            // Given
            TransactionAggregate transaction = createMockTransactionAggregate();
            when(transactionRepository.findByIdAndTenant(TEST_TRANSACTION_ID, TEST_COMPANY_ID))
                    .thenReturn(Optional.of(transaction));

            // When
            TransactionDTO result = transactionService.getTransactionById(TEST_TRANSACTION_ID, TEST_COMPANY_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTransactionId()).isEqualTo(TEST_TRANSACTION_ID);
            verify(transactionRepository).findByIdAndTenant(TEST_TRANSACTION_ID, TEST_COMPANY_ID);
        }

        @Test
        @DisplayName("Should get all transactions by company")
        void shouldGetAllTransactionsByCompany() {
            // Given
            List<TransactionAggregate> transactions = Arrays.asList(
                    createMockTransactionAggregate(),
                    createMockTransactionAggregate()
            );
            when(transactionRepository.findByTenantIdOrderByTransactionDateDesc(TEST_COMPANY_ID))
                    .thenReturn(transactions);

            // When
            List<TransactionDTO> result = transactionService.getTransactionsByCompany(TEST_COMPANY_ID);

            // Then
            assertThat(result).hasSize(2);
            verify(transactionRepository).findByTenantIdOrderByTransactionDateDesc(TEST_COMPANY_ID);
        }

        @Test
        @DisplayName("Should get transactions by type")
        void shouldGetTransactionsByType() {
            // Given
            List<TransactionAggregate> incomeTransactions = Arrays.asList(
                    createMockTransactionAggregate()
            );
            when(transactionRepository.findByTenantIdAndTransactionType(
                    TEST_COMPANY_ID, "INCOME"))
                    .thenReturn(incomeTransactions);

            // When
            List<TransactionDTO> result = transactionService.getTransactionsByType(
                    TEST_COMPANY_ID, TransactionAggregate.TransactionType.INCOME);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTransactionType()).isEqualTo("INCOME");
        }
    }

    // Helper methods for creating mock objects
    private TransactionAggregate createMockTransactionAggregate() {
        TransactionAggregate transaction = mock(TransactionAggregate.class);
        when(transaction.getTransactionId()).thenReturn(TEST_TRANSACTION_ID);
        when(transaction.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(transaction.getAmount()).thenReturn(Money.of(TEST_AMOUNT, TEST_CURRENCY));
        when(transaction.getDescription()).thenReturn(TEST_DESCRIPTION);
        when(transaction.getTransactionType()).thenReturn(TransactionAggregate.TransactionType.INCOME);
        when(transaction.getTransactionDate()).thenReturn(LocalDate.now());
        when(transaction.getDomainEvents()).thenReturn(Arrays.asList());
        return transaction;
    }

    private TransactionAggregate createMockUpdatedTransactionAggregate() {
        TransactionAggregate transaction = createMockTransactionAggregate();
        when(transaction.getAmount()).thenReturn(Money.of(new BigDecimal("2000.00"), TEST_CURRENCY));
        when(transaction.getDescription()).thenReturn("Updated description");
        return transaction;
    }

    private TransactionAggregate createMockPendingTransactionAggregate() {
        TransactionAggregate transaction = createMockTransactionAggregate();
        return transaction;
    }

    private TransactionAggregate createMockApprovedTransactionAggregate() {
        TransactionAggregate transaction = createMockTransactionAggregate();
        return transaction;
    }
}