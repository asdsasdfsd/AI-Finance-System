// backend/src/main/java/org/example/backend/application/dto/ReportSummaryDTO.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for report summary in AI analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSummaryDTO {
    private Integer totalReports;
    private Integer completedReports;
    private Integer reportsWithAI;
    private List<String> reportTypes;
}