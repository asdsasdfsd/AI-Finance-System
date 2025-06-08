// backend/src/main/java/org/example/backend/domain/aggregate/report/ReportAggregate.java
package org.example.backend.domain.aggregate.report;

import org.example.backend.domain.event.ReportGeneratedEvent;
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
import java.util.Objects;

/**
 * Report Aggregate Root
 * 
 * Responsibilities:
 * 1. Manage financial report lifecycle
 * 2. Control report generation process
 * 3. Store report metadata and file path
 * 4. Prepare data for AI analysis
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
    
    @Column(name = "file_path", length = 500)
    private String filePath;
    
    @Column(name = "file_format", length = 20)
    private String fileFormat;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    // AI Analysis preparation
    @Column(name = "ai_analysis_enabled")
    private Boolean aiAnalysisEnabled;
    
    @Column(name = "ai_analysis_data", columnDefinition = "TEXT")
    private String aiAnalysisData;
    
    @Column(name = "ai_analysis_status")
    private String aiAnalysisStatus;
    
    // Audit fields
    @Column(name = "created_by")
    private Integer createdBy;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Error handling
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    // Domain events
    @Transient
    private List<Object> domainEvents = new ArrayList<>();
    
    // ========== Constructors ==========
    
    protected ReportAggregate() {
        // JPA requires default constructor
    }
    
    private ReportAggregate(ReportType reportType, String reportName, LocalDate startDate, 
                          LocalDate endDate, TenantId tenantId, Integer createdBy) {
        validateReportCreation(reportType, reportName, startDate, endDate, tenantId, createdBy);
        
        this.reportType = reportType;
        this.reportName = reportName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.tenantId = tenantId;
        this.createdBy = createdBy;
        this.status = ReportStatus.GENERATING;
        this.aiAnalysisEnabled = false;
        this.fileFormat = "XLSX";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // ========== Factory Methods ==========
    
    /**
     * Create new report generation request
     */
    public static ReportAggregate create(ReportType reportType, String reportName, 
                                       LocalDate startDate, LocalDate endDate, 
                                       TenantId tenantId, Integer createdBy) {
        return new ReportAggregate(reportType, reportName, startDate, endDate, tenantId, createdBy);
    }
    
    /**
     * Create new report with AI analysis enabled
     */
    public static ReportAggregate createWithAI(ReportType reportType, String reportName, 
                                             LocalDate startDate, LocalDate endDate, 
                                             TenantId tenantId, Integer createdBy) {
        ReportAggregate report = new ReportAggregate(reportType, reportName, startDate, endDate, tenantId, createdBy);
        report.enableAIAnalysis();
        return report;
    }
    
    // ========== Business Methods ==========
    
    /**
     * Mark report generation as completed
     */
    public void completeGeneration(String filePath, Long fileSize) {
        if (status != ReportStatus.GENERATING) {
            throw new IllegalStateException("Report is not in generating status");
        }
        
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty");
        }
        
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.status = ReportStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.errorMessage = null; // Clear any previous errors
        
        // Publish domain event
        addDomainEvent(new ReportGeneratedEvent(this.reportId, this.reportType, 
                                              this.tenantId.getValue(), this.aiAnalysisEnabled));
    }
    
    /**
     * Mark report generation as failed
     */
    public void failGeneration(String errorMessage) {
        if (status != ReportStatus.GENERATING) {
            throw new IllegalStateException("Report is not in generating status");
        }
        
        this.status = ReportStatus.FAILED;
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Enable AI analysis for this report
     */
    public void enableAIAnalysis() {
        this.aiAnalysisEnabled = true;
        this.aiAnalysisStatus = "PENDING";
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Prepare data for AI analysis
     */
    public void prepareForAIAnalysis(String analysisData) {
        if (!aiAnalysisEnabled) {
            throw new IllegalStateException("AI analysis is not enabled for this report");
        }
        
        this.aiAnalysisData = analysisData;
        this.aiAnalysisStatus = "READY";
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Mark AI analysis as completed
     */
    public void completeAIAnalysis(String analysisResult) {
        if (!aiAnalysisEnabled) {
            throw new IllegalStateException("AI analysis is not enabled for this report");
        }
        
        this.aiAnalysisData = analysisResult;
        this.aiAnalysisStatus = "COMPLETED";
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Update report name
     */
    public void updateReportName(String newName) {
        if (status == ReportStatus.GENERATING) {
            throw new IllegalStateException("Cannot modify report while generating");
        }
        
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Report name cannot be empty");
        }
        
        this.reportName = newName.trim();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Archive completed report
     */
    public void archive() {
        if (status != ReportStatus.COMPLETED) {
            throw new IllegalStateException("Only completed reports can be archived");
        }
        
        this.status = ReportStatus.ARCHIVED;
        this.updatedAt = LocalDateTime.now();
    }
    
    // ========== Query Methods ==========
    
    /**
     * Check if report generation is completed
     */
    public boolean isCompleted() {
        return status == ReportStatus.COMPLETED;
    }
    
    /**
     * Check if report generation failed
     */
    public boolean isFailed() {
        return status == ReportStatus.FAILED;
    }
    
    /**
     * Check if report is ready for download
     */
    public boolean isReadyForDownload() {
        return status == ReportStatus.COMPLETED && filePath != null;
    }
    
    /**
     * Check if AI analysis is ready
     */
    public boolean isAIAnalysisReady() {
        return aiAnalysisEnabled && "READY".equals(aiAnalysisStatus);
    }
    
    /**
     * Get report period description
     */
    public String getPeriodDescription() {
        return String.format("%s to %s", startDate.toString(), endDate.toString());
    }
    
    /**
     * Get file size in human readable format
     */
    public String getFileSizeFormatted() {
        if (fileSize == null) return "Unknown";
        
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
    }
    
    /**
     * Check if belongs to tenant
     */
    public boolean belongsToTenant(TenantId tenantId) {
        return this.tenantId.equals(tenantId);
    }
    
    // ========== Validation Methods ==========
    
    private void validateReportCreation(ReportType reportType, String reportName, 
                                      LocalDate startDate, LocalDate endDate, 
                                      TenantId tenantId, Integer createdBy) {
        if (reportType == null) {
            throw new IllegalArgumentException("Report type cannot be null");
        }
        
        if (reportName == null || reportName.trim().isEmpty()) {
            throw new IllegalArgumentException("Report name cannot be empty");
        }
        
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        if (endDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("End date cannot be in the future");
        }
        
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }
        
        if (createdBy == null) {
            throw new IllegalArgumentException("Creator user ID cannot be null");
        }
    }
    
    // ========== Domain Events Management ==========
    
    private void addDomainEvent(Object event) {
        this.domainEvents.add(event);
    }
    
    @DomainEvents
    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    @AfterDomainEventPublication
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
    
    // ========== Getters ==========
    
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
    public Boolean getAiAnalysisEnabled() { return aiAnalysisEnabled; }
    public String getAiAnalysisData() { return aiAnalysisData; }
    public String getAiAnalysisStatus() { return aiAnalysisStatus; }
    public Integer getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getErrorMessage() { return errorMessage; }
    
    // ========== Object Methods ==========
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ReportAggregate that = (ReportAggregate) obj;
        return Objects.equals(reportId, that.reportId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(reportId);
    }
    
    @Override
    public String toString() {
        return String.format("Report{id=%d, type=%s, name=%s, status=%s, period=%s}", 
                           reportId, reportType, reportName, status, getPeriodDescription());
    }
}