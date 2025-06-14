// backend/src/main/java/org/example/backend/controller/BalanceSheetController.java
package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.application.dto.BalanceSheetDetailedResponse;
import org.example.backend.application.service.BalanceSheetDataService;
import org.example.backend.application.service.BalanceSheetExportService;
import org.example.backend.domain.valueobject.TenantId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Balance Sheet Controller
 * 
 * Provides REST endpoints for balance sheet reports
 */
@RestController
@RequestMapping("/api/balance-sheet")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class BalanceSheetController {

    private final BalanceSheetDataService balanceSheetDataService;
    private final BalanceSheetExportService balanceSheetExportService;

    /**
     * Get balance sheet data in JSON format
     */
    @GetMapping("/json")
    public ResponseEntity<BalanceSheetDetailedResponse> getBalanceSheetJson(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        
        try {
            BalanceSheetDetailedResponse data = balanceSheetDataService.generateBalanceSheet(companyId, asOfDate);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate balance sheet: " + e.getMessage(), e);
        }
    }

    /**
     * Get balance sheet data in text format
     */
    @GetMapping("/text")
    public ResponseEntity<String> getBalanceSheetText(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        
        try {
            BalanceSheetDetailedResponse data = balanceSheetDataService.generateBalanceSheet(companyId, asOfDate);
            
            // Convert to text format
            StringBuilder textReport = new StringBuilder();
            textReport.append("BALANCE SHEET\n");
            textReport.append("As of ").append(asOfDate).append("\n\n");
            
            textReport.append("ASSETS\n");
            textReport.append("------\n");
            data.getAssets().forEach((categoryName, accounts) -> {
                textReport.append(categoryName).append(":\n");
                accounts.forEach(asset -> {
                    textReport.append(String.format("  %-30s %15s\n", 
                        asset.getAccountName(), 
                        asset.getCurrentMonth().toString()));
                });
            });
            textReport.append(String.format("%-30s %15s\n\n", 
                "Total Assets", data.getTotalAssets().toString()));
            
            textReport.append("LIABILITIES\n");
            textReport.append("-----------\n");
            data.getLiabilities().forEach((categoryName, accounts) -> {
                textReport.append(categoryName).append(":\n");
                accounts.forEach(liability -> {
                    textReport.append(String.format("  %-30s %15s\n", 
                        liability.getAccountName(), 
                        liability.getCurrentMonth().toString()));
                });
            });
            textReport.append(String.format("%-30s %15s\n\n", 
                "Total Liabilities", data.getTotalLiabilities().toString()));
            
            textReport.append("EQUITY\n");
            textReport.append("------\n");
            data.getEquity().forEach((categoryName, accounts) -> {
                textReport.append(categoryName).append(":\n");
                accounts.forEach(equity -> {
                    textReport.append(String.format("  %-30s %15s\n", 
                        equity.getAccountName(), 
                        equity.getCurrentMonth().toString()));
                });
            });
            textReport.append(String.format("%-30s %15s\n\n", 
                "Total Equity", data.getTotalEquity().toString()));
            
            textReport.append(String.format("Balance Check: %s\n", 
                data.isBalanced() ? "BALANCED" : "NOT BALANCED"));
            
            return ResponseEntity.ok(textReport.toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate balance sheet text: " + e.getMessage(), e);
        }
    }

    /**
     * Export balance sheet as Excel file
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportBalanceSheet(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {

        try {
            TenantId tenantId = TenantId.of(companyId);
            
            // Generate Excel file
            byte[] excelData = balanceSheetExportService.exportBalanceSheet(tenantId, asOfDate);
            
            String filename = "Balance_Sheet_" + asOfDate + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
            
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to export balance sheet: " + e.getMessage(), e);
        }
    }
}