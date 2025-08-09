// backend/src/main/java/org/example/backend/application/dto/AITransactionDataDTO.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for transaction data in AI analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AITransactionDataDTO {
    private Integer transactionId;
    private String description;
    private Double amount;
    private String transactionType;
    private String category;
    private LocalDate transactionDate;
    private Integer departmentId;
    private String status;
}

