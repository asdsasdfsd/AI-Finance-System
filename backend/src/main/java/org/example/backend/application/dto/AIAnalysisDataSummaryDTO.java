// backend/src/main/java/org/example/backend/application/dto/AIAnalysisDataSummaryDTO.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for AI analysis data summary
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisDataSummaryDTO {
    private Integer totalTransactions;
    private Integer totalReports;
    private DateRangeDTO dateRange;
    private TransactionSummaryDTO transactionSummary;
    private ReportSummaryDTO reportSummary;
    private List<String> availableAnalysisTypes;
    private String estimatedComplexity;
    private Boolean dataSufficient;
}

