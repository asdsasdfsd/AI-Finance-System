// backend/src/main/java/org/example/backend/controller/IncomeExpenseController.java
package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.application.dto.IncomeExpenseReportData;
import org.example.backend.application.service.IncomeExpenseDataService;
import org.example.backend.application.service.IncomeExpenseExportService;
import org.example.backend.domain.valueobject.TenantId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Income Expense Controller - DDD Implementation
 * 
 * Provides REST endpoints for income vs expense reports
 * FIXED: Complete implementation with proper error handling
 */
@Slf4j
@RestController
@RequestMapping("/api/income-expense")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class IncomeExpenseController {

    private final IncomeExpenseDataService incomeExpenseDataService;
    private final IncomeExpenseExportService incomeExpenseExportService;

    /**
     * Get income expense data in JSON format
     * DDD: Uses domain services for data generation
     */
    @GetMapping("/json")
    public ResponseEntity<IncomeExpenseReportData> getIncomeExpenseJson(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        
        try {
            log.info("Generating income expense report for company {} as of {}", companyId, asOfDate);
            
            // Validate inputs
            if (companyId == null || companyId <= 0) {
                log.error("Invalid company ID: {}", companyId);
                return ResponseEntity.badRequest().build();
            }
            if (asOfDate == null) {
                log.error("As-of date cannot be null");
                return ResponseEntity.badRequest().build();
            }
            
            // DDD: Convert to value object
            TenantId tenantId = TenantId.of(companyId);
            
            // Use DDD service to generate income expense data
            IncomeExpenseReportData data = incomeExpenseDataService
                    .generateIncomeExpenseReportByTenant(tenantId, asOfDate);
            
            log.info("Income expense report generated successfully for tenant {}", tenantId.getValue());
            return ResponseEntity.ok(data);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid input for income expense generation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to generate income expense report for company {}: {}", 
                     companyId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Export income expense as Excel file
     * FIXED: Now properly implemented with actual Excel generation
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportIncomeExpense(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {

        try {
            log.info("Exporting income expense for company {} as of {}", companyId, asOfDate);
            
            // Validate inputs
            if (companyId == null || companyId <= 0) {
                log.error("Invalid company ID for export: {}", companyId);
                return ResponseEntity.badRequest().build();
            }
            if (asOfDate == null) {
                log.error("As-of date cannot be null for export");
                return ResponseEntity.badRequest().build();
            }
            
            // DDD: Convert to value object
            TenantId tenantId = TenantId.of(companyId);
            
            // FIXED: Use the export service to generate actual Excel file
            byte[] excelData = incomeExpenseExportService.exportIncomeExpense(tenantId, asOfDate);
            
            // Validate generated data
            if (excelData == null || excelData.length == 0) {
                log.error("Generated Excel data is empty for company {}", companyId);
                return ResponseEntity.internalServerError()
                        .body("Failed to generate Excel file".getBytes());
            }
            
            // Prepare response headers
            String filename = String.format("Income_Expense_Report_%s_%s.xlsx", 
                                           companyId, 
                                           asOfDate.toString());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
            headers.setContentLength(excelData.length);
            
            log.info("Income expense exported successfully for company {}, file size: {} bytes", 
                     companyId, excelData.length);
            
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid input for income expense export: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to export income expense for company {}: {}", 
                     companyId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check endpoint for this controller
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Income Expense Controller is running");
    }
}