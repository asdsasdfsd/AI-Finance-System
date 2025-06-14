// backend/src/main/java/org/example/backend/application/service/ReportApplicationService.java
package org.example.backend.application.service;

import org.example.backend.application.dto.GenerateReportCommand;
import org.example.backend.application.dto.ReportDTO;
import org.example.backend.application.dto.ReportListQuery;
import org.example.backend.domain.aggregate.report.ReportAggregate;
import org.example.backend.domain.aggregate.report.ReportAggregateRepository;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.ReportType;
import org.example.backend.domain.valueobject.ReportStatus;
import org.example.backend.domain.event.ReportGenerationStartedEvent;
import org.example.backend.domain.event.DomainEventPublisher;
import org.example.backend.infrastructure.report.ReportGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Report Application Service - Enhanced DDD Implementation
 * 
 * Responsibilities:
 * 1. Orchestrate report generation business processes for all report types
 * 2. Coordinate between domain aggregates and infrastructure services
 * 3. Handle report lifecycle management for Balance Sheet, Income Statement, etc.
 * 4. Provide unified interface for all financial reports
 */
@Service
@Transactional
public class ReportApplicationService {
    
    @Autowired
    private ReportAggregateRepository reportRepository;
    
    @Autowired
    private ReportGenerationService reportGenerationService;
    
    @Autowired
    private DomainEventPublisher domainEventPublisher;
    
    // Enhanced data query services for all report types
    @Autowired
    private IncomeStatementDataService incomeStatementDataService;
    
    @Autowired
    private FinancialGroupingDataService financialGroupingDataService;
    
    @Autowired
    private BalanceSheetDataService balanceSheetDataService;
    
    @Autowired
    private IncomeExpenseDataService incomeExpenseDataService;
    
    /**
     * Generate a new financial report - supports all four report types
     */

    public String generateReport(GenerateReportCommand command) {
    validateGenerateReportCommand(command);
    
    TenantId tenantId = TenantId.of(command.getTenantId());
    
    // 修改重复检查逻辑 - 只检查正在生成的报表，允许重新生成已完成的报表
    if (reportRepository.existsGeneratingReport(tenantId, command.getReportType(), 
                                              command.getStartDate(), command.getEndDate())) {
        throw new IllegalArgumentException(
            "A report with the same parameters is currently being generated. Please wait for it to complete.");
    }
    
    // 可选：删除已存在的相同报表（如果需要覆盖）
    List<ReportAggregate> existingReports = reportRepository.findByMultipleCriteria(
        tenantId, command.getReportType(), ReportStatus.COMPLETED, 
        command.getStartDate(), command.getEndDate());
    
    if (!existingReports.isEmpty()) {
        // 可以选择删除旧报表或者给报表加上时间戳后缀
        for (ReportAggregate existingReport : existingReports) {
            if (existingReport.getFilePath() != null) {
                reportGenerationService.deleteReportFile(existingReport.getFilePath());
            }
            reportRepository.delete(existingReport);
        }
    }
    
    // 创建新的报表聚合
    ReportAggregate report = ReportAggregate.create(
        command.getReportType(),
        command.getReportName() + " - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")),
        command.getStartDate(),
        command.getEndDate(),
        tenantId,
        command.getCreatedBy()
    );
    
    // 启用AI分析（如果请求）
    if (Boolean.TRUE.equals(command.getAiAnalysisEnabled())) {
        report.enableAIAnalysis();
    }
    
    // 保存报表
    ReportAggregate savedReport = reportRepository.save(report);
    
    // 发布生成开始事件
    domainEventPublisher.publish(new ReportGenerationStartedEvent(
        savedReport.getReportId(),
        savedReport.getReportType(),
        savedReport.getTenantId().getValue(),
        savedReport.getCreatedBy()
    ));
    
    // 开始异步报表生成
    generateReportAsync(savedReport);
    
    return savedReport.getReportId().toString();
} 
    /**
     * Get report details by ID
     */
    @Transactional(readOnly = true)
    public Optional<ReportDTO> getReport(Integer reportId, Integer tenantId) {
        TenantId tenant = TenantId.of(tenantId);
        
        return reportRepository.findByIdAndTenant(reportId, tenant)
                .map(this::convertToDTO);
    }
    
    /**
     * Get reports list with filtering and pagination
     */
    @Transactional(readOnly = true)
    public List<ReportDTO> getReports(ReportListQuery query) {
        TenantId tenantId = TenantId.of(query.getTenantId());
        
        List<ReportAggregate> reports;
        
        if (query.hasFilters()) {
            reports = reportRepository.findByMultipleCriteria(
                tenantId,
                query.getReportType(),
                query.getStatus(),
                query.getStartDate(),
                query.getEndDate()
            );
        } else {
            reports = reportRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
        }
        
        // Apply pagination if specified
        if (query.getPageSize() != null && query.getPageNumber() != null) {
            int start = query.getPageNumber() * query.getPageSize();
            int end = Math.min(start + query.getPageSize(), reports.size());
            reports = reports.subList(start, end);
        }
        
        return reports.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get recent reports for tenant
     */
    @Transactional(readOnly = true)
    public List<ReportDTO> getRecentReports(Integer tenantId, int limit) {
        TenantId tenant = TenantId.of(tenantId);
        LocalDateTime since = LocalDateTime.now().minusDays(30); // Last 30 days
        
        List<ReportAggregate> reports = reportRepository.findByTenantAndCreatedSince(tenant, since);
        
        return reports.stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get reports by type
     */
    @Transactional(readOnly = true)
    public List<ReportDTO> getReportsByType(Integer tenantId, ReportType reportType) {
        TenantId tenant = TenantId.of(tenantId);
        
        return reportRepository.findByTenantIdAndReportTypeOrderByCreatedAtDesc(tenant, reportType)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Archive a completed report
     */
    public void archiveReport(Integer reportId, Integer tenantId) {
        TenantId tenant = TenantId.of(tenantId);
        
        ReportAggregate report = reportRepository.findByIdAndTenant(reportId, tenant)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        
        report.archive();
        reportRepository.save(report);
    }
    
    /**
     * Delete a failed or cancelled report
     */
    public void deleteReport(Integer reportId, Integer tenantId) {
        TenantId tenant = TenantId.of(tenantId);
        
        ReportAggregate report = reportRepository.findByIdAndTenant(reportId, tenant)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        
        if (report.getStatus() == ReportStatus.GENERATING) {
            throw new IllegalStateException("Cannot delete report while generating");
        }
        
        // Delete file if exists
        if (report.getFilePath() != null) {
            reportGenerationService.deleteReportFile(report.getFilePath());
        }
        
        reportRepository.delete(report);
    }
    
    /**
     * Get report generation statistics
     */
    @Transactional(readOnly = true)
    public ReportStatistics getReportStatistics(Integer tenantId) {
        TenantId tenant = TenantId.of(tenantId);
        
        long totalReports = reportRepository.countByTenantAndStatus(tenant, null);
        long completedReports = reportRepository.countByTenantAndStatus(tenant, ReportStatus.COMPLETED);
        long failedReports = reportRepository.countByTenantAndStatus(tenant, ReportStatus.FAILED);
        long generatingReports = reportRepository.countByTenantAndStatus(tenant, ReportStatus.GENERATING);
        
        return ReportStatistics.builder()
                .totalReports(totalReports)
                .completedReports(completedReports)
                .failedReports(failedReports)
                .generatingReports(generatingReports)
                .totalFileSize(reportRepository.getTotalFileSizeByTenant(tenant))
                .build();
    }
    
    // ========== Private Helper Methods ==========
    
    /**
     * Generate report asynchronously - Enhanced to support all report types
     */
    private void generateReportAsync(ReportAggregate report) {
        // In a real implementation, this would use @Async or a message queue
        try {
            String filePath = null;
            Long fileSize = null;
            
            // Generate different report types using DDD services
            switch (report.getReportType()) {
                case INCOME_STATEMENT:
                    var incomeData = incomeStatementDataService.getIncomeStatementData(
                        report.getTenantId(), 
                        report.getStartDate(), 
                        report.getEndDate()
                    );
                    filePath = reportGenerationService.generateIncomeStatement(incomeData, report.getTenantId().getValue());
                    break;
                    
                case FINANCIAL_GROUPING:
                    var groupingData = financialGroupingDataService.getFinancialGroupingData(
                        report.getTenantId(), 
                        report.getStartDate(), 
                        report.getEndDate()
                    );
                    filePath = reportGenerationService.generateFinancialGrouping(groupingData, report.getTenantId().getValue());
                    break;
                    
                case BALANCE_SHEET:
                    var balanceSheetData = balanceSheetDataService.generateBalanceSheet(
                        report.getTenantId().getValue(),
                        report.getEndDate()
                    );
                    filePath = reportGenerationService.generateBalanceSheet(balanceSheetData, report.getTenantId().getValue());
                    break;
                    
                case INCOME_EXPENSE:
                    var incomeExpenseData = incomeExpenseDataService.generateIncomeExpenseReport(
                        report.getTenantId(),
                        report.getEndDate()
                    );
                    filePath = reportGenerationService.generateIncomeExpense(incomeExpenseData, report.getTenantId().getValue());
                    break;
                    
                default:
                    throw new UnsupportedOperationException("Report type not supported: " + report.getReportType());
            }
            
            // Get file size
            fileSize = reportGenerationService.getFileSize(filePath);
            
            // Update report as completed
            report.completeGeneration(filePath, fileSize);
            
            // Prepare AI analysis data if enabled
            if (Boolean.TRUE.equals(report.getAiAnalysisEnabled())) {
                String analysisData = prepareAIAnalysisData(report);
                report.prepareForAIAnalysis(analysisData);
            }
            
            reportRepository.save(report);
            
        } catch (Exception e) {
            // Mark report as failed
            report.failGeneration("Report generation failed: " + e.getMessage());
            reportRepository.save(report);
            
            System.err.println("Failed to generate report " + report.getReportId() + ": " + e.getMessage());
        }
    }
    
    /**
     * Check if similar report already exists
     */
    private boolean isDuplicateReport(TenantId tenantId, GenerateReportCommand command) {
        return reportRepository.existsSimilarReport(
            tenantId,
            command.getReportType(),
            command.getStartDate(),
            command.getEndDate()
        );
    }
    
    /**
     * Prepare data for AI analysis
     */
    private String prepareAIAnalysisData(ReportAggregate report) {
        // This would prepare structured data for AI analysis
        return String.format("""
            {
                "reportId": %d,
                "reportType": "%s",
                "period": {
                    "startDate": "%s",
                    "endDate": "%s"
                },
                "companyId": %d,
                "filePath": "%s",
                "analysisEnabled": true
            }
            """, 
            report.getReportId(),
            report.getReportType(),
            report.getStartDate(),
            report.getEndDate(),
            report.getTenantId().getValue(),
            report.getFilePath()
        );
    }
    
    /**
     * Convert aggregate to DTO
     */
    private ReportDTO convertToDTO(ReportAggregate report) {
        return ReportDTO.builder()
                .reportId(report.getReportId())
                .reportName(report.getReportName())
                .reportType(report.getReportType())
                .status(report.getStatus())
                .startDate(report.getStartDate())
                .endDate(report.getEndDate())
                .filePath(report.getFilePath())
                .fileFormat(report.getFileFormat())
                .fileSize(report.getFileSize())
                .fileSizeFormatted(report.getFileSizeFormatted())
                .aiAnalysisEnabled(report.getAiAnalysisEnabled())
                .aiAnalysisStatus(report.getAiAnalysisStatus())
                .createdBy(report.getCreatedBy())
                .createdAt(report.getCreatedAt())
                .completedAt(report.getCompletedAt())
                .updatedAt(report.getUpdatedAt())
                .errorMessage(report.getErrorMessage())
                .periodDescription(report.getPeriodDescription())
                .build();
    }
    
    /**
     * Validate generate report command
     */
    private void validateGenerateReportCommand(GenerateReportCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Generate report command cannot be null");
        }
        
        if (command.getTenantId() == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }
        
        if (command.getReportType() == null) {
            throw new IllegalArgumentException("Report type cannot be null");
        }
        
        if (command.getReportName() == null || command.getReportName().trim().isEmpty()) {
            throw new IllegalArgumentException("Report name cannot be empty");
        }
        
        if (command.getStartDate() == null || command.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        
        if (command.getStartDate().isAfter(command.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        if (command.getCreatedBy() == null) {
            throw new IllegalArgumentException("Creator user ID cannot be null");
        }
    }
    
    /**
     * Report Statistics Inner Class
     */
    public static class ReportStatistics {
        private final long totalReports;
        private final long completedReports;
        private final long failedReports;
        private final long generatingReports;
        private final long totalFileSize;
        
        private ReportStatistics(Builder builder) {
            this.totalReports = builder.totalReports;
            this.completedReports = builder.completedReports;
            this.failedReports = builder.failedReports;
            this.generatingReports = builder.generatingReports;
            this.totalFileSize = builder.totalFileSize;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        // Getters
        public long getTotalReports() { return totalReports; }
        public long getCompletedReports() { return completedReports; }
        public long getFailedReports() { return failedReports; }
        public long getGeneratingReports() { return generatingReports; }
        public long getTotalFileSize() { return totalFileSize; }
        
        public double getSuccessRate() {
            return totalReports > 0 ? (double) completedReports / totalReports * 100 : 0;
        }
        
        public String getTotalFileSizeFormatted() {
            if (totalFileSize < 1024) return totalFileSize + " B";
            if (totalFileSize < 1024 * 1024) return String.format("%.1f KB", totalFileSize / 1024.0);
            return String.format("%.1f MB", totalFileSize / (1024.0 * 1024.0));
        }
        
        public static class Builder {
            private long totalReports;
            private long completedReports;
            private long failedReports;
            private long generatingReports;
            private long totalFileSize;
            
            public Builder totalReports(long totalReports) {
                this.totalReports = totalReports;
                return this;
            }
            
            public Builder completedReports(long completedReports) {
                this.completedReports = completedReports;
                return this;
            }
            
            public Builder failedReports(long failedReports) {
                this.failedReports = failedReports;
                return this;
            }
            
            public Builder generatingReports(long generatingReports) {
                this.generatingReports = generatingReports;
                return this;
            }
            
            public Builder totalFileSize(long totalFileSize) {
                this.totalFileSize = totalFileSize;
                return this;
            }
            
            public ReportStatistics build() {
                return new ReportStatistics(this);
            }
        }
    }
}