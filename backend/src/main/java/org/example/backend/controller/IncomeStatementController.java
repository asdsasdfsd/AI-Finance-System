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
 * Income Statement Controller - DDD Implementation
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
            log.info("Generating income statement for company {} from {} to {}", 
                     companyId, startDate, endDate);
            
            if (startDate.isAfter(endDate)) {
                log.error("Invalid date range: start date {} is after end date {}", startDate, endDate);
                return ResponseEntity.badRequest().build();
            }
            
            TenantId tenantId = TenantId.of(companyId);
            IncomeStatementData data = incomeStatementDataService
                    .getIncomeStatementDataByTenant(tenantId, startDate, endDate);
            
            log.info("Income statement generated successfully for tenant {}", tenantId.getValue());
            return ResponseEntity.ok(data);
            
        } catch (Exception e) {
            log.error("Failed to generate income statement: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * FIXED: Generate income statement data for frontend preview
     */
    @GetMapping("/generate")
    public ResponseEntity<?> generateIncomeStatementPreview(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            log.info("Generating income statement preview for company {} from {} to {}", 
                     companyId, startDate, endDate);
            
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", "Start date cannot be after end date")
                );
            }
            
            TenantId tenantId = TenantId.of(companyId);
            IncomeStatementData data = incomeStatementDataService
                    .getIncomeStatementDataByTenant(tenantId, startDate, endDate);
            
            // Convert to frontend-friendly format
            java.util.List<java.util.Map<String, Object>> tableData = new java.util.ArrayList<>();
            
            // FIXED: Add revenues - using correct field name getRevenues()
            if (data.getRevenues() != null) {
                for (IncomeStatementData.RevenueItem revenue : data.getRevenues()) {
                    java.util.Map<String, Object> row = new java.util.HashMap<>();
                    row.put("key", "revenue_" + revenue.getName().hashCode());
                    row.put("Account", revenue.getName());
                    row.put("Amount", revenue.getAmount());
                    row.put("Category", "Revenue");
                    tableData.add(row);
                }
            }
            
            // FIXED: Add expenses - using Map structure from revenueByCategory pattern
            if (data.getExpensesByCategory() != null) {
                for (java.util.Map.Entry<String, java.math.BigDecimal> entry : data.getExpensesByCategory().entrySet()) {
                    java.util.Map<String, Object> row = new java.util.HashMap<>();
                    row.put("key", "expense_" + entry.getKey().hashCode());
                    row.put("Account", entry.getKey());
                    row.put("Amount", entry.getValue());
                    row.put("Category", "Expense");
                    tableData.add(row);
                }
            }
            
            // Add net income
            if (data.getNetIncome() != null) {
                java.util.Map<String, Object> row = new java.util.HashMap<>();
                row.put("key", "net_income");
                row.put("Account", "Net Income");
                row.put("Amount", data.getNetIncome());
                row.put("Category", "Net Income");
                tableData.add(row);
            }
            
            log.info("Income statement preview generated successfully with {} rows", tableData.size());
            return ResponseEntity.ok(tableData);
            
        } catch (Exception e) {
            log.error("Failed to generate income statement preview: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                java.util.Map.of("error", "Failed to generate preview: " + e.getMessage())
            );
        }
    }
    /**
     * Export income statement as Excel file
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportIncomeStatementExcel(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            log.info("Exporting income statement for company {} from {} to {}", 
                     companyId, startDate, endDate);
            
            TenantId tenantId = TenantId.of(companyId);
            
            byte[] excelData = incomeStatementExportService.exportIncomeStatement(tenantId, startDate, endDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                String.format("Income_Statement_%d_%s_to_%s.xlsx", companyId, startDate, endDate));
            
            log.info("Income statement exported successfully for tenant {}", tenantId.getValue());
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
            
        } catch (Exception e) {
            log.error("Failed to export income statement: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Income Statement Service is operational");
    }
}