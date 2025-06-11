// backend/src/main/java/org/example/backend/application/dto/GenerateReportCommand.java
package org.example.backend.application.dto;

import org.example.backend.domain.valueobject.ReportType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * Generate Report Command
 * 
 * Command object for initiating report generation
 */
@Data
@Builder
public class GenerateReportCommand {
    private Integer tenantId;
    private ReportType reportType;
    private String reportName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer createdBy;
    private Boolean aiAnalysisEnabled;
    
    // Validation methods
    public boolean isValid() {
        return tenantId != null && 
               reportType != null && 
               reportName != null && !reportName.trim().isEmpty() &&
               startDate != null && 
               endDate != null && 
               createdBy != null &&
               !startDate.isAfter(endDate);
    }
}

