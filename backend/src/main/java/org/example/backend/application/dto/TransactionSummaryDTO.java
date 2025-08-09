// backend/src/main/java/org/example/backend/application/dto/TransactionSummaryDTO.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * DTO for transaction summary in AI analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummaryDTO {
    private Integer totalTransactions;
    private Double totalIncome;
    private Double totalExpense;
    private Double netAmount;
    private Double averageAmount;
}