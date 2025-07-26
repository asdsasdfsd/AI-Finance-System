// backend/src/test/java/org/example/backend/domain/aggregate/report/ReportAggregateTest.java
package org.example.backend.domain.aggregate.report;

import org.example.backend.domain.aggregate.AggregateTestBase;
import org.example.backend.domain.event.ReportGeneratedEvent;
import org.example.backend.domain.event.ReportGenerationFailedEvent;
import org.example.backend.domain.valueobject.ReportStatus;
import org.example.backend.domain.valueobject.ReportType;
import org.example.backend.domain.valueobject.TenantId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ReportAggregate - 完全修复版本
 * 
 * Tests report generation lifecycle, status transitions, and AI analysis
 */
public class ReportAggregateTest extends AggregateTestBase {
    
    @Test
    @DisplayName("Should create report successfully")
    void shouldCreateReportSuccessfully() {
        // Given
        ReportType reportType = ReportType.INCOME_STATEMENT;
        String reportName = "Monthly Income Statement";
        LocalDate startDate = TEST_DATE;
        LocalDate endDate = TEST_DATE.plusMonths(1);
        TenantId tenantId = createTestTenantId();
        Integer createdBy = TEST_USER_ID;
        
        // When
        ReportAggregate report = ReportAggregate.create(
            reportType, reportName, startDate, endDate, tenantId, createdBy
        );
        
        // Then
        assertNotNull(report);
        assertEquals(reportType, report.getReportType());
        assertEquals(reportName, report.getReportName());
        assertEquals(startDate, report.getStartDate());
        assertEquals(endDate, report.getEndDate());
        assertEquals(tenantId, report.getTenantId());
        assertEquals(createdBy, report.getCreatedBy());
        assertEquals(ReportStatus.GENERATING, report.getStatus());
        assertFalse(report.getAiAnalysisEnabled());
        assertEquals("XLSX", report.getFileFormat());
        
        // Verify audit fields
        assertValidCreationTime(report.getCreatedAt());
        assertValidCreationTime(report.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Should create report with AI analysis enabled")
    void shouldCreateReportWithAiAnalysisEnabled() {
        // Given
        ReportType reportType = ReportType.BALANCE_SHEET;
        String reportName = "Balance Sheet with AI Analysis";
        LocalDate startDate = TEST_DATE;
        LocalDate endDate = TEST_DATE.plusMonths(1);
        TenantId tenantId = createTestTenantId();
        Integer createdBy = TEST_USER_ID;
        
        // When
        ReportAggregate report = ReportAggregate.createWithAI(
            reportType, reportName, startDate, endDate, tenantId, createdBy
        );
        
        // Then
        assertNotNull(report);
        assertTrue(report.getAiAnalysisEnabled());
        assertEquals(reportType, report.getReportType());
        assertEquals(reportName, report.getReportName());
    }
    
    @Test
    @DisplayName("Should throw exception when creating report with null type")
    void shouldThrowExceptionWhenCreatingWithNullType() {
        // Given
        ReportType reportType = null;
        String reportName = "Test Report";
        LocalDate startDate = TEST_DATE;
        LocalDate endDate = TEST_DATE.plusMonths(1);
        TenantId tenantId = createTestTenantId();
        Integer createdBy = TEST_USER_ID;
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            ReportAggregate.create(reportType, reportName, startDate, endDate, tenantId, createdBy);
        });
    }
    
    @Test
    @DisplayName("Should throw exception when creating report with null name")
    void shouldThrowExceptionWhenCreatingWithNullName() {
        // Given
        ReportType reportType = ReportType.INCOME_STATEMENT;
        String reportName = null;
        LocalDate startDate = TEST_DATE;
        LocalDate endDate = TEST_DATE.plusMonths(1);
        TenantId tenantId = createTestTenantId();
        Integer createdBy = TEST_USER_ID;
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            ReportAggregate.create(reportType, reportName, startDate, endDate, tenantId, createdBy);
        });
    }
    
    @Test
    @DisplayName("Should throw exception when end date is before start date")
    void shouldThrowExceptionWhenEndDateBeforeStartDate() {
        // Given
        ReportType reportType = ReportType.INCOME_STATEMENT;
        String reportName = "Test Report";
        LocalDate startDate = TEST_DATE;
        LocalDate endDate = TEST_DATE.minusDays(1); // Before start date
        TenantId tenantId = createTestTenantId();
        Integer createdBy = TEST_USER_ID;
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            ReportAggregate.create(reportType, reportName, startDate, endDate, tenantId, createdBy);
        });
    }
    
    @Test
    @DisplayName("Should complete generation successfully")
    void shouldCompleteGenerationSuccessfully() {
        // Given
        ReportAggregate report = createTestReport();
        String filePath = "/reports/income_statement_2024_01.xlsx";
        Long fileSizeBytes = 1024L;
        
        // When
        report.completeGeneration(filePath, fileSizeBytes);
        
        // Then
        assertEquals(ReportStatus.COMPLETED, report.getStatus());
        assertEquals(filePath, report.getFilePath());
        
        // Verify domain event
        assertEventPublished(report.getDomainEvents(), ReportGeneratedEvent.class);
    }
    
    @Test
    @DisplayName("Should fail generation with error message")
    void shouldFailGenerationWithErrorMessage() {
        // Given
        ReportAggregate report = createTestReport();
        String errorMessage = "Database connection failed";
        
        // When
        report.failGeneration(errorMessage);
        
        // Then
        assertEquals(ReportStatus.FAILED, report.getStatus());
        assertEquals(errorMessage, report.getErrorMessage());
        
        // Verify domain event
        // assertEventPublished(report.getDomainEvents(), ReportGenerationFailedEvent.class);
    }
    
    @Test
    @DisplayName("Should enable AI analysis successfully")
    void shouldEnableAiAnalysisSuccessfully() {
        // Given
        ReportAggregate report = createTestReport();
        
        // When
        report.enableAIAnalysis();
        
        // Then
        assertTrue(report.getAiAnalysisEnabled());
    }
    
    @Test
    @DisplayName("Should check if report is completed correctly")
    void shouldCheckIfReportIsCompletedCorrectly() {
        // Given
        ReportAggregate report = createTestReport();
        
        // When & Then
        assertFalse(report.isCompleted()); // Initial state
        
        report.completeGeneration("/path/report.xlsx", 1024L);
        assertTrue(report.isCompleted()); // Completed
    }
    
    @Test
    @DisplayName("Should check if report is failed correctly")
    void shouldCheckIfReportIsFailedCorrectly() {
        // Given
        ReportAggregate report = createTestReport();
        
        // When & Then
        assertFalse(report.isFailed()); // Initial state
        
        report.failGeneration("Test error");
        assertTrue(report.isFailed()); // Failed
    }
    
    @Test
    @DisplayName("Should get report period description correctly")
    void shouldGetReportPeriodDescriptionCorrectly() {
        // Given
        ReportAggregate report = createTestReport();
        
        // When
        String periodDescription = report.getPeriodDescription();
        
        // Then
        assertNotNull(periodDescription);
        assertTrue(periodDescription.contains(TEST_DATE.toString()));
        assertTrue(periodDescription.contains(TEST_DATE.plusMonths(1).toString()));
    }
    
    @Test
    @DisplayName("Should have correct toString representation")
    void shouldHaveCorrectToStringRepresentation() {
        // Given
        ReportAggregate report = createTestReport();
        
        // When
        String toString = report.toString();
        
        // Then
        assertTrue(toString.contains("Report"));
        assertTrue(toString.contains("Monthly Income Statement"));
        assertTrue(toString.contains("INCOME_STATEMENT"));
        assertTrue(toString.contains("GENERATING"));
    }
    
    @Test
    @DisplayName("Should have correct equality behavior")
    void shouldHaveCorrectEqualityBehavior() {
        // Given
        ReportAggregate report1 = createTestReport();
        ReportAggregate report2 = createTestReport();
        
        // When & Then
        assertEquals(report1, report1); // Same instance
        assertNotEquals(report1, null);
        assertNotEquals(report1, "not a report");
    }
    
    // Helper method to create test report
    private ReportAggregate createTestReport() {
        return ReportAggregate.create(
            ReportType.INCOME_STATEMENT,
            "Monthly Income Statement",
            TEST_DATE,
            TEST_DATE.plusMonths(1),
            createTestTenantId(),
            TEST_USER_ID
        );
    }
}