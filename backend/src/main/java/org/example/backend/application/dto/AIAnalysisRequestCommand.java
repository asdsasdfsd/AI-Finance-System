// backend/src/main/java/org/example/backend/application/dto/AIAnalysisRequestCommand.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Command for requesting AI analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisRequestCommand {
    private Integer companyId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String analysisType;
    private Integer departmentId;
    private List<Integer> selectedTransactionIds;
    private List<Integer> selectedReportIds;
    private List<String> categoryFilter;
    private Double minAmount;
    private Double maxAmount;
}

