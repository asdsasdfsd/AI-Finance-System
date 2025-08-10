// backend/src/main/java/org/example/backend/application/service/ReportApplicationService.java
package org.example.backend.application.service;

import org.example.backend.application.dto.GenerateReportCommand;
import org.example.backend.application.dto.ReportDTO;
import org.example.backend.application.dto.ReportListQuery;
import org.example.backend.application.dto.IncomeExpenseReportData;
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
import org.springframework.scheduling.annotation.Async;

import lombok.extern.slf4j.Slf4j;

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
 * 
 * Note: Removed AI analysis functionality - focuses purely on report generation
 */
@Slf4j
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
     * Removed AI analysis functionality
     */
    public String generateReport(GenerateReportCommand command) {
        validateGenerateReportCommand(command);
        
        TenantId tenantId = TenantId.of(command.getTenantId());
        
        // Check for currently generating reports
        if (reportRepository.existsGeneratingReport(tenantId, command.getReportType(), 
                                                  command.getStartDate(), command.getEndDate())) {
            throw new IllegalArgumentException(
                "A report with the same parameters is currently being generated. Please wait for it to complete.");
        }
        
        // Delete existing completed reports (if any)
        List<ReportAggregate> existingReports = reportRepository.findByMultipleCriteria(
            tenantId, command.getReportType(), ReportStatus.COMPLETED, 
            command.getStartDate(), command.getEndDate());
        
        if (!existingReports.isEmpty()) {
            for (ReportAggregate existingReport : existingReports) {
                if (existingReport.getFilePath() != null) {
                    reportGenerationService.deleteReportFile(existingReport.getFilePath());
                }
                reportRepository.delete(existingReport);
            }
        }
        
        // Create new report aggregate - without AI analysis
        ReportAggregate report = ReportAggregate.create(
            command.getReportType(),
            command.getReportName() + " - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")),
            command.getStartDate(),
            command.getEndDate(),
            tenantId,
            command.getCreatedBy()
        );
        
        // Note: Removed AI analysis enablement
        
        // Save report
        ReportAggregate savedReport = reportRepository.save(report);
        
        // Publish generation started event
        domainEventPublisher.publish(new ReportGenerationStartedEvent(
            savedReport.getReportId(),
            savedReport.getReportType(),
            savedReport.getTenantId().getValue(),
            savedReport.getCreatedBy()
        ));
        
        // Start async report generation
        generateReportAsync(savedReport);
        
        return savedReport.getReportId().toString();
    } 
    
    /**
     * Async report generation - removed AI processing
     */
    @Async
    private void generateReportAsync(ReportAggregate report) {
        try {
            String filePath = null;
            Long fileSize = null;
            String contentData = null;
            String contentFormat = "JSON";
            
            // Generate reports based on type - using correct non-deprecated methods
            switch (report.getReportType()) {
                case INCOME_STATEMENT:
                    var incomeData = incomeStatementDataService.getIncomeStatementDataByTenant(
                        report.getTenantId(), 
                        report.getStartDate(), 
                        report.getEndDate()
                    );
                    filePath = reportGenerationService.generateIncomeStatement(incomeData, report.getTenantId().getValue());
                    contentData = "{\"reportType\": \"INCOME_STATEMENT\", \"generated\": true}";
                    break;
                    
                case BALANCE_SHEET:
                    var balanceData = balanceSheetDataService.generateBalanceSheetByTenant(
                        report.getTenantId(), 
                        report.getEndDate()
                    );
                    filePath = reportGenerationService.generateBalanceSheet(balanceData, report.getTenantId().getValue());
                    contentData = "{\"reportType\": \"BALANCE_SHEET\", \"generated\": true}";
                    break;
                    
                case INCOME_EXPENSE:
                    // FIXED: Use correct method and explicit type declaration
                    IncomeExpenseReportData expenseData = incomeExpenseDataService.generateIncomeExpenseReportByTenant(
                        report.getTenantId(), 
                        report.getEndDate()
                    );
                    filePath = reportGenerationService.generateIncomeExpense(expenseData, report.getTenantId().getValue());
                    contentData = "{\"reportType\": \"INCOME_EXPENSE\", \"generated\": true}";
                    break;
                    
                case FINANCIAL_GROUPING:
                    var groupingData = financialGroupingDataService.getFinancialGroupingDataByTenant(
                        report.getTenantId(), 
                        report.getStartDate(), 
                        report.getEndDate()
                    );
                    filePath = reportGenerationService.generateFinancialGrouping(groupingData, report.getTenantId().getValue());
                    contentData = "{\"reportType\": \"FINANCIAL_GROUPING\", \"generated\": true}";
                    break;
                    
                default:
                    throw new IllegalArgumentException("Unsupported report type: " + report.getReportType());
            }
            
            // Calculate file size
            if (filePath != null) {
                fileSize = reportGenerationService.getFileSize(filePath);
            }
            
            // Complete generation without AI processing
            report.completeGeneration(filePath, fileSize, contentData, contentFormat);
            
            reportRepository.save(report);
            
        } catch (Exception e) {
            log.error("Report generation failed for report {}: {}", report.getReportId(), e.getMessage(), e);
            
            report.failGeneration("Generation failed: " + e.getMessage());
            reportRepository.save(report);
        }
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
        
        return reports.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Archive a report
     */
    @Transactional
    public void archiveReport(Integer reportId, Integer tenantId) {
        TenantId tenant = TenantId.of(tenantId);
        
        ReportAggregate report = reportRepository.findByIdAndTenant(reportId, tenant)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        
        report.archive();
        reportRepository.save(report);
    }
    
    /**
     * Delete a report
     */
    @Transactional
    public void deleteReport(Integer reportId, Integer tenantId) {
        TenantId tenant = TenantId.of(tenantId);
        
        ReportAggregate report = reportRepository.findByIdAndTenant(reportId, tenant)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        
        // Delete physical file if exists
        if (report.getFilePath() != null) {
            reportGenerationService.deleteReportFile(report.getFilePath());
        }
        
        reportRepository.delete(report);
    }
    
    /**
     * Download a report
     */
    @Transactional(readOnly = true)
    public byte[] downloadReport(Integer reportId, Integer tenantId) {
        TenantId tenant = TenantId.of(tenantId);
        
        ReportAggregate report = reportRepository.findByIdAndTenant(reportId, tenant)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        
        if (report.getStatus() != ReportStatus.COMPLETED) {
            throw new IllegalStateException("Report is not ready for download");
        }
        
        // File download implementation would go here
        throw new UnsupportedOperationException("File download feature not yet implemented");
    }
    
    /**
     * Validate generate report command
     */
    private void validateGenerateReportCommand(GenerateReportCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Generate report command cannot be null");
        }
        if (command.getReportType() == null) {
            throw new IllegalArgumentException("Report type is required");
        }
        if (command.getReportName() == null || command.getReportName().trim().isEmpty()) {
            throw new IllegalArgumentException("Report name is required");
        }
        if (command.getTenantId() == null || command.getTenantId() <= 0) {
            throw new IllegalArgumentException("Valid tenant ID is required");
        }
        if (command.getStartDate() == null) {
            throw new IllegalArgumentException("Start date is required");
        }
        if (command.getEndDate() == null) {
            throw new IllegalArgumentException("End date is required");
        }
        if (command.getStartDate().isAfter(command.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }
    
    /**
     * Convert domain aggregate to DTO
     */
    private ReportDTO convertToDTO(ReportAggregate report) {
        return ReportDTO.builder()
                .reportId(report.getReportId())
                .reportType(report.getReportType())
                .reportName(report.getReportName())
                .startDate(report.getStartDate())
                .endDate(report.getEndDate())
                .status(report.getStatus())
                .filePath(report.getFilePath())
                .fileFormat(report.getFileFormat())
                .fileSize(report.getFileSize())
                .createdAt(report.getCreatedAt())
                .completedAt(report.getCompletedAt())
                .updatedAt(report.getUpdatedAt())
                .createdBy(report.getCreatedBy())
                .errorMessage(report.getErrorMessage())
                .periodDescription(report.getPeriodDescription())
                .aiAnalysisEnabled(false) // Always false since AI is removed
                .aiAnalysisStatus("DISABLED") // Always disabled
                .build();
    }
}