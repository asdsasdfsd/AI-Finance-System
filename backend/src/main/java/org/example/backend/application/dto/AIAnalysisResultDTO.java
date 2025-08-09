// backend/src/main/java/org/example/backend/application/dto/AIAnalysisResultDTO.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for AI analysis results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisResultDTO {
    private String analysisType;
    private String summary;
    private List<String> insights;
    private String confidence;
    private Integer dataPoints;
    private LocalDateTime generatedAt;
}