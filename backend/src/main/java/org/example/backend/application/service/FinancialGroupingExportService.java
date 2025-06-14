// backend/src/main/java/org/example/backend/application/service/FinancialGroupingExportService.java
package org.example.backend.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.backend.application.dto.FinancialGroupingData;
import org.example.backend.application.dto.CompanyDTO;
import org.example.backend.domain.valueobject.TenantId;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Financial Grouping Export Service
 * Handles Excel export for financial grouping reports
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialGroupingExportService {
    
    private final FinancialGroupingDataService financialGroupingDataService;
    private final CompanyApplicationService companyApplicationService; // FIXED: Added to get company name
    
    /**
     * Generate Excel file for financial grouping data
     * @param data Financial grouping data
     * @return Excel file as byte array
     */
    public byte[] generateExcel(FinancialGroupingData data, String companyName) {
        log.info("Generating Excel for financial grouping data for company: {}", companyName);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            
            // Create multiple worksheets for different groupings
            createCategoryGroupingSheet(workbook, data, companyName);
            createDepartmentGroupingSheet(workbook, data, companyName);
            createFundGroupingSheet(workbook, data, companyName);
            createTransactionTypeGroupingSheet(workbook, data, companyName);
            createMonthlyGroupingSheet(workbook, data, companyName);
            createSummarySheet(workbook, data, companyName);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            log.error("Failed to generate financial grouping Excel export", e);
            throw new RuntimeException("Failed to generate financial grouping Excel export", e);
        }
    }
    
    /**
     * Export financial grouping report to Excel format using tenant and date range
     */
    public byte[] exportFinancialGrouping(TenantId tenantId, LocalDate startDate, LocalDate endDate) {
        try {
            // Get financial grouping data using DDD service
            FinancialGroupingData reportData = financialGroupingDataService
                    .getFinancialGroupingDataByTenant(tenantId, startDate, endDate);
            
            // Get company name
            CompanyDTO company = companyApplicationService.getCompanyById(tenantId.getValue());
            String companyName = company != null ? company.getCompanyName() : "Unknown Company";
            
            // Generate Excel
            return generateExcel(reportData, companyName);
            
        } catch (Exception e) {
            log.error("Failed to export financial grouping report for tenant {}: {}", tenantId.getValue(), e.getMessage());
            throw new RuntimeException("Failed to export financial grouping report", e);
        }
    }
    
    // ========== Sheet Creation Methods ==========
    
    private void createCategoryGroupingSheet(Workbook workbook, FinancialGroupingData data, String companyName) {
        Sheet sheet = workbook.createSheet("By Category");
        
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle numberStyle = createNumberStyle(workbook);
        CellStyle sectionStyle = createSectionStyle(workbook);
        
        int rowNum = 0;
        
        // Title
        rowNum = createSheetTitle(sheet, "Financial Grouping by Category", data, companyName, titleStyle, rowNum);
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Category", "Transaction Count", "Total Amount", "Average Amount"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data rows - FIXED: Use correct method name
        if (data.getByCategory() != null) {
            for (Map.Entry<String, FinancialGroupingData.CategoryGrouping> entry : data.getByCategory().entrySet()) {
                Row dataRow = sheet.createRow(rowNum++);
                FinancialGroupingData.CategoryGrouping grouping = entry.getValue();
                
                dataRow.createCell(0).setCellValue(entry.getKey());
                dataRow.createCell(1).setCellValue(grouping.getTransactionCount());
                
                Cell amountCell = dataRow.createCell(2);
                amountCell.setCellValue(grouping.getTotalAmount().doubleValue());
                amountCell.setCellStyle(numberStyle);
                
                Cell avgCell = dataRow.createCell(3);
                avgCell.setCellValue(grouping.getAverageAmount().doubleValue());
                avgCell.setCellStyle(numberStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createDepartmentGroupingSheet(Workbook workbook, FinancialGroupingData data, String companyName) {
        Sheet sheet = workbook.createSheet("By Department");
        
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle numberStyle = createNumberStyle(workbook);
        
        int rowNum = 0;
        
        // Title
        rowNum = createSheetTitle(sheet, "Financial Grouping by Department", data, companyName, titleStyle, rowNum);
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Department", "Transaction Count", "Total Amount", "Average Amount"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data rows - FIXED: Use correct method name  
        if (data.getByDepartment() != null) {
            for (Map.Entry<String, FinancialGroupingData.DepartmentGrouping> entry : data.getByDepartment().entrySet()) {
                Row dataRow = sheet.createRow(rowNum++);
                FinancialGroupingData.DepartmentGrouping grouping = entry.getValue();
                
                dataRow.createCell(0).setCellValue(entry.getKey());
                dataRow.createCell(1).setCellValue(grouping.getTransactionCount());
                
                Cell amountCell = dataRow.createCell(2);
                amountCell.setCellValue(grouping.getTotalAmount().doubleValue());
                amountCell.setCellStyle(numberStyle);
                
                Cell avgCell = dataRow.createCell(3);
                avgCell.setCellValue(grouping.getAverageAmount().doubleValue());
                avgCell.setCellStyle(numberStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createFundGroupingSheet(Workbook workbook, FinancialGroupingData data, String companyName) {
        Sheet sheet = workbook.createSheet("By Fund");
        
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle numberStyle = createNumberStyle(workbook);
        
        int rowNum = 0;
        
        // Title
        rowNum = createSheetTitle(sheet, "Financial Grouping by Fund", data, companyName, titleStyle, rowNum);
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Fund", "Transaction Count", "Total Amount", "Average Amount"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data rows - FIXED: Use correct method name
        if (data.getByFund() != null) {
            for (Map.Entry<String, FinancialGroupingData.FundGrouping> entry : data.getByFund().entrySet()) {
                Row dataRow = sheet.createRow(rowNum++);
                FinancialGroupingData.FundGrouping grouping = entry.getValue();
                
                dataRow.createCell(0).setCellValue(entry.getKey());
                dataRow.createCell(1).setCellValue(grouping.getTransactionCount());
                
                Cell amountCell = dataRow.createCell(2);
                amountCell.setCellValue(grouping.getTotalAmount().doubleValue());
                amountCell.setCellStyle(numberStyle);
                
                Cell avgCell = dataRow.createCell(3);
                avgCell.setCellValue(grouping.getAverageAmount().doubleValue());
                avgCell.setCellStyle(numberStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createTransactionTypeGroupingSheet(Workbook workbook, FinancialGroupingData data, String companyName) {
        Sheet sheet = workbook.createSheet("By Transaction Type");
        
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle numberStyle = createNumberStyle(workbook);
        
        int rowNum = 0;
        
        // Title
        rowNum = createSheetTitle(sheet, "Financial Grouping by Transaction Type", data, companyName, titleStyle, rowNum);
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Transaction Type", "Transaction Count", "Total Amount", "Average Amount"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data rows - FIXED: Use correct method name
        if (data.getByTransactionType() != null) {
            for (Map.Entry<String, FinancialGroupingData.TransactionTypeGrouping> entry : data.getByTransactionType().entrySet()) {
                Row dataRow = sheet.createRow(rowNum++);
                FinancialGroupingData.TransactionTypeGrouping grouping = entry.getValue();
                
                dataRow.createCell(0).setCellValue(entry.getKey());
                dataRow.createCell(1).setCellValue(grouping.getTransactionCount());
                
                Cell amountCell = dataRow.createCell(2);
                amountCell.setCellValue(grouping.getTotalAmount().doubleValue());
                amountCell.setCellStyle(numberStyle);
                
                Cell avgCell = dataRow.createCell(3);
                avgCell.setCellValue(grouping.getAverageAmount().doubleValue());
                avgCell.setCellStyle(numberStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createMonthlyGroupingSheet(Workbook workbook, FinancialGroupingData data, String companyName) {
        Sheet sheet = workbook.createSheet("By Month");
        
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle numberStyle = createNumberStyle(workbook);
        
        int rowNum = 0;
        
        // Title
        rowNum = createSheetTitle(sheet, "Financial Grouping by Month", data, companyName, titleStyle, rowNum);
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Month", "Transaction Count", "Total Amount", "Average Amount"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data rows - FIXED: Use correct method name
        if (data.getByMonth() != null) {
            for (Map.Entry<String, FinancialGroupingData.MonthGrouping> entry : data.getByMonth().entrySet()) {
                Row dataRow = sheet.createRow(rowNum++);
                FinancialGroupingData.MonthGrouping grouping = entry.getValue();
                
                dataRow.createCell(0).setCellValue(entry.getKey());
                dataRow.createCell(1).setCellValue(grouping.getTransactionCount());
                
                Cell amountCell = dataRow.createCell(2);
                amountCell.setCellValue(grouping.getTotalAmount().doubleValue());
                amountCell.setCellStyle(numberStyle);
                
                Cell avgCell = dataRow.createCell(3);
                avgCell.setCellValue(grouping.getAverageAmount().doubleValue());
                avgCell.setCellStyle(numberStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createSummarySheet(Workbook workbook, FinancialGroupingData data, String companyName) {
        Sheet sheet = workbook.createSheet("Summary");
        
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle numberStyle = createNumberStyle(workbook);
        CellStyle boldNumberStyle = createBoldNumberStyle(workbook);
        
        int rowNum = 0;
        
        // Title
        rowNum = createSheetTitle(sheet, "Financial Grouping Summary", data, companyName, titleStyle, rowNum);
        
        // Overall totals - FIXED: Use correct method names
        Row totalRow = sheet.createRow(rowNum++);
        totalRow.createCell(0).setCellValue("Total Transactions:");
        totalRow.createCell(1).setCellValue(data.getTotalTransactionCount());
        
        Row amountRow = sheet.createRow(rowNum++);
        amountRow.createCell(0).setCellValue("Total Amount:");
        Cell totalAmountCell = amountRow.createCell(1);
        totalAmountCell.setCellValue(data.getGrandTotal().doubleValue());
        totalAmountCell.setCellStyle(boldNumberStyle);
        
        Row avgRow = sheet.createRow(rowNum++);
        avgRow.createCell(0).setCellValue("Average Transaction:");
        Cell avgAmountCell = avgRow.createCell(1);
        // Calculate average from grand total and transaction count
        BigDecimal avgAmount = data.getTotalTransactionCount() > 0 ? 
            data.getGrandTotal().divide(BigDecimal.valueOf(data.getTotalTransactionCount()), 2, BigDecimal.ROUND_HALF_UP) : 
            BigDecimal.ZERO;
        avgAmountCell.setCellValue(avgAmount.doubleValue());
        avgAmountCell.setCellStyle(numberStyle);
        
        // Date range
        rowNum++; // Blank row
        Row startDateRow = sheet.createRow(rowNum++);
        startDateRow.createCell(0).setCellValue("Period Start:");
        startDateRow.createCell(1).setCellValue(data.getStartDate().toString());
        
        Row endDateRow = sheet.createRow(rowNum++);
        endDateRow.createCell(0).setCellValue("Period End:");
        endDateRow.createCell(1).setCellValue(data.getEndDate().toString());
        
        // Auto-size columns
        for (int i = 0; i < 2; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    // ========== Helper Methods ==========
    
    private int createSheetTitle(Sheet sheet, String title, FinancialGroupingData data, String companyName, CellStyle titleStyle, int rowNum) {
        // Company name
        Row companyRow = sheet.createRow(rowNum++);
        Cell companyCell = companyRow.createCell(0);
        companyCell.setCellValue(companyName);
        companyCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 3));

        // Report title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 3));

        // Date range
        Row dateRow = sheet.createRow(rowNum++);
        Cell dateCell = dateRow.createCell(0);
        dateCell.setCellValue("Period: " + data.getStartDate() + " to " + data.getEndDate());
        dateCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 3));

        return rowNum + 1; // Add blank row
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