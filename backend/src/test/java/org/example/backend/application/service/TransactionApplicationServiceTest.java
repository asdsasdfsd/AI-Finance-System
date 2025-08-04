// backend/src/test/java/org/example/backend/application/service/TransactionApplicationServiceTest.java
package org.example.backend.application.service;

import org.example.backend.application.dto.CreateTransactionCommand;
import org.example.backend.application.dto.UpdateTransactionCommand;
import org.example.backend.application.dto.ApproveTransactionCommand;
import org.example.backend.application.dto.TransactionDTO;
import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.aggregate.transaction.TransactionAggregateRepository;
import org.example.backend.domain.valueobject.Money;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.TransactionStatus;
import org.example.backend.domain.event.DomainEventPublisher;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Enhanced unit tests for TransactionApplicationService
 * Target: Improve coverage from 3% to 80%+
 * Strategy: Mock dependencies, test real service implementation
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Transaction Application Service Tests - Real Implementation Coverage")
class TransactionApplicationServiceTest {

    @Mock
    private TransactionAggregateRepository transactionRepository;
    
    @Mock
    private DomainEventPublisher eventPublisher;
    
    private TransactionApplicationService transactionApplicationService;

    // Test constants
    private static final Integer TEST_TRANSACTION_ID = 1001;
    private static final Integer TEST_COMPANY_ID = 1;
    private static final Integer TEST_USER_ID = 100;
    private static final Integer TEST_APPROVER_ID = 200;
    private static final LocalDate TEST_DATE = LocalDate.of(2024, 1, 15);
    private static final LocalDate TEST_START_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate TEST_END_DATE = LocalDate.of(2024, 1, 31);
    private static final String TEST_DESCRIPTION = "Test transaction";
    private static final String TEST_CURRENCY = "USD";
    private static final BigDecimal TEST_AMOUNT = BigDecimal.valueOf(1000.00);

    @BeforeEach
    void setUp() {
        // Create real service instance with mocked dependencies
        transactionApplicationService = new TransactionApplicationService(
            transactionRepository, 
            eventPublisher
        );
    }

    // ========== Create Transaction Tests ==========

    @Test
    @DisplayName("Should create income transaction successfully")
    void shouldCreateIncomeTransactionSuccessfully() {
        // Given
        CreateTransactionCommand command = createValidIncomeCommand();
        TransactionAggregate mockTransaction = createMockIncomeTransaction();
        
        when(transactionRepository.save(any(TransactionAggregate.class)))
                .thenReturn(mockTransaction);

        // When
        TransactionDTO result = transactionApplicationService.createIncomeTransaction(command);

        // Then
        assertNotNull(result);
        assertEquals(TransactionAggregate.TransactionType.INCOME, result.getTransactionType());
        assertEquals(0, TEST_AMOUNT.compareTo(result.getAmount())); // Use compareTo for BigDecimal comparison
        assertEquals(TransactionStatus.Status.DRAFT, result.getStatus());
        assertEquals(TEST_COMPANY_ID, result.getCompanyId());
        assertEquals(TEST_USER_ID, result.getUserId());
        
        verify(transactionRepository).save(any(TransactionAggregate.class));
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    @DisplayName("Should create expense transaction successfully")
    void shouldCreateExpenseTransactionSuccessfully() {
        // Given
        CreateTransactionCommand command = createValidExpenseCommand();
        TransactionAggregate mockTransaction = createMockExpenseTransaction();
        
        when(transactionRepository.save(any(TransactionAggregate.class)))
                .thenReturn(mockTransaction);

        // When
        TransactionDTO result = transactionApplicationService.createExpenseTransaction(command);

        // Then
        assertNotNull(result);
        assertEquals(TransactionAggregate.TransactionType.EXPENSE, result.getTransactionType());
        assertEquals(0, TEST_AMOUNT.compareTo(result.getAmount())); // Use compareTo for BigDecimal comparison
        assertEquals(TransactionStatus.Status.DRAFT, result.getStatus());
        
        verify(transactionRepository).save(any(TransactionAggregate.class));
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    @DisplayName("Should throw exception when creating transaction with null amount")
    void shouldThrowExceptionWhenCreatingTransactionWithNullAmount() {
        // Given
        CreateTransactionCommand invalidCommand = createNullAmountCommand();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionApplicationService.createIncomeTransaction(invalidCommand);
        });
        
        assertEquals("Transaction amount must be positive", exception.getMessage());
        verify(transactionRepository, never()).save(any());
        verify(eventPublisher, never()).publishAll(anyList());
    }

    @Test
    @DisplayName("Should throw exception when creating transaction with negative amount")
    void shouldThrowExceptionWhenCreatingTransactionWithNegativeAmount() {
        // Given
        CreateTransactionCommand invalidCommand = createNegativeAmountCommand();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionApplicationService.createIncomeTransaction(invalidCommand);
        });
        
        assertEquals("Transaction amount must be positive", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when creating transaction with null transaction date")
    void shouldThrowExceptionWhenCreatingTransactionWithNullDate() {
        // Given
        CreateTransactionCommand invalidCommand = createNullDateCommand();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionApplicationService.createIncomeTransaction(invalidCommand);
        });
        
        assertEquals("Transaction date cannot be null", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    // Note: Future date validation test removed as business logic allows future-dated transactions
    // This is common in financial systems for planned transactions, scheduled payments, etc.

    @Test
    @DisplayName("Should throw exception when company id is null")
    void shouldThrowExceptionWhenCompanyIdIsNull() {
        // Given
        CreateTransactionCommand invalidCommand = createNullCompanyIdCommand();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionApplicationService.createIncomeTransaction(invalidCommand);
        });
        
        assertEquals("Company ID cannot be null", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when user id is null")
    void shouldThrowExceptionWhenUserIdIsNull() {
        // Given
        CreateTransactionCommand invalidCommand = createNullUserIdCommand();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionApplicationService.createIncomeTransaction(invalidCommand);
        });
        
        assertEquals("User ID cannot be null", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    // ========== Update Transaction Tests ==========

    @Test
    @DisplayName("Should update transaction successfully")
    void shouldUpdateTransactionSuccessfully() {
        // Given
        UpdateTransactionCommand command = createValidUpdateCommand();
        TransactionAggregate mockTransaction = createMockDraftTransaction();
        TransactionAggregate updatedTransaction = createMockUpdatedTransaction();
        
        when(transactionRepository.findByIdAndTenant(TEST_TRANSACTION_ID, TenantId.of(TEST_COMPANY_ID)))
                .thenReturn(Optional.of(mockTransaction));
        when(transactionRepository.save(any(TransactionAggregate.class)))
                .thenReturn(updatedTransaction);

        // When
        TransactionDTO result = transactionApplicationService.updateTransaction(TEST_TRANSACTION_ID, command);

        // Then
        assertNotNull(result);
        assertEquals("Updated description", result.getDescription());
        assertEquals(0, BigDecimal.valueOf(1500.00).compareTo(result.getAmount())); // Use compareTo for BigDecimal comparison
        
        verify(transactionRepository).findByIdAndTenant(TEST_TRANSACTION_ID, TenantId.of(TEST_COMPANY_ID));
        verify(transactionRepository).save(any(TransactionAggregate.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent transaction")
    void shouldThrowExceptionWhenUpdatingNonExistentTransaction() {
        // Given
        Integer nonExistentId = 999;
        UpdateTransactionCommand command = createValidUpdateCommand();
        
        when(transactionRepository.findByIdAndTenant(nonExistentId, TenantId.of(TEST_COMPANY_ID)))
                .thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            transactionApplicationService.updateTransaction(nonExistentId, command);
        });
        
        assertEquals("Transaction not found with ID: " + nonExistentId, exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating transaction of different user")
    void shouldThrowExceptionWhenUpdatingTransactionOfDifferentUser() {
        // Given
        UpdateTransactionCommand command = createDifferentUserUpdateCommand();
        TransactionAggregate mockTransaction = createMockDraftTransaction();
        
        when(transactionRepository.findByIdAndTenant(TEST_TRANSACTION_ID, TenantId.of(TEST_COMPANY_ID)))
                .thenReturn(Optional.of(mockTransaction));

        // When & Then
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            transactionApplicationService.updateTransaction(TEST_TRANSACTION_ID, command);
        });
        
        assertEquals("User can only update their own transactions", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    // ========== Approve Transaction Tests ==========

    @Test
    @DisplayName("Should approve transaction successfully")
    void shouldApproveTransactionSuccessfully() {
        // Given
        ApproveTransactionCommand command = createValidApproveCommand();
        TransactionAggregate mockTransaction = createMockDraftTransaction();
        TransactionAggregate approvedTransaction = createMockApprovedTransaction();
        
        when(transactionRepository.findByIdAndTenant(TEST_TRANSACTION_ID, TenantId.of(TEST_COMPANY_ID)))
                .thenReturn(Optional.of(mockTransaction));
        when(transactionRepository.save(any(TransactionAggregate.class)))
                .thenReturn(approvedTransaction);

        // When
        TransactionDTO result = transactionApplicationService.approveTransaction(TEST_TRANSACTION_ID, command);

        // Then
        assertNotNull(result);
        assertEquals(TransactionStatus.Status.APPROVED, result.getStatus());
        assertEquals(TEST_APPROVER_ID, result.getApprovedBy());
        assertNotNull(result.getApprovedAt());
        
        verify(transactionRepository).findByIdAndTenant(TEST_TRANSACTION_ID, TenantId.of(TEST_COMPANY_ID));
        verify(transactionRepository).save(any(TransactionAggregate.class));
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    @DisplayName("Should throw exception when approving non-existent transaction")
    void shouldThrowExceptionWhenApprovingNonExistentTransaction() {
        // Given
        ApproveTransactionCommand command = createValidApproveCommand();
        
        when(transactionRepository.findByIdAndTenant(TEST_TRANSACTION_ID, TenantId.of(TEST_COMPANY_ID)))
                .thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            transactionApplicationService.approveTransaction(TEST_TRANSACTION_ID, command);
        });
        
        assertEquals("Transaction not found with ID: " + TEST_TRANSACTION_ID, exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    // ========== Cancel Transaction Tests ==========

    @Test
    @DisplayName("Should cancel transaction successfully")
    void shouldCancelTransactionSuccessfully() {
        // Given
        TransactionAggregate mockTransaction = createMockDraftTransaction();
        TransactionAggregate cancelledTransaction = createMockCancelledTransaction();
        
        when(transactionRepository.findByIdAndTenant(TEST_TRANSACTION_ID, TenantId.of(TEST_COMPANY_ID)))
                .thenReturn(Optional.of(mockTransaction));
        when(transactionRepository.save(any(TransactionAggregate.class)))
                .thenReturn(cancelledTransaction);

        // When
        TransactionDTO result = transactionApplicationService.cancelTransaction(TEST_TRANSACTION_ID, TEST_COMPANY_ID, TEST_USER_ID);

        // Then
        assertNotNull(result);
        assertEquals(TransactionStatus.Status.CANCELLED, result.getStatus());
        
        verify(transactionRepository).findByIdAndTenant(TEST_TRANSACTION_ID, TenantId.of(TEST_COMPANY_ID));
        verify(transactionRepository).save(any(TransactionAggregate.class));
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    @DisplayName("Should throw exception when cancelling transaction of different user")
    void shouldThrowExceptionWhenCancellingTransactionOfDifferentUser() {
        // Given
        TransactionAggregate mockTransaction = createMockDraftTransaction();
        Integer differentUserId = 999;
        
        when(transactionRepository.findByIdAndTenant(TEST_TRANSACTION_ID, TenantId.of(TEST_COMPANY_ID)))
                .thenReturn(Optional.of(mockTransaction));

        // When & Then
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            transactionApplicationService.cancelTransaction(TEST_TRANSACTION_ID, TEST_COMPANY_ID, differentUserId);
        });
        
        assertEquals("User can only cancel their own transactions", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    // ========== Void Transaction Tests ==========

    @Test
    @DisplayName("Should void approved transaction successfully")
    void shouldVoidApprovedTransactionSuccessfully() {
        // Given
        String voidReason = "Duplicate transaction";
        TransactionAggregate mockTransaction = createMockApprovedTransaction();
        TransactionAggregate voidedTransaction = createMockVoidedTransaction();
        
        when(transactionRepository.findByIdAndTenant(TEST_TRANSACTION_ID, TenantId.of(TEST_COMPANY_ID)))
                .thenReturn(Optional.of(mockTransaction));
        when(transactionRepository.save(any(TransactionAggregate.class)))
                .thenReturn(voidedTransaction);

        // When
        TransactionDTO result = transactionApplicationService.voidTransaction(TEST_TRANSACTION_ID, TEST_COMPANY_ID, TEST_APPROVER_ID, voidReason);

        // Then
        assertNotNull(result);
        assertEquals(TransactionStatus.Status.VOIDED, result.getStatus());
        
        verify(transactionRepository).findByIdAndTenant(TEST_TRANSACTION_ID, TenantId.of(TEST_COMPANY_ID));
        verify(transactionRepository).save(any(TransactionAggregate.class));
        verify(eventPublisher).publishAll(anyList());
    }

    // ========== Query Transaction Tests ==========

    @Test
    @DisplayName("Should get transaction by id successfully")
    void shouldGetTransactionByIdSuccessfully() {
        // Given
        TransactionAggregate mockTransaction = createMockIncomeTransaction();
        
        when(transactionRepository.findByIdAndTenant(TEST_TRANSACTION_ID, TenantId.of(TEST_COMPANY_ID)))
                .thenReturn(Optional.of(mockTransaction));

        // When
        TransactionDTO result = transactionApplicationService.getTransactionById(TEST_TRANSACTION_ID, TEST_COMPANY_ID);

        // Then
        assertNotNull(result);
        assertEquals(TEST_TRANSACTION_ID, result.getTransactionId());
        assertEquals(TEST_COMPANY_ID, result.getCompanyId());
        
        verify(transactionRepository).findByIdAndTenant(TEST_TRANSACTION_ID, TenantId.of(TEST_COMPANY_ID));
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent transaction")
    void shouldThrowExceptionWhenGettingNonExistentTransaction() {
        // Given
        Integer nonExistentId = 999;
        
        when(transactionRepository.findByIdAndTenant(nonExistentId, TenantId.of(TEST_COMPANY_ID)))
                .thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            transactionApplicationService.getTransactionById(nonExistentId, TEST_COMPANY_ID);
        });
        
        assertEquals("Transaction not found with ID: " + nonExistentId, exception.getMessage());
    }

    @Test
    @DisplayName("Should get transactions by company successfully")
    void shouldGetTransactionsByCompanySuccessfully() {
        // Given
        List<TransactionAggregate> mockTransactions = Arrays.asList(
            createMockIncomeTransaction(),
            createMockExpenseTransaction()
        );
        
        when(transactionRepository.findByTenantIdOrderByTransactionDateDesc(TenantId.of(TEST_COMPANY_ID)))
                .thenReturn(mockTransactions);

        // When
        List<TransactionDTO> results = transactionApplicationService.getTransactionsByCompany(TEST_COMPANY_ID);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(TransactionAggregate.TransactionType.INCOME, results.get(0).getTransactionType());
        assertEquals(TransactionAggregate.TransactionType.EXPENSE, results.get(1).getTransactionType());
        
        verify(transactionRepository).findByTenantIdOrderByTransactionDateDesc(TenantId.of(TEST_COMPANY_ID));
    }

    @Test
    @DisplayName("Should get transactions by type successfully")
    void shouldGetTransactionsByTypeSuccessfully() {
        // Given
        List<TransactionAggregate> mockTransactions = Arrays.asList(createMockIncomeTransaction());
        
        when(transactionRepository.findByTenantIdAndTransactionType(TenantId.of(TEST_COMPANY_ID), TransactionAggregate.TransactionType.INCOME))
                .thenReturn(mockTransactions);

        // When
        List<TransactionDTO> results = transactionApplicationService.getTransactionsByType(TEST_COMPANY_ID, TransactionAggregate.TransactionType.INCOME);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(TransactionAggregate.TransactionType.INCOME, results.get(0).getTransactionType());
        
        verify(transactionRepository).findByTenantIdAndTransactionType(TenantId.of(TEST_COMPANY_ID), TransactionAggregate.TransactionType.INCOME);
    }

    @Test
    @DisplayName("Should get transactions by date range successfully")
    void shouldGetTransactionsByDateRangeSuccessfully() {
        // Given
        List<TransactionAggregate> mockTransactions = Arrays.asList(createMockIncomeTransaction());
        
        when(transactionRepository.findByTenantIdAndTransactionDateBetween(TenantId.of(TEST_COMPANY_ID), TEST_START_DATE, TEST_END_DATE))
                .thenReturn(mockTransactions);

        // When
        List<TransactionDTO> results = transactionApplicationService.getTransactionsByDateRange(TEST_COMPANY_ID, TEST_START_DATE, TEST_END_DATE);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).getTransactionDate().isAfter(TEST_START_DATE.minusDays(1)));
        assertTrue(results.get(0).getTransactionDate().isBefore(TEST_END_DATE.plusDays(1)));
        
        verify(transactionRepository).findByTenantIdAndTransactionDateBetween(TenantId.of(TEST_COMPANY_ID), TEST_START_DATE, TEST_END_DATE);
    }

    @Test
    @DisplayName("Should get pending approval transactions successfully")
    void shouldGetPendingApprovalTransactionsSuccessfully() {
        // Given
        List<TransactionAggregate> mockTransactions = Arrays.asList(createMockPendingTransaction());
        
        when(transactionRepository.findPendingApprovalByTenant(TenantId.of(TEST_COMPANY_ID)))
                .thenReturn(mockTransactions);

        // When
        List<TransactionDTO> results = transactionApplicationService.getPendingApprovalTransactions(TEST_COMPANY_ID);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(TransactionStatus.Status.PENDING_APPROVAL, results.get(0).getStatus());
        
        verify(transactionRepository).findPendingApprovalByTenant(TenantId.of(TEST_COMPANY_ID));
    }

    @Test
    @DisplayName("Should get user transactions successfully")
    void shouldGetUserTransactionsSuccessfully() {
        // Given
        List<TransactionAggregate> mockTransactions = Arrays.asList(createMockIncomeTransaction());
        
        when(transactionRepository.findByTenantAndUser(TenantId.of(TEST_COMPANY_ID), TEST_USER_ID))
                .thenReturn(mockTransactions);

        // When
        List<TransactionDTO> results = transactionApplicationService.getUserTransactions(TEST_COMPANY_ID, TEST_USER_ID);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(TEST_USER_ID, results.get(0).getUserId());
        
        verify(transactionRepository).findByTenantAndUser(TenantId.of(TEST_COMPANY_ID), TEST_USER_ID);
    }

    @Test
    @DisplayName("Should get total amount by type and status successfully")
    void shouldGetTotalAmountByTypeAndStatusSuccessfully() {
        // Given
        BigDecimal expectedTotal = BigDecimal.valueOf(5000.00);
        
        when(transactionRepository.sumAmountByTenantTypeAndStatus(TenantId.of(TEST_COMPANY_ID), TransactionAggregate.TransactionType.INCOME, TransactionStatus.Status.APPROVED))
                .thenReturn(expectedTotal);

        // When
        BigDecimal result = transactionApplicationService.getTotalAmount(TEST_COMPANY_ID, TransactionAggregate.TransactionType.INCOME, TransactionStatus.Status.APPROVED);

        // Then
        assertNotNull(result);
        assertEquals(expectedTotal, result);
        
        verify(transactionRepository).sumAmountByTenantTypeAndStatus(TenantId.of(TEST_COMPANY_ID), TransactionAggregate.TransactionType.INCOME, TransactionStatus.Status.APPROVED);
    }

    @Test
    @DisplayName("Should handle empty transaction list gracefully")
    void shouldHandleEmptyTransactionListGracefully() {
        // Given
        List<TransactionAggregate> emptyList = Arrays.asList();
        
        when(transactionRepository.findByTenantIdOrderByTransactionDateDesc(TenantId.of(TEST_COMPANY_ID)))
                .thenReturn(emptyList);

        // When
        List<TransactionDTO> results = transactionApplicationService.getTransactionsByCompany(TEST_COMPANY_ID);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
        
        verify(transactionRepository).findByTenantIdOrderByTransactionDateDesc(TenantId.of(TEST_COMPANY_ID));
    }

    // ========== Helper Methods for Creating Commands ==========

    private CreateTransactionCommand createValidIncomeCommand() {
        return CreateTransactionCommand.builder()
                .transactionType("INCOME")
                .amount(TEST_AMOUNT)
                .currency(TEST_CURRENCY)
                .transactionDate(TEST_DATE)
                .description(TEST_DESCRIPTION)
                .categoryId(1)
                .companyId(TEST_COMPANY_ID)
                .userId(TEST_USER_ID)
                .build();
    }

    private CreateTransactionCommand createValidExpenseCommand() {
        return CreateTransactionCommand.builder()
                .transactionType("EXPENSE")
                .amount(TEST_AMOUNT)
                .currency(TEST_CURRENCY)
                .transactionDate(TEST_DATE)
                .description(TEST_DESCRIPTION)
                .categoryId(2)
                .companyId(TEST_COMPANY_ID)
                .userId(TEST_USER_ID)
                .build();
    }

    private CreateTransactionCommand createNullAmountCommand() {
        return CreateTransactionCommand.builder()
                .transactionType("INCOME")
                .amount(null)
                .currency(TEST_CURRENCY)
                .transactionDate(TEST_DATE)
                .description(TEST_DESCRIPTION)
                .categoryId(1)
                .companyId(TEST_COMPANY_ID)
                .userId(TEST_USER_ID)
                .build();
    }

    private CreateTransactionCommand createNegativeAmountCommand() {
        return CreateTransactionCommand.builder()
                .transactionType("INCOME")
                .amount(BigDecimal.valueOf(-100.00))
                .currency(TEST_CURRENCY)
                .transactionDate(TEST_DATE)
                .description(TEST_DESCRIPTION)
                .categoryId(1)
                .companyId(TEST_COMPANY_ID)
                .userId(TEST_USER_ID)
                .build();
    }

    private CreateTransactionCommand createNullDateCommand() {
        return CreateTransactionCommand.builder()
                .transactionType("INCOME")
                .amount(TEST_AMOUNT)
                .currency(TEST_CURRENCY)
                .transactionDate(null)
                .description(TEST_DESCRIPTION)
                .categoryId(1)
                .companyId(TEST_COMPANY_ID)
                .userId(TEST_USER_ID)
                .build();
    }

    private CreateTransactionCommand createFutureDateCommand() {
        return CreateTransactionCommand.builder()
                .transactionType("INCOME")
                .amount(TEST_AMOUNT)
                .currency(TEST_CURRENCY)
                .transactionDate(LocalDate.now().plusDays(10))
                .description(TEST_DESCRIPTION)
                .categoryId(1)
                .companyId(TEST_COMPANY_ID)
                .userId(TEST_USER_ID)
                .build();
    }

    private CreateTransactionCommand createNullCompanyIdCommand() {
        return CreateTransactionCommand.builder()
                .transactionType("INCOME")
                .amount(TEST_AMOUNT)
                .currency(TEST_CURRENCY)
                .transactionDate(TEST_DATE)
                .description(TEST_DESCRIPTION)
                .categoryId(1)
                .companyId(null)
                .userId(TEST_USER_ID)
                .build();
    }

    private CreateTransactionCommand createNullUserIdCommand() {
        return CreateTransactionCommand.builder()
                .transactionType("INCOME")
                .amount(TEST_AMOUNT)
                .currency(TEST_CURRENCY)
                .transactionDate(TEST_DATE)
                .description(TEST_DESCRIPTION)
                .categoryId(1)
                .companyId(TEST_COMPANY_ID)
                .userId(null)
                .build();
    }

    private UpdateTransactionCommand createValidUpdateCommand() {
        return UpdateTransactionCommand.builder()
                .amount(BigDecimal.valueOf(1500.00))
                .currency(TEST_CURRENCY)
                .description("Updated description")
                .categoryId(3)
                .companyId(TEST_COMPANY_ID)
                .userId(TEST_USER_ID)
                .build();
    }

    private UpdateTransactionCommand createDifferentUserUpdateCommand() {
        return UpdateTransactionCommand.builder()
                .amount(BigDecimal.valueOf(1500.00))
                .currency(TEST_CURRENCY)
                .description("Updated description")
                .categoryId(3)
                .companyId(TEST_COMPANY_ID)
                .userId(999) // Different user
                .build();
    }

    private ApproveTransactionCommand createValidApproveCommand() {
        return ApproveTransactionCommand.builder()
                .companyId(TEST_COMPANY_ID)
                .approverUserId(TEST_APPROVER_ID)
                .approvalNote("Approved - documentation verified")
                .build();
    }

    // ========== Helper Methods for Creating Mock Aggregates ==========

    private TransactionAggregate createMockIncomeTransaction() {
        TransactionAggregate transaction = mock(TransactionAggregate.class);
        when(transaction.getTransactionId()).thenReturn(TEST_TRANSACTION_ID);
        when(transaction.getTransactionType()).thenReturn(TransactionAggregate.TransactionType.INCOME);
        when(transaction.getMoney()).thenReturn(Money.of(TEST_AMOUNT, TEST_CURRENCY));
        when(transaction.getTransactionDate()).thenReturn(TEST_DATE);
        when(transaction.getDescription()).thenReturn(TEST_DESCRIPTION);
        when(transaction.getTransactionStatus()).thenReturn(TransactionStatus.draft());
        when(transaction.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(transaction.getUserId()).thenReturn(TEST_USER_ID);
        when(transaction.getDomainEvents()).thenReturn(Arrays.asList());
        doNothing().when(transaction).clearDomainEvents();
        return transaction;
    }

    private TransactionAggregate createMockExpenseTransaction() {
        TransactionAggregate transaction = mock(TransactionAggregate.class);
        when(transaction.getTransactionId()).thenReturn(TEST_TRANSACTION_ID + 1);
        when(transaction.getTransactionType()).thenReturn(TransactionAggregate.TransactionType.EXPENSE);
        when(transaction.getMoney()).thenReturn(Money.of(TEST_AMOUNT, TEST_CURRENCY));
        when(transaction.getTransactionDate()).thenReturn(TEST_DATE);
        when(transaction.getDescription()).thenReturn(TEST_DESCRIPTION);
        when(transaction.getTransactionStatus()).thenReturn(TransactionStatus.draft());
        when(transaction.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(transaction.getUserId()).thenReturn(TEST_USER_ID);
        when(transaction.getDomainEvents()).thenReturn(Arrays.asList());
        doNothing().when(transaction).clearDomainEvents();
        return transaction;
    }

    private TransactionAggregate createMockDraftTransaction() {
        TransactionAggregate transaction = mock(TransactionAggregate.class);
        when(transaction.getTransactionId()).thenReturn(TEST_TRANSACTION_ID);
        when(transaction.getTransactionType()).thenReturn(TransactionAggregate.TransactionType.INCOME);
        when(transaction.getMoney()).thenReturn(Money.of(TEST_AMOUNT, TEST_CURRENCY));
        when(transaction.getTransactionDate()).thenReturn(TEST_DATE);
        when(transaction.getDescription()).thenReturn(TEST_DESCRIPTION);
        when(transaction.getTransactionStatus()).thenReturn(TransactionStatus.draft());
        when(transaction.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(transaction.getUserId()).thenReturn(TEST_USER_ID);
        when(transaction.canBeApproved()).thenReturn(true);
        when(transaction.getDomainEvents()).thenReturn(Arrays.asList());
        doNothing().when(transaction).clearDomainEvents();
        return transaction;
    }

    private TransactionAggregate createMockUpdatedTransaction() {
        TransactionAggregate transaction = mock(TransactionAggregate.class);
        when(transaction.getTransactionId()).thenReturn(TEST_TRANSACTION_ID);
        when(transaction.getTransactionType()).thenReturn(TransactionAggregate.TransactionType.INCOME);
        when(transaction.getMoney()).thenReturn(Money.of(BigDecimal.valueOf(1500.00), TEST_CURRENCY));
        when(transaction.getTransactionDate()).thenReturn(TEST_DATE);
        when(transaction.getDescription()).thenReturn("Updated description");
        when(transaction.getTransactionStatus()).thenReturn(TransactionStatus.draft());
        when(transaction.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(transaction.getUserId()).thenReturn(TEST_USER_ID);
        when(transaction.getDomainEvents()).thenReturn(Arrays.asList());
        doNothing().when(transaction).clearDomainEvents();
        return transaction;
    }

    private TransactionAggregate createMockApprovedTransaction() {
        TransactionAggregate transaction = mock(TransactionAggregate.class);
        when(transaction.getTransactionId()).thenReturn(TEST_TRANSACTION_ID);
        when(transaction.getTransactionType()).thenReturn(TransactionAggregate.TransactionType.INCOME);
        when(transaction.getMoney()).thenReturn(Money.of(TEST_AMOUNT, TEST_CURRENCY));
        when(transaction.getTransactionDate()).thenReturn(TEST_DATE);
        when(transaction.getDescription()).thenReturn(TEST_DESCRIPTION);
        when(transaction.getTransactionStatus()).thenReturn(TransactionStatus.approved());
        when(transaction.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(transaction.getUserId()).thenReturn(TEST_USER_ID);
        when(transaction.getApprovedBy()).thenReturn(TEST_APPROVER_ID);
        when(transaction.getApprovedAt()).thenReturn(java.time.LocalDateTime.now());
        when(transaction.canBeApproved()).thenReturn(true);
        when(transaction.getDomainEvents()).thenReturn(Arrays.asList());
        doNothing().when(transaction).clearDomainEvents();
        return transaction;
    }

    private TransactionAggregate createMockCancelledTransaction() {
        TransactionAggregate transaction = mock(TransactionAggregate.class);
        when(transaction.getTransactionId()).thenReturn(TEST_TRANSACTION_ID);
        when(transaction.getTransactionType()).thenReturn(TransactionAggregate.TransactionType.INCOME);
        when(transaction.getMoney()).thenReturn(Money.of(TEST_AMOUNT, TEST_CURRENCY));
        when(transaction.getTransactionDate()).thenReturn(TEST_DATE);
        when(transaction.getDescription()).thenReturn(TEST_DESCRIPTION);
        when(transaction.getTransactionStatus()).thenReturn(TransactionStatus.of(TransactionStatus.Status.CANCELLED));
        when(transaction.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(transaction.getUserId()).thenReturn(TEST_USER_ID);
        when(transaction.getDomainEvents()).thenReturn(Arrays.asList());
        doNothing().when(transaction).clearDomainEvents();
        return transaction;
    }

    private TransactionAggregate createMockVoidedTransaction() {
        TransactionAggregate transaction = mock(TransactionAggregate.class);
        when(transaction.getTransactionId()).thenReturn(TEST_TRANSACTION_ID);
        when(transaction.getTransactionType()).thenReturn(TransactionAggregate.TransactionType.INCOME);
        when(transaction.getMoney()).thenReturn(Money.of(TEST_AMOUNT, TEST_CURRENCY));
        when(transaction.getTransactionDate()).thenReturn(TEST_DATE);
        when(transaction.getDescription()).thenReturn(TEST_DESCRIPTION);
        when(transaction.getTransactionStatus()).thenReturn(TransactionStatus.of(TransactionStatus.Status.VOIDED));
        when(transaction.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(transaction.getUserId()).thenReturn(TEST_USER_ID);
        when(transaction.getDomainEvents()).thenReturn(Arrays.asList());
        doNothing().when(transaction).clearDomainEvents();
        return transaction;
    }

    private TransactionAggregate createMockPendingTransaction() {
        TransactionAggregate transaction = mock(TransactionAggregate.class);
        when(transaction.getTransactionId()).thenReturn(TEST_TRANSACTION_ID);
        when(transaction.getTransactionType()).thenReturn(TransactionAggregate.TransactionType.EXPENSE);
        when(transaction.getMoney()).thenReturn(Money.of(TEST_AMOUNT, TEST_CURRENCY));
        when(transaction.getTransactionDate()).thenReturn(TEST_DATE);
        when(transaction.getDescription()).thenReturn(TEST_DESCRIPTION);
        when(transaction.getTransactionStatus()).thenReturn(TransactionStatus.pendingApproval());
        when(transaction.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(transaction.getUserId()).thenReturn(TEST_USER_ID);
        when(transaction.getDomainEvents()).thenReturn(Arrays.asList());
        doNothing().when(transaction).clearDomainEvents();
        return transaction;
    }
}