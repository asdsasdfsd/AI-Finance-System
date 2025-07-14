package org.example.backend.infrastructure.ai;

import org.example.backend.application.dto.AITransactionData;
import org.springframework.stereotype.Component;

@Component
public class FinancialPromptBuilder {

    /**
     * 构建交易分类 Prompt（增强版）
     */
    public String buildClassificationPrompt(AITransactionData data) {
        return String.format("""
            You are a financial assistant. Your task is to classify a financial transaction based on the provided details.

            Transaction Information:
            - Description: %s
            - Amount: %.2f
            - Currency: %s
            - Transaction Type: %s

            Respond in the following JSON format:
            {
              "suggested_category": "CATEGORY_CODE",
              "confidence": 0.0-1.0,
              "reason": "your reasoning here",
              "alternative_categories": ["CATEGORY_A", "CATEGORY_B"],
              "needs_manual_review": true/false
            }
            """,
            data.getDescription(),
            data.getAmount(),
            data.getCurrency(),
            data.getTransactionType()
        );
    }

    /**
     * 构建财务智能问答 Prompt（增强语气）
     */
    public String buildQuestionPrompt(String question, String context) {
        return String.format("""
            You are an expert AI assistant in corporate finance.

            Here is the financial context:
            %s

            Question:
            %s

            Please provide a direct and professional answer based only on the given context. 
            If the question cannot be answered from the context, respond: "Insufficient data."
            """,
            context.trim(), question.trim()
        );
    }

    /**
     * 构建异常检测 Prompt（增强结构）
     */
    public String buildAnomalyDetectionPrompt(AITransactionData data) {
        return String.format("""
            You are a financial anomaly detection model. Analyze the transaction below and determine whether it is anomalous.

            Transaction Details:
            - Description: %s
            - Amount: %.2f
            - Currency: %s
            - Type: %s
            - Category: %s
            - Date: %s

            Respond in the following JSON format:
            {
              "is_anomalous": true/false,
              "anomaly_score": 0.0-1.0,
              "anomaly_type": "description of anomaly type",
              "recommendations": ["suggestion 1", "suggestion 2"]
            }
            """,
            data.getDescription(),
            data.getAmount(),
            data.getCurrency(),
            data.getTransactionType(),
            data.getCategory(),
            data.getTransactionDate()
        );
    }
}
