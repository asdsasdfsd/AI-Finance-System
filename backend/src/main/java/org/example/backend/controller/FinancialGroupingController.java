// backend/src/main/java/org/example/backend/controller/FinancialGroupingController.java
package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.application.dto.FinancialGroupingData;
import org.example.backend.application.service.FinancialGroupingDataService;
import org.example.backend.application.service.FinancialGroupingExportService;
import org.example.backend.domain.valueobject.TenantId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Financial Grouping Controller - DDD Implementation
 * 
 * Provides REST endpoints for financial grouping reports
 */
@Slf4j
@RestController
@RequestMapping("/api/financial-grouping")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class FinancialGroupingController {

    private final FinancialGroupingDataService financialGroupingDataService;
    private final FinancialGroupingExportService financialGroupingExportService; // FIXED: Added export service

    /**
     * Get financial grouping data in JSON format
     * DDD: Uses domain services for data generation
     */
    @GetMapping("/json")
    public ResponseEntity<FinancialGroupingData> getFinancialGroupingJson(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            log.info("Generating financial grouping for company {} from {} to {}", 
                     companyId, startDate, endDate);
            
            // Validate inputs
            if (startDate.isAfter(endDate)) {
                log.error("Invalid date range: start date {} is after end date {}", startDate, endDate);
                return ResponseEntity.badRequest().build();
            }
            
            // DDD: Convert to value object
            TenantId tenantId = TenantId.of(companyId);
            
            // Use DDD service to generate financial grouping data
            FinancialGroupingData data = financialGroupingDataService
                    .getFinancialGroupingDataByTenant(tenantId, startDate, endDate);
            
            log.info("Financial grouping generated successfully for tenant {}", tenantId.getValue());
            return ResponseEntity.ok(data);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid input for financial grouping generation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to generate financial grouping for company {}: {}", 
                     companyId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Export financial grouping as Excel file
     * FIXED: Now properly implemented with actual Excel generation
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportFinancialGrouping(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            log.info("Exporting financial grouping for company {} from {} to {}", 
                     companyId, startDate, endDate);
            
            // Validate inputs
            if (startDate.isAfter(endDate)) {
                log.error("Invalid date range for export: start date {} is after end date {}", 
                         startDate, endDate);
                return ResponseEntity.badRequest().build();
            }
            
            // DDD: Convert to value object
            TenantId tenantId = TenantId.of(companyId);
            
            // FIXED: Use the export service to generate actual Excel file
            byte[] excelData = financialGroupingExportService.exportFinancialGrouping(
                    tenantId, startDate, endDate);
            
            // Prepare response headers
            String filename = String.format("Financial_Grouping_%s_%s_to_%s.xlsx", 
                                           companyId, 
                                           startDate.toString(), 
                                           endDate.toString());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
            headers.setContentLength(excelData.length);
            
            log.info("Financial grouping exported successfully for company {}, file size: {} bytes", 
                     companyId, excelData.length);
            
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid input for financial grouping export: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to export financial grouping for company {}: {}", 
                     companyId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check endpoint for testing connectivity
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Financial Grouping service is running");
    }

    /**
     * Get financial grouping summary
     */
    @GetMapping("/summary")
    public ResponseEntity<FinancialGroupingData> getFinancialGroupingSummary(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            log.info("Generating financial grouping summary for company {} from {} to {}", 
                     companyId, startDate, endDate);
            
            TenantId tenantId = TenantId.of(companyId);
            FinancialGroupingData data = financialGroupingDataService
                    .getFinancialGroupingDataByTenant(tenantId, startDate, endDate);
            
            log.info("Financial grouping summary generated successfully for tenant {}", tenantId.getValue());
            return ResponseEntity.ok(data);
            
        } catch (Exception e) {
            log.error("Failed to generate financial grouping summary for company {}: {}", 
                     companyId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}