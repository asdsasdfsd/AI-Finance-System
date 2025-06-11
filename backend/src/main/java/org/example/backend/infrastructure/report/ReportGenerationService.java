// backend/src/main/java/org/example/backend/infrastructure/report/ReportGenerationService.java
package org.example.backend.infrastructure.report;

import org.example.backend.application.dto.IncomeStatementData;
import org.example.backend.application.dto.FinancialGroupingData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Report Generation Service
 * 
 * Responsibilities:
 * 1. Coordinate report generation across different formats
 * 2. Manage file storage and paths
 * 3. Handle report file lifecycle
 */
@Service
public class ReportGenerationService {
    
    @Autowired
    private IncomeStatementExcelGenerator incomeStatementGenerator;
    
    @Autowired
    private FinancialGroupingExcelGenerator financialGroupingGenerator;
    
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
}

