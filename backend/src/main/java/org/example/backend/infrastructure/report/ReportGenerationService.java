// backend/src/main/java/org/example/backend/infrastructure/report/ReportGenerationService.java
package org.example.backend.infrastructure.report;

import org.example.backend.application.dto.IncomeStatementData;
import org.example.backend.application.dto.FinancialGroupingData;
import org.example.backend.application.dto.BalanceSheetDetailedResponse;
import org.example.backend.application.dto.IncomeExpenseReportRowDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Report Generation Service - Enhanced for All Report Types
 * 
 * Responsibilities:
 * 1. Coordinate report generation across different formats and types
 * 2. Manage file storage and paths for all financial reports
 * 3. Handle report file lifecycle for Balance Sheet, Income Statement, etc.
 * 4. Provide unified generation interface for all report types
 */
@Service
public class ReportGenerationService {
    
    @Autowired
    private IncomeStatementExcelGenerator incomeStatementGenerator;
    
    @Autowired
    private FinancialGroupingExcelGenerator financialGroupingGenerator;
    
    @Autowired
    private BalanceSheetExcelGenerator balanceSheetGenerator;
    
    @Autowired
    private IncomeExpenseExcelGenerator incomeExpenseGenerator;
    
    @Autowired
    private ReportFileManager fileManager;
    
    @Value("${app.reports.storage.path:./reports}")
    private String reportsStoragePath;
    
    /**
     * Generate Income Statement Excel report
     */
    public String generateIncomeStatement(IncomeStatementData data, Integer tenantId) {
        String fileName = generateFileName("income_statement", tenantId, 
                                         data.getStartDate().toString(), 
                                         data.getEndDate().toString());
        
        return incomeStatementGenerator.generateIncomeStatement(data, fileName);
    }
    
    /**
     * Generate Financial Grouping Excel report
     */
    public String generateFinancialGrouping(FinancialGroupingData data, Integer tenantId) {
        String fileName = generateFileName("financial_grouping", tenantId,
                                         data.getStartDate().toString(),
                                         data.getEndDate().toString());
        
        return financialGroupingGenerator.generateFinancialGrouping(data, fileName);
    }
    
    /**
     * Generate Balance Sheet Excel report
     */
    public String generateBalanceSheet(BalanceSheetDetailedResponse data, Integer tenantId) {
        String fileName = generateFileName("balance_sheet", tenantId,
                                         data.getAsOfDate().toString(),
                                         data.getAsOfDate().toString());
        
        return balanceSheetGenerator.generateBalanceSheet(data, fileName);
    }
    
    /**
     * Generate Income Expense Excel report
     */
    public String generateIncomeExpense(List<IncomeExpenseReportRowDTO> data, Integer tenantId) {
        String fileName = generateFileName("income_expense", tenantId,
                                         java.time.LocalDate.now().toString(),
                                         java.time.LocalDate.now().toString());
        
        return incomeExpenseGenerator.generateIncomeExpense(data, fileName);
    }
    
    /**
     * Get file size for given file path
     */
    public Long getFileSize(String filePath) {
        return fileManager.getFileSize(filePath);
    }
    
    /**
     * Delete report file
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
    
    /**
     * Generate unique file name for report
     */
    private String generateFileName(String reportType, Integer tenantId, String startDate, String endDate) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_%d_%s_to_%s_%s.xlsx", 
                           reportType, tenantId, 
                           startDate.replace("-", ""), 
                           endDate.replace("-", ""), 
                           timestamp);
    }
    
    /**
     * Generate report file name with custom pattern
     */
    public String generateCustomFileName(String reportType, Integer tenantId, String dateSuffix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_%d_%s_%s.xlsx", reportType, tenantId, dateSuffix, timestamp);
    }
    
    /**
     * Get reports storage path
     */
    public String getReportsStoragePath() {
        return reportsStoragePath;
    }
}