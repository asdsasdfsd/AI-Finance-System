// backend/src/main/java/org/example/backend/controller/FinancialReportController.java
// ENHANCED Financial Report Controller - Fixed for preview functionality

package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.application.service.FinancialReportExportService;
import org.example.backend.application.service.FinancialReportJsonService;
import org.example.backend.application.dto.IncomeExpenseReportRowDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/financial-report")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class FinancialReportController {

    private final FinancialReportExportService exportService;
    private final FinancialReportJsonService financialReportJsonService;

    /**
     * FIXED: Export financial report as Excel
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportFinancialReport(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {

        try {
            log.info("Exporting financial report for company {} as of {}", companyId, asOfDate);
            
            byte[] excel = exportService.exportIncomeExpenseReport(companyId, asOfDate);
            String filename = "Financial_Report_" + companyId + "_" + asOfDate + ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType
                    .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
            headers.setContentLength(excel.length);

            log.info("Financial report exported successfully, file size: {} bytes", excel.length);
            return new ResponseEntity<>(excel, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Failed to export financial report for company {}: {}", companyId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * FIXED: Get financial report data in JSON format for preview
     */
    @GetMapping("/json")
    public ResponseEntity<List<IncomeExpenseReportRowDTO>> getIncomeExpenseReport(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        
        try {
            log.info("Generating financial report JSON for company {} as of {}", companyId, asOfDate);
            
            List<IncomeExpenseReportRowDTO> rows = financialReportJsonService.getIncomeExpenseReport(companyId, asOfDate);
            
            log.info("Financial report JSON generated successfully with {} rows", rows.size());
            return ResponseEntity.ok(rows);
            
        } catch (Exception e) {
            log.error("Failed to generate financial report JSON for company {}: {}", companyId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * NEW: Generate financial report preview for frontend (unified format)
     */
    @GetMapping("/generate")
    public ResponseEntity<?> generateFinancialReportPreview(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        
        try {
            log.info("Generating financial report preview for company {} as of {}", companyId, asOfDate);
            
            List<IncomeExpenseReportRowDTO> rows = financialReportJsonService.getIncomeExpenseReport(companyId, asOfDate);
            
            // Convert to frontend-friendly format
            List<Map<String, Object>> tableData = new java.util.ArrayList<>();
            
            for (IncomeExpenseReportRowDTO row : rows) {
                Map<String, Object> tableRow = new HashMap<>();
                tableRow.put("key", "financial_" + row.hashCode());
                tableRow.put("Account", row.getDescription());
                // FIXED: Use currentMonth instead of getAmount() which doesn't exist
                tableRow.put("Amount", row.getCurrentMonth());
                tableRow.put("Type", row.getType());
                tableRow.put("Category", row.getCategory());
                // Additional fields for richer display
                tableRow.put("PreviousMonth", row.getPreviousMonth());
                tableRow.put("YearToDate", row.getYearToDate());
                tableData.add(tableRow);
            }
            
            // Create summary response
            Map<String, Object> response = new HashMap<>();
            response.put("data", tableData);
            response.put("asOfDate", asOfDate);
            response.put("companyId", companyId);
            response.put("totalRows", tableData.size());
            response.put("reportType", "FINANCIAL_REPORT");
            
            log.info("Financial report preview generated with {} rows", tableData.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to generate financial report preview for company {}: {}", companyId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Financial Report Controller is operational");
    }
}
