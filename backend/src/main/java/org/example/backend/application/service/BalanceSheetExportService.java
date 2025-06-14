// backend/src/main/java/org/example/backend/application/service/BalanceSheetExportService.java
package org.example.backend.application.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.backend.application.dto.AccountBalanceDTO;
import org.example.backend.application.dto.BalanceSheetDetailedResponse;
import org.example.backend.domain.valueobject.TenantId;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Balance Sheet Export Service - DDD Implementation
 * 
 * Handles Excel export functionality for balance sheets using DDD aggregates
 */
@Service
@RequiredArgsConstructor
public class BalanceSheetExportService {

    private final BalanceSheetDataService balanceSheetDataService;

    /**
     * Export balance sheet to Excel format
     */
    public byte[] exportBalanceSheet(TenantId tenantId, LocalDate asOfDate) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Get balance sheet data using DDD service
            BalanceSheetDetailedResponse balanceSheet = balanceSheetDataService.generateBalanceSheet(tenantId.getValue(), asOfDate);
            
            Sheet sheet = workbook.createSheet("Balance Sheet");
            int rowNum = 0;

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);
            headerStyle.setFont(headerFont);

            // Create section header style
            CellStyle sectionStyle = workbook.createCellStyle();
            Font sectionFont = workbook.createFont();
            sectionFont.setBold(true);
            sectionFont.setFontHeightInPoints((short) 12);
            sectionStyle.setFont(sectionFont);

            // Create data style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BALANCE SHEET");
            titleCell.setCellStyle(headerStyle);
            
            Row dateRow = sheet.createRow(rowNum++);
            dateRow.createCell(0).setCellValue("As of " + asOfDate.toString());
            
            rowNum++; // Empty row

            // Column headers
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("Account");
            headerRow.createCell(1).setCellValue("Current Month");
            headerRow.createCell(2).setCellValue("Previous Month");
            headerRow.createCell(3).setCellValue("Last Year End");

            // ASSETS section
            rowNum++;
            Row assetsHeaderRow = sheet.createRow(rowNum++);
            Cell assetsCell = assetsHeaderRow.createCell(0);
            assetsCell.setCellValue("ASSETS");
            assetsCell.setCellStyle(sectionStyle);

            rowNum = writeAccountSection(sheet, rowNum, balanceSheet.getAssets(), dataStyle);
            
            Row totalAssetsRow = sheet.createRow(rowNum++);
            totalAssetsRow.createCell(0).setCellValue("Total Assets");
            Cell totalAssetsCell = totalAssetsRow.createCell(1);
            totalAssetsCell.setCellValue(balanceSheet.getTotalAssets().doubleValue());
            totalAssetsCell.setCellStyle(dataStyle);

            // LIABILITIES section
            rowNum++;
            Row liabilitiesHeaderRow = sheet.createRow(rowNum++);
            Cell liabilitiesCell = liabilitiesHeaderRow.createCell(0);
            liabilitiesCell.setCellValue("LIABILITIES");
            liabilitiesCell.setCellStyle(sectionStyle);

            rowNum = writeAccountSection(sheet, rowNum, balanceSheet.getLiabilities(), dataStyle);
            
            Row totalLiabilitiesRow = sheet.createRow(rowNum++);
            totalLiabilitiesRow.createCell(0).setCellValue("Total Liabilities");
            Cell totalLiabilitiesCell = totalLiabilitiesRow.createCell(1);
            totalLiabilitiesCell.setCellValue(balanceSheet.getTotalLiabilities().doubleValue());
            totalLiabilitiesCell.setCellStyle(dataStyle);

            // EQUITY section
            rowNum++;
            Row equityHeaderRow = sheet.createRow(rowNum++);
            Cell equityCell = equityHeaderRow.createCell(0);
            equityCell.setCellValue("EQUITY");
            equityCell.setCellStyle(sectionStyle);

            rowNum = writeAccountSection(sheet, rowNum, balanceSheet.getEquity(), dataStyle);
            
            Row totalEquityRow = sheet.createRow(rowNum++);
            totalEquityRow.createCell(0).setCellValue("Total Equity");
            Cell totalEquityCell = totalEquityRow.createCell(1);
            totalEquityCell.setCellValue(balanceSheet.getTotalEquity().doubleValue());
            totalEquityCell.setCellStyle(dataStyle);

            // Balance check
            rowNum++;
            Row balanceCheckRow = sheet.createRow(rowNum++);
            balanceCheckRow.createCell(0).setCellValue("Balance Check:");
            balanceCheckRow.createCell(1).setCellValue(balanceSheet.isBalanced() ? "BALANCED" : "NOT BALANCED");

            // Auto-size columns
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate balance sheet Excel export", e);
        }
    }

    /**
     * Write account section to Excel sheet
     */
    private int writeAccountSection(Sheet sheet, int startRow, Map<String, List<AccountBalanceDTO>> accountsByCategory, CellStyle dataStyle) {
        int rowNum = startRow;
        
        for (Map.Entry<String, List<AccountBalanceDTO>> categoryEntry : accountsByCategory.entrySet()) {
            // Category header
            Row categoryRow = sheet.createRow(rowNum++);
            categoryRow.createCell(0).setCellValue(categoryEntry.getKey());
            
            // Accounts in this category
            for (AccountBalanceDTO account : categoryEntry.getValue()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("  " + account.getAccountName());
                
                Cell currentMonthCell = row.createCell(1);
                currentMonthCell.setCellValue(account.getCurrentMonth().doubleValue());
                currentMonthCell.setCellStyle(dataStyle);
                
                Cell previousMonthCell = row.createCell(2);
                previousMonthCell.setCellValue(account.getPreviousMonth().doubleValue());
                previousMonthCell.setCellStyle(dataStyle);
                
                Cell lastYearEndCell = row.createCell(3);
                lastYearEndCell.setCellValue(account.getLastYearEnd().doubleValue());
                lastYearEndCell.setCellStyle(dataStyle);
            }
        }
        
        return rowNum;
    }
}