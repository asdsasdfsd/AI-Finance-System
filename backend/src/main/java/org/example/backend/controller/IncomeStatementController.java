// backend/src/main/java/org/example/backend/controller/IncomeStatementController.java
package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.application.dto.IncomeStatementData;
import org.example.backend.application.service.IncomeStatementDataService;
import org.example.backend.application.service.IncomeStatementExportService;
import org.example.backend.domain.valueobject.TenantId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Income Statement Controller - Updated with Export Service
 * 
 * Provides REST endpoints for income statement reports
 */
@Slf4j
@RestController
@RequestMapping("/api/income-statement")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class IncomeStatementController {

    private final IncomeStatementDataService incomeStatementDataService;
    private final IncomeStatementExportService incomeStatementExportService;

    /**
     * Get income statement data in JSON format
     */
    @GetMapping("/json")
    public ResponseEntity<IncomeStatementData> getIncomeStatementJson(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            log.info("Generating income statement for company {} from {} to {}", companyId, startDate, endDate);
            
            // Validate inputs
            if (companyId == null || companyId <= 0) {
                log.error("Invalid company ID: {}", companyId);
                return ResponseEntity.badRequest().build();
            }
            if (startDate == null || endDate == null) {
                log.error("Start date and end date cannot be null");
                return ResponseEntity.badRequest().build();
            }
            if (startDate.isAfter(endDate)) {
                log.error("Start date cannot be after end date");
                return ResponseEntity.badRequest().build();
            }
            
            TenantId tenantId = TenantId.of(companyId);
            IncomeStatementData data = incomeStatementDataService.getIncomeStatementData(tenantId, startDate, endDate);
            
            log.info("Income statement generated successfully for company {}", companyId);
            return ResponseEntity.ok(data);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid input for income statement generation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to generate income statement for company {}: {}", 
                     companyId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Export income statement as Excel file
     * FIXED: Now properly implemented with actual Excel generation
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportIncomeStatement(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            log.info("Exporting income statement for company {} from {} to {}", companyId, startDate, endDate);
            
            // Validate inputs
            if (companyId == null || companyId <= 0) {
                log.error("Invalid company ID for export: {}", companyId);
                return ResponseEntity.badRequest().build();
            }
            if (startDate == null || endDate == null) {
                log.error("Start date and end date cannot be null for export");
                return ResponseEntity.badRequest().build();
            }
            if (startDate.isAfter(endDate)) {
                log.error("Start date cannot be after end date for export");
                return ResponseEntity.badRequest().build();
            }
            
            // DDD: Convert to value object
            TenantId tenantId = TenantId.of(companyId);
            
            // FIXED: Use the export service to generate actual Excel file
            byte[] excelData = incomeStatementExportService.exportIncomeStatement(tenantId, startDate, endDate);
            
            // Validate generated data
            if (excelData == null || excelData.length == 0) {
                log.error("Generated Excel data is empty for company {}", companyId);
                return ResponseEntity.internalServerError()
                        .body("Failed to generate Excel file".getBytes());
            }
            
            // Prepare response headers
            String filename = String.format("Income_Statement_%s_%s_to_%s.xlsx", 
                                           companyId, 
                                           startDate.toString(), 
                                           endDate.toString());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
            headers.setContentLength(excelData.length);
            
            log.info("Income statement exported successfully for company {}, file size: {} bytes", 
                     companyId, excelData.length);
            
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid input for income statement export: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to export income statement for company {}: {}", 
                     companyId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check endpoint for this controller
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Income Statement Controller is running");
    }
}