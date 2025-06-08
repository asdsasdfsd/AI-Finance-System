// backend/src/main/java/org/example/backend/domain/event/ReportGenerationFailedEvent.java
package org.example.backend.domain.event;

import org.example.backend.domain.valueobject.ReportType;

/**
 * Report Generation Failed Event
 * 
 * Published when a report generation fails
 */
public class ReportGenerationFailedEvent extends DomainEvent {
    private final Integer reportId;
    private final ReportType reportType;
    private final Integer companyId;
    private final String errorMessage;
    
    public ReportGenerationFailedEvent(Integer reportId, ReportType reportType, 
                                     Integer companyId, String errorMessage) {
        super();
        this.reportId = reportId;
        this.reportType = reportType;
        this.companyId = companyId;
        this.errorMessage = errorMessage;
    }
    
    public Integer getReportId() { return reportId; }
    public ReportType getReportType() { return reportType; }
    public Integer getCompanyId() { return companyId; }
    public String getErrorMessage() { return errorMessage; }
    
    @Override
    public String toString() {
        return String.format("ReportGenerationFailedEvent{reportId=%d, type=%s, companyId=%d, error=%s}", 
                           reportId, reportType, companyId, errorMessage);
    }
}

