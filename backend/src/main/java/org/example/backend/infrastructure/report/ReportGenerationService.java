// backend/src/main/java/org/example/backend/infrastructure/report/ReportGenerationService.java
package org.example.backend.infrastructure.report;

import org.example.backend.application.dto.IncomeStatementData;
import org.example.backend.application.dto.FinancialGroupingData;
import org.example.backend.application.dto.BalanceSheetDetailedResponse;
import org.example.backend.application.dto.IncomeExpenseReportData;
import org.example.backend.application.service.IncomeExpenseExportService;
import org.example.backend.application.service.BalanceSheetExportService;
import org.example.backend.application.service.IncomeStatementExportService;
import org.example.backend.application.service.FinancialGroupingExportService;
import org.example.backend.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * FIXED Report Generation Service - Compatible with existing structure
 * 
 * FIXES:
 * 1. Uses existing Excel generators for unified formatting
 * 2. Fixes method signatures to match existing ReportApplicationService calls
 * 3. Adds missing deleteReportFile and getFileSize methods
 * 4. Maintains compatibility with existing DTO structures
 */
@Service
public class ReportGenerationService {
    
    // Keep existing Excel generators for unified formatting
    @Autowired
    private BalanceSheetExcelGenerator balanceSheetExcelGenerator;
    
    @Autowired
    private IncomeStatementExcelGenerator incomeStatementExcelGenerator;
    
    @Autowired
    private IncomeExpenseExcelGenerator incomeExpenseExcelGenerator;
    
    @Autowired
    private FinancialGroupingExcelGenerator financialGroupingExcelGenerator;
    
    @Autowired
    private ReportFileManager fileManager;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Value("${app.reports.storage.path:./reports}")
    private String reportsStoragePath;
    
    /**
     * FIXED: Generate Income Statement Excel report using existing generator
     */
    public String generateIncomeStatement(IncomeStatementData data, Integer tenantId) {
        try {
            String fileName = generateFileName("income_statement", tenantId, 
                                             data.getStartDate().toString(), 
                                             data.getEndDate().toString());
            
            // Use existing Excel generator for consistent format
            String filePath = incomeStatementExcelGenerator.generateIncomeStatement(data, fileName);
            
            return filePath;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate income statement report: " + e.getMessage(), e);
        }
    }
    
    /**
     * FIXED: Generate Financial Grouping Excel report using existing generator
     */
    public String generateFinancialGrouping(FinancialGroupingData data, Integer tenantId) {
        try {
            String fileName = generateFileName("financial_grouping", tenantId,
                                             data.getStartDate().toString(),
                                             data.getEndDate().toString());
            
            // Use existing Excel generator for consistent format
            String filePath = financialGroupingExcelGenerator.generateFinancialGrouping(data, fileName);
            
            return filePath;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate financial grouping report: " + e.getMessage(), e);
        }
    }
    
    /**
     * FIXED: Generate Balance Sheet Excel report using existing generator
     */
    public String generateBalanceSheet(BalanceSheetDetailedResponse data, Integer tenantId) {
        try {
            String fileName = generateFileName("balance_sheet", tenantId,
                                             data.getAsOfDate().toString(),
                                             data.getAsOfDate().toString());
            
            // Use existing Excel generator for consistent format
            String filePath = balanceSheetExcelGenerator.generateBalanceSheet(data, fileName);
            
            return filePath;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate balance sheet report: " + e.getMessage(), e);
        }
    }
    
    /**
     * FIXED: Generate Income Expense Excel report - corrected method signature
     * This matches the ReportApplicationService call
     */
    public String generateIncomeExpense(IncomeExpenseReportData data, Integer tenantId) {
        try {
            String fileName = generateFileName("income_expense", tenantId,
                                             data.getAsOfDate().toString(),
                                             data.getAsOfDate().toString());
            
            // Convert to format expected by existing generator
            java.util.List<org.example.backend.application.dto.IncomeExpenseReportRowDTO> allRows = 
                new java.util.ArrayList<>();
            
            // Add income rows
            if (data.getIncomeRows() != null) {
                allRows.addAll(data.getIncomeRows());
            }
            
            // Add expense rows  
            if (data.getExpenseRows() != null) {
                allRows.addAll(data.getExpenseRows());
            }
            
            // Use existing Excel generator for consistent format
            String filePath = incomeExpenseExcelGenerator.generateIncomeExpense(allRows, fileName);
            
            return filePath;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate income expense report: " + e.getMessage(), e);
        }
    }
    
    /**
     * FIXED: Added missing getFileSize method
     */
    public Long getFileSize(String filePath) {
        return fileManager.getFileSize(filePath);
    }
    
    /**
     * FIXED: Added missing deleteReportFile method
     */
    public boolean deleteReportFile(String filePath) {
        return fileManager.deleteFile(filePath);
    }
    
    /**
     * Check if file exists
     */
    public boolean fileExists(String filePath) {
        return fileManager.fileExists(filePath);
    }
    
    /**
     * Get file name from path
     */
    public String getFileName(String filePath) {
        return fileManager.getFileName(filePath);
    }
    
    // Helper methods
    
    private String generateFileName(String reportType, Integer tenantId, String startDate, String endDate) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String dateRange = endDate.equals(startDate) ? startDate : startDate + "_to_" + endDate;
        return String.format("%s_company_%d_%s_%s.xlsx", reportType, tenantId, dateRange, timestamp);
    }
    
    private String getCompanyName(Integer companyId) {
        return companyRepository.findById(companyId)
                .map(company -> company.getCompanyName()) // FIXED: Use correct getter method
                .orElse("Unknown Company");
    }
}