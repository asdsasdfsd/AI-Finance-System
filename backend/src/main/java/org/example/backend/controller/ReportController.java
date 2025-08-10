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
 * Simple Report Controller - Compatible with existing codebase
 * 
 * Focus on core functionality with minimal dependencies
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
     * Generate a new report
     */
    @PostMapping("/generate")
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
                .pageNumber(page)
                .pageSize(size);
            
            // Parse parameters safely
            if (reportType != null && !reportType.trim().isEmpty()) {
                try {
                    queryBuilder.reportType(ReportType.valueOf(reportType.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Invalid report type: " + reportType,
                        "timestamp", LocalDateTime.now()
                    ));
                }
            }
            
            if (status != null && !status.trim().isEmpty()) {
                try {
                    queryBuilder.status(ReportStatus.valueOf(status.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Invalid status: " + status,
                        "timestamp", LocalDateTime.now()
                    ));
                }
            }
            
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
                "message", "Failed to retrieve report: " + e.getMessage(),
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
            if (!reportOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            ReportDTO report = reportOpt.get();
            
            if (report.getStatus() != ReportStatus.COMPLETED || report.getFilePath() == null) {
                return ResponseEntity.badRequest().build();
            }
            
            File file = new File(report.getFilePath());
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(file);
            String filename = generateFileName(report);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + filename + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Archive a report
     */
    @PostMapping("/{reportId}/archive")
    public ResponseEntity<Map<String, Object>> archiveReport(@PathVariable Integer reportId) {
        try {
            Integer tenantId = 1; // Default tenant for development
            reportApplicationService.archiveReport(reportId, tenantId);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Report archived successfully",
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
                "message", "Failed to archive report: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
    
    /**
     * Delete a report
     */
    @DeleteMapping("/{reportId}")
    public ResponseEntity<Map<String, Object>> deleteReport(@PathVariable Integer reportId) {
        try {
            Integer tenantId = 1; // Default tenant for development
            reportApplicationService.deleteReport(reportId, tenantId);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Report deleted successfully",
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
                "message", "Failed to delete report: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
    
    /**
     * Get report statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getReportStatistics() {
        try {
            Integer tenantId = 1; // Default tenant for development
            
            ReportListQuery query = ReportListQuery.builder()
                .tenantId(tenantId)
                .build();
            
            List<ReportDTO> reports = reportApplicationService.getReports(query);
            
            // Calculate statistics
            long totalReports = reports.size();
            long completedReports = reports.stream().filter(r -> r.getStatus() == ReportStatus.COMPLETED).count();
            long generatingReports = reports.stream().filter(r -> r.getStatus() == ReportStatus.GENERATING).count();
            long failedReports = reports.stream().filter(r -> r.getStatus() == ReportStatus.FAILED).count();
            
            Map<String, Object> statistics = Map.of(
                "totalReports", totalReports,
                "completedReports", completedReports,
                "generatingReports", generatingReports,
                "failedReports", failedReports
            );
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", statistics,
                "message", "Statistics retrieved successfully",
                "timestamp", LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Failed to retrieve statistics: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
    
    /**
     * Generate download filename
     */
    private String generateFileName(ReportDTO report) {
        String baseName = report.getReportType().name().toLowerCase();
        String dateRange = "";
        
        if (report.getStartDate() != null && report.getEndDate() != null) {
            dateRange = "_" + report.getStartDate() + "_to_" + report.getEndDate();
        }
        
        return baseName + dateRange + ".xlsx";
    }

    
}