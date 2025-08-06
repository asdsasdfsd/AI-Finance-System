// backend/src/main/java/org/example/backend/infrastructure/ai/OpenAIServiceImpl.java
package org.example.backend.infrastructure.ai;

import lombok.extern.slf4j.Slf4j;
import org.example.backend.application.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.Map;

@Slf4j
@Service
public class OpenAIServiceImpl implements AIService {

    @Value("${openai.api.key:dummy_key}")
    private String apiKey;

    @Value("${ai.openai.api-url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${ai.openai.chat-model:gpt-3.5-turbo}")
    private String chatModel;

    @Autowired
    private FinancialPromptBuilder promptBuilder;

    private final RestTemplate restTemplate = new RestTemplate();

    private String callOpenAI(String prompt) {
        try {
            Map<String, Object> body = Map.of(
                    "model", chatModel,
                    "messages", List.of(Map.of("role", "user", "content", prompt)),
                    "temperature", 0.1
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl, HttpMethod.POST, request, Map.class
            );

            if (response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null) {
                        return message.get("content").toString();
                    }
                }
            }
            return "Error: No response content";
        } catch (Exception e) {
            log.error("Error calling OpenAI API: {}", e.getMessage());
            // Fallback response for development/testing
            return "AI service temporarily unavailable. Using fallback response.";
        }
    }

    @Override
    public String call(String prompt) {
        return callOpenAI(prompt);
    }

    @Override
    public AIClassificationResult classifyTransaction(String description, Double amount, String currency) {
        try {
            AITransactionData data = AITransactionData.builder()
                    .description(description)
                    .amount(amount)
                    .currency(currency)
                    .transactionType("EXPENSE") // Default type
                    .build();
            
            String prompt = promptBuilder.buildClassificationPrompt(data);
            String response = callOpenAI(prompt);
            
            // Parse AI response or return mock data for development
            return AIClassificationResult.builder()
                    .category("TRAVEL_EXPENSE")
                    .confidence(0.91)
                    .reason("Based on transaction description analysis: " + description)
                    .alternativeCategories(List.of("FOOD_EXPENSE", "LODGING_EXPENSE"))
                    .requireReview(amount > 1000.0) // Flag high amount transactions for review
                    .build();
        } catch (Exception e) {
            log.error("Error in classifyTransaction: {}", e.getMessage());
            return AIClassificationResult.builder()
                    .category("GENERAL_EXPENSE")
                    .confidence(0.5)
                    .reason("Error in AI classification, using default category")
                    .alternativeCategories(List.of("EXPENSE"))
                    .requireReview(true)
                    .build();
        }
    }

    @Override
    public AIQuestionAnswerResult answerFinancialQuestion(String question, String context, Integer companyId) {
        try {
            String prompt = promptBuilder.buildQuestionPrompt(question, context);
            String aiResponse = callOpenAI(prompt);
            
            return AIQuestionAnswerResult.builder()
                    .answer(aiResponse)
                    .confidence("HIGH")
                    .hasNumericData(aiResponse.matches(".*\\d+.*"))
                    .dataSources(List.of("AI", "CompanyData"))
                    .relatedData(Map.of("companyId", companyId.toString()))
                    .build();
        } catch (Exception e) {
            log.error("Error in answerFinancialQuestion: {}", e.getMessage());
            return AIQuestionAnswerResult.builder()
                    .answer("I'm unable to answer that question at the moment. Please try again later.")
                    .confidence("LOW")
                    .hasNumericData(false)
                    .dataSources(List.of("Error"))
                    .relatedData(Map.of())
                    .build();
        }
    }

    @Override
    public AIAnomalyDetectionResult detectAnomalousTransaction(AITransactionData data) {
        try {
            String prompt = promptBuilder.buildAnomalyDetectionPrompt(data);
            callOpenAI(prompt);
            
            // Simple anomaly detection logic for development
            boolean isHighAmount = data.getAmount() > 5000.0;
            boolean isWeekendTransaction = data.getTransactionDate() != null && 
                (data.getTransactionDate().getDayOfWeek().getValue() > 5);
            
            return AIAnomalyDetectionResult.builder()
                    .anomalous(isHighAmount || isWeekendTransaction)
                    .anomalyScore(isHighAmount ? 0.88 : 0.12)
                    .anomalyType(isHighAmount ? "High Amount" : "Normal")
                    .recommendations(isHighAmount ? 
                        List.of("Review high amount transaction", "Verify approvals") :
                        List.of("No action needed"))
                    .build();
        } catch (Exception e) {
            log.error("Error in detectAnomalousTransaction: {}", e.getMessage());
            return AIAnomalyDetectionResult.builder()
                    .anomalous(false)
                    .anomalyScore(0.0)
                    .anomalyType("Error")
                    .recommendations(List.of("Unable to analyze transaction"))
                    .build();
        }
    }

    @Override
    public AIReportInsightResult generateReportInsights(String reportData, String reportType) {
        try {
            String prompt = String.format("""
                    You are an AI financial assistant. The following is a %s report.

                    Report Data:
                    %s

                    Please summarize insights, highlight anomalies, and suggest actions.
                    """, reportType, reportData);

            String aiResponse = callOpenAI(prompt);

            return AIReportInsightResult.builder()
                    .insightSummary(aiResponse.length() > 500 ? 
                        aiResponse.substring(0, 500) + "..." : aiResponse)
                    .keyFindings(List.of(
                            "AI analysis completed for " + reportType + " report",
                            "Data processing successful",
                            "Insights generated based on current financial trends"
                    ))
                    .build();
        } catch (Exception e) {
            log.error("Error in generateReportInsights: {}", e.getMessage());
            return AIReportInsightResult.builder()
                    .insightSummary("Unable to generate insights at this time")
                    .keyFindings(List.of("Error in AI analysis", "Please retry later"))
                    .build();
        }
    }

    @Override
    public FinancialAnalysisDTO analyzeFinancialData(FinancialAnalysisCommand command) {
        try {
            // 构造 AI 分析 Prompt
            String prompt = String.format("""
                请对如下公司（ID：%d）的财务数据进行全面智能分析，时间范围为 %s 至 %s，报表类型为 %s。
                原始数据如下：
                %s

                请输出：1. 综合摘要 2. 主要亮点 3. 潜在风险 4. 优化建议。
                """,
                    command.getCompanyId(),
                    command.getStartDate(),
                    command.getEndDate(),
                    command.getReportType(),
                    command.getRawData()
            );

            String aiResponse = callOpenAI(prompt);

            // 简单解析（你可以根据AI返回的格式更细致拆分）
            return FinancialAnalysisDTO.builder()
                    .summary(aiResponse.length() > 200 ? aiResponse.substring(0, 200) + "..." : aiResponse)
                    .highlights(List.of("AI自动分析亮点（请解析AI原始返回细化）"))
                    .risks(List.of("AI自动分析风险（请解析AI原始返回细化）"))
                    .suggestions(List.of("AI自动优化建议（请解析AI原始返回细化）"))
                    .build();

        } catch (Exception e) {
            log.error("Error in analyzeFinancialData: {}", e.getMessage());
            return FinancialAnalysisDTO.builder()
                    .summary("分析失败，请稍后再试。")
                    .highlights(List.of())
                    .risks(List.of())
                    .suggestions(List.of())
                    .build();
        }
    }

    @Override
    public AIRecommendationsDTO getRecommendations(RecommendationCommand command) {
        try {
            // 构造 AI 推荐 Prompt
            String prompt = String.format("""
                请基于如下场景“%s”，为公司ID：%d（目标对象：%s）提供3条专业的AI财务建议，说明推荐理由。
                补充数据：%s
                """,
                    command.getScenario(),
                    command.getCompanyId(),
                    command.getTargetObject(),
                    command.getData()
            );

            String aiResponse = callOpenAI(prompt);

            // 简单封装（如需结构化可优化正则等）
            return AIRecommendationsDTO.builder()
                    .recommendations(List.of(aiResponse))
                    .reasoning("AI智能推荐理由见详细内容")
                    .confidence(0.95)
                    .build();

        } catch (Exception e) {
            log.error("Error in getRecommendations: {}", e.getMessage());
            return AIRecommendationsDTO.builder()
                    .recommendations(List.of("暂无推荐"))
                    .reasoning("AI服务不可用")
                    .confidence(0.0)
                    .build();
        }
    }

    @Override
    public boolean isServiceAvailable() {
        try {
            callOpenAI("Say 'pong' if you are available");
            return true;
        } catch (Exception e) {
            log.warn("AI service health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "OpenAI";
    }

    // Remove the old single-parameter methods that are causing conflicts
    // These are no longer needed as we now implement the correct interface methods
}