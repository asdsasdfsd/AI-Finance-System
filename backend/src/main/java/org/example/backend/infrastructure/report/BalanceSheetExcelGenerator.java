// backend/src/main/java/org/example/backend/infrastructure/report/BalanceSheetExcelGenerator.java
package org.example.backend.infrastructure.report;

import org.example.backend.application.dto.BalanceSheetDetailedResponse;
import org.example.backend.application.dto.AccountBalanceDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Balance Sheet Excel Generator - DDD Implementation
 * 
 * Generates Excel format balance sheet reports using DDD data
 */
@Component
public class BalanceSheetExcelGenerator {
    
    @Autowired
    private ReportFileManager fileManager;
    
    @Autowired
    private ExcelStyleManager styleManager;
    
    public String generateBalanceSheet(BalanceSheetDetailedResponse data, String fileName) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Balance Sheet");
            
            // Create styles
            CellStyle titleStyle = styleManager.createTitleStyle(workbook);
            CellStyle headerStyle = styleManager.createHeaderStyle(workbook);
            CellStyle numberStyle = styleManager.createNumberStyle(workbook);
            CellStyle boldNumberStyle = styleManager.createBoldNumberStyle(workbook);
            CellStyle sectionStyle = styleManager.createSectionStyle(workbook);
            
            int rowNum = 0;
            
            // Title
            rowNum = createTitle(sheet, "Balance Sheet", data.getAsOfDate().toString(), titleStyle, rowNum);
            
            // Assets Section
            rowNum = createAssetsSection(sheet, data, headerStyle, numberStyle, sectionStyle, rowNum);
            
            // Liabilities Section
            rowNum = createLiabilitiesSection(sheet, data, headerStyle, numberStyle, sectionStyle, rowNum);
            
            // Equity Section
            rowNum = createEquitySection(sheet, data, headerStyle, numberStyle, sectionStyle, rowNum);
            
            // Summary and Balance Check
            rowNum = createSummarySection(sheet, data, boldNumberStyle, rowNum);
            
            // Auto-size columns
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Save file
            String filePath = fileManager.saveWorkbook(workbook, fileName);
            return filePath;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate balance sheet: " + e.getMessage(), e);
        }
    }
    
    private int createTitle(Sheet sheet, String title, String asOfDate, CellStyle titleStyle, int rowNum) {
        // Main title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 4));
        
        // As of date
        Row dateRow = sheet.createRow(rowNum++);
        dateRow.createCell(0).setCellValue("As of " + asOfDate);
        
        rowNum++; // Empty row
        
        // Column headers
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Account");
        headerRow.createCell(1).setCellValue("Current Month");
        headerRow.createCell(2).setCellValue("Previous Month");
        headerRow.createCell(3).setCellValue("Last Year End");
        headerRow.createCell(4).setCellValue("Notes");
        
        return rowNum;
    }
    
    private int createAssetsSection(Sheet sheet, BalanceSheetDetailedResponse data, 
                                  CellStyle headerStyle, CellStyle numberStyle, 
                                  CellStyle sectionStyle, int rowNum) {
        
        // Section header
        Row sectionRow = sheet.createRow(rowNum++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("ASSETS");
        sectionCell.setCellStyle(sectionStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 4));
        
        // Asset categories and accounts
        for (Map.Entry<String, List<AccountBalanceDTO>> entry : data.getAssets().entrySet()) {
            // Category header
            Row catRow = sheet.createRow(rowNum++);
            catRow.createCell(0).setCellValue("  " + entry.getKey());
            
            // Account details
            for (AccountBalanceDTO account : entry.getValue()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("    " + account.getAccountName());
                
                Cell currentCell = row.createCell(1);
                currentCell.setCellValue(account.getCurrentMonth().doubleValue());
                currentCell.setCellStyle(numberStyle);
                
                Cell prevCell = row.createCell(2);
                prevCell.setCellValue(account.getPreviousMonth().doubleValue());
                prevCell.setCellStyle(numberStyle);
                
                Cell lastYearCell = row.createCell(3);
                lastYearCell.setCellValue(account.getLastYearEnd().doubleValue());
                lastYearCell.setCellStyle(numberStyle);
            }
        }
        
        // Total assets
        Row totalRow = sheet.createRow(rowNum++);
        totalRow.createCell(0).setCellValue("TOTAL ASSETS");
        Cell totalCell = totalRow.createCell(1);
        totalCell.setCellValue(data.getTotalAssets().doubleValue());
        totalCell.setCellStyle(numberStyle);
        
        rowNum++; // Empty row
        return rowNum;
    }
    
    private int createLiabilitiesSection(Sheet sheet, BalanceSheetDetailedResponse data, 
                                       CellStyle headerStyle, CellStyle numberStyle, 
                                       CellStyle sectionStyle, int rowNum) {
        
        // Section header
        Row sectionRow = sheet.createRow(rowNum++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("LIABILITIES");
        sectionCell.setCellStyle(sectionStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 4));
        
        // Liability categories and accounts
        for (Map.Entry<String, List<AccountBalanceDTO>> entry : data.getLiabilities().entrySet()) {
            // Category header
            Row catRow = sheet.createRow(rowNum++);
            catRow.createCell(0).setCellValue("  " + entry.getKey());
            
            // Account details
            for (AccountBalanceDTO account : entry.getValue()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("    " + account.getAccountName());
                
                Cell currentCell = row.createCell(1);
                currentCell.setCellValue(account.getCurrentMonth().doubleValue());
                currentCell.setCellStyle(numberStyle);
                
                Cell prevCell = row.createCell(2);
                prevCell.setCellValue(account.getPreviousMonth().doubleValue());
                prevCell.setCellStyle(numberStyle);
                
                Cell lastYearCell = row.createCell(3);
                lastYearCell.setCellValue(account.getLastYearEnd().doubleValue());
                lastYearCell.setCellStyle(numberStyle);
            }
        }
        
        // Total liabilities
        Row totalRow = sheet.createRow(rowNum++);
        totalRow.createCell(0).setCellValue("TOTAL LIABILITIES");
        Cell totalCell = totalRow.createCell(1);
        totalCell.setCellValue(data.getTotalLiabilities().doubleValue());
        totalCell.setCellStyle(numberStyle);
        
        rowNum++; // Empty row
        return rowNum;
    }
    
    private int createEquitySection(Sheet sheet, BalanceSheetDetailedResponse data, 
                                  CellStyle headerStyle, CellStyle numberStyle, 
                                  CellStyle sectionStyle, int rowNum) {
        
        // Section header
        Row sectionRow = sheet.createRow(rowNum++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("EQUITY");
        sectionCell.setCellStyle(sectionStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 4));
        
        // Equity categories and accounts
        for (Map.Entry<String, List<AccountBalanceDTO>> entry : data.getEquity().entrySet()) {
            // Category header
            Row catRow = sheet.createRow(rowNum++);
            catRow.createCell(0).setCellValue("  " + entry.getKey());
            
            // Account details
            for (AccountBalanceDTO account : entry.getValue()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("    " + account.getAccountName());
                
                Cell currentCell = row.createCell(1);
                currentCell.setCellValue(account.getCurrentMonth().doubleValue());
                currentCell.setCellStyle(numberStyle);
                
                Cell prevCell = row.createCell(2);
                prevCell.setCellValue(account.getPreviousMonth().doubleValue());
                prevCell.setCellStyle(numberStyle);
                
                Cell lastYearCell = row.createCell(3);
                lastYearCell.setCellValue(account.getLastYearEnd().doubleValue());
                lastYearCell.setCellStyle(numberStyle);
            }
        }
        
        // Total equity
        Row totalRow = sheet.createRow(rowNum++);
        totalRow.createCell(0).setCellValue("TOTAL EQUITY");
        Cell totalCell = totalRow.createCell(1);
        totalCell.setCellValue(data.getTotalEquity().doubleValue());
        totalCell.setCellStyle(numberStyle);
        
        rowNum++; // Empty row
        return rowNum;
    }
    
    private int createSummarySection(Sheet sheet, BalanceSheetDetailedResponse data, 
                                   CellStyle boldNumberStyle, int rowNum) {
        
        // Summary
        Row summaryRow = sheet.createRow(rowNum++);
        summaryRow.createCell(0).setCellValue("BALANCE CHECK");
        
        Row liabEquityRow = sheet.createRow(rowNum++);
        liabEquityRow.createCell(0).setCellValue("Total Liabilities + Equity:");
        Cell liabEquityCell = liabEquityRow.createCell(1);
        liabEquityCell.setCellValue(data.getTotalLiabilities().add(data.getTotalEquity()).doubleValue());
        liabEquityCell.setCellStyle(boldNumberStyle);
        
        Row balanceRow = sheet.createRow(rowNum++);
        balanceRow.createCell(0).setCellValue("IS BALANCED:");
        balanceRow.createCell(1).setCellValue(data.isBalanced() ? "✓ YES" : "✗ NO");
        
        return rowNum;
    }
}