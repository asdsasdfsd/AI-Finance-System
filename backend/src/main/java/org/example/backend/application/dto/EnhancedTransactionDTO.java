package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;
import org.example.backend.infrastructure.ai.dto.AIClassificationResult;
import org.example.backend.infrastructure.ai.dto.AIAnomalyDetectionResult;

@Data
@Builder
public class EnhancedTransactionDTO {

    private Object originalTransaction; // 你可以替换为实际的 TransactionDTO
    private AIClassificationResult aiClassification;
    private AIAnomalyDetectionResult anomalyDetection; // 如果没有检测可以为 null
    private boolean aiEnhanced;
    private String enhancementTimestamp;
}
