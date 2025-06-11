// backend/src/main/java/org/example/backend/infrastructure/report/IncomeExpenseExcelGenerator.java
package org.example.backend.infrastructure.report;

import org.example.backend.application.dto.IncomeExpenseReportRowDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Income Expense Excel Generator - DDD Implementation
 * 
 * Generates Excel format income vs expense reports using DDD data
 */
@Component
public class IncomeExpenseExcelGenerator {
    
    @Autowired
    private ReportFileManager fileManager;
    
    @Autowired
    private ExcelStyleManager styleManager;
    
    public String generateIncomeExpense(List<IncomeExpenseReportRowDTO> data, String fileName) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Income vs Expense Report");
            
            // Create styles
            CellStyle titleStyle = styleManager.createTitleStyle(workbook);
            CellStyle headerStyle = styleManager.createHeaderStyle(workbook);
            CellStyle numberStyle = styleManager.createNumberStyle(workbook);
            CellStyle boldNumberStyle = styleManager.createBoldNumberStyle(workbook);
            CellStyle sectionStyle = styleManager.createSectionStyle(workbook);
            
            int rowNum = 0;
            
            // Title
            rowNum = createTitle(sheet, "Income vs Expense Report", titleStyle, rowNum);
            
            // Income Section
            rowNum = createIncomeSection(sheet, data, headerStyle, numberStyle, sectionStyle, rowNum);
            
            // Expense Section
            rowNum = createExpenseSection(sheet, data, headerStyle, numberStyle, sectionStyle, rowNum);
            
            // Summary Section
            rowNum = createSummarySection(sheet, data, boldNumberStyle, rowNum);
            
            // Auto-size columns
            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Save file
            String filePath = fileManager.saveWorkbook(workbook, fileName);
            return filePath;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate income expense report: " + e.getMessage(), e);
        }
    }
    
    private int createTitle(Sheet sheet, String title, CellStyle titleStyle, int rowNum) {
        // Main title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 7));
        
        rowNum++; // Empty row
        
        // Column headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {
            "Category", "Item", "Current Month", "Previous Month", 
            "Year To-Date", "Budget YTD", "Variance", "Full Year Budget"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styleManager.createHeaderStyle(sheet.getWorkbook()));
        }
        
        return rowNum;
    }
    
    private int createIncomeSection(Sheet sheet, List<IncomeExpenseReportRowDTO> data, 
                                  CellStyle headerStyle, CellStyle numberStyle, 
                                  CellStyle sectionStyle, int rowNum) {
        
        // Section header
        Row sectionRow = sheet.createRow(rowNum++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("INCOME");
        sectionCell.setCellStyle(sectionStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 7));
        
        // Filter income data
        List<IncomeExpenseReportRowDTO> incomeData = data.stream()
                .filter(row -> "INCOME".equals(row.getType()))
                .collect(Collectors.toList());
        
        // Group by category
        Map<String, List<IncomeExpenseReportRowDTO>> incomeByCategory = incomeData.stream()
                .collect(Collectors.groupingBy(IncomeExpenseReportRowDTO::getCategory));
        
        BigDecimal totalIncome = BigDecimal.ZERO;
        
        for (Map.Entry<String, List<IncomeExpenseReportRowDTO>> entry : incomeByCategory.entrySet()) {
            // Category header
            Row catRow = sheet.createRow(rowNum++);
            catRow.createCell(0).setCellValue("  " + entry.getKey());
            
            BigDecimal categoryTotal = BigDecimal.ZERO;
            
            // Items in category
            for (IncomeExpenseReportRowDTO item : entry.getValue()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("    " + item.getDescription());
                
                createDataCells(row, item, numberStyle);
                categoryTotal = categoryTotal.add(item.getYearToDate());
            }
            
            totalIncome = totalIncome.add(categoryTotal);
        }
        
        // Total income
        Row totalRow = sheet.createRow(rowNum++);
        totalRow.createCell(0).setCellValue("TOTAL INCOME");
        Cell totalCell = totalRow.createCell(4); // YTD column
        totalCell.setCellValue(totalIncome.doubleValue());
        totalCell.setCellStyle(numberStyle);
        
        rowNum++; // Empty row
        return rowNum;
    }
    
    private int createExpenseSection(Sheet sheet, List<IncomeExpenseReportRowDTO> data, 
                                   CellStyle headerStyle, CellStyle numberStyle, 
                                   CellStyle sectionStyle, int rowNum) {
        
        // Section header
        Row sectionRow = sheet.createRow(rowNum++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("EXPENSES");
        sectionCell.setCellStyle(sectionStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 7));
        
        // Filter expense data
        List<IncomeExpenseReportRowDTO> expenseData = data.stream()
                .filter(row -> "EXPENSE".equals(row.getType()))
                .collect(Collectors.toList());
        
        // Group by category
        Map<String, List<IncomeExpenseReportRowDTO>> expenseByCategory = expenseData.stream()
                .collect(Collectors.groupingBy(IncomeExpenseReportRowDTO::getCategory));
        
        BigDecimal totalExpenses = BigDecimal.ZERO;
        
        for (Map.Entry<String, List<IncomeExpenseReportRowDTO>> entry : expenseByCategory.entrySet()) {
            // Category header
            Row catRow = sheet.createRow(rowNum++);
            catRow.createCell(0).setCellValue("  " + entry.getKey());
            
            BigDecimal categoryTotal = BigDecimal.ZERO;
            
            // Items in category
            for (IncomeExpenseReportRowDTO item : entry.getValue()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("    " + item.getDescription());
                
                createDataCells(row, item, numberStyle);
                categoryTotal = categoryTotal.add(item.getYearToDate());
            }
            
            totalExpenses = totalExpenses.add(categoryTotal);
        }
        
        // Total expenses
        Row totalRow = sheet.createRow(rowNum++);
        totalRow.createCell(0).setCellValue("TOTAL EXPENSES");
        Cell totalCell = totalRow.createCell(4); // YTD column
        totalCell.setCellValue(totalExpenses.doubleValue());
        totalCell.setCellStyle(numberStyle);
        
        rowNum++; // Empty row
        return rowNum;
    }
    
    private int createSummarySection(Sheet sheet, List<IncomeExpenseReportRowDTO> data, 
                                   CellStyle boldNumberStyle, int rowNum) {
        
        // Calculate totals
        BigDecimal totalIncome = data.stream()
                .filter(row -> "INCOME".equals(row.getType()))
                .map(IncomeExpenseReportRowDTO::getYearToDate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalExpenses = data.stream()
                .filter(row -> "EXPENSE".equals(row.getType()))
                .map(IncomeExpenseReportRowDTO::getYearToDate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal netIncome = totalIncome.subtract(totalExpenses);
        
        // Summary section
        Row summaryHeaderRow = sheet.createRow(rowNum++);
        summaryHeaderRow.createCell(0).setCellValue("SUMMARY");
        
        Row totalIncomeRow = sheet.createRow(rowNum++);
        totalIncomeRow.createCell(0).setCellValue("Total Income (YTD):");
        Cell totalIncomeCell = totalIncomeRow.createCell(1);
        totalIncomeCell.setCellValue(totalIncome.doubleValue());
        totalIncomeCell.setCellStyle(boldNumberStyle);
        
        Row totalExpenseRow = sheet.createRow(rowNum++);
        totalExpenseRow.createCell(0).setCellValue("Total Expenses (YTD):");
        Cell totalExpenseCell = totalExpenseRow.createCell(1);
        totalExpenseCell.setCellValue(totalExpenses.doubleValue());
        totalExpenseCell.setCellStyle(boldNumberStyle);
        
        Row netIncomeRow = sheet.createRow(rowNum++);
        netIncomeRow.createCell(0).setCellValue("Net Income (YTD):");
        Cell netIncomeCell = netIncomeRow.createCell(1);
        netIncomeCell.setCellValue(netIncome.doubleValue());
        netIncomeCell.setCellStyle(boldNumberStyle);
        
        // Profit margin calculation
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            Row marginRow = sheet.createRow(rowNum++);
            marginRow.createCell(0).setCellValue("Profit Margin:");
            Cell marginCell = marginRow.createCell(1);
            BigDecimal margin = netIncome.divide(totalIncome, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            marginCell.setCellValue(margin.doubleValue() + "%");
        }
        
        return rowNum;
    }
    
    private void createDataCells(Row row, IncomeExpenseReportRowDTO item, CellStyle numberStyle) {
        // Current Month
        Cell currentMonthCell = row.createCell(2);
        currentMonthCell.setCellValue(item.getCurrentMonth().doubleValue());
        currentMonthCell.setCellStyle(numberStyle);
        
        // Previous Month
        Cell prevMonthCell = row.createCell(3);
        prevMonthCell.setCellValue(item.getPreviousMonth().doubleValue());
        prevMonthCell.setCellStyle(numberStyle);
        
        // Year To Date
        Cell ytdCell = row.createCell(4);
        ytdCell.setCellValue(item.getYearToDate().doubleValue());
        ytdCell.setCellStyle(numberStyle);
        
        // Budget YTD
        Cell budgetYtdCell = row.createCell(5);
        budgetYtdCell.setCellValue(item.getBudgetYtd().doubleValue());
        budgetYtdCell.setCellStyle(numberStyle);
        
        // Variance
        Cell varianceCell = row.createCell(6);
        varianceCell.setCellValue(item.getVariance().doubleValue());
        varianceCell.setCellStyle(numberStyle);
        
        // Full Year Budget
        Cell fullBudgetCell = row.createCell(7);
        fullBudgetCell.setCellValue(item.getFullYearBudget().doubleValue());
        fullBudgetCell.setCellStyle(numberStyle);
    }
}