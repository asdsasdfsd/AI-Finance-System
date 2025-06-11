// backend/src/main/java/org/example/backend/domain/event/ReportGeneratedEvent.java
package org.example.backend.domain.event;

import org.example.backend.domain.valueobject.ReportType;

/**
 * Report Generated Event
 * 
 * Published when a report generation is completed successfully
 */
public class ReportGeneratedEvent extends DomainEvent {
    private final Integer reportId;
    private final ReportType reportType;
    private final Integer companyId;
    private final Boolean aiAnalysisEnabled;
    
    public ReportGeneratedEvent(Integer reportId, ReportType reportType, 
                              Integer companyId, Boolean aiAnalysisEnabled) {
        super();
        this.reportId = reportId;
        this.reportType = reportType;
        this.companyId = companyId;
        this.aiAnalysisEnabled = aiAnalysisEnabled;
    }
    
    public Integer getReportId() { return reportId; }
    public ReportType getReportType() { return reportType; }
    public Integer getCompanyId() { return companyId; }
    public Boolean getAiAnalysisEnabled() { return aiAnalysisEnabled; }
    
    @Override
    public String toString() {
        return String.format("ReportGeneratedEvent{reportId=%d, type=%s, companyId=%d, aiEnabled=%s}", 
                           reportId, reportType, companyId, aiAnalysisEnabled);
    }
}

