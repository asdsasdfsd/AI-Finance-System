// backend/src/main/java/org/example/backend/controller/ReportController.java
package org.example.backend.controller;

import org.example.backend.application.dto.GenerateReportCommand;
import org.example.backend.application.dto.ReportDTO;
import org.example.backend.application.dto.ReportListQuery;
import org.example.backend.application.service.ReportApplicationService;
import org.example.backend.domain.valueobject.ReportType;
import org.example.backend.domain.valueobject.ReportStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Report Controller - Compatible with existing codebase and DDD architecture
 * 
 * Focus on core functionality with minimal dependencies
 * Follows proper separation of concerns and HTTP layer responsibilities
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:3000")
public class ReportController {
    
    @Autowired
    private ReportApplicationService reportApplicationService;
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Report service is running",
            "timestamp", LocalDateTime.now()
        ));
    }
    
    /**
     * Generate a new report using GenerateReportRequest
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateReport(@RequestBody GenerateReportRequest request) {
        try {
            // Validate request
            if (request.getReportType() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Report type is required",
                    "timestamp", LocalDateTime.now()
                ));
            }
            
            if (request.getStartDate() == null || request.getEndDate() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Start date and end date are required",
                    "timestamp", LocalDateTime.now()
                ));
            }
            
            // Convert request to command
            GenerateReportCommand command = GenerateReportCommand.builder()
                    .reportType(request.getReportType())
                    .reportName(request.getReportName())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .aiAnalysisEnabled(request.getAiAnalysisEnabled() != null ? request.getAiAnalysisEnabled() : false)
                    .tenantId(1) // Default tenant for development
                    .createdBy(1) // Default user for development
                    .build();
            
            String reportId = reportApplicationService.generateReport(command);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", Map.of("reportId", reportId),
                "message", "Report generation started successfully",
                "timestamp", LocalDateTime.now()
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Failed to generate report. Please try again.",
                "timestamp", LocalDateTime.now()
            ));
        }
    }
    
    /**
     * Generate a new report using GenerateReportCommand directly (for backward compatibility)
     */
    @PostMapping("/generate-direct")
    public ResponseEntity<Map<String, Object>> generateReport(@RequestBody GenerateReportCommand command) {
        try {
            // Set default tenant and user for development
            if (command.getTenantId() == null) {
                command.setTenantId(1);
            }
            if (command.getCreatedBy() == null) {
                command.setCreatedBy(1);
            }
            
            String reportId = reportApplicationService.generateReport(command);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", Map.of("reportId", reportId),
                "message", "Report generation started successfully",
                "timestamp", LocalDateTime.now()
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Failed to generate report: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
    
    /**
     * Get reports list with filtering
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getReports(
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        
        try {
            Integer tenantId = 1; // Default tenant for development
            
            // Build query
            ReportListQuery.ReportListQueryBuilder queryBuilder = ReportListQuery.builder()
                .tenantId(tenantId)
                .pageSize(page != null ? page : 0)
                .pageSize(size != null ? size : 20);
            
            // Parse report type
            if (reportType != null && !reportType.trim().isEmpty()) {
                try {
                    queryBuilder.reportType(ReportType.valueOf(reportType));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Invalid report type. Valid types: " + java.util.Arrays.toString(ReportType.values()),
                        "timestamp", LocalDateTime.now()
                    ));
                }
            }
            
            // Parse status
            if (status != null && !status.trim().isEmpty()) {
                try {
                    queryBuilder.status(ReportStatus.valueOf(status));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Invalid status. Valid statuses: " + java.util.Arrays.toString(ReportStatus.values()),
                        "timestamp", LocalDateTime.now()
                    ));
                }
            }
            
            // Parse dates
            if (startDate != null && !startDate.trim().isEmpty()) {
                try {
                    queryBuilder.startDate(LocalDate.parse(startDate));
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Invalid start date format. Use YYYY-MM-DD",
                        "timestamp", LocalDateTime.now()
                    ));
                }
            }
            
            if (endDate != null && !endDate.trim().isEmpty()) {
                try {
                    queryBuilder.endDate(LocalDate.parse(endDate));
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Invalid end date format. Use YYYY-MM-DD",
                        "timestamp", LocalDateTime.now()
                    ));
                }
            }
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                queryBuilder.searchTerm(searchTerm.trim());
            }
            
            ReportListQuery query = queryBuilder.build();
            List<ReportDTO> reports = reportApplicationService.getReports(query);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", reports,
                "message", "Reports retrieved successfully",
                "timestamp", LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Failed to retrieve reports: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
    
    /**
     * Get report details by ID
     */
    @GetMapping("/{reportId}")
    public ResponseEntity<Map<String, Object>> getReport(@PathVariable Integer reportId) {
        try {
            Integer tenantId = 1; // Default tenant for development
            
            Optional<ReportDTO> report = reportApplicationService.getReport(reportId, tenantId);
            
            if (report.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", report.get(),
                    "message", "Report details retrieved successfully",
                    "timestamp", LocalDateTime.now()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", "error",
                    "message", "Report not found",
                    "timestamp", LocalDateTime.now()
                ));
            }
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Failed to retrieve report details: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
    
    /**
     * Download report file
     */
    @GetMapping("/{reportId}/download")
    public ResponseEntity<Resource> downloadReport(@PathVariable Integer reportId) {
        try {
            Integer tenantId = 1; // Default tenant for development
            
            Optional<ReportDTO> reportOpt = reportApplicationService.getReport(reportId, tenantId);
            
            if (reportOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            ReportDTO report = reportOpt.get();
            
            if (report.getFilePath() == null || report.getFilePath().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            File file = new File(report.getFilePath());
            if (!file.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            Resource resource = new FileSystemResource(file);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + report.getReportName() + "." + report.getFileFormat() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get report statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getReportStatistics() {
        try {
            Integer tenantId = 1; // Default tenant for development
            
            ReportApplicationService.ReportStatistics stats = reportApplicationService.getReportStatistics(tenantId);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", stats,
                "message", "Report statistics retrieved successfully",
                "timestamp", LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Failed to retrieve report statistics: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
    
    /**
     * GenerateReportRequest - Inner class for request binding
     * 
     * This class represents the HTTP request body structure for report generation
     */
    public static class GenerateReportRequest {
        private ReportType reportType;
        private String reportName;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean aiAnalysisEnabled;
        
        // Default constructor
        public GenerateReportRequest() {}
        
        // Getters and setters
        public ReportType getReportType() {
            return reportType;
        }
        
        public void setReportType(ReportType reportType) {
            this.reportType = reportType;
        }
        
        public String getReportName() {
            return reportName;
        }
        
        public void setReportName(String reportName) {
            this.reportName = reportName;
        }
        
        public LocalDate getStartDate() {
            return startDate;
        }
        
        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }
        
        public LocalDate getEndDate() {
            return endDate;
        }
        
        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }
        
        public Boolean getAiAnalysisEnabled() {
            return aiAnalysisEnabled;
        }
        
        public void setAiAnalysisEnabled(Boolean aiAnalysisEnabled) {
            this.aiAnalysisEnabled = aiAnalysisEnabled;
        }
        
        @Override
        public String toString() {
            return "GenerateReportRequest{" +
                   "reportType=" + reportType +
                   ", reportName='" + reportName + '\'' +
                   ", startDate=" + startDate +
                   ", endDate=" + endDate +
                   ", aiAnalysisEnabled=" + aiAnalysisEnabled +
                   '}';
        }
    }
}