// backend/src/main/java/org/example/backend/domain/event/ReportDownloadedEvent.java
package org.example.backend.domain.event;

import org.example.backend.domain.valueobject.ReportType;

/**
 * Report Downloaded Event
 * 
 * Published when a report is downloaded by a user
 */
public class ReportDownloadedEvent extends DomainEvent {
    private final Integer reportId;
    private final ReportType reportType;
    private final Integer companyId;
    private final Integer downloadedBy;
    private final String downloadMethod; // WEB, API, etc.
    
    public ReportDownloadedEvent(Integer reportId, ReportType reportType, 
                               Integer companyId, Integer downloadedBy, String downloadMethod) {
        super();
        this.reportId = reportId;
        this.reportType = reportType;
        this.companyId = companyId;
        this.downloadedBy = downloadedBy;
        this.downloadMethod = downloadMethod;
    }
    
    public Integer getReportId() { return reportId; }
    public ReportType getReportType() { return reportType; }
    public Integer getCompanyId() { return companyId; }
    public Integer getDownloadedBy() { return downloadedBy; }
    public String getDownloadMethod() { return downloadMethod; }
    
    @Override
    public String toString() {
        return String.format("ReportDownloadedEvent{reportId=%d, type=%s, companyId=%d, downloadedBy=%d, method=%s}", 
                           reportId, reportType, companyId, downloadedBy, downloadMethod);
    }
}

