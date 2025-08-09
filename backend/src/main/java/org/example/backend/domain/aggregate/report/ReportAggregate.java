// backend/src/main/java/org/example/backend/domain/aggregate/report/ReportAggregate.java
package org.example.backend.domain.aggregate.report;

import org.example.backend.domain.valueobject.ReportContent;
import org.example.backend.domain.event.ReportGeneratedEvent;
import org.example.backend.domain.event.ReportContentStoredEvent;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.ReportType;
import org.example.backend.domain.valueobject.ReportStatus;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Report Aggregate Root - DDD Compliant
 * 
 * Responsibilities:
 * 1. Manage financial report lifecycle
 * 2. Control report generation and storage process  
 * 3. Encapsulate report content as value object
 * 4. Coordinate with AI analysis through domain events
 */
@Entity
@Table(name = "Report", indexes = {
    @Index(name = "idx_report_tenant_type", columnList = "company_id, report_type"),
    @Index(name = "idx_report_status", columnList = "status"),
    @Index(name = "idx_report_date_range", columnList = "start_date, end_date")
})
public class ReportAggregate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Integer reportId;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "company_id", nullable = false))
    private TenantId tenantId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;
    
    @Column(name = "report_name", nullable = false)
    private String reportName;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status;
    
    // File storage for download
    @Column(name = "file_path", length = 500)
    private String filePath;
    
    @Column(name = "file_format", length = 20)
    private String fileFormat;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    // Content storage as value object
    @Embedded
    private ReportContent content;
    
    // AI Analysis state
    @Column(name = "ai_analysis_enabled")
    private Boolean aiAnalysisEnabled;
    
    @Column(name = "ai_analysis_status")
    private String aiAnalysisStatus;
    
    @Lob
    @Column(name = "ai_analysis_results")
    private String aiAnalysisResults;
    
    // Audit fields
    @Column(name = "created_by")
    private Integer createdBy;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    // Domain events
    @Transient
    private List<Object> domainEvents = new ArrayList<>();
    
    // Constructors
    protected ReportAggregate() {}
    
    /**
     * Create new report for generation
     */
    public static ReportAggregate create(ReportType reportType, String reportName, 
                                       LocalDate startDate, LocalDate endDate, 
                                       TenantId tenantId, Integer createdBy) {
        ReportAggregate report = new ReportAggregate();
        report.reportType = reportType;
        report.reportName = reportName;
        report.startDate = startDate;
        report.endDate = endDate;
        report.tenantId = tenantId;
        report.createdBy = createdBy;
        report.status = ReportStatus.GENERATING;
        report.fileFormat = "XLSX";
        report.aiAnalysisEnabled = false;
        report.createdAt = LocalDateTime.now();
        report.updatedAt = LocalDateTime.now();
        
        return report;
    }
    
    /**
     * Create report with AI analysis enabled
     */
    public static ReportAggregate createWithAI(ReportType reportType, String reportName,
                                              LocalDate startDate, LocalDate endDate,
                                              TenantId tenantId, Integer createdBy) {
        ReportAggregate report = create(reportType, reportName, startDate, endDate, tenantId, createdBy);
        report.enableAIAnalysis();
        return report;
    }
    
    /**
     * Complete report generation with file and content
     */
    public void completeGeneration(String filePath, Long fileSize, String contentData, String contentFormat) {
        if (status != ReportStatus.GENERATING) {
            throw new IllegalStateException("Report is not in generating status");
        }
        
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty");
        }
        
        // Store file information
        this.filePath = filePath;
        this.fileSize = fileSize;
        
        // Store content as value object
        if (contentData != null && !contentData.trim().isEmpty()) {
            this.content = ReportContent.create(contentData, contentFormat);
        }
        
        this.status = ReportStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.errorMessage = null;
        
        // Publish domain events
        addDomainEvent(new ReportGeneratedEvent(this.reportId, this.reportType, 
                                              this.tenantId.getValue(), this.aiAnalysisEnabled));
        
        if (hasContentData()) {
            addDomainEvent(new ReportContentStoredEvent(this.reportId, contentFormat, 
                                                      this.content.getSize()));
        }
    }
    
    /**
     * Enable AI analysis
     */
    public void enableAIAnalysis() {
        this.aiAnalysisEnabled = true;
        this.aiAnalysisStatus = "PENDING";
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Complete AI analysis with results
     */
    public void completeAIAnalysis(String analysisResults) {
        if (!aiAnalysisEnabled) {
            throw new IllegalStateException("AI analysis is not enabled for this report");
        }
        
        if (!isReadyForAI()) {
            throw new IllegalStateException("Report is not ready for AI analysis");
        }
        
        this.aiAnalysisResults = analysisResults;
        this.aiAnalysisStatus = "COMPLETED";
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Archive report (business rule: soft delete)
     */
    public void archive() {
        if (status == ReportStatus.GENERATING) {
            throw new IllegalStateException("Cannot archive report that is still generating");
        }
        
        this.status = ReportStatus.ARCHIVED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Fail report generation
     */
    public void failGeneration(String errorMessage) {
        if (status != ReportStatus.GENERATING) {
            throw new IllegalStateException("Report is not in generating status");
        }
        
        this.status = ReportStatus.FAILED;
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();
    }
    
    // Business rule methods
    
    /**
     * Check if report can be viewed
     */
    public boolean canBeViewed() {
        return status == ReportStatus.COMPLETED && hasContentData();
    }
    
    /**
     * Check if report can be downloaded
     */
    public boolean canBeDownloaded() {
        return status == ReportStatus.COMPLETED && filePath != null;
    }
    
    /**
     * Check if report can be deleted
     */
    public boolean canBeDeleted() {
        return status != ReportStatus.GENERATING;
    }
    
    /**
     * Check if report has content data
     */
    public boolean hasContentData() {
        return content != null && content.isViewable();
    }
    
    /**
     * Check if report is ready for AI analysis
     */
    public boolean isReadyForAI() {
        return status == ReportStatus.COMPLETED && 
               aiAnalysisEnabled && 
               content != null &&
               content.isSuitableForAI() &&
               ("PENDING".equals(aiAnalysisStatus) || "READY".equals(aiAnalysisStatus));
    }
    
    /**
     * Get content for AI analysis
     */
    public String getContentForAI() {
        if (!isReadyForAI()) {
            throw new IllegalStateException("Report is not ready for AI analysis");
        }
        return content.getData();
    }
    
    /**
     * Get display-friendly period description
     */
    public String getPeriodDescription() {
        return startDate + " to " + endDate;
    }
    
    /**
     * Get formatted file size
     */
    public String getFileSizeFormatted() {
        if (fileSize == null) return "Unknown";
        return formatBytes(fileSize);
    }
    
    /**
     * Get formatted content size
     */
    public String getContentSizeFormatted() {
        if (content == null) return "No content";
        return content.getFormattedSize();
    }
    
    // Helper methods
    
    private String formatBytes(long bytes) {
        double size = bytes;
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", size, units[unitIndex]);
    }
    
    private void addDomainEvent(Object event) {
        if (domainEvents == null) {
            domainEvents = new ArrayList<>();
        }
        domainEvents.add(event);
    }
    
    @DomainEvents
    public List<Object> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    @AfterDomainEventPublication
    public void clearDomainEvents() {
        domainEvents.clear();
    }
    
    /**
     * Check if report is completed
     */
    public boolean isCompleted() {
        return status == ReportStatus.COMPLETED;
    }
    
    /**
     * Check if report is failed
     */
    public boolean isFailed() {
        return status == ReportStatus.FAILED;
    }
    
    /**
     * Get domain events (for testing)
     */
    public List<Object> getDomainEvents() {
        return domainEvents();
    }

    public Integer getReportId() { return reportId; }
    public TenantId getTenantId() { return tenantId; }
    public ReportType getReportType() { return reportType; }
    public String getReportName() { return reportName; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public ReportStatus getStatus() { return status; }
    public String getFilePath() { return filePath; }
    public String getFileFormat() { return fileFormat; }
    public Long getFileSize() { return fileSize; }
    public ReportContent getContent() { return content; }
    public Boolean getAiAnalysisEnabled() { return aiAnalysisEnabled; }
    public String getAiAnalysisStatus() { return aiAnalysisStatus; }
    public String getAiAnalysisResults() { return aiAnalysisResults; }
    public Integer getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getErrorMessage() { return errorMessage; }
}