// backend/src/main/java/org/example/backend/infrastructure/report/IncomeStatementExcelGenerator.java
package org.example.backend.infrastructure.report;

import org.example.backend.application.dto.IncomeStatementData;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Income Statement Excel Generator
 * 
 * Generates Excel format income statement reports
 */
@Component
public class IncomeStatementExcelGenerator {
    
    @Autowired
    private ReportFileManager fileManager;
    
    @Autowired
    private ExcelStyleManager styleManager;
    
    public String generateIncomeStatement(IncomeStatementData data, String fileName) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Income Statement");
            
            // Create styles
            CellStyle titleStyle = styleManager.createTitleStyle(workbook);
            CellStyle headerStyle = styleManager.createHeaderStyle(workbook);
            CellStyle numberStyle = styleManager.createNumberStyle(workbook);
            CellStyle boldNumberStyle = styleManager.createBoldNumberStyle(workbook);
            CellStyle sectionStyle = styleManager.createSectionStyle(workbook);
            
            int rowNum = 0;
            
            // Title
            rowNum = createTitle(sheet, "Income Statement", data.getPeriodDescription(), titleStyle, rowNum);
            
            // Revenue Section
            rowNum = createRevenueSection(sheet, data, headerStyle, numberStyle, sectionStyle, rowNum);
            
            // Expense Sections
            rowNum = createExpensesSections(sheet, data, headerStyle, numberStyle, sectionStyle, rowNum);
            
            // Net Profit
            rowNum = createNetProfitSection(sheet, data, boldNumberStyle, rowNum);
            
            // Auto-size columns
            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Save file
            String filePath = fileManager.saveWorkbook(workbook, fileName);
            return filePath;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate income statement: " + e.getMessage(), e);
        }
    }
    
    private int createTitle(Sheet sheet, String title, String period, CellStyle titleStyle, int rowNum) {
        // Main title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));
        
        // Period
        Row periodRow = sheet.createRow(rowNum++);
        periodRow.createCell(0).setCellValue(period);
        
        rowNum++; // Empty row
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Item");
        headerRow.createCell(1).setCellValue("Amount");
        headerRow.createCell(2).setCellValue("Notes");
        
        return rowNum;
    }
    
    private int createRevenueSection(Sheet sheet, IncomeStatementData data, 
                                   CellStyle headerStyle, CellStyle numberStyle, 
                                   CellStyle sectionStyle, int rowNum) {
        // Section header
        Row sectionRow = sheet.createRow(rowNum++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("I. Operating Revenue");
        sectionCell.setCellStyle(sectionStyle);
        
        // Revenue items
        for (IncomeStatementData.RevenueItem item : data.getRevenues()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("  " + item.getName());
            Cell amountCell = row.createCell(1);
            amountCell.setCellValue(item.getAmount().doubleValue());
            amountCell.setCellStyle(numberStyle);
        }
        
        // Total revenue
        Row totalRow = sheet.createRow(rowNum++);
        totalRow.createCell(0).setCellValue("Total Operating Revenue");
        Cell totalCell = totalRow.createCell(1);
        totalCell.setCellValue(data.getTotalRevenue().doubleValue());
        totalCell.setCellStyle(numberStyle);
        
        rowNum++; // Empty row
        return rowNum;
    }
    
    private int createExpensesSections(Sheet sheet, IncomeStatementData data,
                                     CellStyle headerStyle, CellStyle numberStyle,
                                     CellStyle sectionStyle, int rowNum) {
        
        // Operating Expenses
        rowNum = createExpenseSection(sheet, "II. Operating Expenses", 
                                    data.getOperatingExpenses(), 
                                    data.getTotalOperatingExpenses(),
                                    sectionStyle, numberStyle, rowNum);
        
        // Administrative Expenses
        rowNum = createExpenseSection(sheet, "III. Administrative Expenses", 
                                    data.getAdministrativeExpenses(), 
                                    data.getTotalAdministrativeExpenses(),
                                    sectionStyle, numberStyle, rowNum);
        
        // Financial Expenses
        rowNum = createExpenseSection(sheet, "IV. Financial Expenses", 
                                    data.getFinancialExpenses(), 
                                    data.getTotalFinancialExpenses(),
                                    sectionStyle, numberStyle, rowNum);
        
        // Other Income
        if (!data.getOtherIncomes().isEmpty()) {
            rowNum = createOtherIncomeSection(sheet, "V. Other Income", 
                                            data.getOtherIncomes(), 
                                            data.getTotalOtherIncomes(),
                                            sectionStyle, numberStyle, rowNum);
        }
        
        // Other Expenses
        if (!data.getOtherExpenses().isEmpty()) {
            rowNum = createExpenseSection(sheet, "VI. Other Expenses", 
                                        data.getOtherExpenses(), 
                                        data.getTotalOtherExpenses(),
                                        sectionStyle, numberStyle, rowNum);
        }
        
        // Total Expenses
        Row totalExpenseRow = sheet.createRow(rowNum++);
        totalExpenseRow.createCell(0).setCellValue("Total Expenses");
        Cell totalExpenseCell = totalExpenseRow.createCell(1);
        totalExpenseCell.setCellValue(data.getTotalExpenses().doubleValue());
        totalExpenseCell.setCellStyle(numberStyle);
        
        rowNum++; // Empty row
        return rowNum;
    }
    
    private int createExpenseSection(Sheet sheet, String sectionTitle, 
                                   java.util.List<IncomeStatementData.ExpenseItem> expenses,
                                   BigDecimal total, CellStyle sectionStyle, 
                                   CellStyle numberStyle, int rowNum) {
        
        // Section header
        Row sectionRow = sheet.createRow(rowNum++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue(sectionTitle);
        sectionCell.setCellStyle(sectionStyle);
        
        // Expense items
        for (IncomeStatementData.ExpenseItem item : expenses) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("  " + item.getName());
            Cell amountCell = row.createCell(1);
            amountCell.setCellValue(item.getAmount().doubleValue());
            amountCell.setCellStyle(numberStyle);
        }
        
        // Section total
        Row totalRow = sheet.createRow(rowNum++);
        totalRow.createCell(0).setCellValue("Subtotal");
        Cell totalCell = totalRow.createCell(1);
        totalCell.setCellValue(total.doubleValue());
        totalCell.setCellStyle(numberStyle);
        
        rowNum++; // Empty row
        return rowNum;
    }
    
    private int createOtherIncomeSection(Sheet sheet, String sectionTitle, 
                                       java.util.List<IncomeStatementData.IncomeItem> incomes,
                                       BigDecimal total, CellStyle sectionStyle, 
                                       CellStyle numberStyle, int rowNum) {
        
        // Section header
        Row sectionRow = sheet.createRow(rowNum++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue(sectionTitle);
        sectionCell.setCellStyle(sectionStyle);
        
        // Income items
        for (IncomeStatementData.IncomeItem item : incomes) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("  " + item.getName());
            Cell amountCell = row.createCell(1);
            amountCell.setCellValue(item.getAmount().doubleValue());
            amountCell.setCellStyle(numberStyle);
        }
        
        // Section total
        Row totalRow = sheet.createRow(rowNum++);
        totalRow.createCell(0).setCellValue("Subtotal");
        Cell totalCell = totalRow.createCell(1);
        totalCell.setCellValue(total.doubleValue());
        totalCell.setCellStyle(numberStyle);
        
        rowNum++; // Empty row
        return rowNum;
    }
    
    private int createNetProfitSection(Sheet sheet, IncomeStatementData data, 
                                     CellStyle boldNumberStyle, int rowNum) {
        
        Row netProfitRow = sheet.createRow(rowNum++);
        netProfitRow.createCell(0).setCellValue("NET PROFIT");
        Cell netProfitCell = netProfitRow.createCell(1);
        netProfitCell.setCellValue(data.getNetProfit().doubleValue());
        netProfitCell.setCellStyle(boldNumberStyle);
        
        return rowNum;
    }
}

