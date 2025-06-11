// backend/src/main/java/org/example/backend/domain/event/ReportGenerationStartedEvent.java
package org.example.backend.domain.event;

import org.example.backend.domain.valueobject.ReportType;

/**
 * Report Generation Started Event
 * 
 * Published when a report generation process begins
 */
public class ReportGenerationStartedEvent extends DomainEvent {
    private final Integer reportId;
    private final ReportType reportType;
    private final Integer companyId;
    private final Integer createdBy;
    
    public ReportGenerationStartedEvent(Integer reportId, ReportType reportType, 
                                      Integer companyId, Integer createdBy) {
        super();
        this.reportId = reportId;
        this.reportType = reportType;
        this.companyId = companyId;
        this.createdBy = createdBy;
    }
    
    public Integer getReportId() { return reportId; }
    public ReportType getReportType() { return reportType; }
    public Integer getCompanyId() { return companyId; }
    public Integer getCreatedBy() { return createdBy; }
    
    @Override
    public String toString() {
        return String.format("ReportGenerationStartedEvent{reportId=%d, type=%s, companyId=%d, createdBy=%d}", 
                           reportId, reportType, companyId, createdBy);
    }
}

