// backend/src/test/java/org/example/backend/application/service/IncomeStatementDataServiceTest.java
package org.example.backend.application.service;

import org.example.backend.application.dto.IncomeStatementData;
import org.example.backend.domain.aggregate.transaction.TransactionAggregateRepository;
import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.aggregate.company.CompanyAggregateRepository;
import org.example.backend.domain.aggregate.company.CompanyAggregate;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.Money;
import org.example.backend.repository.CategoryRepository;
import org.example.backend.model.Category;

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
import java.util.Collections;

/**
 * Unit tests for IncomeStatementDataService
 * Tests income statement calculation logic and data aggregation
 */
@DisplayName("IncomeStatementDataService Tests")
class IncomeStatementDataServiceTest {

    @Mock
    private TransactionAggregateRepository transactionRepository;
    
    @Mock
    private CompanyAggregateRepository companyRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    private IncomeStatementDataService incomeStatementService;
    
    // Test data constants
    private static final Integer TEST_COMPANY_ID = 999;
    private static final LocalDate START_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate END_DATE = LocalDate.of(2024, 12, 31);
    private static final String TEST_CURRENCY = "CNY";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        incomeStatementService = new IncomeStatementDataService(
                transactionRepository, companyRepository, categoryRepository);
    }

    @Nested
    @DisplayName("Income Statement Generation Tests")
    class IncomeStatementGenerationTests {

        @Test
        @DisplayName("Should generate income statement with revenue and expenses")
        void shouldGenerateIncomeStatementWithRevenueAndExpenses() {
            // Given
            TenantId tenantId = TenantId.of(TEST_COMPANY_ID);
            CompanyAggregate company = createMockCompany();
            List<TransactionAggregate> transactions = createMockTransactions();

            when(companyRepository.findById(TEST_COMPANY_ID))
                    .thenReturn(Optional.of(company));
            
            // Mock the actual repository method that exists
            when(transactionRepository.findByTenantIdOrderByTransactionDateDesc(TEST_COMPANY_ID))
                    .thenReturn(transactions);

            // When
            IncomeStatementData result = incomeStatementService.getIncomeStatementDataByTenant(
                    tenantId, START_DATE, END_DATE);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCompanyName()).isEqualTo("Test Company");
            assertThat(result.getStartDate()).isEqualTo(START_DATE);
            assertThat(result.getEndDate()).isEqualTo(END_DATE);
            
            // Verify revenue calculation
            assertThat(result.getTotalRevenue()).isEqualTo(new BigDecimal("5000.00"));
            
            // Verify expense calculation
            assertThat(result.getTotalExpenses()).isEqualTo(new BigDecimal("3000.00"));
            
            // Verify net income calculation
            assertThat(result.getNetIncome()).isEqualTo(new BigDecimal("2000.00"));

            verify(companyRepository).findById(TEST_COMPANY_ID);
            verify(transactionRepository).findByTenantIdOrderByTransactionDateDesc(TEST_COMPANY_ID);
        }

        @Test
        @DisplayName("Should handle empty transaction list")
        void shouldHandleEmptyTransactionList() {
            // Given
            TenantId tenantId = TenantId.of(TEST_COMPANY_ID);
            CompanyAggregate company = createMockCompany();

            when(companyRepository.findById(TEST_COMPANY_ID))
                    .thenReturn(Optional.of(company));
            when(transactionRepository.findByTenantIdOrderByTransactionDateDesc(TEST_COMPANY_ID))
                    .thenReturn(Collections.emptyList());

            // When
            IncomeStatementData result = incomeStatementService.getIncomeStatementDataByTenant(
                    tenantId, START_DATE, END_DATE);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalRevenue()).isEqualTo(BigDecimal.ZERO);
            assertThat(result.getTotalExpenses()).isEqualTo(BigDecimal.ZERO);
            assertThat(result.getNetIncome()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should throw exception when company not found")
        void shouldThrowExceptionWhenCompanyNotFound() {
            // Given
            TenantId tenantId = TenantId.of(999);
            when(companyRepository.findById(999))
                    .thenReturn(Optional.empty());

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> incomeStatementService.getIncomeStatementDataByTenant(
                            tenantId, START_DATE, END_DATE));
            
            assertThat(exception.getMessage()).contains("Company not found: 999");
        }

        @Test
        @DisplayName("Should calculate revenue correctly from income transactions")
        void shouldCalculateRevenueCorrectlyFromIncomeTransactions() {
            // Given
            TenantId tenantId = TenantId.of(TEST_COMPANY_ID);
            CompanyAggregate company = createMockCompany();
            List<TransactionAggregate> incomeOnlyTransactions = Arrays.asList(
                    createMockIncomeTransaction(new BigDecimal("1000.00")),
                    createMockIncomeTransaction(new BigDecimal("2000.00")),
                    createMockIncomeTransaction(new BigDecimal("1500.00"))
            );

            when(companyRepository.findById(TEST_COMPANY_ID))
                    .thenReturn(Optional.of(company));
            when(transactionRepository.findByTenantIdOrderByTransactionDateDesc(TEST_COMPANY_ID))
                    .thenReturn(incomeOnlyTransactions);

            // When
            IncomeStatementData result = incomeStatementService.getIncomeStatementDataByTenant(
                    tenantId, START_DATE, END_DATE);

            // Then
            assertThat(result.getTotalRevenue()).isEqualTo(new BigDecimal("4500.00"));
            assertThat(result.getTotalExpenses()).isEqualTo(BigDecimal.ZERO);
            assertThat(result.getNetIncome()).isEqualTo(new BigDecimal("4500.00"));
        }

        @Test
        @DisplayName("Should calculate expenses correctly from expense transactions")
        void shouldCalculateExpensesCorrectlyFromExpenseTransactions() {
            // Given
            TenantId tenantId = TenantId.of(TEST_COMPANY_ID);
            CompanyAggregate company = createMockCompany();
            List<TransactionAggregate> expenseOnlyTransactions = Arrays.asList(
                    createMockExpenseTransaction(new BigDecimal("800.00")),
                    createMockExpenseTransaction(new BigDecimal("1200.00")),
                    createMockExpenseTransaction(new BigDecimal("500.00"))
            );

            when(companyRepository.findById(TEST_COMPANY_ID))
                    .thenReturn(Optional.of(company));
            when(transactionRepository.findByTenantIdOrderByTransactionDateDesc(TEST_COMPANY_ID))
                    .thenReturn(expenseOnlyTransactions);

            // When
            IncomeStatementData result = incomeStatementService.getIncomeStatementDataByTenant(
                    tenantId, START_DATE, END_DATE);

            // Then
            assertThat(result.getTotalRevenue()).isEqualTo(BigDecimal.ZERO);
            assertThat(result.getTotalExpenses()).isEqualTo(new BigDecimal("2500.00"));
            assertThat(result.getNetIncome()).isEqualTo(new BigDecimal("-2500.00"));
        }
    }

    @Nested
    @DisplayName("Business Rule Tests")
    class BusinessRuleTests {

        @Test
        @DisplayName("Should correctly identify revenue transactions")
        void shouldCorrectlyIdentifyRevenueTransactions() {
            // Given
            TransactionAggregate incomeTransaction = createMockIncomeTransaction(new BigDecimal("1000.00"));
            TransactionAggregate expenseTransaction = createMockExpenseTransaction(new BigDecimal("500.00"));
            
            List<TransactionAggregate> mixedTransactions = Arrays.asList(
                    incomeTransaction, expenseTransaction
            );

            TenantId tenantId = TenantId.of(TEST_COMPANY_ID);
            CompanyAggregate company = createMockCompany();

            when(companyRepository.findById(TEST_COMPANY_ID))
                    .thenReturn(Optional.of(company));
            when(transactionRepository.findByTenantIdOrderByTransactionDateDesc(TEST_COMPANY_ID))
                    .thenReturn(mixedTransactions);

            // When
            IncomeStatementData result = incomeStatementService.getIncomeStatementDataByTenant(
                    tenantId, START_DATE, END_DATE);

            // Then
            assertThat(result.getTotalRevenue()).isEqualTo(new BigDecimal("1000.00"));
            assertThat(result.getTotalExpenses()).isEqualTo(new BigDecimal("500.00"));
        }

        @Test
        @DisplayName("Should handle null amounts gracefully")
        void shouldHandleNullAmountsGracefully() {
            // Given
            TenantId tenantId = TenantId.of(TEST_COMPANY_ID);
            CompanyAggregate company = createMockCompany();
            
            TransactionAggregate transactionWithNullAmount = mock(TransactionAggregate.class);
            when(transactionWithNullAmount.getTransactionType())
                    .thenReturn(TransactionAggregate.TransactionType.INCOME);
            when(transactionWithNullAmount.getAmount()).thenReturn(null);

            List<TransactionAggregate> transactionsWithNull = Arrays.asList(
                    transactionWithNullAmount,
                    createMockIncomeTransaction(new BigDecimal("1000.00"))
            );

            when(companyRepository.findById(TEST_COMPANY_ID))
                    .thenReturn(Optional.of(company));
            when(transactionRepository.findByTenantIdOrderByTransactionDateDesc(TEST_COMPANY_ID))
                    .thenReturn(transactionsWithNull);

            // When & Then - should not throw exception
            assertDoesNotThrow(() -> {
                IncomeStatementData result = incomeStatementService.getIncomeStatementDataByTenant(
                        tenantId, START_DATE, END_DATE);
                // Should only count the valid transaction
                assertThat(result.getTotalRevenue()).isEqualTo(new BigDecimal("1000.00"));
            });
        }
    }

    @Nested
    @DisplayName("Date Range Validation Tests")
    class DateRangeValidationTests {

        @Test
        @DisplayName("Should validate date range correctly")
        void shouldValidateDateRangeCorrectly() {
            // Given
            TenantId tenantId = TenantId.of(TEST_COMPANY_ID);
            LocalDate invalidStartDate = LocalDate.of(2025, 1, 1);
            LocalDate invalidEndDate = LocalDate.of(2024, 12, 31);

            // When & Then
            assertThrows(IllegalArgumentException.class,
                    () -> incomeStatementService.getIncomeStatementDataByTenant(
                            tenantId, invalidStartDate, invalidEndDate));
        }

        @Test
        @DisplayName("Should handle same start and end date")
        void shouldHandleSameStartAndEndDate() {
            // Given
            TenantId tenantId = TenantId.of(TEST_COMPANY_ID);
            CompanyAggregate company = createMockCompany();
            LocalDate sameDate = LocalDate.of(2024, 6, 15);

            when(companyRepository.findById(TEST_COMPANY_ID))
                    .thenReturn(Optional.of(company));
            when(transactionRepository.findByTenantIdOrderByTransactionDateDesc(TEST_COMPANY_ID))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertDoesNotThrow(() -> {
                IncomeStatementData result = incomeStatementService.getIncomeStatementDataByTenant(
                        tenantId, sameDate, sameDate);
                assertThat(result).isNotNull();
            });
        }
    }

    // Helper methods for creating mock objects
    private CompanyAggregate createMockCompany() {
        CompanyAggregate company = mock(CompanyAggregate.class);
        when(company.getCompanyId()).thenReturn(TEST_COMPANY_ID);
        when(company.getCompanyName()).thenReturn("Test Company");
        return company;
    }

    private List<TransactionAggregate> createMockTransactions() {
        return Arrays.asList(
                createMockIncomeTransaction(new BigDecimal("3000.00")),
                createMockIncomeTransaction(new BigDecimal("2000.00")),
                createMockExpenseTransaction(new BigDecimal("1500.00")),
                createMockExpenseTransaction(new BigDecimal("1500.00"))
        );
    }

    private TransactionAggregate createMockIncomeTransaction(BigDecimal amount) {
        TransactionAggregate transaction = mock(TransactionAggregate.class);
        when(transaction.getTransactionType()).thenReturn(TransactionAggregate.TransactionType.INCOME);
        when(transaction.getAmount()).thenReturn(Money.of(amount, TEST_CURRENCY));
        when(transaction.getTransactionDate()).thenReturn(LocalDate.now());
        return transaction;
    }

    private TransactionAggregate createMockExpenseTransaction(BigDecimal amount) {
        TransactionAggregate transaction = mock(TransactionAggregate.class);
        when(transaction.getTransactionType()).thenReturn(TransactionAggregate.TransactionType.EXPENSE);
        when(transaction.getAmount()).thenReturn(Money.of(amount, TEST_CURRENCY));
        when(transaction.getTransactionDate()).thenReturn(LocalDate.now());
        return transaction;
    }
}