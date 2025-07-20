// backend/src/main/java/org/example/backend/application/service/IncomeExpenseExportService.java
package org.example.backend.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.backend.application.dto.IncomeExpenseReportData;
import org.example.backend.application.dto.IncomeExpenseReportRowDTO;
import org.example.backend.domain.valueobject.TenantId;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Income Expense Export Service
 * Handles Excel export for income vs expense reports
 * FIXED: Complete implementation with proper error handling
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IncomeExpenseExportService {
    
    private final IncomeExpenseDataService incomeExpenseDataService;
    
    /**
     * Export income expense report to Excel format using tenant and date
     * FIXED: Added comprehensive error handling and validation
     */
    public byte[] exportIncomeExpense(TenantId tenantId, LocalDate asOfDate) {
        try {
            log.info("Starting income expense export for tenant {} as of {}", tenantId.getValue(), asOfDate);
            
            // Validate inputs
            if (tenantId == null || tenantId.getValue() == null) {
                throw new IllegalArgumentException("Tenant ID cannot be null");
            }
            if (asOfDate == null) {
                throw new IllegalArgumentException("As-of date cannot be null");
            }
            
            // Get income expense data using DDD service
            IncomeExpenseReportData reportData = incomeExpenseDataService
                    .generateIncomeExpenseReportByTenant(tenantId, asOfDate);
            
            if (reportData == null) {
                throw new RuntimeException("Failed to generate income expense report data");
            }
            
            // Generate Excel
            byte[] excelData = generateExcel(reportData);
            
            log.info("Income expense export completed successfully for tenant {}, size: {} bytes", 
                    tenantId.getValue(), excelData.length);
            
            return excelData;
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid input for income expense export: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to export income expense report for tenant {}: {}", 
                     tenantId.getValue(), e.getMessage(), e);
            throw new RuntimeException("Failed to export income expense report", e);
        }
    }

    /**
     * Generate Excel file for income expense data
     * FIXED: Complete implementation with all required sections
     */
    public byte[] generateExcel(IncomeExpenseReportData data) {
        log.info("Generating Excel for income expense data for company: {}", data.getCompanyName());
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Income vs Expense Report");
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
            
            // Income section
            rowNum = createIncomeSection(sheet, data, sectionStyle, headerStyle, dataStyle, numberStyle, rowNum);
            
            // Expense section
            rowNum = createExpenseSection(sheet, data, sectionStyle, headerStyle, dataStyle, numberStyle, rowNum);
            
            // Summary section
            rowNum = createSummarySection(sheet, data, boldNumberStyle, rowNum);

            // Auto-size columns
            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Failed to generate income expense Excel export", e);
            throw new RuntimeException("Failed to generate income expense Excel export", e);
        }
    }

    // ========== Helper Methods for Excel Creation ==========

    private int createTitle(Sheet sheet, IncomeExpenseReportData data, CellStyle titleStyle, int rowNum) {
        // Company name
        Row companyRow = sheet.createRow(rowNum++);
        Cell companyCell = companyRow.createCell(0);
        companyCell.setCellValue(data.getCompanyName());
        companyCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 7));

        // Report title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Income vs Expense Report");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 7));

        // As of date
        Row dateRow = sheet.createRow(rowNum++);
        Cell dateCell = dateRow.createCell(0);
        dateCell.setCellValue("As of: " + data.getAsOfDate().toString());
        dateCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 7));

        return rowNum + 1; // Add blank row
    }

    private int createIncomeSection(Sheet sheet, IncomeExpenseReportData data, 
                                  CellStyle sectionStyle, CellStyle headerStyle, 
                                  CellStyle dataStyle, CellStyle numberStyle, int rowNum) {
        
        // Income section header
        Row sectionRow = sheet.createRow(rowNum++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("INCOME");
        sectionCell.setCellStyle(sectionStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 7));

        // Column headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Category", "Description", "Current Month", "Previous Month", 
                           "Year to Date", "Budget YTD", "Variance", "Full Year Budget"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Income data rows
        List<IncomeExpenseReportRowDTO> incomeRows = data.getIncomeRows();
        if (incomeRows != null) {
            for (IncomeExpenseReportRowDTO row : incomeRows) {
                Row dataRow = sheet.createRow(rowNum++);
                populateDataRow(dataRow, row, dataStyle, numberStyle);
            }
        }

        return rowNum + 1; // Add blank row
    }

    private int createExpenseSection(Sheet sheet, IncomeExpenseReportData data, 
                                   CellStyle sectionStyle, CellStyle headerStyle, 
                                   CellStyle dataStyle, CellStyle numberStyle, int rowNum) {
        
        // Expense section header
        Row sectionRow = sheet.createRow(rowNum++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("EXPENSES");
        sectionCell.setCellStyle(sectionStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 7));

        // Column headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Category", "Description", "Current Month", "Previous Month", 
                           "Year to Date", "Budget YTD", "Variance", "Full Year Budget"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Expense data rows
        List<IncomeExpenseReportRowDTO> expenseRows = data.getExpenseRows();
        if (expenseRows != null) {
            for (IncomeExpenseReportRowDTO row : expenseRows) {
                Row dataRow = sheet.createRow(rowNum++);
                populateDataRow(dataRow, row, dataStyle, numberStyle);
            }
        }

        return rowNum + 1; // Add blank row
    }

    private int createSummarySection(Sheet sheet, IncomeExpenseReportData data, 
                                   CellStyle boldNumberStyle, int rowNum) {
        
        // Summary section header
        Row summaryHeaderRow = sheet.createRow(rowNum++);
        Cell summaryHeaderCell = summaryHeaderRow.createCell(0);
        summaryHeaderCell.setCellValue("SUMMARY");
        summaryHeaderCell.setCellStyle(boldNumberStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 7));

        // Total Income
        Row totalIncomeRow = sheet.createRow(rowNum++);
        totalIncomeRow.createCell(0).setCellValue("Total Income");
        Cell totalIncomeCell = totalIncomeRow.createCell(2);
        totalIncomeCell.setCellValue(data.getTotalIncome() != null ? data.getTotalIncome().doubleValue() : 0.0);
        totalIncomeCell.setCellStyle(boldNumberStyle);

        // Total Expenses
        Row totalExpenseRow = sheet.createRow(rowNum++);
        totalExpenseRow.createCell(0).setCellValue("Total Expenses");
        Cell totalExpenseCell = totalExpenseRow.createCell(2);
        totalExpenseCell.setCellValue(data.getTotalExpenses() != null ? data.getTotalExpenses().doubleValue() : 0.0);
        totalExpenseCell.setCellStyle(boldNumberStyle);

        // Net Income
        Row netIncomeRow = sheet.createRow(rowNum++);
        netIncomeRow.createCell(0).setCellValue("Net Income");
        Cell netIncomeCell = netIncomeRow.createCell(2);
        netIncomeCell.setCellValue(data.getNetIncome() != null ? data.getNetIncome().doubleValue() : 0.0);
        netIncomeCell.setCellStyle(boldNumberStyle);

        return rowNum;
    }

    private void populateDataRow(Row dataRow, IncomeExpenseReportRowDTO row, 
                               CellStyle dataStyle, CellStyle numberStyle) {
        dataRow.createCell(0).setCellValue(row.getCategory() != null ? row.getCategory() : "");
        dataRow.createCell(1).setCellValue(row.getDescription() != null ? row.getDescription() : "");
        
        Cell currentMonthCell = dataRow.createCell(2);
        currentMonthCell.setCellValue(row.getCurrentMonth() != null ? row.getCurrentMonth().doubleValue() : 0.0);
        currentMonthCell.setCellStyle(numberStyle);
        
        Cell previousMonthCell = dataRow.createCell(3);
        previousMonthCell.setCellValue(row.getPreviousMonth() != null ? row.getPreviousMonth().doubleValue() : 0.0);
        previousMonthCell.setCellStyle(numberStyle);
        
        Cell ytdCell = dataRow.createCell(4);
        ytdCell.setCellValue(row.getYearToDate() != null ? row.getYearToDate().doubleValue() : 0.0);
        ytdCell.setCellStyle(numberStyle);
        
        Cell budgetYtdCell = dataRow.createCell(5);
        budgetYtdCell.setCellValue(row.getBudgetYtd() != null ? row.getBudgetYtd().doubleValue() : 0.0);
        budgetYtdCell.setCellStyle(numberStyle);
        
        Cell varianceCell = dataRow.createCell(6);
        varianceCell.setCellValue(row.getVariance() != null ? row.getVariance().doubleValue() : 0.0);
        varianceCell.setCellStyle(numberStyle);
        
        Cell fullBudgetCell = dataRow.createCell(7);
        fullBudgetCell.setCellValue(row.getFullYearBudget() != null ? row.getFullYearBudget().doubleValue() : 0.0);
        fullBudgetCell.setCellStyle(numberStyle);
    }

    // ========== Style Creation Methods ==========

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 16);
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
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
        font.setFontHeightInPoints((short) 14);
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
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
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return style;
    }

    private CellStyle createBoldNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return style;
    }
}