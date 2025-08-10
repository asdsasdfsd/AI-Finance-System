// backend/src/main/java/org/example/backend/controller/FinancialGroupingController.java
// NEW Financial Grouping Controller for transaction grouping reports

package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/financial-grouping")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class FinancialGroupingController {

    /**
     * Generate financial grouping report preview
     */
    @GetMapping("/generate")
    public ResponseEntity<?> generateFinancialGroupingPreview(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            log.info("Generating financial grouping preview for company {} from {} to {}", 
                     companyId, startDate, endDate);
            
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Start date cannot be after end date")
                );
            }
            
            // Generate sample financial grouping data
            List<Map<String, Object>> tableData = generateSampleFinancialGroupingData(companyId, startDate, endDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("data", tableData);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("companyId", companyId);
            response.put("totalRows", tableData.size());
            response.put("reportType", "FINANCIAL_GROUPING");
            
            log.info("Financial grouping preview generated with {} rows", tableData.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to generate financial grouping preview: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get financial grouping data in JSON format
     */
    @GetMapping("/json")
    public ResponseEntity<?> getFinancialGroupingReport(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            log.info("Getting financial grouping JSON for company {} from {} to {}", 
                     companyId, startDate, endDate);
            
            List<Map<String, Object>> data = generateSampleFinancialGroupingData(companyId, startDate, endDate);
            
            log.info("Financial grouping JSON generated with {} rows", data.size());
            return ResponseEntity.ok(data);
            
        } catch (Exception e) {
            log.error("Failed to get financial grouping JSON: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Export financial grouping report as Excel
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportFinancialGrouping(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            log.info("Exporting financial grouping for company {} from {} to {}", 
                     companyId, startDate, endDate);
            
            // Generate sample Excel data (implement actual Excel generation as needed)
            byte[] excelData = generateSampleExcelData();
            String filename = String.format("Financial_Grouping_%d_%s_to_%s.xlsx", 
                                           companyId, startDate, endDate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType
                    .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
            headers.setContentLength(excelData.length);
            
            log.info("Financial grouping exported successfully, file size: {} bytes", excelData.length);
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Failed to export financial grouping: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Financial Grouping Controller is operational");
    }

    // Helper methods
    private List<Map<String, Object>> generateSampleFinancialGroupingData(Integer companyId, LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> data = new ArrayList<>();
        
        // Sample grouping by category
        String[] categories = {"Sales Revenue", "Operating Expenses", "Administrative Costs", "Marketing Expenses", "R&D Investment"};
        double[] amounts = {450000.00, 125000.00, 85000.00, 65000.00, 95000.00};
        
        for (int i = 0; i < categories.length; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("key", "grouping_" + i);
            row.put("Category", categories[i]);
            row.put("Amount", amounts[i]);
            row.put("Count", (int)(Math.random() * 20) + 5);
            row.put("Percentage", String.format("%.1f%%", (amounts[i] / 820000.00) * 100));
            data.add(row);
        }
        
        return data;
    }

    private byte[] generateSampleExcelData() {
        // Return minimal Excel file for demo
        return "Sample Excel Data".getBytes();
    }
}