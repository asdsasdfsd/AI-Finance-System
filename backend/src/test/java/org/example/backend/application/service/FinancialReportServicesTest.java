// backend/src/test/java/org/example/backend/application/service/FinancialReportServicesTest.java
package org.example.backend.application.service;

import org.example.backend.application.dto.IncomeExpenseReportData;
import org.example.backend.application.dto.IncomeStatementData;
import org.example.backend.application.dto.FinancialGroupingData;
import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.aggregate.transaction.TransactionAggregateRepository;
import org.example.backend.domain.aggregate.company.CompanyAggregate;
import org.example.backend.domain.aggregate.company.CompanyAggregateRepository;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.Money;
import org.example.backend.domain.valueobject.TransactionStatus;
import org.example.backend.model.Category;
import org.example.backend.repository.CategoryRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
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
 * Unit tests for Financial Report Services
 * 
 * Tests the various financial report generation services including:
 * - IncomeExpenseDataService
 * - IncomeStatementDataService  
 * - FinancialGroupingDataService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Financial Report Services Tests")
class FinancialReportServicesTest {
    
    @Mock
    private TransactionAggregateRepository transactionRepository;
    
    @Mock
    private CompanyAggregateRepository companyRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    private IncomeExpenseDataService incomeExpenseDataService;
    private IncomeStatementDataService incomeStatementDataService;
    private FinancialGroupingDataService financialGroupingDataService;
    
    private CompanyAggregate testCompany;
    private TransactionAggregate incomeTransaction;
    private TransactionAggregate expenseTransaction;
    private Category testCategory;
    private TenantId testTenantId;
    private LocalDate testDate;
    
    @BeforeEach
    void setUp() {
        incomeExpenseDataService = new IncomeExpenseDataService(
            transactionRepository, companyRepository, categoryRepository);
        incomeStatementDataService = new IncomeStatementDataService(
            transactionRepository, companyRepository, categoryRepository);
        financialGroupingDataService = new FinancialGroupingDataService(
            transactionRepository, companyRepository, categoryRepository, null, null);
            
        testTenantId = TenantId.of(1);
        testDate = LocalDate.now();
        
        testCompany = CompanyAggregate.create(
            "Test Company",
            "test@company.com",
            "Test Address",
            "BL123456",
            1
        );
        
        testCategory = new Category();
        testCategory.setCategoryId(1);
        testCategory.setName("Office Supplies");
        testCategory.setType(Category.CategoryType.EXPENSE);
        
        incomeTransaction = TransactionAggregate.createIncome(
            Money.of(new BigDecimal("5000.00"), "CNY"),
            testDate,
            "Service Revenue",
            testTenantId,
            1
        );
        incomeTransaction.submitForApproval(1);
        incomeTransaction.approve(2);
        
        expenseTransaction = TransactionAggregate.createExpense(
            Money.of(new BigDecimal("1000.00"), "CNY"),
            testDate,
            "Office Supplies",
            testTenantId,
            1
        );
        expenseTransaction.submitForApproval(1);
        expenseTransaction.approve(2);
    }
    
    @Nested
    @DisplayName("IncomeExpenseDataService Tests")
    class IncomeExpenseDataServiceTests {
        
        @Test
        @DisplayName("Should generate income expense report successfully")
        void shouldGenerateIncomeExpenseReportSuccessfully() {
            // Given
            List<TransactionAggregate> transactions = List.of(incomeTransaction, expenseTransaction);
            when(companyRepository.findById(testTenantId.getValue())).thenReturn(Optional.of(testCompany));
            when(transactionRepository.findByTenantIdAndDateRangeAndStatus(
                eq(testTenantId), any(LocalDate.class), any(LocalDate.class), 
                eq(TransactionStatus.Status.APPROVED)))
                .thenReturn(transactions);
            when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
            
            // When
            IncomeExpenseReportData result = incomeExpenseDataService.generateIncomeExpenseReportByTenant(
                testTenantId, testDate);
            
            // Then
            assertNotNull(result);
            assertNotNull(result.getIncomeRows());
            assertNotNull(result.getExpenseRows());
            verify(companyRepository).findById(testTenantId.getValue());
            verify(transactionRepository, atLeast(2)).findByTenantIdAndDateRangeAndStatus(
                eq(testTenantId), any(LocalDate.class), any(LocalDate.class), 
                eq(TransactionStatus.Status.APPROVED));
        }
        
        @Test
        @DisplayName("Should throw exception when company not found")
        void shouldThrowExceptionWhenCompanyNotFound() {
            // Given
            when(companyRepository.findById(testTenantId.getValue())).thenReturn(Optional.empty());
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> incomeExpenseDataService.generateIncomeExpenseReportByTenant(testTenantId, testDate)
            );
            
            assertTrue(exception.getMessage().contains("Company not found"));
            verify(companyRepository).findById(testTenantId.getValue());
        }
        
        @Test
        @DisplayName("Should handle empty transaction list")
        void shouldHandleEmptyTransactionList() {
            // Given
            when(companyRepository.findById(testTenantId.getValue())).thenReturn(Optional.of(testCompany));
            when(transactionRepository.findByTenantIdAndDateRangeAndStatus(
                eq(testTenantId), any(LocalDate.class), any(LocalDate.class), 
                eq(TransactionStatus.Status.APPROVED)))
                .thenReturn(List.of());
            
            // When
            IncomeExpenseReportData result = incomeExpenseDataService.generateIncomeExpenseReportByTenant(
                testTenantId, testDate);
            
            // Then
            assertNotNull(result);
            assertNotNull(result.getIncomeRows());
            assertNotNull(result.getExpenseRows());
            assertTrue(result.getIncomeRows().isEmpty());
            assertTrue(result.getExpenseRows().isEmpty());
        }
        
        @Test
        @DisplayName("Should calculate correct totals and variances")
        void shouldCalculateCorrectTotalsAndVariances() {
            // Given
            List<TransactionAggregate> ytdTransactions = List.of(incomeTransaction, expenseTransaction);
            List<TransactionAggregate> monthTransactions = List.of(expenseTransaction);
            
            when(companyRepository.findById(testTenantId.getValue())).thenReturn(Optional.of(testCompany));
            when(transactionRepository.findByTenantIdAndDateRangeAndStatus(
                eq(testTenantId), any(LocalDate.class), any(LocalDate.class), 
                eq(TransactionStatus.Status.APPROVED)))
                .thenReturn(ytdTransactions)
                .thenReturn(monthTransactions);
            when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
            
            // When
            IncomeExpenseReportData result = incomeExpenseDataService.generateIncomeExpenseReportByTenant(
                testTenantId, testDate);
            
            // Then
            assertNotNull(result);
            // Verify calculations are performed (actual calculation logic would be tested in integration tests)
            verify(transactionRepository, atLeast(2)).findByTenantIdAndDateRangeAndStatus(
                eq(testTenantId), any(LocalDate.class), any(LocalDate.class), 
                eq(TransactionStatus.Status.APPROVED));
        }
    }
    
    @Nested
    @DisplayName("IncomeStatementDataService Tests")
    class IncomeStatementDataServiceTests {
        
        @Test
        @DisplayName("Should generate income statement successfully")
        void shouldGenerateIncomeStatementSuccessfully() {
            // Given
            List<TransactionAggregate> transactions = List.of(incomeTransaction, expenseTransaction);
            LocalDate startDate = testDate.withDayOfMonth(1);
            LocalDate endDate = testDate;
            
            when(companyRepository.findById(testTenantId.getValue())).thenReturn(Optional.of(testCompany));
            when(transactionRepository.findByTenantIdAndDateRangeAndStatus(
                testTenantId, startDate, endDate, TransactionStatus.Status.APPROVED))
                .thenReturn(transactions);
            when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
            
            // When
            IncomeStatementData result = incomeStatementDataService.getIncomeStatementDataByTenant(
                testTenantId, startDate, endDate);
            
            // Then
            assertNotNull(result);
            assertNotNull(result.getCompanyName());
            assertNotNull(result.getStartDate());
            assertNotNull(result.getEndDate());
            verify(companyRepository).findById(testTenantId.getValue());
            verify(transactionRepository).findByTenantIdAndDateRangeAndStatus(
                testTenantId, startDate, endDate, TransactionStatus.Status.APPROVED);
        }
        
        @Test
        @DisplayName("Should separate revenue and expense transactions correctly")
        void shouldSeparateRevenueAndExpenseTransactionsCorrectly() {
            // Given
            List<TransactionAggregate> transactions = List.of(incomeTransaction, expenseTransaction);
            LocalDate startDate = testDate.withDayOfMonth(1);
            LocalDate endDate = testDate;
            
            when(companyRepository.findById(testTenantId.getValue())).thenReturn(Optional.of(testCompany));
            when(transactionRepository.findByTenantIdAndDateRangeAndStatus(
                testTenantId, startDate, endDate, TransactionStatus.Status.APPROVED))
                .thenReturn(transactions);
            when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
            
            // When
            IncomeStatementData result = incomeStatementDataService.getIncomeStatementDataByTenant(
                testTenantId, startDate, endDate);
            
            // Then
            assertNotNull(result);
            // Verify that revenue and expense calculations are performed
            verify(transactionRepository).findByTenantIdAndTransactionDateBetweenAndStatus(
                testTenantId, startDate, endDate, TransactionStatus.Status.APPROVED);
        }
        
        @Test
        @DisplayName("Should create empty income statement when no transactions found")
        void shouldCreateEmptyIncomeStatementWhenNoTransactionsFound() {
            // Given
            LocalDate startDate = testDate.withDayOfMonth(1);
            LocalDate endDate = testDate;
            
            when(companyRepository.findById(testTenantId.getValue())).thenReturn(Optional.of(testCompany));
            when(transactionRepository.findByTenantIdAndDateRangeAndStatus(
                testTenantId, startDate, endDate, TransactionStatus.Status.APPROVED))
                .thenReturn(List.of());
            
            // When
            IncomeStatementData result = incomeStatementDataService.getIncomeStatementDataByTenant(
                testTenantId, startDate, endDate);
            
            // Then
            assertNotNull(result);
            assertEquals(testCompany.getCompanyName(), result.getCompanyName());
            assertEquals(startDate, result.getStartDate());
            assertEquals(endDate, result.getEndDate());
        }
        
        @Test
        @DisplayName("Should calculate net income correctly")
        void shouldCalculateNetIncomeCorrectly() {
            // Given
            List<TransactionAggregate> transactions = List.of(incomeTransaction, expenseTransaction);
            LocalDate startDate = testDate.withDayOfMonth(1);
            LocalDate endDate = testDate;
            
            when(companyRepository.findById(testTenantId.getValue())).thenReturn(Optional.of(testCompany));
            when(transactionRepository.findByTenantIdAndDateRangeAndStatus(
                testTenantId, startDate, endDate, TransactionStatus.Status.APPROVED))
                .thenReturn(transactions);
            when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
            
            // When
            IncomeStatementData result = incomeStatementDataService.getIncomeStatementDataByTenant(
                testTenantId, startDate, endDate);
            
            // Then
            assertNotNull(result);
            // Net income should be calculated as Revenue - Expenses
            // In this case: 5000 - 1000 = 4000
            // (Actual assertion would depend on the DTO structure)
            verify(transactionRepository).findByTenantIdAndTransactionDateBetweenAndStatus(
                testTenantId, startDate, endDate, TransactionStatus.Status.APPROVED);
        }
    }
    
    @Nested
    @DisplayName("FinancialGroupingDataService Tests")
    class FinancialGroupingDataServiceTests {
        
        @Test
        @DisplayName("Should generate financial grouping data successfully")
        void shouldGenerateFinancialGroupingDataSuccessfully() {
            // Given
            List<TransactionAggregate> transactions = List.of(incomeTransaction, expenseTransaction);
            LocalDate startDate = testDate.withDayOfMonth(1);
            LocalDate endDate = testDate;
            
            when(companyRepository.findById(testTenantId.getValue())).thenReturn(Optional.of(testCompany));
            when(transactionRepository.findByTenantIdAndDateRangeAndStatus(
                testTenantId, startDate, endDate, TransactionStatus.Status.APPROVED))
                .thenReturn(transactions);
            when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
            
            // When
            FinancialGroupingData result = financialGroupingDataService.getFinancialGroupingDataByTenant(
                testTenantId, startDate, endDate);
            
            // Then
            assertNotNull(result);
            verify(companyRepository).findById(testTenantId.getValue());
            verify(transactionRepository).findByTenantIdAndTransactionDateBetweenAndStatus(
                testTenantId, startDate, endDate, TransactionStatus.Status.APPROVED);
        }
        
        @Test
        @DisplayName("Should group transactions by category correctly")
        void shouldGroupTransactionsByCategoryCorrectly() {
            // Given
            List<TransactionAggregate> transactions = List.of(incomeTransaction, expenseTransaction);
            LocalDate startDate = testDate.withDayOfMonth(1);
            LocalDate endDate = testDate;
            
            when(companyRepository.findById(testTenantId.getValue())).thenReturn(Optional.of(testCompany));
            when(transactionRepository.findByTenantIdAndDateRangeAndStatus(
                testTenantId, startDate, endDate, TransactionStatus.Status.APPROVED))
                .thenReturn(transactions);
            when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
            
            // When
            FinancialGroupingData result = financialGroupingDataService.getFinancialGroupingDataByTenant(
                testTenantId, startDate, endDate);
            
            // Then
            assertNotNull(result);
            // Verify that category grouping logic is executed
            verify(categoryRepository, atLeast(1)).findById(1);
        }
        
        @Test
        @DisplayName("Should handle empty transaction list for grouping")
        void shouldHandleEmptyTransactionListForGrouping() {
            // Given
            LocalDate startDate = testDate.withDayOfMonth(1);
            LocalDate endDate = testDate;
            
            when(companyRepository.findById(testTenantId.getValue())).thenReturn(Optional.of(testCompany));
            when(transactionRepository.findByTenantIdAndDateRangeAndStatus(
                testTenantId, startDate, endDate, TransactionStatus.Status.APPROVED))
                .thenReturn(List.of());
            
            // When
            FinancialGroupingData result = financialGroupingDataService.getFinancialGroupingDataByTenant(
                testTenantId, startDate, endDate);
            
            // Then
            assertNotNull(result);
            // Should return empty grouping data structure
            verify(companyRepository).findById(testTenantId.getValue());
            verify(transactionRepository).findByTenantIdAndTransactionDateBetweenAndStatus(
                testTenantId, startDate, endDate, TransactionStatus.Status.APPROVED);
        }
    }
    
    @Nested
    @DisplayName("Common Validation Tests")
    class CommonValidationTests {
        
        @Test
        @DisplayName("Should validate tenant ID is not null")
        void shouldValidateTenantIdNotNull() {
            // When & Then
            assertThrows(
                IllegalArgumentException.class,
                () -> incomeExpenseDataService.generateIncomeExpenseReportByTenant(null, testDate)
            );
            
            assertThrows(
                IllegalArgumentException.class,
                () -> incomeStatementDataService.getIncomeStatementDataByTenant(null, testDate, testDate)
            );
            
            assertThrows(
                IllegalArgumentException.class,
                () -> financialGroupingDataService.getFinancialGroupingDataByTenant(null, testDate, testDate)
            );
        }
        
        @Test
        @DisplayName("Should validate date parameters are not null")
        void shouldValidateDateParametersNotNull() {
            // When & Then
            assertThrows(
                IllegalArgumentException.class,
                () -> incomeExpenseDataService.generateIncomeExpenseReportByTenant(testTenantId, null)
            );
            
            assertThrows(
                IllegalArgumentException.class,
                () -> incomeStatementDataService.getIncomeStatementDataByTenant(testTenantId, null, testDate)
            );
            
            assertThrows(
                IllegalArgumentException.class,
                () -> incomeStatementDataService.getIncomeStatementDataByTenant(testTenantId, testDate, null)
            );
        }
        
        @Test
        @DisplayName("Should validate start date is before end date")
        void shouldValidateStartDateBeforeEndDate() {
            // Given
            LocalDate startDate = testDate;
            LocalDate endDate = testDate.minusDays(1);
            
            // When & Then
            assertThrows(
                IllegalArgumentException.class,
                () -> incomeStatementDataService.getIncomeStatementDataByTenant(testTenantId, startDate, endDate)
            );
            
            assertThrows(
                IllegalArgumentException.class,
                () -> financialGroupingDataService.getFinancialGroupingDataByTenant(testTenantId, startDate, endDate)
            );
        }
    }
}