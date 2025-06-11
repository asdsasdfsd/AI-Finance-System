// backend/src/main/java/org/example/backend/infrastructure/report/FinancialGroupingExcelGenerator.java
package org.example.backend.infrastructure.report;

import org.example.backend.application.dto.FinancialGroupingData;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Financial Grouping Excel Generator
 * 
 * Generates Excel format financial grouping reports with multiple worksheets
 */
@Component
public class FinancialGroupingExcelGenerator {
    
    @Autowired
    private ReportFileManager fileManager;
    
    @Autowired
    private ExcelStyleManager styleManager;
    
    public String generateFinancialGrouping(FinancialGroupingData data, String fileName) {
        try (Workbook workbook = new XSSFWorkbook()) {
            
            // Create multiple worksheets for different groupings
            createCategoryGroupingSheet(workbook, data);
            createDepartmentGroupingSheet(workbook, data);
            createFundGroupingSheet(workbook, data);
            createTransactionTypeGroupingSheet(workbook, data);
            createMonthlyGroupingSheet(workbook, data);
            createSummarySheet(workbook, data);
            
            // Save file
            String filePath = fileManager.saveWorkbook(workbook, fileName);
            return filePath;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate financial grouping report: " + e.getMessage(), e);
        }
    }
    
    private void createCategoryGroupingSheet(Workbook workbook, FinancialGroupingData data) {
        Sheet sheet = workbook.createSheet("By Category");
        
        CellStyle titleStyle = styleManager.createTitleStyle(workbook);
        CellStyle headerStyle = styleManager.createHeaderStyle(workbook);
        CellStyle numberStyle = styleManager.createNumberStyle(workbook);
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Financial Grouping by Category");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 4));
        
        // Period
        Row periodRow = sheet.createRow(rowNum++);
        periodRow.createCell(0).setCellValue(data.getPeriodDescription());
        
        rowNum++; // Empty row
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Category");
        headerRow.createCell(1).setCellValue("Transaction Count");
        headerRow.createCell(2).setCellValue("Total Amount");
        headerRow.createCell(3).setCellValue("Average Amount");
        headerRow.createCell(4).setCellValue("Percentage");
        
        // Apply header style
        for (int i = 0; i < 5; i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }
        
        BigDecimal grandTotal = data.getGrandTotal();
        
        // Data rows
        for (FinancialGroupingData.CategoryGrouping grouping : data.getByCategory().values()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(grouping.getCategoryName());
            
            Cell countCell = row.createCell(1);
            countCell.setCellValue(grouping.getTransactionCount());
            
            Cell totalCell = row.createCell(2);
            totalCell.setCellValue(grouping.getTotalAmount().doubleValue());
            totalCell.setCellStyle(numberStyle);
            
            Cell avgCell = row.createCell(3);
            avgCell.setCellValue(grouping.getAverageAmount().doubleValue());
            avgCell.setCellStyle(numberStyle);
            
            Cell percentCell = row.createCell(4);
            percentCell.setCellValue(String.format("%.2f%%", grouping.getPercentage(grandTotal)));
        }
        
        // Totals row
        Row totalRow = sheet.createRow(rowNum++);
        totalRow.createCell(0).setCellValue("TOTAL");
        totalRow.createCell(1).setCellValue(data.getTotalTransactionCount());
        Cell grandTotalCell = totalRow.createCell(2);
        grandTotalCell.setCellValue(grandTotal.doubleValue());
        grandTotalCell.setCellStyle(numberStyle);
        totalRow.createCell(4).setCellValue("100.00%");
        
        // Auto-size columns
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createDepartmentGroupingSheet(Workbook workbook, FinancialGroupingData data) {
        Sheet sheet = workbook.createSheet("By Department");
        
        CellStyle titleStyle = styleManager.createTitleStyle(workbook);
        CellStyle headerStyle = styleManager.createHeaderStyle(workbook);
        CellStyle numberStyle = styleManager.createNumberStyle(workbook);
        
        int rowNum = 0;
        
        // Title and headers
        rowNum = createStandardHeader(sheet, "Financial Grouping by Department", 
                                    data.getPeriodDescription(), titleStyle, rowNum);
        
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Department");
        headerRow.createCell(1).setCellValue("Transaction Count");
        headerRow.createCell(2).setCellValue("Total Amount");
        headerRow.createCell(3).setCellValue("Average Amount");
        
        // Apply header style
        for (int i = 0; i < 4; i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }
        
        // Data rows
        for (FinancialGroupingData.DepartmentGrouping grouping : data.getByDepartment().values()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(grouping.getDepartmentName());
            row.createCell(1).setCellValue(grouping.getTransactionCount());
            
            Cell totalCell = row.createCell(2);
            totalCell.setCellValue(grouping.getTotalAmount().doubleValue());
            totalCell.setCellStyle(numberStyle);
            
            Cell avgCell = row.createCell(3);
            avgCell.setCellValue(grouping.getAverageAmount().doubleValue());
            avgCell.setCellStyle(numberStyle);
        }
        
        // Auto-size columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createFundGroupingSheet(Workbook workbook, FinancialGroupingData data) {
        Sheet sheet = workbook.createSheet("By Fund");
        
        CellStyle titleStyle = styleManager.createTitleStyle(workbook);
        CellStyle headerStyle = styleManager.createHeaderStyle(workbook);
        CellStyle numberStyle = styleManager.createNumberStyle(workbook);
        
        int rowNum = 0;
        
        // Title and headers
        rowNum = createStandardHeader(sheet, "Financial Grouping by Fund", 
                                    data.getPeriodDescription(), titleStyle, rowNum);
        
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Fund");
        headerRow.createCell(1).setCellValue("Transaction Count");
        headerRow.createCell(2).setCellValue("Total Amount");
        headerRow.createCell(3).setCellValue("Average Amount");
        
        // Apply header style
        for (int i = 0; i < 4; i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }
        
        // Data rows
        for (FinancialGroupingData.FundGrouping grouping : data.getByFund().values()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(grouping.getFundName());
            row.createCell(1).setCellValue(grouping.getTransactionCount());
            
            Cell totalCell = row.createCell(2);
            totalCell.setCellValue(grouping.getTotalAmount().doubleValue());
            totalCell.setCellStyle(numberStyle);
            
            Cell avgCell = row.createCell(3);
            avgCell.setCellValue(grouping.getAverageAmount().doubleValue());
            avgCell.setCellStyle(numberStyle);
        }
        
        // Auto-size columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createTransactionTypeGroupingSheet(Workbook workbook, FinancialGroupingData data) {
        Sheet sheet = workbook.createSheet("By Transaction Type");
        
        CellStyle titleStyle = styleManager.createTitleStyle(workbook);
        CellStyle headerStyle = styleManager.createHeaderStyle(workbook);
        CellStyle numberStyle = styleManager.createNumberStyle(workbook);
        
        int rowNum = 0;
        
        // Title and headers
        rowNum = createStandardHeader(sheet, "Financial Grouping by Transaction Type", 
                                    data.getPeriodDescription(), titleStyle, rowNum);
        
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Transaction Type");
        headerRow.createCell(1).setCellValue("Transaction Count");
        headerRow.createCell(2).setCellValue("Total Amount");
        headerRow.createCell(3).setCellValue("Average Amount");
        
        // Apply header style
        for (int i = 0; i < 4; i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }
        
        // Data rows
        for (FinancialGroupingData.TransactionTypeGrouping grouping : data.getByTransactionType().values()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(grouping.getTypeName());
            row.createCell(1).setCellValue(grouping.getTransactionCount());
            
            Cell totalCell = row.createCell(2);
            totalCell.setCellValue(grouping.getTotalAmount().doubleValue());
            totalCell.setCellStyle(numberStyle);
            
            Cell avgCell = row.createCell(3);
            avgCell.setCellValue(grouping.getAverageAmount().doubleValue());
            avgCell.setCellStyle(numberStyle);
        }
        
        // Auto-size columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createMonthlyGroupingSheet(Workbook workbook, FinancialGroupingData data) {
        Sheet sheet = workbook.createSheet("By Month");
        
        CellStyle titleStyle = styleManager.createTitleStyle(workbook);
        CellStyle headerStyle = styleManager.createHeaderStyle(workbook);
        CellStyle numberStyle = styleManager.createNumberStyle(workbook);
        
        int rowNum = 0;
        
        // Title and headers
        rowNum = createStandardHeader(sheet, "Financial Grouping by Month", 
                                    data.getPeriodDescription(), titleStyle, rowNum);
        
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Month");
        headerRow.createCell(1).setCellValue("Transaction Count");
        headerRow.createCell(2).setCellValue("Total Amount");
        headerRow.createCell(3).setCellValue("Average Amount");
        
        // Apply header style
        for (int i = 0; i < 4; i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }
        
        // Data rows (sorted by month)
        data.getByMonth().values().stream()
            .sorted((a, b) -> a.getFirstDayOfMonth().compareTo(b.getFirstDayOfMonth()))
            .forEach(grouping -> {
                Row row = sheet.createRow(sheet.getLastRowNum() + 1);
                row.createCell(0).setCellValue(grouping.getDisplayName());
                row.createCell(1).setCellValue(grouping.getTransactionCount());
                
                Cell totalCell = row.createCell(2);
                totalCell.setCellValue(grouping.getTotalAmount().doubleValue());
                totalCell.setCellStyle(numberStyle);
                
                Cell avgCell = row.createCell(3);
                avgCell.setCellValue(grouping.getAverageAmount().doubleValue());
                avgCell.setCellStyle(numberStyle);
            });
        
        // Auto-size columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createSummarySheet(Workbook workbook, FinancialGroupingData data) {
        Sheet sheet = workbook.createSheet("Summary");
        
        CellStyle titleStyle = styleManager.createTitleStyle(workbook);
        CellStyle sectionStyle = styleManager.createSectionStyle(workbook);
        CellStyle numberStyle = styleManager.createNumberStyle(workbook);
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Financial Grouping Report Summary");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 3));
        
        // Period
        Row periodRow = sheet.createRow(rowNum++);
        periodRow.createCell(0).setCellValue(data.getPeriodDescription());
        
        rowNum++; // Empty row
        
        // Summary statistics
        Row grandTotalRow = sheet.createRow(rowNum++);
        grandTotalRow.createCell(0).setCellValue("Grand Total Amount:");
        Cell grandTotalCell = grandTotalRow.createCell(1);
        grandTotalCell.setCellValue(data.getGrandTotal().doubleValue());
        grandTotalCell.setCellStyle(numberStyle);
        
        Row totalCountRow = sheet.createRow(rowNum++);
        totalCountRow.createCell(0).setCellValue("Total Transaction Count:");
        totalCountRow.createCell(1).setCellValue(data.getTotalTransactionCount());
        
        Row categoryCountRow = sheet.createRow(rowNum++);
        categoryCountRow.createCell(0).setCellValue("Number of Categories:");
        categoryCountRow.createCell(1).setCellValue(data.getByCategory().size());
        
        Row departmentCountRow = sheet.createRow(rowNum++);
        departmentCountRow.createCell(0).setCellValue("Number of Departments:");
        departmentCountRow.createCell(1).setCellValue(data.getByDepartment().size());
        
        Row fundCountRow = sheet.createRow(rowNum++);
        fundCountRow.createCell(0).setCellValue("Number of Funds:");
        fundCountRow.createCell(1).setCellValue(data.getByFund().size());
        
        // Auto-size columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private int createStandardHeader(Sheet sheet, String title, String period, CellStyle titleStyle, int rowNum) {
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 3));
        
        // Period
        Row periodRow = sheet.createRow(rowNum++);
        periodRow.createCell(0).setCellValue(period);
        
        rowNum++; // Empty row
        
        return rowNum;
    }
}