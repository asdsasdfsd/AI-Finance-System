// backend/src/main/java/org/example/backend/application/dto/AIReportDataDTO.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for report data in AI analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIReportDataDTO {
    private Integer reportId;
    private String reportName;
    private String reportType;
    private String periodDescription;
    private Boolean hasContent;
    private Long contentSize;
    private Boolean aiAnalysisEnabled;
    private String status;
    private LocalDateTime createdAt;
}

