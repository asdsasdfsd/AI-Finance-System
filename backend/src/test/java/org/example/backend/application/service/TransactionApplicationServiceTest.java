// backend/src/test/java/org/example/backend/application/service/TransactionApplicationServiceTest.java
package org.example.backend.application.service;

import org.example.backend.application.dto.CreateTransactionCommand;
import org.example.backend.application.dto.UpdateTransactionCommand;
import org.example.backend.application.dto.ApproveTransactionCommand;
import org.example.backend.application.dto.TransactionDTO;
import org.example.backend.domain.aggregate.company.CompanyAggregate;
import org.example.backend.domain.aggregate.company.CompanyAggregateRepository;
import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.aggregate.transaction.TransactionAggregateRepository;
import org.example.backend.domain.event.DomainEventPublisher;
import org.example.backend.domain.valueobject.Money;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.TransactionStatus;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionApplicationService - Fixed Version
 */
@ExtendWith(MockitoExtension.class)
class TransactionApplicationServiceTest {

    @Mock
    private TransactionAggregateRepository transactionRepository;

    @Mock
    private CompanyAggregateRepository companyRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private TransactionApplicationService transactionApplicationService;

    // Test constants
    private static final Integer TEST_TRANSACTION_ID = 1001;
    private static final Integer TEST_COMPANY_ID = 1;
    private static final Integer TEST_USER_ID = 100;
    private static final Integer TEST_APPROVER_ID = 200;
    private static final LocalDate TEST_DATE = LocalDate.of(2024, 1, 15);
    private static final String TEST_DESCRIPTION = "Test transaction";
    private static final String TEST_CURRENCY = "CNY";
    private static final BigDecimal TEST_AMOUNT = BigDecimal.valueOf(1000.00);

    private TransactionAggregate testTransaction;
    private CompanyAggregate testCompany;

    @BeforeEach
    void setUp() {
        testTransaction = createMockTransaction();
        testCompany = createMockCompany();
    }

    // ========== Create Transaction Tests ==========

    @Test
    @DisplayName("Should create income transaction successfully")
    void shouldCreateIncomeTransactionSuccessfully() {
        // Given
        CreateTransactionCommand command = createValidIncomeCommand();
        when(companyRepository.findById(TEST_COMPANY_ID)).thenReturn(Optional.of(testCompany));
        when(transactionRepository.save(any(TransactionAggregate.class))).thenReturn(testTransaction);

        // When
        TransactionDTO result = transactionApplicationService.createIncomeTransaction(command);

        // Then
        assertNotNull(result);
        assertEquals(testTransaction.getTransactionId(), result.getTransactionId());
        
        verify(companyRepository).findById(TEST_COMPANY_ID);
        verify(transactionRepository).save(any(TransactionAggregate.class));
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    @DisplayName("Should create expense transaction successfully")
    void shouldCreateExpenseTransactionSuccessfully() {
        // Given
        CreateTransactionCommand command = createValidExpenseCommand();
        when(companyRepository.findById(TEST_COMPANY_ID)).thenReturn(Optional.of(testCompany));
        when(transactionRepository.save(any(TransactionAggregate.class))).thenReturn(testTransaction);

        // When
        TransactionDTO result = transactionApplicationService.createExpenseTransaction(command);

        // Then
        assertNotNull(result);
        verify(companyRepository).findById(TEST_COMPANY_ID);
        verify(transactionRepository).save(any(TransactionAggregate.class));
    }

    @Test
    @DisplayName("Should throw exception when company not found")
    void shouldThrowExceptionWhenCompanyNotFound() {
        // Given
        CreateTransactionCommand command = createValidIncomeCommand();
        when(companyRepository.findById(TEST_COMPANY_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            transactionApplicationService.createIncomeTransaction(command);
        });
    }

    // ========== Update Transaction Tests ==========

    @Test
    @DisplayName("Should update transaction successfully")
    void shouldUpdateTransactionSuccessfully() {
        // Given
        UpdateTransactionCommand command = createValidUpdateCommand();
        when(transactionRepository.findByIdAndTenant(TEST_TRANSACTION_ID, TEST_COMPANY_ID))
                .thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(TransactionAggregate.class))).thenReturn(testTransaction);

        // When
        TransactionDTO result = transactionApplicationService.updateTransaction(TEST_TRANSACTION_ID, command);

        // Then
        assertNotNull(result);
        verify(transactionRepository).findByIdAndTenant(TEST_TRANSACTION_ID, TEST_COMPANY_ID);
        verify(transactionRepository).save(testTransaction);
    }

    @Test
    @DisplayName("Should throw exception when user tries to update others transaction")
    void shouldThrowExceptionWhenUserTriesToUpdateOthersTransaction() {
        // Given
        UpdateTransactionCommand command = createValidUpdateCommand();
        command.setUserId(999); // Different user
        when(transactionRepository.findByIdAndTenant(TEST_TRANSACTION_ID, TEST_COMPANY_ID))
                .thenReturn(Optional.of(testTransaction));

        // When & Then
        assertThrows(UnauthorizedException.class, () -> {
            transactionApplicationService.updateTransaction(TEST_TRANSACTION_ID, command);
        });
    }

    // ========== Approve Transaction Tests ==========

    @Test
    @DisplayName("Should approve transaction successfully")
    void shouldApproveTransactionSuccessfully() {
        // Given
        ApproveTransactionCommand command = createValidApproveCommand();
        when(transactionRepository.findByIdAndTenant(TEST_TRANSACTION_ID, TEST_COMPANY_ID))
                .thenReturn(Optional.of(testTransaction));
        when(testTransaction.canBeApproved()).thenReturn(true);
        when(transactionRepository.save(any(TransactionAggregate.class))).thenReturn(testTransaction);

        // When
        TransactionDTO result = transactionApplicationService.approveTransaction(TEST_TRANSACTION_ID, command);

        // Then
        assertNotNull(result);
        verify(transactionRepository).findByIdAndTenant(TEST_TRANSACTION_ID, TEST_COMPANY_ID);
        verify(testTransaction).approve(TEST_APPROVER_ID);
        verify(transactionRepository).save(testTransaction);
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    @DisplayName("Should throw exception when transaction cannot be approved")
    void shouldThrowExceptionWhenTransactionCannotBeApproved() {
        // Given
        ApproveTransactionCommand command = createValidApproveCommand();
        when(transactionRepository.findByIdAndTenant(TEST_TRANSACTION_ID, TEST_COMPANY_ID))
                .thenReturn(Optional.of(testTransaction));
        when(testTransaction.canBeApproved()).thenReturn(false);

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            transactionApplicationService.approveTransaction(TEST_TRANSACTION_ID, command);
        });
    }

    // ========== Cancel Transaction Tests ==========

    @Test
    @DisplayName("Should cancel transaction successfully")
    void shouldCancelTransactionSuccessfully() {
        // Given
        when(transactionRepository.findByIdAndTenant(TEST_TRANSACTION_ID, TEST_COMPANY_ID))
                .thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(TransactionAggregate.class))).thenReturn(testTransaction);

        // When
        TransactionDTO result = transactionApplicationService.cancelTransaction(TEST_TRANSACTION_ID, TEST_COMPANY_ID, TEST_USER_ID);

        // Then
        assertNotNull(result);
        verify(transactionRepository).findByIdAndTenant(TEST_TRANSACTION_ID, TEST_COMPANY_ID);
        verify(testTransaction).cancel();
        verify(transactionRepository).save(testTransaction);
    }

    @Test
    @DisplayName("Should throw exception when user tries to cancel others transaction")
    void shouldThrowExceptionWhenUserTriesToCancelOthersTransaction() {
        // Given
        Integer otherUserId = 999;
        when(transactionRepository.findByIdAndTenant(TEST_TRANSACTION_ID, TEST_COMPANY_ID))
                .thenReturn(Optional.of(testTransaction));

        // When & Then
        assertThrows(UnauthorizedException.class, () -> {
            transactionApplicationService.cancelTransaction(TEST_TRANSACTION_ID, TEST_COMPANY_ID, otherUserId);
        });
    }

    // ========== Query Tests ==========

    @Test
    @DisplayName("Should get transactions by company successfully")
    void shouldGetTransactionsByCompanySuccessfully() {
        // Given
        List<TransactionAggregate> transactions = List.of(testTransaction);
        when(transactionRepository.findByTenantIdOrderByTransactionDateDesc(TEST_COMPANY_ID))
                .thenReturn(transactions);

        // When
        List<TransactionDTO> result = transactionApplicationService.getTransactionsByCompany(TEST_COMPANY_ID);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transactionRepository).findByTenantIdOrderByTransactionDateDesc(TEST_COMPANY_ID);
    }

    @Test
    @DisplayName("Should get pending transactions successfully")
    void shouldGetPendingTransactionsSuccessfully() {
        // Given
        List<TransactionAggregate> transactions = List.of(testTransaction);
        when(transactionRepository.findByTenantIdAndTransactionStatus_Status(TenantId.of(TEST_COMPANY_ID), TransactionStatus.Status.PENDING_APPROVAL))
                .thenReturn(transactions);

        // When
        List<TransactionDTO> result = transactionApplicationService.getPendingApprovalTransactions(TEST_COMPANY_ID);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transactionRepository).findByTenantIdAndTransactionStatus_Status(TenantId.of(TEST_COMPANY_ID), TransactionStatus.Status.PENDING_APPROVAL);
    }

    // ========== Helper Methods ==========

    private CreateTransactionCommand createValidIncomeCommand() {
        return CreateTransactionCommand.builder()
                .transactionType("INCOME")
                .amount(TEST_AMOUNT)
                .currency(TEST_CURRENCY)
                .transactionDate(TEST_DATE)
                .description(TEST_DESCRIPTION)
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
                .companyId(TEST_COMPANY_ID)
                .userId(TEST_USER_ID)
                .build();
    }

    private UpdateTransactionCommand createValidUpdateCommand() {
        return UpdateTransactionCommand.builder()
                .amount(BigDecimal.valueOf(1500.00))
                .currency(TEST_CURRENCY)
                .description("Updated description")
                .companyId(TEST_COMPANY_ID)
                .userId(TEST_USER_ID)
                .build();
    }

    private ApproveTransactionCommand createValidApproveCommand() {
        return ApproveTransactionCommand.builder()
                .companyId(TEST_COMPANY_ID)
                .approverUserId(TEST_APPROVER_ID)
                .build();
    }

    private TransactionAggregate createMockTransaction() {
        TransactionAggregate transaction = mock(TransactionAggregate.class);
        
        when(transaction.getTransactionId()).thenReturn(TEST_TRANSACTION_ID);
        when(transaction.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(transaction.getUserId()).thenReturn(TEST_USER_ID);
        when(transaction.getTransactionDate()).thenReturn(TEST_DATE);
        when(transaction.getDescription()).thenReturn(TEST_DESCRIPTION);
        when(transaction.getMoney()).thenReturn(Money.of(TEST_AMOUNT, TEST_CURRENCY));
        when(transaction.getTransactionType()).thenReturn(TransactionAggregate.TransactionType.INCOME);
        when(transaction.getTransactionStatus()).thenReturn(TransactionStatus.draft());
        when(transaction.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(transaction.getDomainEvents()).thenReturn(new ArrayList<>());
        
        // Mock behavior methods
        doNothing().when(transaction).updateTransaction(any(Money.class), anyString(), anyString(), anyString());
        doNothing().when(transaction).approve(anyInt());
        doNothing().when(transaction).cancel();
        doNothing().when(transaction).clearDomainEvents();
        
        return transaction;
    }

    private CompanyAggregate createMockCompany() {
        CompanyAggregate company = mock(CompanyAggregate.class);
        
        when(company.getCompanyId()).thenReturn(TEST_COMPANY_ID);
        when(company.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        
        return company;
    }
}