// backend/src/main/java/org/example/backend/application/service/AIApplicationService.java
package org.example.backend.application.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.application.dto.CreateTransactionCommand;
import org.example.backend.application.dto.EnhancedTransactionDTO;
import org.example.backend.application.dto.FinancialQuestionCommand;
import org.example.backend.infrastructure.ai.AIService;
import org.example.backend.infrastructure.ai.dto.AITransactionData;
import org.example.backend.infrastructure.ai.dto.AIClassificationResult;
import org.example.backend.infrastructure.ai.dto.AIQuestionAnswerResult;
import org.example.backend.infrastructure.ai.dto.AIAnomalyDetectionResult;
import org.example.backend.infrastructure.ai.dto.AIReportInsightResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI Application Service
 * 
 * Orchestrates AI-related use cases and coordinates between
 * domain services and infrastructure AI services
 */
@Service
@RequiredArgsConstructor
public class AIApplicationService {
    
    private final AIService aiService;

    /**
     * Enhance transaction creation with AI classification
     * 
     * @param cmd CreateTransactionCommand to enhance
     * @return EnhancedTransactionDTO with AI classification results
     */
    public EnhancedTransactionDTO enhanceTransactionCreation(CreateTransactionCommand cmd) {
        try {
            // Call AI service to classify the transaction
            // Convert BigDecimal to Double for AI service compatibility
            Double amountAsDouble = cmd.getAmount() != null ? cmd.getAmount().doubleValue() : 0.0;
            
            AIClassificationResult result = aiService.classifyTransaction(
                cmd.getDescription(), 
                amountAsDouble, 
                cmd.getCurrency()
            );
            
            return EnhancedTransactionDTO.builder()
                    .aiClassification(result)
                    .aiEnhanced(true)
                    .enhancementTimestamp(LocalDateTime.now().toString())
                    .build();
        } catch (Exception e) {
            // Handle AI service errors gracefully
            return EnhancedTransactionDTO.builder()
                    .aiEnhanced(false)
                    .enhancementTimestamp(LocalDateTime.now().toString())
                    .build();
        }
    }

    /**
     * Ask financial question using AI
     * 
     * @param cmd FinancialQuestionCommand containing the question
     * @return AIQuestionAnswerResult with AI response
     */
    public AIQuestionAnswerResult askFinancialQuestion(FinancialQuestionCommand cmd) {
        return aiService.answerFinancialQuestion(
            cmd.getQuestion(), 
            "", // Context can be empty for now
            cmd.getCompanyId()
        );
    }

    /**
     * Detect anomalous transactions using AI
     * 
     * @param data AITransactionData to analyze
     * @return List of AIAnomalyDetectionResult
     */
    public List<AIAnomalyDetectionResult> detectAnomalousTransaction(AITransactionData data) {
        try {
            AIAnomalyDetectionResult result = aiService.detectAnomalousTransaction(data);
            return List.of(result);
        } catch (Exception e) {
            // Return empty list if AI detection fails
            return List.of();
        }
    }

    /**
     * Generate AI insights for reports
     * 
     * @param data Report data as string
     * @param type Report type
     * @return AIReportInsightResult with AI insights
     */
    public AIReportInsightResult generateReportInsights(String data, String type) {
        return aiService.generateReportInsights(data, type);
    }
}