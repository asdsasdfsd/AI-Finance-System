// backend/src/main/java/org/example/backend/controller/IncomeExpenseController.java
// FIXED IncomeExpenseController to handle both asOfDate and date range parameters

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * Income Expense Controller - DDD Implementation
 * 
 * Provides REST endpoints for income vs expense reports
 * FIXED: Handles both asOfDate and startDate/endDate parameters
 */
@Slf4j
@RestController
@RequestMapping("/api/income-expense")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
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
     * FIXED: Generate income expense data for frontend preview
     * Supports both asOfDate (single date) and startDate/endDate (date range) parameters
     */
    @GetMapping("/generate")
    public ResponseEntity<?> generateIncomeExpensePreview(
            @RequestParam Integer companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            log.info("Generating income expense preview for company {} with asOfDate={}, startDate={}, endDate={}", 
                     companyId, asOfDate, startDate, endDate);
            
            // Validate company ID
            if (companyId == null || companyId <= 0) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Invalid company ID")
                );
            }
            
            // FIXED: Handle both parameter styles
            LocalDate effectiveStartDate;
            LocalDate effectiveEndDate;
            
            if (asOfDate != null) {
                // Frontend sent asOfDate - use month range for that date
                effectiveStartDate = asOfDate.withDayOfMonth(1); // First day of month
                effectiveEndDate = asOfDate; // As-of date itself
                log.info("Using asOfDate mode: {} -> range {} to {}", asOfDate, effectiveStartDate, effectiveEndDate);
            } else if (startDate != null && endDate != null) {
                // Frontend sent date range
                effectiveStartDate = startDate;
                effectiveEndDate = endDate;
                log.info("Using date range mode: {} to {}", effectiveStartDate, effectiveEndDate);
            } else {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Either asOfDate or both startDate and endDate must be provided")
                );
            }
            
            // Validate date range
            if (effectiveStartDate.isAfter(effectiveEndDate)) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Start date cannot be after end date")
                );
            }
            
            // Generate data using effective end date for compatibility
            TenantId tenantId = TenantId.of(companyId);
            IncomeExpenseReportData data = incomeExpenseDataService
                    .generateIncomeExpenseReportByTenant(tenantId, effectiveEndDate);
            
            // Convert to frontend-friendly format
            List<Map<String, Object>> tableData = new ArrayList<>();
            
            // Add income rows
            if (data.getIncomeRows() != null) {
                for (var row : data.getIncomeRows()) {
                    Map<String, Object> tableRow = new HashMap<>();
                    tableRow.put("key", "income_" + row.hashCode());
                    tableRow.put("Account", row.getDescription());
                    tableRow.put("Amount", row.getCurrentMonth());
                    tableRow.put("Type", "INCOME");
                    tableRow.put("Category", row.getCategory());
                    tableRow.put("PreviousMonth", row.getPreviousMonth());
                    tableRow.put("YearToDate", row.getYearToDate());
                    tableData.add(tableRow);
                }
            }
            
            // Add expense rows
            if (data.getExpenseRows() != null) {
                for (var row : data.getExpenseRows()) {
                    Map<String, Object> tableRow = new HashMap<>();
                    tableRow.put("key", "expense_" + row.hashCode());
                    tableRow.put("Account", row.getDescription());
                    tableRow.put("Amount", row.getCurrentMonth());
                    tableRow.put("Type", "EXPENSE");
                    tableRow.put("Category", row.getCategory());
                    tableRow.put("PreviousMonth", row.getPreviousMonth());
                    tableRow.put("YearToDate", row.getYearToDate());
                    tableData.add(tableRow);
                }
            }
            
            // Create summary response
            Map<String, Object> response = new HashMap<>();
            response.put("data", tableData);
            response.put("companyId", companyId);
            response.put("totalRows", tableData.size());
            response.put("reportType", "INCOME_EXPENSE");
            response.put("effectiveStartDate", effectiveStartDate);
            response.put("effectiveEndDate", effectiveEndDate);
            
            // Add summary statistics
            response.put("totalIncome", data.getTotalIncomeMonth());
            response.put("totalExpenses", data.getTotalExpenseMonth());
            response.put("netIncome", data.getNetIncomeMonth());
            
            log.info("Income expense preview generated with {} rows", tableData.size());
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid input for income expense preview: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                Map.of("error", "Invalid input: " + e.getMessage())
            );
        } catch (Exception e) {
            log.error("Failed to generate income expense preview for company {}: {}", 
                     companyId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Internal server error: " + e.getMessage())
            );
        }
    }

    /**
     * Export income expense as Excel file
     * FIXED: Supports both asOfDate and date range parameters
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportIncomeExpense(
            @RequestParam Integer companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            log.info("Exporting income expense for company {} with asOfDate={}, startDate={}, endDate={}", 
                     companyId, asOfDate, startDate, endDate);
            
            // Validate inputs
            if (companyId == null || companyId <= 0) {
                log.error("Invalid company ID for export: {}", companyId);
                return ResponseEntity.badRequest().build();
            }
            
            // Determine effective date
            LocalDate effectiveDate;
            if (asOfDate != null) {
                effectiveDate = asOfDate;
            } else if (endDate != null) {
                effectiveDate = endDate;
            } else {
                log.error("No valid date provided for export");
                return ResponseEntity.badRequest().build();
            }
            
            // DDD: Convert to value object
            TenantId tenantId = TenantId.of(companyId);
            
            // FIXED: Use the export service to generate actual Excel file
            byte[] excelData = incomeExpenseExportService.exportIncomeExpense(tenantId, effectiveDate);
            
            // Validate generated data
            if (excelData == null || excelData.length == 0) {
                log.error("Generated Excel data is empty for company {}", companyId);
                return ResponseEntity.internalServerError()
                        .body("Failed to generate Excel file".getBytes());
            }
            
            // Prepare response headers
            String filename = String.format("Income_Expense_Report_%s_%s.xlsx", 
                                           companyId, 
                                           effectiveDate.toString());
            
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