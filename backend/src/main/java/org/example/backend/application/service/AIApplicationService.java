package org.example.backend.application.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.application.dto.*;
import org.example.backend.infrastructure.ai.AIService;
import org.example.backend.infrastructure.ai.dto.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AIApplicationService {
    private final AIService aiService;

    public EnhancedTransactionDTO enhanceTransactionCreation(CreateTransactionCommand cmd) {
        var result = aiService.classifyTransaction(cmd.getDescription(), cmd.getAmount(), cmd.getCurrency());
        return EnhancedTransactionDTO.builder()
                .aiClassification(result)
                .aiEnhanced(true)
                .enhancementTimestamp(LocalDateTime.now().toString())
                .build();
    }

    public AIQuestionAnswerResult askFinancialQuestion(FinancialQuestionCommand cmd) {
        return aiService.answerFinancialQuestion(cmd.getQuestion(), "", cmd.getCompanyId());
    }

    public List<AIAnomalyDetectionResult> detectAnomalousTransaction(AITransactionData data) {
        return List.of(aiService.detectAnomalousTransaction(data));
    }

    public AIReportInsightResult generateReportInsights(String data, String type) {
        return aiService.generateReportInsights(data, type);
    }
}
