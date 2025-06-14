// backend/src/main/java/org/example/backend/controller/BalanceSheetController.java
package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.application.dto.BalanceSheetDetailedResponse;
import org.example.backend.application.service.BalanceSheetDataService;
import org.example.backend.application.service.BalanceSheetExportService;
import org.example.backend.domain.valueobject.TenantId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Balance Sheet Controller - DDD Compliant
 * 
 * Provides REST endpoints for balance sheet reports following DDD principles
 */
@Slf4j
@RestController
@RequestMapping("/api/balance-sheet")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class BalanceSheetController {

    private final BalanceSheetDataService balanceSheetDataService;
    private final BalanceSheetExportService balanceSheetExportService;

    /**
     * Get balance sheet data in JSON format
     * DDD: Uses TenantId value object for proper domain modeling
     */
    @GetMapping("/json")
    public ResponseEntity<BalanceSheetDetailedResponse> getBalanceSheetJson(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        
        try {
            log.info("Generating balance sheet for company {} as of {}", companyId, asOfDate);
            
            // DDD: Convert primitive to value object
            TenantId tenantId = TenantId.of(companyId);
            
            // DDD: Use domain service for business logic
            BalanceSheetDetailedResponse data = balanceSheetDataService.generateBalanceSheetByTenant(tenantId, asOfDate);
            
            log.info("Balance sheet generated successfully for tenant {}", tenantId.getValue());
            return ResponseEntity.ok(data);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid input for balance sheet generation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to generate balance sheet for company {}: {}", companyId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Export balance sheet as Excel file
     * DDD: Follows domain-driven approach for export functionality
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportBalanceSheet(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {

        try {
            log.info("Exporting balance sheet for company {} as of {}", companyId, asOfDate);
            
            // DDD: Convert to value object
            TenantId tenantId = TenantId.of(companyId);
            
            // Get the balance sheet data using DDD service
            BalanceSheetDetailedResponse data = balanceSheetDataService.generateBalanceSheetByTenant(tenantId, asOfDate);
            
            // Generate Excel using domain service
            byte[] excelData = balanceSheetExportService.generateExcel(data);
            
            // Prepare response headers
            String filename = String.format("BalanceSheet_%s_%s.xlsx", 
                                           tenantId.getValue(), 
                                           asOfDate.toString());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
            headers.setContentLength(excelData.length);
            
            log.info("Balance sheet exported successfully for tenant {}, file size: {} bytes", 
                     tenantId.getValue(), excelData.length);
            
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid input for balance sheet export: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to export balance sheet for company {}: {}", companyId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check endpoint for testing connectivity
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Balance Sheet service is running");
    }

    /**
     * Get balance sheet summary statistics
     * DDD: Provides domain-specific summary information
     */
    @GetMapping("/summary")
    public ResponseEntity<Object> getBalanceSheetSummary(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        
        try {
            TenantId tenantId = TenantId.of(companyId);
            
            // Get basic balance sheet data
            BalanceSheetDetailedResponse data = balanceSheetDataService.generateBalanceSheetByTenant(tenantId, asOfDate);
            
            // Create summary object
            var summary = new Object() {
                public final String asOfDate = data.getAsOfDate().toString();
                public final String totalAssets = data.getTotalAssets().toString();
                public final String totalLiabilities = data.getTotalLiabilities().toString();
                public final String totalEquity = data.getTotalEquity().toString();
                public final boolean isBalanced = data.isBalanced();
                public final int assetCategories = data.getAssets().size();
                public final int liabilityCategories = data.getLiabilities().size();
                public final int equityCategories = data.getEquity().size();
            };
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("Failed to generate balance sheet summary: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}