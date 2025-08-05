// File: backend/src/test/java/org/example/backend/application/service/ReportApplicationServiceTest.java
package org.example.backend.application.service;

import org.example.backend.application.dto.GenerateReportCommand;
import org.example.backend.application.dto.ReportDTO;
import org.example.backend.application.dto.ReportListQuery;
import org.example.backend.domain.aggregate.report.ReportAggregate;
import org.example.backend.domain.aggregate.report.ReportAggregateRepository;
import org.example.backend.domain.event.DomainEventPublisher;
import org.example.backend.domain.event.ReportGenerationStartedEvent;
import org.example.backend.domain.valueobject.ReportStatus;
import org.example.backend.domain.valueobject.ReportType;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.infrastructure.report.ReportGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReportApplicationService
 * 
 * Tests the core business logic of report management including:
 * 1. Report generation workflow
 * 2. Query operations (get, list, filter)  
 * 3. Status management (archive, delete)
 * 4. Business rule validation
 * 5. Exception handling
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Report Application Service Tests - Priority Coverage Improvement")
class ReportApplicationServiceTest {

    @InjectMocks
    private ReportApplicationService reportApplicationService;

    @Mock
    private ReportAggregateRepository reportRepository;
    
    @Mock
    private ReportGenerationService reportGenerationService;
    
    @Mock
    private DomainEventPublisher domainEventPublisher;
    
    @Mock
    private IncomeStatementDataService incomeStatementDataService;
    
    @Mock
    private FinancialGroupingDataService financialGroupingDataService;
    
    @Mock
    private BalanceSheetDataService balanceSheetDataService;
    
    @Mock
    private IncomeExpenseDataService incomeExpenseDataService;

    // Test constants
    private static final Integer TEST_COMPANY_ID = 1;
    private static final Integer TEST_USER_ID = 100;
    private static final Integer TEST_REPORT_ID = 1001;
    private static final LocalDate TEST_START_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate TEST_END_DATE = LocalDate.of(2024, 12, 31);
    private static final String TEST_REPORT_NAME = "Test Financial Report";

    @BeforeEach
    void setUp() {
        // Setup common mock behavior only when needed for specific tests
    }

    // ========== Generate Report Tests (Core Functionality) ==========

    @Test
    @DisplayName("Should generate income statement report successfully")
    void shouldGenerateIncomeStatementReportSuccessfully() {
        // Given
        GenerateReportCommand command = createIncomeStatementCommand();
        ReportAggregate mockReport = createMockReportAggregate();
        
        when(reportRepository.existsGeneratingReport(any(TenantId.class), any(ReportType.class), 
                any(LocalDate.class), any(LocalDate.class))).thenReturn(false);
        when(reportRepository.findByMultipleCriteria(any(TenantId.class), any(ReportType.class), 
                any(ReportStatus.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(reportRepository.save(any(ReportAggregate.class))).thenReturn(mockReport);

        // When
        String result = reportApplicationService.generateReport(command);

        // Then
        assertNotNull(result);
        assertEquals(TEST_REPORT_ID.toString(), result);
        
        // Verify repository interactions - Note: save() called twice (once in generateReport, once in generateReportAsync)
        verify(reportRepository).existsGeneratingReport(any(TenantId.class), eq(ReportType.INCOME_STATEMENT), 
                eq(TEST_START_DATE), eq(TEST_END_DATE));
        verify(reportRepository, times(2)).save(any(ReportAggregate.class)); // Called twice: main flow + async flow
        
        // Verify event publishing
        ArgumentCaptor<ReportGenerationStartedEvent> eventCaptor = 
                ArgumentCaptor.forClass(ReportGenerationStartedEvent.class);
        verify(domainEventPublisher).publish(eventCaptor.capture());
        
        ReportGenerationStartedEvent publishedEvent = eventCaptor.getValue();
        assertEquals(TEST_REPORT_ID, publishedEvent.getReportId());
        assertEquals(ReportType.INCOME_STATEMENT, publishedEvent.getReportType());
    }

    @Test
    @DisplayName("Should generate financial grouping report successfully")
    void shouldGenerateFinancialGroupingReportSuccessfully() {
        // Given
        GenerateReportCommand command = createFinancialGroupingCommand();
        ReportAggregate mockReport = createMockReportAggregate();
        
        when(reportRepository.existsGeneratingReport(any(TenantId.class), any(ReportType.class), 
                any(LocalDate.class), any(LocalDate.class))).thenReturn(false);
        when(reportRepository.findByMultipleCriteria(any(TenantId.class), any(ReportType.class), 
                any(ReportStatus.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(reportRepository.save(any(ReportAggregate.class))).thenReturn(mockReport);

        // When
        String result = reportApplicationService.generateReport(command);

        // Then
        assertNotNull(result);
        assertEquals(TEST_REPORT_ID.toString(), result);
        verify(reportRepository, times(2)).save(any(ReportAggregate.class)); // Called twice: main flow + async flow
        verify(domainEventPublisher).publish(any(ReportGenerationStartedEvent.class));
    }

    @Test
    @DisplayName("Should generate balance sheet report successfully")
    void shouldGenerateBalanceSheetReportSuccessfully() {
        // Given
        GenerateReportCommand command = createBalanceSheetCommand();
        ReportAggregate mockReport = createMockReportAggregate();
        
        when(reportRepository.existsGeneratingReport(any(TenantId.class), any(ReportType.class), 
                any(LocalDate.class), any(LocalDate.class))).thenReturn(false);
        when(reportRepository.findByMultipleCriteria(any(TenantId.class), any(ReportType.class), 
                any(ReportStatus.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(reportRepository.save(any(ReportAggregate.class))).thenReturn(mockReport);

        // When
        String result = reportApplicationService.generateReport(command);

        // Then
        assertNotNull(result);
        assertEquals(TEST_REPORT_ID.toString(), result);
        verify(reportRepository, times(2)).save(any(ReportAggregate.class)); // Called twice: main flow + async flow
    }

    // ========== Business Rule Validation Tests ==========

    @Test
    @DisplayName("Should throw exception when generate report command is null")
    void shouldThrowExceptionWhenGenerateReportCommandIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reportApplicationService.generateReport(null);
        });
        
        assertEquals("Generate report command cannot be null", exception.getMessage());
        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when tenant ID is null")
    void shouldThrowExceptionWhenTenantIdIsNull() {
        // Given
        GenerateReportCommand command = GenerateReportCommand.builder()
                .tenantId(null)
                .reportType(ReportType.INCOME_STATEMENT)
                .reportName(TEST_REPORT_NAME)
                .startDate(TEST_START_DATE)
                .endDate(TEST_END_DATE)
                .createdBy(TEST_USER_ID)
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reportApplicationService.generateReport(command);
        });
        
        assertEquals("Tenant ID cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when report type is null")
    void shouldThrowExceptionWhenReportTypeIsNull() {
        // Given
        GenerateReportCommand command = GenerateReportCommand.builder()
                .tenantId(TEST_COMPANY_ID)
                .reportType(null)
                .reportName(TEST_REPORT_NAME)
                .startDate(TEST_START_DATE)
                .endDate(TEST_END_DATE)
                .createdBy(TEST_USER_ID)
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reportApplicationService.generateReport(command);
        });
        
        assertEquals("Report type cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when report name is empty")
    void shouldThrowExceptionWhenReportNameIsEmpty() {
        // Given
        GenerateReportCommand command = GenerateReportCommand.builder()
                .tenantId(TEST_COMPANY_ID)
                .reportType(ReportType.INCOME_STATEMENT)
                .reportName("")
                .startDate(TEST_START_DATE)
                .endDate(TEST_END_DATE)
                .createdBy(TEST_USER_ID)
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reportApplicationService.generateReport(command);
        });
        
        assertEquals("Report name cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when start date is after end date")
    void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
        // Given
        GenerateReportCommand command = GenerateReportCommand.builder()
                .tenantId(TEST_COMPANY_ID)
                .reportType(ReportType.INCOME_STATEMENT)
                .reportName(TEST_REPORT_NAME)
                .startDate(LocalDate.of(2024, 12, 31))
                .endDate(LocalDate.of(2024, 1, 1))
                .createdBy(TEST_USER_ID)
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reportApplicationService.generateReport(command);
        });
        
        assertEquals("Start date cannot be after end date", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when duplicate report is generating")
    void shouldThrowExceptionWhenDuplicateReportIsGenerating() {
        // Given
        GenerateReportCommand command = createIncomeStatementCommand();
        
        when(reportRepository.existsGeneratingReport(any(TenantId.class), any(ReportType.class), 
                any(LocalDate.class), any(LocalDate.class))).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reportApplicationService.generateReport(command);
        });
        
        assertTrue(exception.getMessage().contains("currently being generated"));
        verify(reportRepository, never()).save(any());
    }

    // ========== Query Operation Tests ==========

    @Test
    @DisplayName("Should get report by ID successfully")
    void shouldGetReportByIdSuccessfully() {
        // Given
        ReportAggregate mockReport = createMockReportAggregateForQuery();
        when(reportRepository.findByIdAndTenant(TEST_REPORT_ID, TenantId.of(TEST_COMPANY_ID)))
                .thenReturn(Optional.of(mockReport));

        // When
        Optional<ReportDTO> result = reportApplicationService.getReport(TEST_REPORT_ID, TEST_COMPANY_ID);

        // Then
        assertTrue(result.isPresent());
        ReportDTO reportDTO = result.get();
        assertEquals(TEST_REPORT_ID, reportDTO.getReportId());
        assertEquals(TEST_REPORT_NAME, reportDTO.getReportName());
        assertEquals(ReportType.INCOME_STATEMENT, reportDTO.getReportType());
        
        verify(reportRepository).findByIdAndTenant(TEST_REPORT_ID, TenantId.of(TEST_COMPANY_ID));
    }

    @Test
    @DisplayName("Should return empty when report not found")
    void shouldReturnEmptyWhenReportNotFound() {
        // Given
        when(reportRepository.findByIdAndTenant(TEST_REPORT_ID, TenantId.of(TEST_COMPANY_ID)))
                .thenReturn(Optional.empty());

        // When
        Optional<ReportDTO> result = reportApplicationService.getReport(TEST_REPORT_ID, TEST_COMPANY_ID);

        // Then
        assertFalse(result.isPresent());
        verify(reportRepository).findByIdAndTenant(TEST_REPORT_ID, TenantId.of(TEST_COMPANY_ID));
    }

    @Test
    @DisplayName("Should get reports list with filters successfully")
    void shouldGetReportsListWithFiltersSuccessfully() {
        // Given
        ReportListQuery query = createReportListQuery();
        List<ReportAggregate> mockReports = Arrays.asList(
                createMockReportAggregateForQuery(),
                createMockReportAggregateForQuery()
        );
        
        when(reportRepository.findByMultipleCriteria(any(TenantId.class), any(ReportType.class), 
                any(ReportStatus.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(mockReports);

        // When
        List<ReportDTO> result = reportApplicationService.getReports(query);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(reportRepository).findByMultipleCriteria(any(TenantId.class), eq(ReportType.INCOME_STATEMENT), 
                eq(ReportStatus.COMPLETED), eq(TEST_START_DATE), eq(TEST_END_DATE));
    }

    @Test
    @DisplayName("Should get reports list without filters successfully")
    void shouldGetReportsListWithoutFiltersSuccessfully() {
        // Given
        ReportListQuery query = ReportListQuery.builder()
                .tenantId(TEST_COMPANY_ID)
                .build();
        List<ReportAggregate> mockReports = Arrays.asList(createMockReportAggregateForQuery());
        
        when(reportRepository.findByTenantIdOrderByCreatedAtDesc(TenantId.of(TEST_COMPANY_ID)))
                .thenReturn(mockReports);

        // When
        List<ReportDTO> result = reportApplicationService.getReports(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reportRepository).findByTenantIdOrderByCreatedAtDesc(TenantId.of(TEST_COMPANY_ID));
    }

    @Test
    @DisplayName("Should get recent reports successfully")
    void shouldGetRecentReportsSuccessfully() {
        // Given
        int limit = 5;
        List<ReportAggregate> mockReports = Arrays.asList(createMockReportAggregateForQuery());
        
        when(reportRepository.findByTenantAndCreatedSince(any(TenantId.class), any(LocalDateTime.class)))
                .thenReturn(mockReports);

        // When
        List<ReportDTO> result = reportApplicationService.getRecentReports(TEST_COMPANY_ID, limit);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reportRepository).findByTenantAndCreatedSince(eq(TenantId.of(TEST_COMPANY_ID)), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should get reports by type successfully")
    void shouldGetReportsByTypeSuccessfully() {
        // Given
        ReportType reportType = ReportType.INCOME_STATEMENT;
        List<ReportAggregate> mockReports = Arrays.asList(createMockReportAggregateForQuery());
        
        when(reportRepository.findByTenantIdAndReportTypeOrderByCreatedAtDesc(TenantId.of(TEST_COMPANY_ID), reportType))
                .thenReturn(mockReports);

        // When
        List<ReportDTO> result = reportApplicationService.getReportsByType(TEST_COMPANY_ID, reportType);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reportRepository).findByTenantIdAndReportTypeOrderByCreatedAtDesc(TenantId.of(TEST_COMPANY_ID), reportType);
    }

    // ========== Status Management Tests ==========

    @Test
    @DisplayName("Should archive report successfully")
    void shouldArchiveReportSuccessfully() {
        // Given
        ReportAggregate mockReport = mock(ReportAggregate.class);
        when(reportRepository.findByIdAndTenant(TEST_REPORT_ID, TenantId.of(TEST_COMPANY_ID)))
                .thenReturn(Optional.of(mockReport));

        // When
        reportApplicationService.archiveReport(TEST_REPORT_ID, TEST_COMPANY_ID);

        // Then
        verify(reportRepository).findByIdAndTenant(TEST_REPORT_ID, TenantId.of(TEST_COMPANY_ID));
        verify(reportRepository).save(mockReport);
    }

    @Test
    @DisplayName("Should throw exception when archiving non-existent report")
    void shouldThrowExceptionWhenArchivingNonExistentReport() {
        // Given
        when(reportRepository.findByIdAndTenant(TEST_REPORT_ID, TenantId.of(TEST_COMPANY_ID)))
                .thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reportApplicationService.archiveReport(TEST_REPORT_ID, TEST_COMPANY_ID);
        });
        
        assertEquals("Report not found", exception.getMessage());
        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete report successfully")
    void shouldDeleteReportSuccessfully() {
        // Given
        ReportAggregate mockReport = mock(ReportAggregate.class);
        when(mockReport.getStatus()).thenReturn(ReportStatus.COMPLETED);
        when(mockReport.getFilePath()).thenReturn("/reports/test_report.xlsx");
        when(reportRepository.findByIdAndTenant(TEST_REPORT_ID, TenantId.of(TEST_COMPANY_ID)))
                .thenReturn(Optional.of(mockReport));

        // When
        reportApplicationService.deleteReport(TEST_REPORT_ID, TEST_COMPANY_ID);

        // Then
        verify(reportRepository).findByIdAndTenant(TEST_REPORT_ID, TenantId.of(TEST_COMPANY_ID));
        verify(reportRepository).delete(mockReport);
        // Verify file deletion if file path exists
        verify(reportGenerationService).deleteReportFile("/reports/test_report.xlsx");
    }

    @Test
    @DisplayName("Should throw exception when deleting generating report")
    void shouldThrowExceptionWhenDeletingGeneratingReport() {
        // Given
        ReportAggregate mockReport = mock(ReportAggregate.class);
        when(mockReport.getStatus()).thenReturn(ReportStatus.GENERATING);
        when(reportRepository.findByIdAndTenant(TEST_REPORT_ID, TenantId.of(TEST_COMPANY_ID)))
                .thenReturn(Optional.of(mockReport));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            reportApplicationService.deleteReport(TEST_REPORT_ID, TEST_COMPANY_ID);
        });
        
        assertEquals("Cannot delete report while generating", exception.getMessage());
        verify(reportRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should get report statistics successfully")
    void shouldGetReportStatisticsSuccessfully() {
        // Given
        TenantId tenantId = TenantId.of(TEST_COMPANY_ID);
        when(reportRepository.countByTenantAndStatus(tenantId, null)).thenReturn(10L);
        when(reportRepository.countByTenantAndStatus(tenantId, ReportStatus.COMPLETED)).thenReturn(8L);
        when(reportRepository.countByTenantAndStatus(tenantId, ReportStatus.FAILED)).thenReturn(1L);
        when(reportRepository.countByTenantAndStatus(tenantId, ReportStatus.GENERATING)).thenReturn(1L);
        when(reportRepository.getTotalFileSizeByTenant(tenantId)).thenReturn(1024000L);

        // When
        ReportApplicationService.ReportStatistics result = 
                reportApplicationService.getReportStatistics(TEST_COMPANY_ID);

        // Then
        assertNotNull(result);
        assertEquals(10L, result.getTotalReports());
        assertEquals(8L, result.getCompletedReports());
        assertEquals(1L, result.getFailedReports());
        assertEquals(1L, result.getGeneratingReports());
        assertEquals(1024000L, result.getTotalFileSize());
        
        verify(reportRepository).countByTenantAndStatus(tenantId, null);
        verify(reportRepository).countByTenantAndStatus(tenantId, ReportStatus.COMPLETED);
        verify(reportRepository).countByTenantAndStatus(tenantId, ReportStatus.FAILED);
        verify(reportRepository).countByTenantAndStatus(tenantId, ReportStatus.GENERATING);
        verify(reportRepository).getTotalFileSizeByTenant(tenantId);
    }

    // ========== Helper Methods ==========

    private GenerateReportCommand createIncomeStatementCommand() {
        return GenerateReportCommand.builder()
                .tenantId(TEST_COMPANY_ID)
                .reportType(ReportType.INCOME_STATEMENT)
                .reportName(TEST_REPORT_NAME)
                .startDate(TEST_START_DATE)
                .endDate(TEST_END_DATE)
                .createdBy(TEST_USER_ID)
                .aiAnalysisEnabled(false)
                .build();
    }

    private GenerateReportCommand createFinancialGroupingCommand() {
        return GenerateReportCommand.builder()
                .tenantId(TEST_COMPANY_ID)
                .reportType(ReportType.FINANCIAL_GROUPING)
                .reportName("Financial Grouping Report")
                .startDate(TEST_START_DATE)
                .endDate(TEST_END_DATE)
                .createdBy(TEST_USER_ID)
                .aiAnalysisEnabled(false)
                .build();
    }

    private GenerateReportCommand createBalanceSheetCommand() {
        return GenerateReportCommand.builder()
                .tenantId(TEST_COMPANY_ID)
                .reportType(ReportType.BALANCE_SHEET)
                .reportName("Balance Sheet Report")
                .startDate(TEST_START_DATE)
                .endDate(TEST_END_DATE)
                .createdBy(TEST_USER_ID)
                .aiAnalysisEnabled(false)
                .build();
    }

    private ReportListQuery createReportListQuery() {
        return ReportListQuery.builder()
                .tenantId(TEST_COMPANY_ID)
                .reportType(ReportType.INCOME_STATEMENT)
                .status(ReportStatus.COMPLETED)
                .startDate(TEST_START_DATE)
                .endDate(TEST_END_DATE)
                .pageNumber(0)
                .pageSize(20)
                .build();
    }

    private ReportAggregate createMockReportAggregate() {
        ReportAggregate mockReport = mock(ReportAggregate.class);
        when(mockReport.getReportId()).thenReturn(TEST_REPORT_ID);
        when(mockReport.getReportName()).thenReturn(TEST_REPORT_NAME);
        when(mockReport.getReportType()).thenReturn(ReportType.INCOME_STATEMENT);
        when(mockReport.getStatus()).thenReturn(ReportStatus.COMPLETED);
        when(mockReport.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(mockReport.getCreatedBy()).thenReturn(TEST_USER_ID);
        when(mockReport.getFilePath()).thenReturn("/reports/test_report.xlsx");
        return mockReport;
    }

    private ReportAggregate createMockReportAggregateForQuery() {
        ReportAggregate mockReport = mock(ReportAggregate.class);
        when(mockReport.getReportId()).thenReturn(TEST_REPORT_ID);
        when(mockReport.getReportName()).thenReturn(TEST_REPORT_NAME);
        when(mockReport.getReportType()).thenReturn(ReportType.INCOME_STATEMENT);
        when(mockReport.getStatus()).thenReturn(ReportStatus.COMPLETED);
        when(mockReport.getStartDate()).thenReturn(TEST_START_DATE);
        when(mockReport.getEndDate()).thenReturn(TEST_END_DATE);
        when(mockReport.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(mockReport.getCreatedBy()).thenReturn(TEST_USER_ID);
        when(mockReport.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(mockReport.getUpdatedAt()).thenReturn(LocalDateTime.now());
        when(mockReport.getFilePath()).thenReturn("/reports/test_report.xlsx");
        when(mockReport.getFileFormat()).thenReturn("xlsx");
        when(mockReport.getFileSize()).thenReturn(1024L);
        when(mockReport.getAiAnalysisEnabled()).thenReturn(false);
        return mockReport;
    }
}