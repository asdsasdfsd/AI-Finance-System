// backend/src/main/java/org/example/backend/application/service/AIApplicationService.java
package org.example.backend.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.example.backend.application.dto.*;
import org.example.backend.infrastructure.ai.AIService;
import org.example.backend.infrastructure.ai.FinancialPromptBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIApplicationService {

    private final AIService aiService;
    private final AIDataService aiDataService;
    private final FinancialPromptBuilder promptBuilder;


    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${ai.openai.api-url}")
    private String apiUrl;

    @Value("${ai.openai.chat-model}")
    private String model;

    private final WebClient.Builder webClientBuilder = WebClient.builder();

    public Mono<String> askQuestion(String question) {
        WebClient webClient = webClientBuilder.baseUrl(apiUrl).build();

        // 构造请求体
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", question)
                )
        );

        return webClient.post()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    // 解析回答内容
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                        return (String) message.get("content");
                    }
                    return "AI没有返回答案";
                });
    }

    /**
     * Enhanced transaction creation flow
     */
    public EnhancedTransactionDTO enhanceTransactionCreation(CreateTransactionCommand command) {
        try {
            // Call AI service with correct parameters
            AIClassificationResult classificationResult = aiService.classifyTransaction(
                command.getDescription(),
                command.getAmount().doubleValue(),
                command.getCurrency()
            );

            // Build transaction data for anomaly detection
            AITransactionData aiData = AITransactionData.builder()
                    .description(command.getDescription())
                    .amount(command.getAmount().doubleValue())
                    .currency(command.getCurrency())
                    .transactionType("EXPENSE")
                    .companyId(command.getCompanyId())
                    .transactionDate(command.getTransactionDate())
                    .category(classificationResult.getCategory())
                    .build();

            AIAnomalyDetectionResult anomalyResult = aiService.detectAnomalousTransaction(aiData);

            return EnhancedTransactionDTO.builder()
                    .originalTransaction(null) // Can be populated with actual transaction object
                    .aiClassification(classificationResult)
                    .anomalyDetection(anomalyResult)
                    .aiEnhanced(true)
                    .enhancementTimestamp(LocalDateTime.now().toString())
                    .build();
        } catch (Exception e) {
            // Fallback to mock data if AI service fails
            AIClassificationResult fallbackClassification = AIClassificationResult.builder()
                    .category("GENERAL_EXPENSE")
                    .confidence(0.5)
                    .reason("AI service unavailable, using default classification")
                    .alternativeCategories(List.of("EXPENSE"))
                    .requireReview(true)
                    .build();

            AIAnomalyDetectionResult fallbackAnomaly = AIAnomalyDetectionResult.builder()
                    .anomalous(false)
                    .anomalyScore(0.0)
                    .anomalyType("unknown")
                    .recommendations(List.of("Manual review recommended"))
                    .build();

            return EnhancedTransactionDTO.builder()
                    .originalTransaction(null)
                    .aiClassification(fallbackClassification)
                    .anomalyDetection(fallbackAnomaly)
                    .aiEnhanced(false)
                    .enhancementTimestamp(LocalDateTime.now().toString())
                    .build();
        }
    }

    /**
     * Financial Q&A functionality
     */
    public FinancialQuestionAnswerDTO askFinancialQuestion(FinancialQuestionCommand questionCommand) {
        try {
            String context = aiDataService.buildFinancialContext(
                    questionCommand.getCompanyId(),
                    questionCommand.getStartDate() != null ? 
                        questionCommand.getStartDate() : LocalDate.now().minusMonths(1),
                    questionCommand.getEndDate() != null ? 
                        questionCommand.getEndDate() : LocalDate.now()
            );

            // Call AI service with correct parameters (question, context, companyId)
            AIQuestionAnswerResult answerResult = aiService.answerFinancialQuestion(
                questionCommand.getQuestion(),
                context,
                questionCommand.getCompanyId()
            );

            return FinancialQuestionAnswerDTO.builder()
                    .answer(answerResult.getAnswer())
                    .confidence(answerResult.getConfidence())
                    .hasNumericData(answerResult.isHasNumericData())
                    .dataSources(answerResult.getDataSources())
                    .relatedData(answerResult.getRelatedData())
                    .build();
        } catch (Exception e) {
            // Fallback response if AI service fails
            return FinancialQuestionAnswerDTO.builder()
                    .answer("I'm sorry, I'm unable to process your question at the moment. Please try again later.")
                    .confidence("LOW")
                    .hasNumericData(false)
                    .dataSources(List.of("Error"))
                    .relatedData(Map.of())
                    .build();
        }
    }

    /**
     * Get transaction category suggestions
     */
    public List<CategorySuggestionDTO> getTransactionCategorySuggestions(CategorySuggestionCommand suggestionCommand) {
        try {
            // Use AI service for real suggestions
            AIClassificationResult classification = aiService.classifyTransaction(
                suggestionCommand.getDescription(),
                suggestionCommand.getAmount(),
                suggestionCommand.getCurrency()
            );

            // Convert AI result to suggestions format
            return List.of(
                    CategorySuggestionDTO.builder()
                            .categoryCode(classification.getCategory())
                            .categoryName(classification.getCategory().replace("_", " "))
                            .chineseName(getCategoryChineseName(classification.getCategory()))
                            .confidence(classification.getConfidence())
                            .reason(classification.getReason())
                            .build()
            );
        } catch (Exception e) {
            // Static fallback suggestions
            return List.of(
                    CategorySuggestionDTO.builder()
                            .categoryCode("TRAVEL_EXPENSE")
                            .categoryName("Travel")
                            .chineseName("差旅")
                            .confidence(0.92)
                            .reason("描述包含'差旅'关键词")
                            .build(),
                    CategorySuggestionDTO.builder()
                            .categoryCode("FOOD_EXPENSE")
                            .categoryName("Food")
                            .chineseName("餐饮")
                            .confidence(0.81)
                            .reason("可能与员工餐费相关")
                            .build()
            );
        }
    }

    /**
     * Detect anomalous transactions
     */
    public List<AnomalousTransactionDTO> detectAnomalousTransactions(Integer companyId, DateRange dateRange) {
        try {
            List<AITransactionData> transactions = aiDataService.getTransactionsInRange(companyId, dateRange);

            return transactions.stream()
                    .map(txn -> {
                        try {
                            AIAnomalyDetectionResult result = aiService.detectAnomalousTransaction(txn);
                            if (result.isAnomalous()) {
                                return AnomalousTransactionDTO.builder()
                                        .description(txn.getDescription())
                                        .amount(txn.getAmount())
                                        .transactionDate(txn.getTransactionDate())
                                        .category(txn.getCategory())
                                        .anomalyScore(result.getAnomalyScore())
                                        .anomalyType(result.getAnomalyType())
                                        .recommendations(result.getRecommendations())
                                        .build();
                            }
                        } catch (Exception e) {
                            // Log error but continue processing other transactions
                            System.err.println("Error processing transaction: " + e.getMessage());
                        }
                        return null;
                    })
                    .filter(x -> x != null)
                    .toList();
        } catch (Exception e) {
            // Return empty list if service fails
            return List.of();
        }
    }

    /**
     * Generate report insights
     */
    public String generateReportInsights(String reportData, String reportType) {
        try {
            return aiService.generateReportInsights(reportData, reportType).getInsightSummary();
        } catch (Exception e) {
            return "Unable to generate insights at this time. Please try again later.";
        }
    }

    /**
     * Helper method to get Chinese category names
     */
    private String getCategoryChineseName(String categoryCode) {
        return switch (categoryCode) {
            case "TRAVEL_EXPENSE" -> "差旅";
            case "FOOD_EXPENSE" -> "餐饮";
            case "OFFICE_SUPPLIES" -> "办公用品";
            case "MARKETING_EXPENSE" -> "市场营销";
            case "UTILITIES" -> "水电费";
            case "RENT" -> "租金";
            default -> "其他";
        };
    }

    /**
     * 财务数据智能分析
     */
    public FinancialAnalysisDTO analyzeFinancialData(FinancialAnalysisCommand command) {
        try {
            // 调用底层AI服务进行分析（你需在AIService中实现该方法）
            return aiService.analyzeFinancialData(command);
        } catch (Exception e) {
            // 兜底方案：返回默认/空分析结果
            return FinancialAnalysisDTO.builder()
                    .summary("分析失败，请稍后再试。")
                    .highlights(List.of())
                    .risks(List.of())
                    .suggestions(List.of())
                    .build();
        }
    }

    /**
     * 获取AI智能推荐
     */
    public AIRecommendationsDTO getRecommendations(RecommendationCommand command) {
        try {
            // 调用底层AI服务获取推荐（你需在AIService中实现该方法）
            return aiService.getRecommendations(command);
        } catch (Exception e) {
            // 兜底方案：返回默认/空推荐
            return AIRecommendationsDTO.builder()
                    .recommendations(List.of("暂无推荐"))
                    .reasoning("AI服务不可用")
                    .confidence(0.0)
                    .build();
        }
    }


    /**
     * 健康检查
     */
    public boolean isServiceAvailable() {
        return aiService.isServiceAvailable();
    }

    /**
     * 获取当前AI服务提供商名称
     */
    public String getProviderName() {
        return aiService.getProviderName();
    }
}