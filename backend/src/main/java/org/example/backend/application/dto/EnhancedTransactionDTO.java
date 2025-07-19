// backend/src/main/java/org/example/backend/application/dto/EnhancedTransactionDTO.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnhancedTransactionDTO {

    private Object originalTransaction; // You can replace with actual TransactionDTO
    private AIClassificationResult aiClassification;
    private AIAnomalyDetectionResult anomalyDetection; // Can be null if no detection performed
    private boolean aiEnhanced;
    private String enhancementTimestamp;
}