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
 * Financial Grouping Controller - DDD Compliant
 * 
 * Provides REST endpoints for financial grouping reports following DDD principles
 */
@Slf4j
@RestController
@RequestMapping("/api/financial-grouping")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class FinancialGroupingController {

    private final FinancialGroupingDataService financialGroupingDataService;
    private final FinancialGroupingExportService financialGroupingExportService;

    /**
     * Get financial grouping data in JSON format
     * DDD: Uses TenantId value object and domain services
     */
    @GetMapping("/json")
    public ResponseEntity<FinancialGroupingData> getFinancialGroupingJson(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            log.info("Generating financial grouping for company {} from {} to {}", 
                     companyId, startDate, endDate);
            
            // Validate date range
            if (startDate.isAfter(endDate)) {
                log.error("Invalid date range: start date {} is after end date {}", startDate, endDate);
                return ResponseEntity.badRequest().build();
            }
            
            // DDD: Convert primitive to value object
            TenantId tenantId = TenantId.of(companyId);
            
            // DDD: Use domain service for business logic
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
     * DDD: Uses domain services for export functionality
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
            
            // Get the financial grouping data using DDD service
            FinancialGroupingData data = financialGroupingDataService
                    .getFinancialGroupingDataByTenant(tenantId, startDate, endDate);
            
            // Generate Excel using domain service
            byte[] excelData = financialGroupingExportService.generateExcel(data);
            
            // Prepare response headers
            String filename = String.format("Financial_Grouping_%s_%s_to_%s.xlsx", 
                                           tenantId.getValue(), 
                                           startDate.toString(), 
                                           endDate.toString());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
            headers.setContentLength(excelData.length);
            
            log.info("Financial grouping exported successfully for tenant {}, file size: {} bytes", 
                     tenantId.getValue(), excelData.length);
            
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
     * DDD: Provides domain-specific summary information
     */
    @GetMapping("/summary")
    public ResponseEntity<Object> getFinancialGroupingSummary(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            TenantId tenantId = TenantId.of(companyId);
            
            // Get financial grouping data
            FinancialGroupingData data = financialGroupingDataService
                    .getFinancialGroupingDataByTenant(tenantId, startDate, endDate);
            
            // Create summary object with domain information
            var summary = new Object() {
                public final String periodDescription = data.getPeriodDescription();
                public final String grandTotal = data.getGrandTotal().toString();
                public final int totalTransactionCount = data.getTotalTransactionCount();
                public final int categoryCount = data.getByCategory().size();
                public final int departmentCount = data.getByDepartment().size();
                public final int monthCount = data.getByMonth().size();
                public final int fundCount = data.getByFund().size();
            };
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("Failed to generate financial grouping summary: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get financial grouping by specific criteria
     * DDD: Provides domain-specific filtering capabilities
     */
    @GetMapping("/by-category")
    public ResponseEntity<Object> getFinancialGroupingByCategory(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            TenantId tenantId = TenantId.of(companyId);
            
            FinancialGroupingData data = financialGroupingDataService
                    .getFinancialGroupingDataByTenant(tenantId, startDate, endDate);
            
            return ResponseEntity.ok(data.getByCategory());
            
        } catch (Exception e) {
            log.error("Failed to get financial grouping by category: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get financial grouping by department
     * DDD: Provides department-specific domain information
     */
    @GetMapping("/by-department")
    public ResponseEntity<Object> getFinancialGroupingByDepartment(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            TenantId tenantId = TenantId.of(companyId);
            
            FinancialGroupingData data = financialGroupingDataService
                    .getFinancialGroupingDataByTenant(tenantId, startDate, endDate);
            
            return ResponseEntity.ok(data.getByDepartment());
            
        } catch (Exception e) {
            log.error("Failed to get financial grouping by department: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}