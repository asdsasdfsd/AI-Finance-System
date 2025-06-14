// backend/src/main/java/org/example/backend/controller/IncomeExpenseController.java
package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.application.dto.IncomeExpenseReportData;
import org.example.backend.application.service.IncomeExpenseDataService;
import org.example.backend.domain.valueobject.TenantId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Income vs Expense Controller
 * 
 * Provides REST endpoints for income vs expense reports
 */
@Slf4j
@RestController
@RequestMapping("/api/income-expense")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class IncomeExpenseController {

    private final IncomeExpenseDataService incomeExpenseDataService;

    /**
     * Get income vs expense data in JSON format
     */
    @GetMapping("/json")
    public ResponseEntity<IncomeExpenseReportData> getIncomeExpenseJson(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        
        try {
            log.info("Generating income expense report for company {} as of {}", companyId, asOfDate);
            
            TenantId tenantId = TenantId.of(companyId);
            IncomeExpenseReportData data = incomeExpenseDataService.generateIncomeExpenseReportByTenant(tenantId, asOfDate);
            
            log.info("Income expense report generated successfully for tenant {}", tenantId.getValue());
            return ResponseEntity.ok(data);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid input for income expense report: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to generate income expense report for company {}: {}", companyId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Export income vs expense as Excel file
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportIncomeExpense(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {

        try {
            log.info("Exporting income expense report for company {} as of {}", companyId, asOfDate);
            
            TenantId tenantId = TenantId.of(companyId);
            
            // Get the data
            IncomeExpenseReportData data = incomeExpenseDataService.generateIncomeExpenseReportByTenant(tenantId, asOfDate);
            
            // For now, return empty Excel file as placeholder
            // TODO: Implement Excel generation service
            String filename = "Income_Expense_Report_" + asOfDate.toString() + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
            
            // Return empty byte array for now - TODO: implement actual Excel generation
            return new ResponseEntity<>(new byte[0], headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Failed to export income expense report for company {}: {}", companyId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}