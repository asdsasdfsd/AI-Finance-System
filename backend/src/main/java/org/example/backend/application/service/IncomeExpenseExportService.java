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
import java.time.LocalDate;
import java.util.List;

/**
 * Income Expense Export Service
 * Handles Excel export for income vs expense reports
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IncomeExpenseExportService {
    
    private final IncomeExpenseDataService incomeExpenseDataService;
    
    /**
     * Generate Excel file for income expense data
     * @param data Income expense report data
     * @return Excel file as byte array
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
    
    /**
     * Export income expense report to Excel format using tenant and date
     */
    public byte[] exportIncomeExpense(TenantId tenantId, LocalDate asOfDate) {
        try {
            // Get income expense data using DDD service
            IncomeExpenseReportData reportData = incomeExpenseDataService
                    .generateIncomeExpenseReportByTenant(tenantId, asOfDate);
            
            // Generate Excel
            return generateExcel(reportData);
            
        } catch (Exception e) {
            log.error("Failed to export income expense report for tenant {}: {}", tenantId.getValue(), e.getMessage());
            throw new RuntimeException("Failed to export income expense report", e);
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

        // Column headers - FIXED: Using correct field names
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Category", "Description", "Current Month", "Previous Month", "Year to Date", "Budget YTD", "Variance", "Variance %"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Income data - FIXED: Using correct field names from DTO
        for (IncomeExpenseReportRowDTO row : data.getIncomeRows()) {
            Row dataRow = sheet.createRow(rowNum++);
            
            dataRow.createCell(0).setCellValue(row.getCategory());
            dataRow.createCell(1).setCellValue(row.getDescription());
            
            // FIXED: Use correct field names
            Cell currentMonthCell = dataRow.createCell(2);
            currentMonthCell.setCellValue(row.getCurrentMonth().doubleValue());
            currentMonthCell.setCellStyle(numberStyle);
            
            Cell previousMonthCell = dataRow.createCell(3);
            previousMonthCell.setCellValue(row.getPreviousMonth() != null ? row.getPreviousMonth().doubleValue() : 0.0);
            previousMonthCell.setCellStyle(numberStyle);
            
            Cell ytdCell = dataRow.createCell(4);
            ytdCell.setCellValue(row.getYearToDate().doubleValue());
            ytdCell.setCellStyle(numberStyle);
            
            Cell budgetCell = dataRow.createCell(5);
            budgetCell.setCellValue(row.getBudgetYtd() != null ? row.getBudgetYtd().doubleValue() : 0.0);
            budgetCell.setCellStyle(numberStyle);
            
            Cell varianceCell = dataRow.createCell(6);
            varianceCell.setCellValue(row.getVariance() != null ? row.getVariance().doubleValue() : 0.0);
            varianceCell.setCellStyle(numberStyle);
            
            Cell percentCell = dataRow.createCell(7);
            percentCell.setCellValue(row.getVariancePercentage() != null ? row.getVariancePercentage().doubleValue() : 0.0);
            percentCell.setCellStyle(numberStyle);
        }

        // Income totals
        Row totalRow = sheet.createRow(rowNum++);
        totalRow.createCell(0).setCellValue("TOTAL INCOME");
        
        Cell totalMonthCell = totalRow.createCell(2);
        totalMonthCell.setCellValue(data.getTotalIncomeMonth().doubleValue());
        totalMonthCell.setCellStyle(numberStyle);
        
        Cell totalYtdCell = totalRow.createCell(4);
        totalYtdCell.setCellValue(data.getTotalIncomeYTD().doubleValue());
        totalYtdCell.setCellStyle(numberStyle);

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

        // Column headers - FIXED: Using correct field names
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Category", "Description", "Current Month", "Previous Month", "Year to Date", "Budget YTD", "Variance", "Variance %"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Expense data - FIXED: Using correct field names from DTO
        for (IncomeExpenseReportRowDTO row : data.getExpenseRows()) {
            Row dataRow = sheet.createRow(rowNum++);
            
            dataRow.createCell(0).setCellValue(row.getCategory());
            dataRow.createCell(1).setCellValue(row.getDescription());
            
            // FIXED: Use correct field names
            Cell currentMonthCell = dataRow.createCell(2);
            currentMonthCell.setCellValue(row.getCurrentMonth().doubleValue());
            currentMonthCell.setCellStyle(numberStyle);
            
            Cell previousMonthCell = dataRow.createCell(3);
            previousMonthCell.setCellValue(row.getPreviousMonth() != null ? row.getPreviousMonth().doubleValue() : 0.0);
            previousMonthCell.setCellStyle(numberStyle);
            
            Cell ytdCell = dataRow.createCell(4);
            ytdCell.setCellValue(row.getYearToDate().doubleValue());
            ytdCell.setCellStyle(numberStyle);
            
            Cell budgetCell = dataRow.createCell(5);
            budgetCell.setCellValue(row.getBudgetYtd() != null ? row.getBudgetYtd().doubleValue() : 0.0);
            budgetCell.setCellStyle(numberStyle);
            
            Cell varianceCell = dataRow.createCell(6);
            varianceCell.setCellValue(row.getVariance() != null ? row.getVariance().doubleValue() : 0.0);
            varianceCell.setCellStyle(numberStyle);
            
            Cell percentCell = dataRow.createCell(7);
            percentCell.setCellValue(row.getVariancePercentage() != null ? row.getVariancePercentage().doubleValue() : 0.0);
            percentCell.setCellStyle(numberStyle);
        }

        // Expense totals
        Row totalRow = sheet.createRow(rowNum++);
        totalRow.createCell(0).setCellValue("TOTAL EXPENSES");
        
        Cell totalMonthCell = totalRow.createCell(2);
        totalMonthCell.setCellValue(data.getTotalExpenseMonth().doubleValue());
        totalMonthCell.setCellStyle(numberStyle);
        
        Cell totalYtdCell = totalRow.createCell(4);
        totalYtdCell.setCellValue(data.getTotalExpenseYTD().doubleValue());
        totalYtdCell.setCellStyle(numberStyle);

        return rowNum + 1; // Add blank row
    }

    private int createSummarySection(Sheet sheet, IncomeExpenseReportData data, 
                                   CellStyle boldNumberStyle, int rowNum) {
        // Net income summary
        Row netIncomeRow = sheet.createRow(rowNum++);
        netIncomeRow.createCell(0).setCellValue("NET INCOME");
        
        Cell netMonthCell = netIncomeRow.createCell(2);
        netMonthCell.setCellValue(data.getNetIncomeMonth().doubleValue());
        netMonthCell.setCellStyle(boldNumberStyle);
        
        Cell netYtdCell = netIncomeRow.createCell(4);
        netYtdCell.setCellValue(data.getNetIncomeYTD().doubleValue());
        netYtdCell.setCellStyle(boldNumberStyle);

        return rowNum;
    }

    // ========== Style Creation Methods ==========

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
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createBoldNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        return style;
    }
}