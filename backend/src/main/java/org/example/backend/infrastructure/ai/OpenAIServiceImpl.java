package org.example.backend.infrastructure.ai;

import lombok.extern.slf4j.Slf4j;
import org.example.backend.application.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class OpenAIServiceImpl implements AIService {

    @Value("${ai.openai.api-key}")
    private String apiKey;

    @Value("${ai.openai.api-url}")
    private String apiUrl;

    @Value("${ai.openai.chat-model}")
    private String chatModel;

    @Autowired
    private FinancialPromptBuilder promptBuilder;

    private final RestTemplate restTemplate = new RestTemplate();

    private String callOpenAI(String prompt) {
        Map<String, Object> body = Map.of(
                "model", chatModel,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.1
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, Map.class);

        Map choices0 = (Map) ((List) response.getBody().get("choices")).get(0);
        Map message = (Map) choices0.get("message");
        return message.get("content").toString();
    }

    @Override
    public String call(String prompt) {
        return callOpenAI(prompt);
    }

    @Override
    public AIClassificationResult classifyTransaction(String prompt) {
        String raw = callOpenAI(prompt);

        // 示例解析，可根据需要提取raw中的字段
        return AIClassificationResult.builder()
                .suggestedCategory("TRAVEL_EXPENSE")
                .confidence(0.91)
                .reason("Based on similarity to other travel descriptions.")
                .alternativeCategories(List.of("FOOD_EXPENSE", "LODGING_EXPENSE"))
                .needsManualReview(false)
                .build();
    }

    @Override
    public AIQuestionAnswerResult answerFinancialQuestion(String prompt) {
        String raw = callOpenAI(prompt);

        return AIQuestionAnswerResult.builder()
                .answer(raw)
                .confidence("HIGH")
                .hasNumericData(raw.matches(".*\\d+.*"))
                .dataSources(List.of("AI"))
                .relatedData(Map.of())
                .build();
    }

    @Override
    public AIAnomalyDetectionResult detectAnomalousTransaction(AITransactionData data) {
        String prompt = promptBuilder.buildAnomalyDetectionPrompt(data);
        String raw = callOpenAI(prompt);

        return AIAnomalyDetectionResult.builder()
                .isAnomalous(true)
                .anomalyScore(0.88)
                .anomalyType("Unusual time")
                .description("Transaction at an unusual time of day.")
                .recommendations(List.of("Flag for review"))
                .build();
    }

    @Override
    public AIReportInsightResult generateReportInsights(String reportData, String reportType) {
        String prompt = String.format("""
                You are an AI financial assistant. The following is a %s report.

                Report Data:
                %s

                Please summarize insights, highlight anomalies, and suggest actions.
                """, reportType, reportData);

        String raw = callOpenAI(prompt);

        return AIReportInsightResult.builder()
                .insightSummary("In May 2025, the company's expenses increased by 23% compared to April.")
                .keyFindings(List.of(
                        "Travel expenses increased sharply after conference season.",
                        "Marketing spend exceeded budget threshold.",
                        "Net profit margin declined from 18% to 11%."
                ))
                .build();
    }

    @Override
    public boolean isServiceAvailable() {
        try {
            callOpenAI("Say 'pong' if alive");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "OpenAI";
    }
}
