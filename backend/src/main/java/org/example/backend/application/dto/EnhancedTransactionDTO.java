package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class EnhancedTransactionDTO {
    private Object originalTransaction; // 可替换为真实类型
    private AIClassificationResult aiClassification;
    private AIAnomalyDetectionResult anomalyDetection;
    private boolean aiEnhanced;
    private String enhancementTimestamp;
}
