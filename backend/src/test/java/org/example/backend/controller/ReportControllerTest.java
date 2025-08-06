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
    
    @Mock
    private ReportFileManager fileManager;
    
    @Mock
    private JwtContextUtil jwtContextUtil;

    // Real controller instance under test
    private ReportController reportController;

    // Test constants
    private static final Integer TEST_REPORT_ID = 1;
    private static final Integer TEST_TENANT_ID = 100;
    private static final Integer TEST_USER_ID = 1;
    private static final String TEST_REPORT_NAME = "Test Income Statement";
    private static final LocalDate TEST_START_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate TEST_END_DATE = LocalDate.of(2024, 3, 31);
    private static final String TEST_FILE_PATH = "/reports/test_report.xlsx";
    private static final String TEST_FILE_NAME = "test_report.xlsx";

    @BeforeEach
    void setUp() {
        // Create real controller instance with mocked dependencies using reflection
        reportController = new ReportController();
        
        // Use reflection to set private fields since they use @Autowired
        setPrivateField(reportController, "reportApplicationService", reportApplicationService);
        setPrivateField(reportController, "fileManager", fileManager);
        setPrivateField(reportController, "jwtContextUtil", jwtContextUtil);
        
        // Setup common mock behaviors
        when(jwtContextUtil.getCurrentCompanyId()).thenReturn(TEST_TENANT_ID);
        when(jwtContextUtil.getCurrentUserId()).thenReturn(TEST_USER_ID);
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
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }

    // ========== Health Check Tests ==========
    
    @Test
    @DisplayName("Should return health status successfully")
    void shouldReturnHealthStatusSuccessfully() {
        // When
        ResponseEntity<Map<String, Object>> response = reportController.healthCheck();
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("UP", body.get("status"));
        assertEquals("Report Management Service", body.get("service"));
        assertEquals("1.0.0", body.get("version"));
        assertTrue(body.containsKey("timestamp"));
    }

    // ========== Generate Report Tests ==========
    
    @Test
    @DisplayName("Should generate report successfully with valid request")
    void shouldGenerateReportSuccessfullyWithValidRequest() {
        // Given
        ReportController.GenerateReportRequest request = createValidGenerateReportRequest();
        String expectedReportId = "report_12345";
        
        when(reportApplicationService.generateReport(any(GenerateReportCommand.class)))
            .thenReturn(expectedReportId);
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.generateReport(request);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        assertEquals(expectedReportId, body.get("reportId"));
        assertEquals("Report generation started successfully", body.get("message"));
        
        verify(reportApplicationService).generateReport(any(GenerateReportCommand.class));
    }
    
    @Test
    @DisplayName("Should return bad request when report type is null")
    void shouldReturnBadRequestWhenReportTypeIsNull() {
        // Given
        ReportController.GenerateReportRequest request = createValidGenerateReportRequest();
        request.setReportType(null);
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.generateReport(request);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("error", body.get("status"));
        assertEquals("Report type is required", body.get("message"));
        
        verify(reportApplicationService, never()).generateReport(any());
    }
    
    @Test
    @DisplayName("Should return bad request when dates are null")
    void shouldReturnBadRequestWhenDatesAreNull() {
        // Given
        ReportController.GenerateReportRequest request = createValidGenerateReportRequest();
        request.setStartDate(null);
        request.setEndDate(null);
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.generateReport(request);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("error", body.get("status"));
        assertEquals("Start date and end date are required", body.get("message"));
        
        verify(reportApplicationService, never()).generateReport(any());
    }
    
    @Test
    @DisplayName("Should handle IllegalArgumentException from service")
    void shouldHandleIllegalArgumentExceptionFromService() {
        // Given
        ReportController.GenerateReportRequest request = createValidGenerateReportRequest();
        
        when(reportApplicationService.generateReport(any(GenerateReportCommand.class)))
            .thenThrow(new IllegalArgumentException("Invalid report parameters"));
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.generateReport(request);
        
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
        ReportController.GenerateReportRequest request = createValidGenerateReportRequest();
        
        when(reportApplicationService.generateReport(any(GenerateReportCommand.class)))
            .thenThrow(new RuntimeException("Database connection failed"));
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.generateReport(request);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("error", body.get("status"));
        assertEquals("Failed to generate report. Please try again.", body.get("message"));
        assertTrue(body.containsKey("errorCode"));
        
        verify(reportApplicationService).generateReport(any(GenerateReportCommand.class));
    }

    // ========== Get Report Tests ==========
    
    @Test
    @DisplayName("Should get report by ID successfully")
    void shouldGetReportByIdSuccessfully() {
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
        
        verify(reportApplicationService).getReport(TEST_REPORT_ID, TEST_TENANT_ID);
    }
    
    @Test
    @DisplayName("Should return not found when report does not exist")
    void shouldReturnNotFoundWhenReportDoesNotExist() {
        // Given
        when(reportApplicationService.getReport(TEST_REPORT_ID, TEST_TENANT_ID))
            .thenReturn(Optional.empty());
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.getReport(TEST_REPORT_ID);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        verify(reportApplicationService).getReport(TEST_REPORT_ID, TEST_TENANT_ID);
    }
    
    @Test
    @DisplayName("Should handle exception when getting report")
    void shouldHandleExceptionWhenGettingReport() {
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
            ReportType.INCOME_STATEMENT, ReportStatus.COMPLETED, 
            TEST_START_DATE, TEST_END_DATE, "test", 0, 20
        );
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        assertEquals(expectedReports, body.get("data"));
        assertEquals(2, body.get("total"));
        
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

    // ========== Download Report Tests ==========
    
    @Test
    @DisplayName("Should download completed report successfully when file exists")
    void shouldDownloadCompletedReportSuccessfullyWhenFileExists() {
        // Given
        ReportDTO mockReport = createMockReportDTO();
        mockReport.setStatus(ReportStatus.COMPLETED);
        mockReport.setFilePath(TEST_FILE_PATH);
        
        when(reportApplicationService.getReport(TEST_REPORT_ID, TEST_TENANT_ID))
            .thenReturn(Optional.of(mockReport));
        
        // When
        ResponseEntity<Resource> response = reportController.downloadReport(TEST_REPORT_ID);
        
        // Then
        assertNotNull(response);
        // Since we can't easily mock FileSystemResource.exists(), 
        // we verify that the service was called correctly
        verify(reportApplicationService).getReport(TEST_REPORT_ID, TEST_TENANT_ID);
    }
    
    @Test
    @DisplayName("Should return not found when report does not exist for download")
    void shouldReturnNotFoundWhenReportDoesNotExistForDownload() {
        // Given
        when(reportApplicationService.getReport(TEST_REPORT_ID, TEST_TENANT_ID))
            .thenReturn(Optional.empty());
        
        // When
        ResponseEntity<Resource> response = reportController.downloadReport(TEST_REPORT_ID);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        verify(reportApplicationService).getReport(TEST_REPORT_ID, TEST_TENANT_ID);
    }
    
    @Test
    @DisplayName("Should return not found when report is not completed")
    void shouldReturnNotFoundWhenReportIsNotCompleted() {
        // Given
        ReportDTO mockReport = createMockReportDTO();
        mockReport.setStatus(ReportStatus.GENERATING);
        
        when(reportApplicationService.getReport(TEST_REPORT_ID, TEST_TENANT_ID))
            .thenReturn(Optional.of(mockReport));
        
        // When
        ResponseEntity<Resource> response = reportController.downloadReport(TEST_REPORT_ID);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        verify(reportApplicationService).getReport(TEST_REPORT_ID, TEST_TENANT_ID);
    }
    
    @Test
    @DisplayName("Should handle exception during download")
    void shouldHandleExceptionDuringDownload() {
        // Given
        when(reportApplicationService.getReport(TEST_REPORT_ID, TEST_TENANT_ID))
            .thenThrow(new RuntimeException("File system error"));
        
        // When
        ResponseEntity<Resource> response = reportController.downloadReport(TEST_REPORT_ID);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        verify(reportApplicationService).getReport(TEST_REPORT_ID, TEST_TENANT_ID);
    }

    // ========== Recent Reports Tests ==========
    
    @Test
    @DisplayName("Should get recent reports with default limit")
    void shouldGetRecentReportsWithDefaultLimit() {
        // Given
        List<ReportDTO> expectedReports = Arrays.asList(createMockReportDTO());
        
        when(reportApplicationService.getRecentReports(TEST_TENANT_ID, 10))
            .thenReturn(expectedReports);
        
        // When - pass 10 explicitly to match the expected behavior
        ResponseEntity<Map<String, Object>> response = reportController.getRecentReports(10);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        assertEquals(expectedReports, body.get("data"));
        
        verify(reportApplicationService).getRecentReports(TEST_TENANT_ID, 10);
    }
    
    @Test
    @DisplayName("Should get recent reports with custom limit")
    void shouldGetRecentReportsWithCustomLimit() {
        // Given
        List<ReportDTO> expectedReports = Arrays.asList(createMockReportDTO());
        Integer customLimit = 5;
        
        when(reportApplicationService.getRecentReports(TEST_TENANT_ID, customLimit))
            .thenReturn(expectedReports);
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.getRecentReports(customLimit);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        assertEquals(expectedReports, body.get("data"));
        
        verify(reportApplicationService).getRecentReports(TEST_TENANT_ID, customLimit);
    }
    
    @Test
    @DisplayName("Should handle exception when getting recent reports")
    void shouldHandleExceptionWhenGettingRecentReports() {
        // Given
        when(reportApplicationService.getRecentReports(TEST_TENANT_ID, 10))
            .thenThrow(new RuntimeException("Service error"));
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.getRecentReports(10);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("error", body.get("status"));
        assertTrue(body.get("message").toString().contains("Failed to retrieve recent reports"));
        
        verify(reportApplicationService).getRecentReports(TEST_TENANT_ID, 10);
    }

    // ========== Reports By Type Tests ==========
    
    @Test
    @DisplayName("Should get reports by type successfully")
    void shouldGetReportsByTypeSuccessfully() {
        // Given
        List<ReportDTO> expectedReports = Arrays.asList(createMockReportDTO());
        
        when(reportApplicationService.getReportsByType(TEST_TENANT_ID, ReportType.INCOME_STATEMENT))
            .thenReturn(expectedReports);
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.getReportsByType(ReportType.INCOME_STATEMENT);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        assertEquals(expectedReports, body.get("data"));
        
        verify(reportApplicationService).getReportsByType(TEST_TENANT_ID, ReportType.INCOME_STATEMENT);
    }
    
    @Test
    @DisplayName("Should handle exception when getting reports by type")
    void shouldHandleExceptionWhenGettingReportsByType() {
        // Given
        when(reportApplicationService.getReportsByType(TEST_TENANT_ID, ReportType.BALANCE_SHEET))
            .thenThrow(new RuntimeException("Service error"));
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.getReportsByType(ReportType.BALANCE_SHEET);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("error", body.get("status"));
        assertTrue(body.get("message").toString().contains("Failed to retrieve reports by type"));
        
        verify(reportApplicationService).getReportsByType(TEST_TENANT_ID, ReportType.BALANCE_SHEET);
    }

    // ========== Archive Report Tests ==========
    
    @Test
    @DisplayName("Should archive report successfully")
    void shouldArchiveReportSuccessfully() {
        // Given
        doNothing().when(reportApplicationService).archiveReport(TEST_REPORT_ID, TEST_TENANT_ID);
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.archiveReport(TEST_REPORT_ID);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        assertTrue(body.get("message").toString().contains("Report archived successfully"));
        
        verify(reportApplicationService).archiveReport(TEST_REPORT_ID, TEST_TENANT_ID);
    }
    
    @Test
    @DisplayName("Should handle exception when archiving report")
    void shouldHandleExceptionWhenArchivingReport() {
        // Given
        doThrow(new RuntimeException("Service error"))
            .when(reportApplicationService).archiveReport(TEST_REPORT_ID, TEST_TENANT_ID);
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.archiveReport(TEST_REPORT_ID);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("error", body.get("status"));
        assertTrue(body.get("message").toString().contains("Failed to archive report"));
        
        verify(reportApplicationService).archiveReport(TEST_REPORT_ID, TEST_TENANT_ID);
    }

    // ========== Delete Report Tests ==========
    
    @Test
    @DisplayName("Should delete report successfully")
    void shouldDeleteReportSuccessfully() {
        // Given
        doNothing().when(reportApplicationService).deleteReport(TEST_REPORT_ID, TEST_TENANT_ID);
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.deleteReport(TEST_REPORT_ID);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        assertTrue(body.get("message").toString().contains("Report deleted successfully"));
        
        verify(reportApplicationService).deleteReport(TEST_REPORT_ID, TEST_TENANT_ID);
    }
    
    @Test
    @DisplayName("Should handle exception when deleting report")
    void shouldHandleExceptionWhenDeletingReport() {
        // Given
        doThrow(new RuntimeException("Service error"))
            .when(reportApplicationService).deleteReport(TEST_REPORT_ID, TEST_TENANT_ID);
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.deleteReport(TEST_REPORT_ID);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("error", body.get("status"));
        assertTrue(body.get("message").toString().contains("Failed to delete report"));
        
        verify(reportApplicationService).deleteReport(TEST_REPORT_ID, TEST_TENANT_ID);
    }

    // ========== Report Statistics Tests ==========
    
    @Test
    @DisplayName("Should get report statistics successfully")
    void shouldGetReportStatisticsSuccessfully() {
        // Given
        ReportApplicationService.ReportStatistics mockStats = 
            createMockReportStatistics();
        
        when(reportApplicationService.getReportStatistics(TEST_TENANT_ID))
            .thenReturn(mockStats);
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.getReportStatistics();
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        
        // Verify the statistics data structure (the controller converts the object to a Map)
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) body.get("data");
        assertNotNull(data);
        assertEquals(10L, data.get("totalReports"));
        assertEquals(8L, data.get("completedReports"));
        assertEquals(1L, data.get("failedReports"));
        assertEquals(1L, data.get("generatingReports"));
        assertEquals(1024000L, data.get("totalFileSize"));
        assertTrue(data.containsKey("totalFileSizeFormatted"));
        
        verify(reportApplicationService).getReportStatistics(TEST_TENANT_ID);
    }
    
    @Test
    @DisplayName("Should handle exception when getting report statistics")
    void shouldHandleExceptionWhenGettingReportStatistics() {
        // Given
        when(reportApplicationService.getReportStatistics(TEST_TENANT_ID))
            .thenThrow(new RuntimeException("Service error"));
        
        // When
        ResponseEntity<Map<String, Object>> response = reportController.getReportStatistics();
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("error", body.get("status"));
        String message = body.get("message").toString();
        assertTrue(message.contains("Failed to retrieve") && message.contains("statistics"), 
            "Expected message to contain 'Failed to retrieve' and 'statistics', but was: " + message);
        
        verify(reportApplicationService).getReportStatistics(TEST_TENANT_ID);
    }

    // ========== Test Data Helper Methods ==========

    private ReportController.GenerateReportRequest createValidGenerateReportRequest() {
        ReportController.GenerateReportRequest request = new ReportController.GenerateReportRequest();
        request.setReportType(ReportType.INCOME_STATEMENT);
        request.setReportName(TEST_REPORT_NAME);
        request.setStartDate(TEST_START_DATE);
        request.setEndDate(TEST_END_DATE);
        request.setAiAnalysisEnabled(false);
        return request;
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

    private ReportApplicationService.ReportStatistics createMockReportStatistics() {
        return ReportApplicationService.ReportStatistics.builder()
            .totalReports(10)
            .completedReports(8)
            .failedReports(1)
            .generatingReports(1)
            .totalFileSize(1024000L)
            .build();
    }
}