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
 * Report Generation Service - Unified for All Report Types
 * 
 * Responsibilities:
 * 1. Coordinate report generation across different formats and types
 * 2. Manage file storage and paths for all financial reports
 * 3. Handle report file lifecycle for Balance Sheet, Income Statement, etc.
 * 4. Provide unified generation interface for all report types
 * 
 * Note: Removed AI functionality - reports are now purely data-driven
 */
@Service
public class ReportGenerationService {
    
    // Note: Excel generators are now replaced by Export services for unified format
    
    // Export services for enhanced format
    @Autowired
    private IncomeExpenseExportService incomeExpenseExportService;
    
    @Autowired  
    private BalanceSheetExportService balanceSheetExportService;
    
    @Autowired
    private IncomeStatementExportService incomeStatementExportService;
    
    @Autowired
    private FinancialGroupingExportService financialGroupingExportService;
    
    @Autowired
    private ReportFileManager fileManager;
    
    @Value("${app.reports.storage.path:./reports}")
    private String reportsStoragePath;
    
    /**
     * Generate Income Statement Excel report
     */
    public String generateIncomeStatement(IncomeStatementData data, Integer tenantId) {
        try {
            String fileName = generateFileName("income_statement", tenantId, 
                                             data.getStartDate().toString(), 
                                             data.getEndDate().toString());
            
            // Use existing export service to generate Excel with enhanced format
            byte[] excelData = incomeStatementExportService.generateExcel(data);
            
            // Save to file system
            String filePath = saveExcelToFile(excelData, fileName);
            
            return filePath;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate income statement report: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate Financial Grouping Excel report
     */
    public String generateFinancialGrouping(FinancialGroupingData data, Integer tenantId) {
        try {
            String fileName = generateFileName("financial_grouping", tenantId,
                                             data.getStartDate().toString(),
                                             data.getEndDate().toString());
            
            // Use existing export service to generate Excel with enhanced format
            // Note: Need to get company name from a service since FinancialGroupingData doesn't have it
            String companyName = getCompanyName(tenantId);
            byte[] excelData = financialGroupingExportService.generateExcel(data, companyName);
            
            // Save to file system
            String filePath = saveExcelToFile(excelData, fileName);
            
            return filePath;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate financial grouping report: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate Balance Sheet Excel report
     */
    public String generateBalanceSheet(BalanceSheetDetailedResponse data, Integer tenantId) {
        try {
            String fileName = generateFileName("balance_sheet", tenantId,
                                             data.getAsOfDate().toString(),
                                             data.getAsOfDate().toString());
            
            // Use existing export service to generate Excel with enhanced format
            byte[] excelData = balanceSheetExportService.generateExcel(data);
            
            // Save to file system
            String filePath = saveExcelToFile(excelData, fileName);
            
            return filePath;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate balance sheet report: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate Income Expense Excel report
     */
    public String generateIncomeExpense(IncomeExpenseReportData data, Integer tenantId) {
        try {
            String fileName = generateFileName("income_expense", tenantId,
                                             data.getAsOfDate().toString(),
                                             data.getAsOfDate().toString());
            
            // Use existing export service to generate Excel with enhanced format
            byte[] excelData = incomeExpenseExportService.generateExcel(data);
            
            // Save to file system
            String filePath = saveExcelToFile(excelData, fileName);
            
            return filePath;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate income expense report: " + e.getMessage(), e);
        }
    }
    
    /**
     * Helper method to save Excel data to file
     */
    private String saveExcelToFile(byte[] excelData, String fileName) throws IOException {
        String filePath = reportsStoragePath + "/" + fileName;
        
        // Ensure directory exists
        File directory = new File(reportsStoragePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        // Write Excel data to file
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            fileOut.write(excelData);
        }
        
        return filePath;
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
    
    
    // Additional dependencies for company info
    @Autowired
    private CompanyRepository companyRepository;
    
    /**
     * Helper method to get company name by tenant ID
     */
    private String getCompanyName(Integer tenantId) {
        try {
            return companyRepository.findById(tenantId)
                    .map(company -> company.getCompanyName())
                    .orElse("Unknown Company");
        } catch (Exception e) {
            return "Unknown Company";
        }
    }

    /**
     * Get reports storage path
     */
    public String getReportsStoragePath() {
        return reportsStoragePath;
    }
}