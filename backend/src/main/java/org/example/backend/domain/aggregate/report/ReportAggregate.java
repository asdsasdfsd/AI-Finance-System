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
 * Report Aggregate Root - Compatible with Current Database Schema
 * 
 * Fixed to match exactly with your database structure without ReportContent dependency
 */
@Entity
@Table(name = "Report", indexes = {
    @Index(name = "idx_report_tenant_type", columnList = "company_id, report_type"),
    @Index(name = "idx_report_status", columnList = "status"),
    @Index(name = "idx_report_date_range", columnList = "start_date, end_date"),
    @Index(name = "idx_report_content_size", columnList = "content_size"),
    @Index(name = "idx_report_ai_status", columnList = "ai_analysis_status"),
    @Index(name = "idx_report_company_status", columnList = "company_id, status")
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
    
    @Column(name = "report_name", nullable = false, length = 255)
    private String reportName;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status;
    
    // File storage - matches database columns 10-12
    @Column(name = "file_path", length = 500)
    private String filePath;
    
    @Column(name = "file_format", length = 20)
    private String fileFormat;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    // Content storage - matches database columns 20-23 (direct mapping instead of embedded)
    @Lob
    @Column(name = "content_data", columnDefinition = "LONGTEXT")
    private String contentData;
    
    @Column(name = "content_format", length = 255)
    private String contentFormat;
    
    @Column(name = "content_hash", length = 255)
    private String contentHash;
    
    @Column(name = "content_size")
    private Long contentSize;
    
    // AI Analysis fields - matches database columns 2-4, 19
    @Column(name = "ai_analysis_enabled")
    private Boolean aiAnalysisEnabled;
    
    @Lob
    @Column(name = "ai_analysis_data", columnDefinition = "TEXT")
    private String aiAnalysisData;
    
    @Column(name = "ai_analysis_status", length = 255)
    private String aiAnalysisStatus;
    
    @Lob
    @Column(name = "ai_analysis_results", columnDefinition = "LONGTEXT")
    private String aiAnalysisResults;
    
    // Audit fields - matches database columns 5-7, 17-18
    @Column(name = "created_by")
    private Integer createdBy;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Error handling - matches database column 9
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    // Domain events
    @Transient
    private final List<Object> domainEvents = new ArrayList<>();
    
    // Constructors
    protected ReportAggregate() {
        this.aiAnalysisEnabled = false;
        this.aiAnalysisStatus = "NOT_STARTED";
        this.contentFormat = "JSON";
    }
    
    private ReportAggregate(ReportType reportType, String reportName, 
                           LocalDate startDate, LocalDate endDate, 
                           TenantId tenantId, Integer createdBy) {
        this();
        this.reportType = reportType;
        this.reportName = reportName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.tenantId = tenantId;
        this.createdBy = createdBy;
        this.status = ReportStatus.GENERATING;  // Start with GENERATING instead of PENDING
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.fileFormat = "XLSX";
    }
    
    // Factory methods
    public static ReportAggregate create(ReportType reportType, String reportName,
                                       LocalDate startDate, LocalDate endDate,
                                       TenantId tenantId, Integer createdBy) {
        return new ReportAggregate(reportType, reportName, startDate, endDate, tenantId, createdBy);
    }
    
    public static ReportAggregate createWithAI(ReportType reportType, String reportName,
                                              LocalDate startDate, LocalDate endDate,
                                              TenantId tenantId, Integer createdBy) {
        ReportAggregate report = create(reportType, reportName, startDate, endDate, tenantId, createdBy);
        report.enableAIAnalysis();
        return report;
    }
    
    // Business methods
    public void startGeneration() {
        // Allow starting generation from either initial state or if it was failed before
        if (this.status != ReportStatus.GENERATING && this.status != ReportStatus.FAILED) {
            throw new IllegalStateException("Can only start generation for new or failed reports");
        }
        this.status = ReportStatus.GENERATING;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void completeGeneration(String filePath, Long fileSize) {
        if (this.status != ReportStatus.GENERATING) {
            throw new IllegalStateException("Can only complete generating reports");
        }
        this.status = ReportStatus.COMPLETED;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.fileFormat = "XLSX";
        
        // Add domain event
        addDomainEvent(new ReportGeneratedEvent(this.reportId, this.reportType, 
                                              this.tenantId.getValue(), this.aiAnalysisEnabled));
    }
    
    public void completeGeneration(String filePath, Long fileSize, String contentData, String contentFormat) {
        completeGeneration(filePath, fileSize);
        setContentData(contentData, contentFormat);
    }
    
    public void failGeneration(String errorMessage) {
        this.status = ReportStatus.FAILED;
        this.errorMessage = errorMessage != null && errorMessage.length() > 1000 
            ? errorMessage.substring(0, 997) + "..." 
            : errorMessage;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void archive() {
        if (this.status == ReportStatus.GENERATING) {
            throw new IllegalStateException("Cannot archive report that is still generating");
        }
        this.status = ReportStatus.ARCHIVED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void enableAIAnalysis() {
        this.aiAnalysisEnabled = true;
        this.aiAnalysisStatus = "ENABLED";
        this.updatedAt = LocalDateTime.now();
    }
    
    public void prepareForAIAnalysis(String analysisData) {
        this.aiAnalysisData = analysisData;
        this.aiAnalysisStatus = "PREPARED";
        this.updatedAt = LocalDateTime.now();
    }
    
    public void completeAIAnalysis(String analysisResults) {
        if (!Boolean.TRUE.equals(aiAnalysisEnabled)) {
            throw new IllegalStateException("AI analysis is not enabled for this report");
        }
        
        if (!isReadyForAI()) {
            throw new IllegalStateException("Report is not ready for AI analysis");
        }
        
        this.aiAnalysisResults = analysisResults;
        this.aiAnalysisStatus = "COMPLETED";
        this.updatedAt = LocalDateTime.now();
    }
    
    // Content management methods
    public void setContentData(String contentData, String contentFormat) {
        this.contentData = contentData;
        this.contentFormat = contentFormat != null ? contentFormat : "JSON";
        this.contentSize = contentData != null ? (long) contentData.length() : 0L;
        this.updatedAt = LocalDateTime.now();
        
        // Generate content hash for integrity checking
        if (contentData != null) {
            this.contentHash = Integer.toHexString(contentData.hashCode());
        }
    }
    
    // Business rule methods
    public boolean canBeViewed() {
        return status == ReportStatus.COMPLETED && hasContentData();
    }
    
    public boolean canBeDownloaded() {
        return status == ReportStatus.COMPLETED && filePath != null;
    }
    
    public boolean canBeDeleted() {
        return status != ReportStatus.GENERATING;
    }
    
    public boolean canBeArchived() {
        return status == ReportStatus.COMPLETED;
    }
    
    public boolean hasContentData() {
        return contentData != null && !contentData.trim().isEmpty();
    }
    
    public boolean isReadyForAI() {
        return status == ReportStatus.COMPLETED && 
               Boolean.TRUE.equals(aiAnalysisEnabled) && 
               hasContentData() &&
               ("PENDING".equals(aiAnalysisStatus) || 
                "ENABLED".equals(aiAnalysisStatus) || 
                "PREPARED".equals(aiAnalysisStatus));
    }
    
    public boolean isCompleted() {
        return status == ReportStatus.COMPLETED;
    }
    
    public boolean isFailed() {
        return status == ReportStatus.FAILED;
    }
    
    public boolean isDownloadable() {
        return status == ReportStatus.COMPLETED && filePath != null;
    }
    
    // Helper methods
    public String getPeriodDescription() {
        if (startDate.equals(endDate)) {
            return "As of " + startDate;
        } else {
            return startDate + " to " + endDate;
        }
    }
    
    public String getFileSizeFormatted() {
        if (fileSize == null) return "Unknown";
        return formatBytes(fileSize);
    }
    
    public String getContentSizeFormatted() {
        if (contentSize == null) return "No content";
        return formatBytes(contentSize);
    }
    
    public String getContentForAI() {
        if (!isReadyForAI()) {
            throw new IllegalStateException("Report is not ready for AI analysis");
        }
        return contentData;
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
    
    private void addDomainEvent(Object event) {
        domainEvents.add(event);
    }
    
    // Domain events
    @DomainEvents
    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    @AfterDomainEventPublication
    public void clearDomainEvents() {
        domainEvents.clear();
    }
    
    // Getters - complete set matching database structure
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
    public String getContentData() { return contentData; }
    public String getContentFormat() { return contentFormat; }
    public String getContentHash() { return contentHash; }
    public Long getContentSize() { return contentSize; }
    public Boolean getAiAnalysisEnabled() { return aiAnalysisEnabled; }
    public String getAiAnalysisData() { return aiAnalysisData; }
    public String getAiAnalysisStatus() { return aiAnalysisStatus; }
    public String getAiAnalysisResults() { return aiAnalysisResults; }
    public Integer getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getErrorMessage() { return errorMessage; }
    
    // Compatibility methods for backward compatibility with existing code
    
    /**
     * @deprecated Use getContentData() instead
     * This method provides backward compatibility for existing code
     */
    @Deprecated
    public ReportContentProxy getContent() {
        return new ReportContentProxy(this.contentData, this.contentFormat, this.contentSize);
    }
    
    /**
     * Inner class to provide backward compatibility for ReportContent access
     */
    @Deprecated
    public static class ReportContentProxy {
        private final String data;
        private final String format;
        private final Long size;
        
        public ReportContentProxy(String data, String format, Long size) {
            this.data = data;
            this.format = format;
            this.size = size;
        }
        
        public String getData() { return data; }
        public String getFormat() { return format; }
        public Long getSize() { return size; }
        public boolean isViewable() { return data != null && !data.trim().isEmpty(); }
        public boolean isSuitableForAI() { return isViewable() && data.length() > 10; }
        
        public String getFormattedSize() { 
            if (size == null) return "No content";
            if (size < 1024) return size + " B";
            if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        }
        
        // Add missing getSummary() method
        public String getSummary() {
            if (data == null || data.trim().isEmpty()) {
                return "No content available";
            }
            
            // Generate a simple summary based on content length and format
            String summary = String.format("Content size: %s", getFormattedSize());
            
            if (format != null) {
                summary += String.format(", Format: %s", format);
            }
            
            // Add basic content analysis
            if (data.length() > 1000) {
                summary += ", Content: Large dataset";
            } else if (data.length() > 100) {
                summary += ", Content: Medium dataset";
            } else {
                summary += ", Content: Small dataset";
            }
            
            return summary;
        }
    }
    
    // Equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportAggregate that = (ReportAggregate) o;
        return Objects.equals(reportId, that.reportId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(reportId);
    }
    
    @Override
    public String toString() {
        return "ReportAggregate{" +
                "reportId=" + reportId +
                ", reportType=" + reportType +
                ", reportName='" + reportName + '\'' +
                ", status=" + status +
                ", period=" + getPeriodDescription() +
                '}';
    }
}