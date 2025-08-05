// backend/src/main/java/org/example/backend/application/service/IncomeStatementExportService.java
package org.example.backend.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.backend.application.dto.IncomeStatementData;
import org.example.backend.domain.valueobject.TenantId;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Income Statement Export Service
 * Handles Excel export for income statements
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IncomeStatementExportService {
    
    private final IncomeStatementDataService incomeStatementDataService;
    
    /**
     * Export income statement to Excel format
     */
    public byte[] exportIncomeStatement(TenantId tenantId, LocalDate startDate, LocalDate endDate) {
        try {
            log.info("Starting income statement export for tenant {} from {} to {}", 
                    tenantId.getValue(), startDate, endDate);
            
            // Validate inputs
            if (tenantId == null || tenantId.getValue() == null) {
                throw new IllegalArgumentException("Tenant ID cannot be null");
            }
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("Start date and end date cannot be null");
            }
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }
            
            // Get income statement data using DDD service
            IncomeStatementData reportData = incomeStatementDataService
                    .getIncomeStatementData(tenantId, startDate, endDate);
            
            if (reportData == null) {
                throw new RuntimeException("Failed to generate income statement data");
            }
            
            // Generate Excel
            byte[] excelData = generateExcel(reportData);
            
            log.info("Income statement export completed successfully for tenant {}, size: {} bytes", 
                    tenantId.getValue(), excelData.length);
            
            return excelData;
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid input for income statement export: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to export income statement for tenant {}: {}", 
                     tenantId.getValue(), e.getMessage(), e);
            throw new RuntimeException("Failed to export income statement", e);
        }
    }
    
    /**
     * Generate Excel file for income statement data
     */
    public byte[] generateExcel(IncomeStatementData data) {
        log.info("Generating Excel for income statement data for company: {}", data.getCompanyName());
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Income Statement");
            int rowNum = 0;

            // Create styles
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle sectionStyle = createSectionStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle boldNumberStyle = createBoldNumberStyle(workbook);

            // Title and company info
            rowNum = createTitle(sheet, data, titleStyle, rowNum);
            
            // Column headers
            rowNum = createColumnHeaders(sheet, headerStyle, rowNum);
            
            // Revenue section
            rowNum = createRevenueSection(sheet, data, sectionStyle, dataStyle, numberStyle, rowNum);
            
            // Expenses section
            rowNum = createExpensesSection(sheet, data, sectionStyle, dataStyle, numberStyle, rowNum);
            
            // Net Income section
            rowNum = createNetIncomeSection(sheet, data, boldNumberStyle, rowNum);

            // Auto-size columns
            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Failed to generate income statement Excel export", e);
            throw new RuntimeException("Failed to generate income statement Excel export", e);
        }
    }

    // ========== Helper Methods for Excel Creation ==========

    private int createTitle(Sheet sheet, IncomeStatementData data, CellStyle titleStyle, int rowNum) {
        // Company name
        Row companyRow = sheet.createRow(rowNum++);
        Cell companyCell = companyRow.createCell(0);
        companyCell.setCellValue(data.getCompanyName());
        companyCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));

        // Report title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Income Statement");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));

        // Period
        Row periodRow = sheet.createRow(rowNum++);
        Cell periodCell = periodRow.createCell(0);
        periodCell.setCellValue("For the period: " + data.getPeriodStartDate() + " to " + data.getPeriodEndDate());
        periodCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));

        // Empty row
        sheet.createRow(rowNum++);
        
        return rowNum;
    }

    private int createColumnHeaders(Sheet sheet, CellStyle headerStyle, int rowNum) {
        Row headerRow = sheet.createRow(rowNum++);
        
        Cell accountCell = headerRow.createCell(0);
        accountCell.setCellValue("Account");
        accountCell.setCellStyle(headerStyle);
        
        Cell categoryCell = headerRow.createCell(1);
        categoryCell.setCellValue("Category");
        categoryCell.setCellStyle(headerStyle);
        
        Cell amountCell = headerRow.createCell(2);
        amountCell.setCellValue("Amount");
        amountCell.setCellStyle(headerStyle);
        
        return rowNum;
    }

    private int createRevenueSection(Sheet sheet, IncomeStatementData data, 
                                   CellStyle sectionStyle, CellStyle dataStyle, 
                                   CellStyle numberStyle, int rowNum) {
        // Revenue section header
        Row revenueHeaderRow = sheet.createRow(rowNum++);
        Cell revenueHeaderCell = revenueHeaderRow.createCell(0);
        revenueHeaderCell.setCellValue("REVENUE");
        revenueHeaderCell.setCellStyle(sectionStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));

        // Revenue by category
        if (data.getRevenueByCategory() != null) {
            for (Map.Entry<String, BigDecimal> entry : data.getRevenueByCategory().entrySet()) {
                Row dataRow = sheet.createRow(rowNum++);
                
                Cell categoryCell = dataRow.createCell(0);
                categoryCell.setCellValue("  " + entry.getKey()); // Indent
                categoryCell.setCellStyle(dataStyle);
                
                Cell amountCell = dataRow.createCell(2);
                amountCell.setCellValue(entry.getValue().doubleValue());
                amountCell.setCellStyle(numberStyle);
            }
        }

        // Total Revenue
        Row totalRevenueRow = sheet.createRow(rowNum++);
        Cell totalRevenueLabelCell = totalRevenueRow.createCell(1);
        totalRevenueLabelCell.setCellValue("Total Revenue");
        totalRevenueLabelCell.setCellStyle(sectionStyle);
        
        Cell totalRevenueAmountCell = totalRevenueRow.createCell(2);
        totalRevenueAmountCell.setCellValue(data.getTotalRevenue().doubleValue());
        totalRevenueAmountCell.setCellStyle(numberStyle);

        // Empty row
        sheet.createRow(rowNum++);
        
        return rowNum;
    }

    private int createExpensesSection(Sheet sheet, IncomeStatementData data, 
                                    CellStyle sectionStyle, CellStyle dataStyle, 
                                    CellStyle numberStyle, int rowNum) {
        // Expenses section header
        Row expensesHeaderRow = sheet.createRow(rowNum++);
        Cell expensesHeaderCell = expensesHeaderRow.createCell(0);
        expensesHeaderCell.setCellValue("EXPENSES");
        expensesHeaderCell.setCellStyle(sectionStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));

        // Expenses by category
        if (data.getExpensesByCategory() != null) {
            for (Map.Entry<String, BigDecimal> entry : data.getExpensesByCategory().entrySet()) {
                Row dataRow = sheet.createRow(rowNum++);
                
                Cell categoryCell = dataRow.createCell(0);
                categoryCell.setCellValue("  " + entry.getKey()); // Indent
                categoryCell.setCellStyle(dataStyle);
                
                Cell amountCell = dataRow.createCell(2);
                amountCell.setCellValue(entry.getValue().doubleValue());
                amountCell.setCellStyle(numberStyle);
            }
        }

        // Total Expenses
        Row totalExpensesRow = sheet.createRow(rowNum++);
        Cell totalExpensesLabelCell = totalExpensesRow.createCell(1);
        totalExpensesLabelCell.setCellValue("Total Expenses");
        totalExpensesLabelCell.setCellStyle(sectionStyle);
        
        Cell totalExpensesAmountCell = totalExpensesRow.createCell(2);
        totalExpensesAmountCell.setCellValue(data.getTotalExpenses().doubleValue());
        totalExpensesAmountCell.setCellStyle(numberStyle);

        // Empty row
        sheet.createRow(rowNum++);
        
        return rowNum;
    }

    private int createNetIncomeSection(Sheet sheet, IncomeStatementData data, 
                                     CellStyle boldNumberStyle, int rowNum) {
        // Net Income
        Row netIncomeRow = sheet.createRow(rowNum++);
        Cell netIncomeLabelCell = netIncomeRow.createCell(1);
        netIncomeLabelCell.setCellValue("NET INCOME");
        netIncomeLabelCell.setCellStyle(boldNumberStyle);
        
        Cell netIncomeAmountCell = netIncomeRow.createCell(2);
        netIncomeAmountCell.setCellValue(data.getNetIncome().doubleValue());
        netIncomeAmountCell.setCellStyle(boldNumberStyle);
        
        return rowNum;
    }

    // ========== Style Creation Methods ==========

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
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
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
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
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createBoldNumberStyle(Workbook workbook) {
        CellStyle style = createNumberStyle(workbook);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setBorderTop(BorderStyle.DOUBLE);
        return style;
    }
}