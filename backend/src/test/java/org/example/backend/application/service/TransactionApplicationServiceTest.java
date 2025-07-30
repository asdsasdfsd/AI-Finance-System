// backend/src/test/java/org/example/backend/application/service/TransactionApplicationServiceTest.java
package org.example.backend.application.service;

import org.example.backend.application.dto.CreateTransactionCommand;
import org.example.backend.application.dto.UpdateTransactionCommand;
import org.example.backend.application.dto.ApproveTransactionCommand;
import org.example.backend.application.dto.TransactionDTO;
import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.aggregate.transaction.TransactionAggregateRepository;
import org.example.backend.domain.aggregate.company.CompanyAggregateRepository;
import org.example.backend.domain.aggregate.company.CompanyAggregate;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.Money;
import org.example.backend.domain.valueobject.TransactionStatus;
import org.example.backend.domain.event.DomainEventPublisher;
import org.example.backend.exception.ResourceNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionApplicationService
 * 
 * Tests transaction management functionality including creation, updates, approval workflows, and queries
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionApplicationService Tests")
class TransactionApplicationServiceTest {
    
    @Mock
    private TransactionAggregateRepository transactionRepository;
    
    @Mock
    private CompanyAggregateRepository companyRepository;
    
    @Mock
    private DomainEventPublisher eventPublisher;
    
    @InjectMocks
    private TransactionApplicationService transactionApplicationService;
    
    private CreateTransactionCommand createCommand;
    private UpdateTransactionCommand updateCommand;
    private ApproveTransactionCommand approveCommand;
    private TransactionAggregate testTransaction;
    private CompanyAggregate testCompany;
    
    @BeforeEach
    void setUp() {
        testCompany = CompanyAggregate.create(
            "Test Company",
            "test@company.com",
            "Test Address",
            "BL123456",
            1
        );
        
        createCommand = CreateTransactionCommand.builder()
            .amount(new BigDecimal("1000.00"))
            .currency("CNY")
            .description("Test Transaction")
            .transactionDate(LocalDate.now())
            .categoryId(1)
            .companyId(1)
            .userId(1)
            .departmentId(1)
            .fundId(1)
            .build();
            
        updateCommand = UpdateTransactionCommand.builder()
            .amount(new BigDecimal("1500.00"))
            .currency("CNY")
            .description("Updated Transaction")
            .categoryId(2)
            .companyId(1)
            .departmentId(2)
            .fundId(2)
            .userId(1)
            .build();
            
        approveCommand = ApproveTransactionCommand.builder()
            .companyId(1)
            .approverId(2)
            .comments("Approved for processing")
            .build();
            
        testTransaction = TransactionAggregate.createExpense(
            Money.of(new BigDecimal("1000.00"), "CNY"),
            LocalDate.now(),
            "Test Transaction",
            TenantId.of(1),
            1
        );
    }
    
    @Nested
    @DisplayName("Create Transaction Tests")
    class CreateTransactionTests {
        
        @Test
        @DisplayName("Should create expense transaction successfully")
        void shouldCreateExpenseTransactionSuccessfully() {
            // Given
            when(companyRepository.findById(createCommand.getCompanyId())).thenReturn(Optional.of(testCompany));
            when(transactionRepository.save(any(TransactionAggregate.class))).thenReturn(testTransaction);
            
            // When
            TransactionDTO result = transactionApplicationService.createExpenseTransaction(createCommand);
            
            // Then
            assertNotNull(result);
            assertEquals(createCommand.getAmount(), result.getAmount());
            assertEquals(createCommand.getDescription(), result.getDescription());
            assertEquals("EXPENSE", result.getTransactionType());
            assertEquals("DRAFT", result.getStatus());
            
            verify(companyRepository).findById(createCommand.getCompanyId());
            verify(transactionRepository).save(any(TransactionAggregate.class));
            verify(eventPublisher).publishAll(any());
        }
        
        @Test
        @DisplayName("Should create income transaction successfully")
        void shouldCreateIncomeTransactionSuccessfully() {
            // Given
            TransactionAggregate incomeTransaction = TransactionAggregate.createIncome(
                Money.of(createCommand.getAmount(), createCommand.getCurrency()),
                createCommand.getTransactionDate(),
                createCommand.getDescription(),
                TenantId.of(createCommand.getCompanyId()),
                createCommand.getUserId()
            );
            
            when(companyRepository.findById(createCommand.getCompanyId())).thenReturn(Optional.of(testCompany));
            when(transactionRepository.save(any(TransactionAggregate.class))).thenReturn(incomeTransaction);
            
            // When
            TransactionDTO result = transactionApplicationService.createIncomeTransaction(createCommand);
            
            // Then
            assertNotNull(result);
            assertEquals(createCommand.getAmount(), result.getAmount());
            assertEquals("INCOME", result.getTransactionType());
            assertEquals("DRAFT", result.getStatus());
            
            verify(companyRepository).findById(createCommand.getCompanyId());
            verify(transactionRepository).save(any(TransactionAggregate.class));
            verify(eventPublisher).publishAll(any());
        }
        
        @Test
        @DisplayName("Should throw exception when company not found")
        void shouldThrowExceptionWhenCompanyNotFound() {
            // Given
            when(companyRepository.findById(createCommand.getCompanyId())).thenReturn(Optional.empty());
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionApplicationService.createExpenseTransaction(createCommand)
            );
            
            assertTrue(exception.getMessage().contains("Company not found"));
            verify(companyRepository).findById(createCommand.getCompanyId());
            verify(transactionRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Should throw exception when amount is negative")
        void shouldThrowExceptionWhenAmountIsNegative() {
            // Given
            createCommand = CreateTransactionCommand.builder()
                .amount(new BigDecimal("-100.00"))
                .currency("CNY")
                .description("Negative amount test")
                .transactionDate(LocalDate.now())
                .categoryId(1)
                .companyId(1)
                .userId(1)
                .build();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionApplicationService.createExpenseTransaction(createCommand)
            );
            
            assertTrue(exception.getMessage().contains("Amount must be positive"));
            verify(transactionRepository, never()).save(any());
        }
    }
    
    @Nested
    @DisplayName("Update Transaction Tests")
    class UpdateTransactionTests {
        
        @Test
        @DisplayName("Should update transaction successfully when in draft status")
        void shouldUpdateTransactionSuccessfully() {
            // Given
            when(transactionRepository.findById(1)).thenReturn(Optional.of(testTransaction));
            when(transactionRepository.save(any(TransactionAggregate.class))).thenReturn(testTransaction);
            
            // When
            TransactionDTO result = transactionApplicationService.updateTransaction(1, updateCommand);
            
            // Then
            assertNotNull(result);
            verify(transactionRepository).findById(1);
            verify(transactionRepository).save(testTransaction);
        }
        
        @Test
        @DisplayName("Should throw exception when transaction not found")
        void shouldThrowExceptionWhenTransactionNotFound() {
            // Given
            when(transactionRepository.findById(1)).thenReturn(Optional.empty());
            
            // When & Then
            ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> transactionApplicationService.updateTransaction(1, updateCommand)
            );
            
            assertTrue(exception.getMessage().contains("Transaction not found"));
            verify(transactionRepository).findById(1);
            verify(transactionRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Should throw exception when trying to update approved transaction")
        void shouldThrowExceptionWhenUpdatingApprovedTransaction() {
            // Given
            testTransaction.submitForApproval(1);
            testTransaction.approve(2);
            when(transactionRepository.findById(1)).thenReturn(Optional.of(testTransaction));
            
            // When & Then
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> transactionApplicationService.updateTransaction(1, updateCommand)
            );
            
            assertTrue(exception.getMessage().contains("Cannot update transaction"));
            verify(transactionRepository).findById(1);
            verify(transactionRepository, never()).save(any());
        }
    }
    
    @Nested
    @DisplayName("Transaction Approval Tests")
    class TransactionApprovalTests {
        
        @Test
        @DisplayName("Should submit transaction for approval successfully")
        void shouldSubmitForApprovalSuccessfully() {
            // Given
            when(transactionRepository.findById(1)).thenReturn(Optional.of(testTransaction));
            when(transactionRepository.save(any(TransactionAggregate.class))).thenReturn(testTransaction);
            
            // When
            TransactionDTO result = transactionApplicationService.submitForApproval(1, 1);
            
            // Then
            assertNotNull(result);
            assertEquals("PENDING_APPROVAL", result.getStatus());
            verify(transactionRepository).findById(1);
            verify(transactionRepository).save(testTransaction);
            verify(eventPublisher).publishAll(any());
        }
        
        @Test
        @DisplayName("Should approve transaction successfully")
        void shouldApproveTransactionSuccessfully() {
            // Given
            testTransaction.submitForApproval(1);
            when(transactionRepository.findById(1)).thenReturn(Optional.of(testTransaction));
            when(transactionRepository.save(any(TransactionAggregate.class))).thenReturn(testTransaction);
            
            // When
            TransactionDTO result = transactionApplicationService.approveTransaction(1, approveCommand);
            
            // Then
            assertNotNull(result);
            assertEquals("APPROVED", result.getStatus());
            verify(transactionRepository).findById(1);
            verify(transactionRepository).save(testTransaction);
            verify(eventPublisher).publishAll(any());
        }
        
        @Test
        @DisplayName("Should reject transaction successfully")
        void shouldRejectTransactionSuccessfully() {
            // Given
            testTransaction.submitForApproval(1);
            when(transactionRepository.findById(1)).thenReturn(Optional.of(testTransaction));
            when(transactionRepository.save(any(TransactionAggregate.class))).thenReturn(testTransaction);
            
            // When
            TransactionDTO result = transactionApplicationService.rejectTransaction(1, 1, 2, "Insufficient documentation");
            
            // Then
            assertNotNull(result);
            assertEquals("REJECTED", result.getStatus());
            verify(transactionRepository).findById(1);
            verify(transactionRepository).save(testTransaction);
            verify(eventPublisher).publishAll(any());
        }
        
        @Test
        @DisplayName("Should throw exception when approving draft transaction")
        void shouldThrowExceptionWhenApprovingDraftTransaction() {
            // Given
            when(transactionRepository.findById(1)).thenReturn(Optional.of(testTransaction));
            
            // When & Then
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> transactionApplicationService.approveTransaction(1, approveCommand)
            );
            
            assertTrue(exception.getMessage().contains("Cannot approve transaction"));
            verify(transactionRepository).findById(1);
            verify(transactionRepository, never()).save(any());
        }
    }
    
    @Nested
    @DisplayName("Transaction Cancellation Tests")
    class TransactionCancellationTests {
        
        @Test
        @DisplayName("Should cancel transaction successfully")
        void shouldCancelTransactionSuccessfully() {
            // Given
            when(transactionRepository.findById(1)).thenReturn(Optional.of(testTransaction));
            when(transactionRepository.save(any(TransactionAggregate.class))).thenReturn(testTransaction);
            
            // When
            TransactionDTO result = transactionApplicationService.cancelTransaction(1, 1, 1);
            
            // Then
            assertNotNull(result);
            assertEquals("CANCELLED", result.getStatus());
            verify(transactionRepository).findById(1);
            verify(transactionRepository).save(testTransaction);
            verify(eventPublisher).publishAll(any());
        }
        
        @Test
        @DisplayName("Should void transaction successfully")
        void shouldVoidTransactionSuccessfully() {
            // Given
            testTransaction.submitForApproval(1);
            testTransaction.approve(2);
            when(transactionRepository.findById(1)).thenReturn(Optional.of(testTransaction));
            when(transactionRepository.save(any(TransactionAggregate.class))).thenReturn(testTransaction);
            
            // When
            TransactionDTO result = transactionApplicationService.voidTransaction(1, 1, 1, "Error in processing");
            
            // Then
            assertNotNull(result);
            assertEquals("VOIDED", result.getStatus());
            verify(transactionRepository).findById(1);
            verify(transactionRepository).save(testTransaction);
            verify(eventPublisher).publishAll(any());
        }
    }
    
    @Nested
    @DisplayName("Query Tests")
    class QueryTests {
        
        @Test
        @DisplayName("Should get transaction by ID successfully")
        void shouldGetTransactionByIdSuccessfully() {
            // Given
            when(transactionRepository.findById(1)).thenReturn(Optional.of(testTransaction));
            
            // When
            TransactionDTO result = transactionApplicationService.getTransactionById(1, 1);
            
            // Then
            assertNotNull(result);
            assertEquals(testTransaction.getTransactionId(), result.getTransactionId());
            verify(transactionRepository).findById(1);
        }
        
        @Test
        @DisplayName("Should get transactions by company successfully")
        void shouldGetTransactionsByCompanySuccessfully() {
            // Given
            List<TransactionAggregate> transactions = List.of(testTransaction);
            when(transactionRepository.findByTenantIdOrderByTransactionDateDesc(any(TenantId.class))).thenReturn(transactions);
            
            // When
            List<TransactionDTO> result = transactionApplicationService.getTransactionsByCompany(1);
            
            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(transactionRepository).findByTenantIdOrderByTransactionDateDesc(any(TenantId.class));
        }
        
        @Test
        @DisplayName("Should get pending approval transactions successfully")
        void shouldGetPendingApprovalTransactionsSuccessfully() {
            // Given
            testTransaction.submitForApproval(1);
            List<TransactionAggregate> pendingTransactions = List.of(testTransaction);
            when(transactionRepository.findByTenantIdAndStatus(any(TenantId.class), eq(TransactionStatus.Status.PENDING_APPROVAL)))
                .thenReturn(pendingTransactions);
            
            // When
            List<TransactionDTO> result = transactionApplicationService.getPendingApprovalTransactions(1);
            
            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("PENDING_APPROVAL", result.get(0).getStatus());
            verify(transactionRepository).findByTenantIdAndStatus(any(TenantId.class), eq(TransactionStatus.Status.PENDING_APPROVAL));
        }
        
        @Test
        @DisplayName("Should get transactions by date range successfully")
        void shouldGetTransactionsByDateRangeSuccessfully() {
            // Given
            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();
            List<TransactionAggregate> transactions = List.of(testTransaction);
            when(transactionRepository.findByTenantIdAndTransactionDateBetween(any(TenantId.class), eq(startDate), eq(endDate)))
                .thenReturn(transactions);
            
            // When
            List<TransactionDTO> result = transactionApplicationService.getTransactionsByDateRange(1, startDate, endDate);
            
            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(transactionRepository).findByTenantIdAndTransactionDateBetween(any(TenantId.class), eq(startDate), eq(endDate));
        }
    }
    
    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {
        
        @Test
        @DisplayName("Should throw exception when create command is null")
        void shouldThrowExceptionWhenCreateCommandIsNull() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionApplicationService.createExpenseTransaction(null)
            );
            
            assertEquals("CreateTransactionCommand cannot be null", exception.getMessage());
            verify(transactionRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Should throw exception when description is empty")
        void shouldThrowExceptionWhenDescriptionIsEmpty() {
            // Given
            createCommand = CreateTransactionCommand.builder()
                .amount(new BigDecimal("100.00"))
                .currency("CNY")
                .description("")
                .transactionDate(LocalDate.now())
                .categoryId(1)
                .companyId(1)
                .userId(1)
                .build();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionApplicationService.createExpenseTransaction(createCommand)
            );
            
            assertTrue(exception.getMessage().contains("Description cannot be empty"));
            verify(transactionRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Should throw exception when transaction date is in future")
        void shouldThrowExceptionWhenTransactionDateInFuture() {
            // Given
            createCommand = CreateTransactionCommand.builder()
                .amount(new BigDecimal("100.00"))
                .currency("CNY")
                .description("Future transaction")
                .transactionDate(LocalDate.now().plusDays(1))
                .categoryId(1)
                .companyId(1)
                .userId(1)
                .build();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionApplicationService.createExpenseTransaction(createCommand)
            );
            
            assertTrue(exception.getMessage().contains("Transaction date cannot be in the future"));
            verify(transactionRepository, never()).save(any());
        }
    }
}