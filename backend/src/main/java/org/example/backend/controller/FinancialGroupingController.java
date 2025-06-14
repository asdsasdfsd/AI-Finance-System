// backend/src/main/java/org/example/backend/controller/FinancialGroupingController.java
package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.application.dto.FinancialGroupingData;
import org.example.backend.application.service.FinancialGroupingDataService;
import org.example.backend.domain.valueobject.TenantId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Financial Grouping Controller
 * 
 * Provides REST endpoints for financial grouping reports
 */
@RestController
@RequestMapping("/api/financial-grouping")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class FinancialGroupingController {

    private final FinancialGroupingDataService financialGroupingDataService;

    /**
     * Get financial grouping data in JSON format
     */
    @GetMapping("/json")
    public ResponseEntity<FinancialGroupingData> getFinancialGroupingJson(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            TenantId tenantId = TenantId.of(companyId);
            FinancialGroupingData data = financialGroupingDataService.getFinancialGroupingData(tenantId, startDate, endDate);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate financial grouping report: " + e.getMessage(), e);
        }
    }

    /**
     * Export financial grouping as Excel file
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportFinancialGrouping(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            TenantId tenantId = TenantId.of(companyId);
            
            // Get the data
            FinancialGroupingData data = financialGroupingDataService.getFinancialGroupingData(tenantId, startDate, endDate);
            
            // For now, return empty Excel file as placeholder
            // TODO: Implement Excel generation service
            String filename = "Financial_Grouping_" + startDate + "_to_" + endDate + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
            
            // Return empty byte array for now - TODO: implement actual Excel generation
            return new ResponseEntity<>(new byte[0], headers, HttpStatus.OK);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to export financial grouping: " + e.getMessage(), e);
        }
    }
}