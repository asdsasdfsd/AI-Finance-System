// backend/src/main/java/org/example/backend/application/service/BalanceSheetExportService.java
package org.example.backend.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
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
 * Balance Sheet Export Service - Fixed DDD Implementation
 * 
 * Handles Excel export functionality for balance sheets using DDD aggregates
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceSheetExportService {

    private final BalanceSheetDataService balanceSheetDataService;

    /**
     * Generate Excel file for balance sheet data - Fixed method signature
     * @param data Balance sheet data
     * @return Excel file as byte array
     */
    public byte[] generateExcel(BalanceSheetDetailedResponse data) {
        log.info("Generating Excel for balance sheet data");
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Balance Sheet");
            int rowNum = 0;

            // Create styles
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle sectionStyle = createSectionStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);

            // Title and header
            rowNum = createTitle(sheet, "Balance Sheet", data.getAsOfDate().toString(), titleStyle, rowNum);
            rowNum = createColumnHeaders(sheet, headerStyle, rowNum);

            // ASSETS section
            rowNum = createAssetsSection(sheet, data, sectionStyle, dataStyle, numberStyle, rowNum);

            // LIABILITIES section
            rowNum = createLiabilitiesSection(sheet, data, sectionStyle, dataStyle, numberStyle, rowNum);

            // EQUITY section
            rowNum = createEquitySection(sheet, data, sectionStyle, dataStyle, numberStyle, rowNum);

            // Balance check
            rowNum = createBalanceCheck(sheet, data, headerStyle, numberStyle, rowNum);

            // Auto-size columns
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Failed to generate balance sheet Excel export", e);
            throw new RuntimeException("Failed to generate balance sheet Excel export", e);
        }
    }

    /**
     * Export balance sheet to Excel format using tenant and date
     */
    public byte[] exportBalanceSheet(TenantId tenantId, LocalDate asOfDate) {
        try {
            // Get balance sheet data using DDD service
            BalanceSheetDetailedResponse balanceSheet = balanceSheetDataService
                    .generateBalanceSheetByTenant(tenantId, asOfDate);
            
            // Generate Excel using the fixed method
            return generateExcel(balanceSheet);
            
        } catch (Exception e) {
            log.error("Failed to export balance sheet for tenant {}: {}", tenantId.getValue(), e.getMessage());
            throw new RuntimeException("Failed to export balance sheet", e);
        }
    }

    // ========== Helper Methods for Excel Creation ==========

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createSectionStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
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
        return rowNum;
    }

    private int createColumnHeaders(Sheet sheet, CellStyle headerStyle, int rowNum) {
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Account");
        headerRow.createCell(1).setCellValue("Current Month");
        headerRow.createCell(2).setCellValue("Previous Month");
        headerRow.createCell(3).setCellValue("Last Year End");
        headerRow.createCell(4).setCellValue("Notes");
        
        // Apply header style to all cells
        for (int i = 0; i < 5; i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }
        
        return rowNum;
    }

    private int createAssetsSection(Sheet sheet, BalanceSheetDetailedResponse data, 
                                  CellStyle sectionStyle, CellStyle dataStyle, 
                                  CellStyle numberStyle, int rowNum) {
        
        // Section header
        Row sectionRow = sheet.createRow(rowNum++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("ASSETS");
        sectionCell.setCellStyle(sectionStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 4));
        
        // Asset categories and accounts
        rowNum = writeAccountSection(sheet, rowNum, data.getAssets(), dataStyle, numberStyle);
        
        // Total assets
        Row totalRow = sheet.createRow(rowNum++);
        totalRow.createCell(0).setCellValue("Total Assets");
        Cell totalCell = totalRow.createCell(1);
        totalCell.setCellValue(data.getTotalAssets().doubleValue());
        totalCell.setCellStyle(numberStyle);
        
        rowNum++; // Empty row
        return rowNum;
    }

    private int createLiabilitiesSection(Sheet sheet, BalanceSheetDetailedResponse data, 
                                       CellStyle sectionStyle, CellStyle dataStyle, 
                                       CellStyle numberStyle, int rowNum) {
        
        // Section header
        Row sectionRow = sheet.createRow(rowNum++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("LIABILITIES");
        sectionCell.setCellStyle(sectionStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 4));
        
        // Liability categories and accounts
        rowNum = writeAccountSection(sheet, rowNum, data.getLiabilities(), dataStyle, numberStyle);
        
        // Total liabilities
        Row totalRow = sheet.createRow(rowNum++);
        totalRow.createCell(0).setCellValue("Total Liabilities");
        Cell totalCell = totalRow.createCell(1);
        totalCell.setCellValue(data.getTotalLiabilities().doubleValue());
        totalCell.setCellStyle(numberStyle);
        
        rowNum++; // Empty row
        return rowNum;
    }

    private int createEquitySection(Sheet sheet, BalanceSheetDetailedResponse data, 
                                  CellStyle sectionStyle, CellStyle dataStyle, 
                                  CellStyle numberStyle, int rowNum) {
        
        // Section header
        Row sectionRow = sheet.createRow(rowNum++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("EQUITY");
        sectionCell.setCellStyle(sectionStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 4));
        
        // Equity categories and accounts
        rowNum = writeAccountSection(sheet, rowNum, data.getEquity(), dataStyle, numberStyle);
        
        // Total equity
        Row totalRow = sheet.createRow(rowNum++);
        totalRow.createCell(0).setCellValue("Total Equity");
        Cell totalCell = totalRow.createCell(1);
        totalCell.setCellValue(data.getTotalEquity().doubleValue());
        totalCell.setCellStyle(numberStyle);
        
        rowNum++; // Empty row
        return rowNum;
    }

    private int createBalanceCheck(Sheet sheet, BalanceSheetDetailedResponse data, 
                                 CellStyle headerStyle, CellStyle numberStyle, int rowNum) {
        
        Row balanceCheckRow = sheet.createRow(rowNum++);
        balanceCheckRow.createCell(0).setCellValue("Balance Check:");
        balanceCheckRow.createCell(1).setCellValue(data.isBalanced() ? "BALANCED" : "NOT BALANCED");
        
        // Apply styles
        balanceCheckRow.getCell(0).setCellStyle(headerStyle);
        balanceCheckRow.getCell(1).setCellStyle(headerStyle);
        
        return rowNum;
    }

    /**
     * Write account section to Excel sheet
     */
    private int writeAccountSection(Sheet sheet, int startRow, Map<String, List<AccountBalanceDTO>> accountsByCategory, 
                                   CellStyle dataStyle, CellStyle numberStyle) {
        int rowNum = startRow;
        
        for (Map.Entry<String, List<AccountBalanceDTO>> categoryEntry : accountsByCategory.entrySet()) {
            // Category header
            Row categoryRow = sheet.createRow(rowNum++);
            Cell categoryCell = categoryRow.createCell(0);
            categoryCell.setCellValue(categoryEntry.getKey());
            categoryCell.setCellStyle(dataStyle);
            
            // Accounts in this category
            for (AccountBalanceDTO account : categoryEntry.getValue()) {
                Row row = sheet.createRow(rowNum++);
                
                // Account name (indented)
                Cell nameCell = row.createCell(0);
                nameCell.setCellValue("  " + account.getAccountName());
                nameCell.setCellStyle(dataStyle);
                
                // Current month
                Cell currentMonthCell = row.createCell(1);
                currentMonthCell.setCellValue(account.getCurrentMonth().doubleValue());
                currentMonthCell.setCellStyle(numberStyle);
                
                // Previous month
                Cell previousMonthCell = row.createCell(2);
                previousMonthCell.setCellValue(account.getPreviousMonth().doubleValue());
                previousMonthCell.setCellStyle(numberStyle);
                
                // Last year end
                Cell lastYearEndCell = row.createCell(3);
                lastYearEndCell.setCellValue(account.getLastYearEnd().doubleValue());
                lastYearEndCell.setCellStyle(numberStyle);
                
                // Notes (empty for now)
                Cell notesCell = row.createCell(4);
                notesCell.setCellValue("");
                notesCell.setCellStyle(dataStyle);
            }
        }
        
        return rowNum;
    }
}