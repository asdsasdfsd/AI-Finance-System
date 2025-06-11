// backend/src/main/java/org/example/backend/domain/event/handler/ReportEventHandler.java
package org.example.backend.domain.event.handler;

import org.example.backend.domain.event.ReportGeneratedEvent;
import org.example.backend.domain.event.ReportGenerationStartedEvent;
import org.example.backend.domain.event.ReportGenerationFailedEvent;
import org.example.backend.domain.event.ReportDownloadedEvent;
import org.example.backend.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Report Event Handler
 * 
 * Responsibilities:
 * 1. Handle report-related domain events
 * 2. Execute event-driven business logic
 * 3. Maintain audit trails for report operations
 * 4. Trigger downstream processes (notifications, AI analysis, etc.)
 */
@Component
@Transactional
public class ReportEventHandler {
    
    @Autowired
    private AuditLogService auditLogService;
    
    // Future services for enhanced functionality
    // @Autowired
    // private NotificationService notificationService;
    // @Autowired 
    // private AIAnalysisService aiAnalysisService;
    // @Autowired
    // private FileStorageService fileStorageService;
    
    /**
     * Handle report generation started event
     */
    @EventListener
    public void handleReportGenerationStarted(ReportGenerationStartedEvent event) {
        try {
            // 1. Record audit log
            auditLogService.logAction(
                null, // User object - simplified for now
                "REPORT_GENERATION_STARTED",
                "Report",
                event.getReportId().toString(),
                String.format("Started generating %s report for company %d", 
                    event.getReportType().getDisplayName(), 
                    event.getCompanyId()),
                "system"
            );
            
            // 2. Send notification to user (future enhancement)
            // notificationService.notifyReportGenerationStarted(event);
            
            // 3. Update system metrics
            // metricsService.incrementReportGenerationCount(event.getReportType());
            
            System.out.println("Report generation started event handled: " + event);
            
        } catch (Exception e) {
            System.err.println("Failed to handle report generation started event: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Handle report generation completed event
     */
    @EventListener
    public void handleReportGenerated(ReportGeneratedEvent event) {
        try {
            // 1. Record audit log
            auditLogService.logAction(
                null,
                "REPORT_GENERATED",
                "Report",
                event.getReportId().toString(),
                String.format("Successfully generated %s report for company %d", 
                    event.getReportType().getDisplayName(), 
                    event.getCompanyId()),
                "system"
            );
            
            // 2. Trigger AI analysis if enabled
            if (Boolean.TRUE.equals(event.getAiAnalysisEnabled())) {
                // aiAnalysisService.scheduleAnalysis(event.getReportId());
                System.out.println("AI analysis scheduled for report: " + event.getReportId());
            }
            
            // 3. Send completion notification
            // notificationService.notifyReportCompleted(event);
            
            // 4. Update report statistics
            // statisticsService.updateReportCompletionStats(event);
            
            System.out.println("Report generation completed event handled: " + event);
            
        } catch (Exception e) {
            System.err.println("Failed to handle report generation completed event: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Handle report generation failed event
     */
    @EventListener
    public void handleReportGenerationFailed(ReportGenerationFailedEvent event) {
        try {
            // 1. Record audit log with error details
            auditLogService.logAction(
                null,
                "REPORT_GENERATION_FAILED",
                "Report",
                event.getReportId().toString(),
                String.format("Failed to generate %s report for company %d. Error: %s", 
                    event.getReportType().getDisplayName(), 
                    event.getCompanyId(),
                    event.getErrorMessage()),
                "system"
            );
            
            // 2. Send failure notification
            // notificationService.notifyReportGenerationFailed(event);
            
            // 3. Log error for monitoring
            // errorTrackingService.logReportGenerationError(event);
            
            // 4. Update failure statistics
            // statisticsService.updateReportFailureStats(event);
            
            System.out.println("Report generation failed event handled: " + event);
            
        } catch (Exception e) {
            System.err.println("Failed to handle report generation failed event: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Handle report downloaded event
     */
    @EventListener
    public void handleReportDownloaded(ReportDownloadedEvent event) {
        try {
            // 1. Record audit log
            auditLogService.logAction(
                null,
                "REPORT_DOWNLOADED",
                "Report",
                event.getReportId().toString(),
                String.format("Report %s downloaded by user %d via %s", 
                    event.getReportType().getDisplayName(), 
                    event.getDownloadedBy(),
                    event.getDownloadMethod()),
                "system"
            );
            
            // 2. Update download statistics
            // statisticsService.updateDownloadStats(event);
            
            // 3. Track usage patterns for analytics
            // analyticsService.trackReportUsage(event);
            
            System.out.println("Report download event handled: " + event);
            
        } catch (Exception e) {
            System.err.println("Failed to handle report download event: " + e.getMessage());
            throw e;
        }
    }
}