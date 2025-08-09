// backend/src/main/java/org/example/backend/domain/event/ReportContentStoredEvent.java
package org.example.backend.domain.event;

/**
 * Report Content Stored Event
 * 
 * Published when report content is stored for viewing and AI analysis
 */
public class ReportContentStoredEvent extends DomainEvent {
    private final Integer reportId;
    private final String contentFormat;
    private final Long contentSize;
    
    public ReportContentStoredEvent(Integer reportId, String contentFormat, Long contentSize) {
        super();
        this.reportId = reportId;
        this.contentFormat = contentFormat;
        this.contentSize = contentSize;
    }
    
    public Integer getReportId() { return reportId; }
    public String getContentFormat() { return contentFormat; }
    public Long getContentSize() { return contentSize; }
    
    @Override
    public String toString() {
        return String.format("ReportContentStoredEvent{reportId=%d, format=%s, size=%d}", 
                           reportId, contentFormat, contentSize);
    }
}