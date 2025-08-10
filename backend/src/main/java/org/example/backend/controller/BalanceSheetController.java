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
 * Balance Sheet Controller - DDD Compliant (Fixed Version)
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
     * Export balance sheet as Excel file - Fixed Method
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
            
            // Generate Excel using FIXED domain service method
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
     * Get balance sheet summary (simplified version for dashboard)
     * DDD: Provides simplified view using domain aggregates
     */
    @GetMapping("/summary")
    public ResponseEntity<BalanceSheetSummary> getBalanceSheetSummary(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        
        try {
            log.info("Generating balance sheet summary for company {} as of {}", companyId, asOfDate);
            
            // DDD: Convert primitive to value object
            TenantId tenantId = TenantId.of(companyId);
            
            // Get detailed data and extract summary
            BalanceSheetDetailedResponse data = balanceSheetDataService.generateBalanceSheetByTenant(tenantId, asOfDate);
            
            // Create summary object - removed unused fields to fix warnings
            BalanceSheetSummary summary = new BalanceSheetSummary(
                data.getAsOfDate(),
                data.getTotalAssets(),
                data.getTotalLiabilities(),
                data.getTotalEquity(),
                data.isBalanced()
            );
            
            log.info("Balance sheet summary generated successfully for tenant {}", tenantId.getValue());
            return ResponseEntity.ok(summary);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid input for balance sheet summary: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to generate balance sheet summary for company {}: {}", companyId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check endpoint for balance sheet service
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Balance Sheet Service is operational");
    }

    /**
     * FIXED: Generate balance sheet data for frontend preview
     */
    @GetMapping("/generate")
    public ResponseEntity<?> generateBalanceSheetPreview(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        
        try {
            log.info("Generating balance sheet preview for company {} as of {}", companyId, asOfDate);
            
            TenantId tenantId = TenantId.of(companyId);
            BalanceSheetDetailedResponse data = balanceSheetDataService.generateBalanceSheetByTenant(tenantId, asOfDate);
            
            // Convert to frontend-friendly format
            java.util.List<java.util.Map<String, Object>> tableData = new java.util.ArrayList<>();
            
            // FIXED: Add assets - correct iteration over Map<String, List<AccountBalanceDTO>>
            if (data.getAssets() != null) {
                for (java.util.Map.Entry<String, java.util.List<org.example.backend.application.dto.AccountBalanceDTO>> entry : data.getAssets().entrySet()) {
                    for (org.example.backend.application.dto.AccountBalanceDTO asset : entry.getValue()) {
                        java.util.Map<String, Object> row = new java.util.HashMap<>();
                        row.put("key", "asset_" + asset.getAccountName().hashCode());
                        row.put("Account", asset.getAccountName());
                        row.put("Amount", asset.getCurrentMonth());
                        row.put("Type", "Asset");
                        tableData.add(row);
                    }
                }
            }
            
            // FIXED: Add liabilities - correct iteration
            if (data.getLiabilities() != null) {
                for (java.util.Map.Entry<String, java.util.List<org.example.backend.application.dto.AccountBalanceDTO>> entry : data.getLiabilities().entrySet()) {
                    for (org.example.backend.application.dto.AccountBalanceDTO liability : entry.getValue()) {
                        java.util.Map<String, Object> row = new java.util.HashMap<>();
                        row.put("key", "liability_" + liability.getAccountName().hashCode());
                        row.put("Account", liability.getAccountName());
                        row.put("Amount", liability.getCurrentMonth());
                        row.put("Type", "Liability");
                        tableData.add(row);
                    }
                }
            }
            
            // FIXED: Add equity - correct iteration
            if (data.getEquity() != null) {
                for (java.util.Map.Entry<String, java.util.List<org.example.backend.application.dto.AccountBalanceDTO>> entry : data.getEquity().entrySet()) {
                    for (org.example.backend.application.dto.AccountBalanceDTO equity : entry.getValue()) {
                        java.util.Map<String, Object> row = new java.util.HashMap<>();
                        row.put("key", "equity_" + equity.getAccountName().hashCode());
                        row.put("Account", equity.getAccountName());
                        row.put("Amount", equity.getCurrentMonth());
                        row.put("Type", "Equity");
                        tableData.add(row);
                    }
                }
            }
            
            log.info("Balance sheet preview generated successfully with {} rows", tableData.size());
            return ResponseEntity.ok(tableData);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid input for balance sheet preview: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                java.util.Map.of("error", "Invalid input: " + e.getMessage())
            );
        } catch (Exception e) {
            log.error("Failed to generate balance sheet preview for company {}: {}", companyId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                java.util.Map.of("error", "Failed to generate balance sheet preview: " + e.getMessage())
            );
        }
    }

    /**
     * Inner class for balance sheet summary - Fixed to remove unused field warnings
     */
    public static class BalanceSheetSummary {
        private final LocalDate asOfDate;
        private final java.math.BigDecimal totalAssets;
        private final java.math.BigDecimal totalLiabilities;
        private final java.math.BigDecimal totalEquity;
        private final boolean isBalanced;
        
        public BalanceSheetSummary(LocalDate asOfDate, java.math.BigDecimal totalAssets, 
                                 java.math.BigDecimal totalLiabilities, java.math.BigDecimal totalEquity, 
                                 boolean isBalanced) {
            this.asOfDate = asOfDate;
            this.totalAssets = totalAssets;
            this.totalLiabilities = totalLiabilities;
            this.totalEquity = totalEquity;
            this.isBalanced = isBalanced;
        }
        
        // Getters
        public LocalDate getAsOfDate() { return asOfDate; }
        public java.math.BigDecimal getTotalAssets() { return totalAssets; }
        public java.math.BigDecimal getTotalLiabilities() { return totalLiabilities; }
        public java.math.BigDecimal getTotalEquity() { return totalEquity; }
        public boolean isBalanced() { return isBalanced; }
        
        // Additional calculated fields
        public java.math.BigDecimal getWorkingCapital() {
            return totalAssets.subtract(totalLiabilities);
        }
        
        public double getDebtToEquityRatio() {
            return totalEquity.compareTo(java.math.BigDecimal.ZERO) != 0 ?
                totalLiabilities.divide(totalEquity, 4, java.math.RoundingMode.HALF_UP).doubleValue() : 0.0;
        }
    }
}