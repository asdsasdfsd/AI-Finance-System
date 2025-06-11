// backend/src/main/java/org/example/backend/controller/ReportController.java
package org.example.backend.controller;

import org.example.backend.application.dto.GenerateReportCommand;
import org.example.backend.application.dto.ReportDTO;
import org.example.backend.application.dto.ReportListQuery;
import org.example.backend.application.service.ReportApplicationService;
import org.example.backend.domain.valueobject.ReportType;
import org.example.backend.domain.valueobject.ReportStatus;
import org.example.backend.infrastructure.report.ReportFileManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Report Controller
 * 
 * REST API endpoints for report management
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {
    
    @Autowired
    private ReportApplicationService reportApplicationService;
    
    @Autowired
    private ReportFileManager fileManager;
    
    /**
     * Generate a new report
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateReport(@RequestBody GenerateReportRequest request) {
        try {
            // Extract tenant ID from security context (simplified)
            Integer tenantId = getCurrentTenantId();
            Integer userId = getCurrentUserId();
            
            GenerateReportCommand command = GenerateReportCommand.builder()
                .tenantId(tenantId)
                .reportType(request.getReportType())
                .reportName(request.getReportName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdBy(userId)
                .aiAnalysisEnabled(request.getAiAnalysisEnabled())
                .build();
            
            String reportId = reportApplicationService.generateReport(command);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Report generation started",
                "reportId", reportId
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Failed to start report generation: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get report details
     */
    @GetMapping("/{reportId}")
    public ResponseEntity<Map<String, Object>> getReport(@PathVariable Integer reportId) {
        try {
            Integer tenantId = getCurrentTenantId();
            
            return reportApplicationService.getReport(reportId, tenantId)
                .map(report -> ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", report
                )))
                .orElse(ResponseEntity.notFound().build());
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Failed to retrieve report: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get reports list with filtering
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getReports(
            @RequestParam(required = false) ReportType reportType,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        
        try {
            Integer tenantId = getCurrentTenantId();
            
            ReportListQuery query = ReportListQuery.builder()
                .tenantId(tenantId)
                .reportType(reportType)
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .searchTerm(searchTerm)
                .pageNumber(page)
                .pageSize(size)
                .build();
            
            List<ReportDTO> reports = reportApplicationService.getReports(query);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", reports,
                "total", reports.size()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Failed to retrieve reports: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Download report file
     */
    @GetMapping("/{reportId}/download")
    public ResponseEntity<Resource> downloadReport(@PathVariable Integer reportId) {
        try {
            Integer tenantId = getCurrentTenantId();
            
            return reportApplicationService.getReport(reportId, tenantId)
                .filter(ReportDTO::isDownloadable)
                .map(report -> {
                    Resource resource = new FileSystemResource(report.getFilePath());
                    
                    if (!resource.exists()) {
                        return ResponseEntity.notFound().<Resource>build();
                    }
                    
                    String fileName = fileManager.getFileName(report.getFilePath());
                    
                    return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                        .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                        .body(resource);
                })
                .orElse(ResponseEntity.notFound().build());
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get recent reports
     */
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentReports(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        
        try {
            Integer tenantId = getCurrentTenantId();
            
            List<ReportDTO> reports = reportApplicationService.getRecentReports(tenantId, limit);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", reports
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Failed to retrieve recent reports: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get reports by type
     */
    @GetMapping("/by-type/{reportType}")
    public ResponseEntity<Map<String, Object>> getReportsByType(@PathVariable ReportType reportType) {
        try {
            Integer tenantId = getCurrentTenantId();
            
            List<ReportDTO> reports = reportApplicationService.getReportsByType(tenantId, reportType);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", reports
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Failed to retrieve reports by type: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Archive a report
     */
    @PostMapping("/{reportId}/archive")
    public ResponseEntity<Map<String, Object>> archiveReport(@PathVariable Integer reportId) {
        try {
            Integer tenantId = getCurrentTenantId();
            
            reportApplicationService.archiveReport(reportId, tenantId);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Report archived successfully"
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Failed to archive report: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Delete a report
     */
    @DeleteMapping("/{reportId}")
    public ResponseEntity<Map<String, Object>> deleteReport(@PathVariable Integer reportId) {
        try {
            Integer tenantId = getCurrentTenantId();
            
            reportApplicationService.deleteReport(reportId, tenantId);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Report deleted successfully"
            ));
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Failed to delete report: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get report statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getReportStatistics() {
        try {
            Integer tenantId = getCurrentTenantId();
            
            ReportApplicationService.ReportStatistics stats = 
                reportApplicationService.getReportStatistics(tenantId);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", Map.of(
                    "totalReports", stats.getTotalReports(),
                    "completedReports", stats.getCompletedReports(),
                    "failedReports", stats.getFailedReports(),
                    "generatingReports", stats.getGeneratingReports(),
                    "totalFileSize", stats.getTotalFileSize(),
                    "totalFileSizeFormatted", stats.getTotalFileSizeFormatted(),
                    "successRate", stats.getSuccessRate()
                )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Failed to retrieve statistics: " + e.getMessage()
            ));
        }
    }
    
    // ========== Helper Methods ==========
    
    /**
     * Get current tenant ID from security context
     * This is simplified - in real implementation, extract from JWT or session
     */
    private Integer getCurrentTenantId() {
        // TODO: Extract from security context
        return 1; // Placeholder
    }
    
    /**
     * Get current user ID from security context
     */
    private Integer getCurrentUserId() {
        // TODO: Extract from security context  
        return 1; // Placeholder
    }
    
    // ========== Request DTOs ==========
    
    public static class GenerateReportRequest {
        private ReportType reportType;
        private String reportName;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean aiAnalysisEnabled;
        
        // Getters and setters
        public ReportType getReportType() { return reportType; }
        public void setReportType(ReportType reportType) { this.reportType = reportType; }
        
        public String getReportName() { return reportName; }
        public void setReportName(String reportName) { this.reportName = reportName; }
        
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        
        public Boolean getAiAnalysisEnabled() { return aiAnalysisEnabled; }
        public void setAiAnalysisEnabled(Boolean aiAnalysisEnabled) { this.aiAnalysisEnabled = aiAnalysisEnabled; }
    }
}