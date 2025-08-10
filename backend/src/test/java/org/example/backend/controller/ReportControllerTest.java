// backend/src/test/java/org/example/backend/controller/ReportControllerTest.java
package org.example.backend.controller;

import org.example.backend.application.service.ReportApplicationService;
import org.example.backend.application.dto.ReportDTO;
import org.example.backend.application.dto.GenerateReportCommand;
import org.example.backend.application.dto.ReportListQuery;
import org.example.backend.domain.valueobject.ReportType;
import org.example.backend.domain.valueobject.ReportStatus;
import org.example.backend.infrastructure.report.ReportFileManager;
import org.example.backend.util.JwtContextUtil;
import org.example.backend.exception.ResourceNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReportController - Testing Real Controller with Mocked Dependencies
 * 
 * This test class creates a REAL instance of ReportController and mocks its dependencies,
 * following the proper unit testing approach for testing HTTP layer business logic.
 * 
 * Coverage Target: From 0% to 80%+ for all Controller methods
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Report Controller Tests - Real Controller Implementation")
class ReportControllerTest {

    // Mock dependencies (not the controller itself!)
    @Mock
    private ReportApplicationService reportApplicationService;

    // Real controller instance under test
    private ReportController reportController;

    // Test constants
    private static final Integer TEST_REPORT_ID = 1;
    private static final Integer TEST_TENANT_ID = 1;
    private static final Integer TEST_USER_ID = 1;
    private static final String TEST_REPORT_NAME = "Test Income Statement";
    private static final LocalDate TEST_START_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate TEST_END_DATE = LocalDate.of(2024, 3, 31);
    private static final String TEST_FILE_PATH = "/reports/test_report.xlsx";

    @BeforeEach
    void setUp() {
        // Create real controller instance with mocked dependencies using reflection
        reportController = new ReportController();
        
        // Use reflection to set private fields since they use @Autowired
        setPrivateField(reportController, "reportApplicationService", reportApplicationService);
    }

    /**
     * Helper method to set private fields using reflection
     */
    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    // ========== Health Check Tests ==========
    
    @Test
    @DisplayName("Should return health check status")
    void shouldReturnHealthCheckStatus() {
        // When
        ResponseEntity<Map<String, Object>> response = reportController.healthCheck();
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        assertEquals("Report service is running", body.get("message"));
        assertNotNull(body.get("timestamp"));
    }

    // ========== Generate Report Tests ==========
    
    @Test
    @DisplayName("Should generate report successfully")
    void shouldGenerateReportSuccessfully() {
        // Given
        GenerateReportCommand command = createValidGenerateReportCommand();
        String expectedReportId = "REPORT_123";
        
        when(reportApplicationService.generateReport(any(GenerateReportCommand.class)))
            .thenReturn(expectedReportId);
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.generateReport(command);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) body.get("data");
        assertEquals(expectedReportId, data.get("reportId"));
        assertEquals("Report generation started successfully", body.get("message"));
        
        verify(reportApplicationService).generateReport(any(GenerateReportCommand.class));
    }
    
    @Test
    @DisplayName("Should handle IllegalArgumentException from service")
    void shouldHandleIllegalArgumentExceptionFromService() {
        // Given
        GenerateReportCommand command = createValidGenerateReportCommand();
        
        when(reportApplicationService.generateReport(any(GenerateReportCommand.class)))
            .thenThrow(new IllegalArgumentException("Invalid report parameters"));
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.generateReport(command);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("error", body.get("status"));
        assertEquals("Invalid report parameters", body.get("message"));
        
        verify(reportApplicationService).generateReport(any(GenerateReportCommand.class));
    }
    
    @Test
    @DisplayName("Should handle general exception from service")
    void shouldHandleGeneralExceptionFromService() {
        // Given
        GenerateReportCommand command = createValidGenerateReportCommand();
        
        when(reportApplicationService.generateReport(any(GenerateReportCommand.class)))
            .thenThrow(new RuntimeException("Database connection failed"));
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.generateReport(command);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("error", body.get("status"));
        assertTrue(body.get("message").toString().contains("Failed to generate report"));
        
        verify(reportApplicationService).generateReport(any(GenerateReportCommand.class));
    }

    @Test
    @DisplayName("Should set default tenant and user when null")
    void shouldSetDefaultTenantAndUserWhenNull() {
        // Given
        GenerateReportCommand command = GenerateReportCommand.builder()
                .reportType(ReportType.INCOME_STATEMENT)
                .reportName(TEST_REPORT_NAME)
                .startDate(TEST_START_DATE)
                .endDate(TEST_END_DATE)
                .aiAnalysisEnabled(false)
                // tenantId and createdBy are null
                .build();
        
        when(reportApplicationService.generateReport(any(GenerateReportCommand.class)))
            .thenReturn("REPORT_123");
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.generateReport(command);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verify that defaults were set
        assertEquals(Integer.valueOf(1), command.getTenantId());
        assertEquals(Integer.valueOf(1), command.getCreatedBy());
        
        verify(reportApplicationService).generateReport(command);
    }

    // ========== Get Reports List Tests ==========
    
    @Test
    @DisplayName("Should get reports list with all parameters")
    void shouldGetReportsListWithAllParameters() {
        // Given
        List<ReportDTO> expectedReports = Arrays.asList(createMockReportDTO(), createMockReportDTO());
        
        when(reportApplicationService.getReports(any(ReportListQuery.class)))
            .thenReturn(expectedReports);
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.getReports(
            "INCOME_STATEMENT", "COMPLETED", 
            "2024-01-01", "2024-03-31", "test", 0, 20
        );
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        assertEquals(expectedReports, body.get("data"));
        assertEquals("Reports retrieved successfully", body.get("message"));
        
        verify(reportApplicationService).getReports(any(ReportListQuery.class));
    }
    
    @Test
    @DisplayName("Should get reports list with minimal parameters")
    void shouldGetReportsListWithMinimalParameters() {
        // Given
        List<ReportDTO> expectedReports = Arrays.asList(createMockReportDTO());
        
        when(reportApplicationService.getReports(any(ReportListQuery.class)))
            .thenReturn(expectedReports);
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.getReports(
            null, null, null, null, null, null, null
        );
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        assertEquals(expectedReports, body.get("data"));
        
        verify(reportApplicationService).getReports(any(ReportListQuery.class));
    }

    @Test
    @DisplayName("Should handle invalid start date format")
    void shouldHandleInvalidStartDateFormat() {
        // When
        ResponseEntity<Map<String, Object>> response = reportController.getReports(
            null, null, "invalid-date", null, null, 0, 20
        );
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("error", body.get("status"));
        assertTrue(body.get("message").toString().contains("Invalid start date format"));
        
        verify(reportApplicationService, never()).getReports(any());
    }

    @Test
    @DisplayName("Should handle invalid end date format")
    void shouldHandleInvalidEndDateFormat() {
        // When
        ResponseEntity<Map<String, Object>> response = reportController.getReports(
            null, null, null, "invalid-date", null, 0, 20
        );
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("error", body.get("status"));
        assertTrue(body.get("message").toString().contains("Invalid end date format"));
        
        verify(reportApplicationService, never()).getReports(any());
    }
    
    @Test
    @DisplayName("Should handle exception when getting reports list")
    void shouldHandleExceptionWhenGettingReportsList() {
        // Given
        when(reportApplicationService.getReports(any(ReportListQuery.class)))
            .thenThrow(new RuntimeException("Service error"));
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.getReports(
            null, null, null, null, null, 0, 20
        );
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("error", body.get("status"));
        assertTrue(body.get("message").toString().contains("Failed to retrieve reports"));
        
        verify(reportApplicationService).getReports(any(ReportListQuery.class));
    }

    // ========== Get Report Details Tests ==========
    
    @Test
    @DisplayName("Should get report details successfully")
    void shouldGetReportDetailsSuccessfully() {
        // Given
        ReportDTO expectedReport = createMockReportDTO();
        
        when(reportApplicationService.getReport(TEST_REPORT_ID, TEST_TENANT_ID))
            .thenReturn(Optional.of(expectedReport));
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.getReport(TEST_REPORT_ID);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        assertEquals(expectedReport, body.get("data"));
        assertEquals("Report details retrieved successfully", body.get("message"));
        
        verify(reportApplicationService).getReport(TEST_REPORT_ID, TEST_TENANT_ID);
    }
    
    @Test
    @DisplayName("Should handle report not found")
    void shouldHandleReportNotFound() {
        // Given
        when(reportApplicationService.getReport(TEST_REPORT_ID, TEST_TENANT_ID))
            .thenReturn(Optional.empty());
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.getReport(TEST_REPORT_ID);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("error", body.get("status"));
        assertEquals("Report not found", body.get("message"));
        
        verify(reportApplicationService).getReport(TEST_REPORT_ID, TEST_TENANT_ID);
    }

    @Test
    @DisplayName("Should handle exception when getting report details")
    void shouldHandleExceptionWhenGettingReportDetails() {
        // Given
        when(reportApplicationService.getReport(TEST_REPORT_ID, TEST_TENANT_ID))
            .thenThrow(new RuntimeException("Database error"));
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.getReport(TEST_REPORT_ID);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("error", body.get("status"));
        assertTrue(body.get("message").toString().contains("Failed to retrieve report"));
        
        verify(reportApplicationService).getReport(TEST_REPORT_ID, TEST_TENANT_ID);
    }

    // ========== Test Data Helper Methods ==========

    private GenerateReportCommand createValidGenerateReportCommand() {
        return GenerateReportCommand.builder()
            .reportType(ReportType.INCOME_STATEMENT)
            .reportName(TEST_REPORT_NAME)
            .startDate(TEST_START_DATE)
            .endDate(TEST_END_DATE)
            .aiAnalysisEnabled(false)
            .tenantId(TEST_TENANT_ID)
            .createdBy(TEST_USER_ID)
            .build();
    }

    private ReportDTO createMockReportDTO() {
        return ReportDTO.builder()
            .reportId(TEST_REPORT_ID)
            .reportName(TEST_REPORT_NAME)
            .reportType(ReportType.INCOME_STATEMENT)
            .status(ReportStatus.COMPLETED)
            .startDate(TEST_START_DATE)
            .endDate(TEST_END_DATE)
            .filePath(TEST_FILE_PATH)
            .fileFormat("xlsx")
            .fileSize(1024L)
            .fileSizeFormatted("1.0 KB")
            .aiAnalysisEnabled(false)
            .createdBy(TEST_USER_ID)
            .createdAt(LocalDateTime.now().minusDays(1))
            .completedAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
}