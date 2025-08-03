// backend/src/test/java/org/example/backend/application/service/FinancialReportServicesTest.java
package org.example.backend.application.service;

import org.example.backend.application.dto.GenerateReportCommand;
import org.example.backend.application.dto.FinancialGroupingData;
import org.example.backend.application.dto.IncomeExpenseReportRowDTO;
import org.example.backend.application.dto.IncomeExpenseReportData;
import org.example.backend.application.dto.IncomeStatementData;
import org.example.backend.domain.aggregate.company.CompanyAggregate;
import org.example.backend.domain.aggregate.company.CompanyAggregateRepository;
import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.aggregate.transaction.TransactionAggregateRepository;
import org.example.backend.domain.valueobject.Money;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.TransactionStatus;
import org.example.backend.domain.valueobject.ReportType;
import org.example.backend.exception.ResourceNotFoundException;
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
 * Unit tests for Financial Report Services - Fixed Version
 */
@ExtendWith(MockitoExtension.class)
class FinancialReportServicesTest {

    @Mock
    private CompanyAggregateRepository companyRepository;

    @Mock
    private TransactionAggregateRepository transactionRepository;

    @Mock
    private FinancialGroupingDataService financialGroupingDataService;

    @Mock
    private IncomeExpenseDataService incomeExpenseDataService;

    @Mock
    private IncomeStatementDataService incomeStatementDataService;

    @InjectMocks
    private ReportApplicationService reportApplicationService;

    // Test constants
    private static final Integer TEST_COMPANY_ID = 1;
    private static final Integer TEST_USER_ID = 100;
    private static final LocalDate TEST_START_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate TEST_END_DATE = LocalDate.of(2024, 12, 31);
    private static final String TEST_CURRENCY = "CNY";

    private CompanyAggregate testCompany;
    private List<TransactionAggregate> testTransactions;

    @BeforeEach
    void setUp() {
        testCompany = createMockCompany();
        testTransactions = createMockTransactions();
    }

    // ========== Generate Financial Grouping Report Tests ==========

    @Test
    @DisplayName("Should generate financial grouping report successfully")
    void shouldGenerateFinancialGroupingReportSuccessfully() {
        // Given
        GenerateReportCommand command = createFinancialGroupingCommand();
        FinancialGroupingData mockData = createMockFinancialGroupingData();
        
        when(companyRepository.findById(TEST_COMPANY_ID)).thenReturn(Optional.of(testCompany));
        when(financialGroupingDataService.getFinancialGroupingDataByTenant(
                any(TenantId.class), eq(TEST_START_DATE), eq(TEST_END_DATE)))
                .thenReturn(mockData);

        // When
        String reportId = reportApplicationService.generateReport(command);

        // Then
        assertNotNull(reportId);
        
        verify(companyRepository).findById(TEST_COMPANY_ID);
        verify(financialGroupingDataService).getFinancialGroupingDataByTenant(
                any(TenantId.class), eq(TEST_START_DATE), eq(TEST_END_DATE));
    }

    @Test
    @DisplayName("Should throw exception when company not found for financial grouping report")
    void shouldThrowExceptionWhenCompanyNotFoundForFinancialGroupingReport() {
        // Given
        GenerateReportCommand command = createFinancialGroupingCommand();
        when(companyRepository.findById(TEST_COMPANY_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            reportApplicationService.generateReport(command);
        });
    }

    // ========== Generate Income Expense Report Tests ==========

    @Test
    @DisplayName("Should generate income expense report successfully")
    void shouldGenerateIncomeExpenseReportSuccessfully() {
        // Given
        GenerateReportCommand command = createIncomeExpenseCommand();
        List<IncomeExpenseReportRowDTO> mockData = createMockIncomeExpenseData();
        
        when(companyRepository.findById(TEST_COMPANY_ID)).thenReturn(Optional.of(testCompany));
        when(incomeExpenseDataService.generateIncomeExpenseReportByTenant(
                any(TenantId.class), eq(TEST_START_DATE)))
                .thenReturn(createMockIncomeExpenseReportData());

        // When
        String reportId = reportApplicationService.generateReport(command);

        // Then
        assertNotNull(reportId);
        
        verify(companyRepository).findById(TEST_COMPANY_ID);
        verify(incomeExpenseDataService).generateIncomeExpenseReportByTenant(
                any(TenantId.class), eq(TEST_START_DATE));
    }

    // ========== Generate Income Statement Report Tests ==========

    @Test
    @DisplayName("Should generate income statement report successfully")
    void shouldGenerateIncomeStatementReportSuccessfully() {
        // Given
        GenerateReportCommand command = createIncomeStatementCommand();
        IncomeStatementData mockData = createMockIncomeStatementData();
        
        when(companyRepository.findById(TEST_COMPANY_ID)).thenReturn(Optional.of(testCompany));
        when(incomeStatementDataService.getIncomeStatementData(
                any(TenantId.class), eq(TEST_START_DATE), eq(TEST_END_DATE)))
                .thenReturn(mockData);

        // When
        String reportId = reportApplicationService.generateReport(command);

        // Then
        assertNotNull(reportId);
        
        verify(companyRepository).findById(TEST_COMPANY_ID);
        verify(incomeStatementDataService).getIncomeStatementData(
                any(TenantId.class), eq(TEST_START_DATE), eq(TEST_END_DATE));
    }

    // ========== Transaction Repository Query Tests ==========

    @Test
    @DisplayName("Should find approved transactions by date range and status")
    void shouldFindApprovedTransactionsByDateRangeAndStatus() {
        // Given
        TenantId tenantId = TenantId.of(TEST_COMPANY_ID);
        when(transactionRepository.findByTenantIdAndDateRangeAndStatus(
                tenantId, TEST_START_DATE, TEST_END_DATE, TransactionStatus.Status.APPROVED))
                .thenReturn(testTransactions);

        // When
        List<TransactionAggregate> result = transactionRepository.findByTenantIdAndDateRangeAndStatus(
                tenantId, TEST_START_DATE, TEST_END_DATE, TransactionStatus.Status.APPROVED);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(transactionRepository).findByTenantIdAndDateRangeAndStatus(
                tenantId, TEST_START_DATE, TEST_END_DATE, TransactionStatus.Status.APPROVED);
    }

    // ========== Validation Tests ==========

    @Test
    @DisplayName("Should throw exception when generate report command is null")
    void shouldThrowExceptionWhenGenerateReportCommandIsNull() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reportApplicationService.generateReport(null);
        });
    }

    @Test
    @DisplayName("Should throw exception when report type is null")
    void shouldThrowExceptionWhenReportTypeIsNull() {
        // Given
        GenerateReportCommand command = GenerateReportCommand.builder()
                .reportType(null)
                .tenantId(TEST_COMPANY_ID)
                .startDate(TEST_START_DATE)
                .endDate(TEST_END_DATE)
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reportApplicationService.generateReport(command);
        });
    }

    @Test
    @DisplayName("Should throw exception when start date is after end date")
    void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
        // Given
        GenerateReportCommand command = GenerateReportCommand.builder()
                .reportType(ReportType.FINANCIAL_GROUPING)
                .tenantId(TEST_COMPANY_ID)
                .startDate(TEST_END_DATE)
                .endDate(TEST_START_DATE)
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reportApplicationService.generateReport(command);
        });
    }

    // ========== Helper Methods ==========

    private GenerateReportCommand createFinancialGroupingCommand() {
        return GenerateReportCommand.builder()
                .reportType(ReportType.FINANCIAL_GROUPING)
                .tenantId(TEST_COMPANY_ID)
                .startDate(TEST_START_DATE)
                .endDate(TEST_END_DATE)
                .reportName("Financial Grouping Report")
                .createdBy(TEST_USER_ID)
                .build();
    }

    private GenerateReportCommand createIncomeExpenseCommand() {
        return GenerateReportCommand.builder()
                .reportType(ReportType.INCOME_EXPENSE)
                .tenantId(TEST_COMPANY_ID)
                .startDate(TEST_START_DATE)
                .endDate(TEST_END_DATE)
                .reportName("Income Expense Report")
                .createdBy(TEST_USER_ID)
                .build();
    }

    private GenerateReportCommand createIncomeStatementCommand() {
        return GenerateReportCommand.builder()
                .reportType(ReportType.INCOME_STATEMENT)
                .tenantId(TEST_COMPANY_ID)
                .startDate(TEST_START_DATE)
                .endDate(TEST_END_DATE)
                .reportName("Income Statement Report")
                .createdBy(TEST_USER_ID)
                .build();
    }

    private CompanyAggregate createMockCompany() {
        CompanyAggregate company = mock(CompanyAggregate.class);
        
        when(company.getCompanyId()).thenReturn(TEST_COMPANY_ID);
        when(company.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(company.getCompanyName()).thenReturn("Test Company");
        
        return company;
    }

    private List<TransactionAggregate> createMockTransactions() {
        List<TransactionAggregate> transactions = new ArrayList<>();
        
        // Income transaction
        TransactionAggregate incomeTransaction = mock(TransactionAggregate.class);
        when(incomeTransaction.getTransactionId()).thenReturn(1001);
        when(incomeTransaction.getTransactionType()).thenReturn(TransactionAggregate.TransactionType.INCOME);
        when(incomeTransaction.getMoney()).thenReturn(Money.of(BigDecimal.valueOf(5000.00), TEST_CURRENCY));
        when(incomeTransaction.getTransactionStatus()).thenReturn(TransactionStatus.approved());
        when(incomeTransaction.getTransactionDate()).thenReturn(TEST_START_DATE.plusDays(10));
        transactions.add(incomeTransaction);
        
        // Expense transaction
        TransactionAggregate expenseTransaction = mock(TransactionAggregate.class);
        when(expenseTransaction.getTransactionId()).thenReturn(1002);
        when(expenseTransaction.getTransactionType()).thenReturn(TransactionAggregate.TransactionType.EXPENSE);
        when(expenseTransaction.getMoney()).thenReturn(Money.of(BigDecimal.valueOf(2000.00), TEST_CURRENCY));
        when(expenseTransaction.getTransactionStatus()).thenReturn(TransactionStatus.approved());
        when(expenseTransaction.getTransactionDate()).thenReturn(TEST_START_DATE.plusDays(20));
        transactions.add(expenseTransaction);
        
        return transactions;
    }

    private FinancialGroupingData createMockFinancialGroupingData() {
        return FinancialGroupingData.builder()
                .build();
    }

    private IncomeExpenseReportData createMockIncomeExpenseReportData() {
        List<IncomeExpenseReportRowDTO> incomeRows = List.of(
            IncomeExpenseReportRowDTO.builder()
                .category("Sales Revenue")
                .description("Income description")
                .type("INCOME")
                .currentMonth(BigDecimal.valueOf(5000.00))
                .yearToDate(BigDecimal.valueOf(50000.00))
                .build()
        );
        
        List<IncomeExpenseReportRowDTO> expenseRows = List.of(
            IncomeExpenseReportRowDTO.builder()
                .category("Operating Expenses")
                .description("Expense description")
                .type("EXPENSE")
                .currentMonth(BigDecimal.valueOf(2000.00))
                .yearToDate(BigDecimal.valueOf(20000.00))
                .build()
        );
        
        return IncomeExpenseReportData.builder()
                .companyName("Test Company")
                .asOfDate(TEST_END_DATE)
                .incomeRows(incomeRows)
                .expenseRows(expenseRows)
                .totalIncomeYTD(BigDecimal.valueOf(50000.00))
                .totalExpenseYTD(BigDecimal.valueOf(20000.00))
                .netIncomeYTD(BigDecimal.valueOf(30000.00))
                .build();
    }

    private List<IncomeExpenseReportRowDTO> createMockIncomeExpenseData() {
        List<IncomeExpenseReportRowDTO> data = new ArrayList<>();
        
        data.add(IncomeExpenseReportRowDTO.builder()
                .category("Sales Revenue")
                .description("Income from sales")
                .type("INCOME")
                .currentMonth(BigDecimal.valueOf(5000.00))
                .yearToDate(BigDecimal.valueOf(50000.00))
                .build());
        
        data.add(IncomeExpenseReportRowDTO.builder()
                .category("Operating Expenses")
                .description("Office expenses")
                .type("EXPENSE")
                .currentMonth(BigDecimal.valueOf(2000.00))
                .yearToDate(BigDecimal.valueOf(20000.00))
                .build());
        
        return data;
    }

    private IncomeStatementData createMockIncomeStatementData() {
        return IncomeStatementData.builder()
                .totalRevenue(BigDecimal.valueOf(5000.00))
                .totalExpenses(BigDecimal.valueOf(2000.00))
                .grossProfit(BigDecimal.valueOf(5000.00))
                .netIncome(BigDecimal.valueOf(3000.00))
                .build();
    }
}