// backend/src/main/java/org/example/backend/controller/IncomeStatementController.java
package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.application.dto.IncomeStatementData;
import org.example.backend.application.service.IncomeStatementDataService;
import org.example.backend.domain.valueobject.TenantId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Income Statement Controller
 * 
 * Provides REST endpoints for income statement reports
 */
@RestController
@RequestMapping("/api/income-statement")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class IncomeStatementController {

    private final IncomeStatementDataService incomeStatementDataService;

    /**
     * Get income statement data in JSON format
     */
    @GetMapping("/json")
    public ResponseEntity<IncomeStatementData> getIncomeStatementJson(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            TenantId tenantId = TenantId.of(companyId);
            IncomeStatementData data = incomeStatementDataService.getIncomeStatementData(tenantId, startDate, endDate);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate income statement: " + e.getMessage(), e);
        }
    }

    /**
     * Export income statement as Excel file
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportIncomeStatement(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            TenantId tenantId = TenantId.of(companyId);
            
            // Get the data
            IncomeStatementData data = incomeStatementDataService.getIncomeStatementData(tenantId, startDate, endDate);
            
            // For now, return empty Excel file as placeholder
            // TODO: Implement Excel generation service
            String filename = "Income_Statement_" + startDate + "_to_" + endDate + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
            
            // Return empty byte array for now - TODO: implement actual Excel generation
            return new ResponseEntity<>(new byte[0], headers, HttpStatus.OK);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to export income statement: " + e.getMessage(), e);
        }
    }
}