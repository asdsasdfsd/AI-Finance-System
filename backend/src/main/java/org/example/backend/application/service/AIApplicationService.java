package org.example.backend.application.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.application.dto.*;
import org.example.backend.infrastructure.ai.AIService;
import org.example.backend.infrastructure.ai.FinancialPromptBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AIApplicationService {

    private final AIService aiService;
    private final AIDataService aiDataService;
    private final FinancialPromptBuilder promptBuilder;

    /**
     * 增强交易创建流程
     */
    public EnhancedTransactionDTO enhanceTransactionCreation(CreateTransactionCommand command) {
    AITransactionData aiData = AITransactionData.builder()
            .description(command.getDescription())
            .amount(command.getAmount().doubleValue())
            .currency(command.getCurrency())
            .transactionType("EXPENSE")
            .companyId(command.getCompanyId())
            .transactionDate(command.getTransactionDate())
            .category("GENERAL")
            .build();

    // String classifyPrompt = promptBuilder.buildClassificationPrompt(aiData);
    // AIClassificationResult classificationResult = aiService.classifyTransaction(classifyPrompt);
    // AIAnomalyDetectionResult anomalyResult = aiService.detectAnomalousTransaction(aiData);

    // ✅ 使用 mock 结果代替真实 OpenAI 调用
    AIClassificationResult classificationResult = AIClassificationResult.builder()
            .category("FOOD_EXPENSE")
            .confidence(0.88)
            .reason("包含关键词 'lunch'")
            .alternativeCategories(List.of("TRAVEL_EXPENSE", "OFFICE_SUPPLIES"))
            .requireReview(false)
            .build();

    AIAnomalyDetectionResult anomalyResult = AIAnomalyDetectionResult.builder()
            .anomalous(false)
            .anomalyScore(0.12)
            .anomalyType("none")
            .recommendations(List.of("No action needed"))
            .build();

    return EnhancedTransactionDTO.builder()
            .originalTransaction(null) // 可补充真实交易对象
            .aiClassification(classificationResult)
            .anomalyDetection(anomalyResult)
            .aiEnhanced(true)
            .enhancementTimestamp(LocalDateTime.now().toString())
            .build();
}

    /**
     * 财务问答
     */
    public FinancialQuestionAnswerDTO askFinancialQuestion(FinancialQuestionCommand questionCommand) {
        String context = aiDataService.buildFinancialContext(
                questionCommand.getCompanyId(),
                questionCommand.getStartDate() != null ? questionCommand.getStartDate() : LocalDate.now().minusMonths(1),
                questionCommand.getEndDate() != null ? questionCommand.getEndDate() : LocalDate.now()
        );

        String prompt = promptBuilder.buildQuestionPrompt(questionCommand.getQuestion(), context);
        AIQuestionAnswerResult answerResult = aiService.answerFinancialQuestion(prompt);

        return FinancialQuestionAnswerDTO.builder()
                .answer(answerResult.getAnswer())
                .confidence(answerResult.getConfidence())
                .hasNumericData(answerResult.isHasNumericData())
                .dataSources(answerResult.getDataSources())
                .relatedData(answerResult.getRelatedData())
                .build();
    }

    /**
     * 获取交易分类建议
     */
    public List<CategorySuggestionDTO> getTransactionCategorySuggestions(CategorySuggestionCommand suggestionCommand) {
        // 静态建议模拟
        return List.of(
                CategorySuggestionDTO.builder().categoryCode("TRAVEL_EXPENSE").categoryName("Travel").chineseName("差旅").confidence(0.92).reason("描述包含'差旅'关键词").build(),
                CategorySuggestionDTO.builder().categoryCode("FOOD_EXPENSE").categoryName("Food").chineseName("餐饮").confidence(0.81).reason("可能与员工餐费相关").build()
        );
    }

    /**
     * 检测异常交易
     */
    public List<AnomalousTransactionDTO> detectAnomalousTransactions(Integer companyId, DateRange dateRange) {
        List<AITransactionData> transactions = aiDataService.getTransactionsInRange(companyId, dateRange);

        return transactions.stream()
                .map(txn -> {
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
                    } else return null;
                })
                .filter(x -> x != null)
                .toList();
    }

    /**
     * 生成报表洞察
     */
    public String generateReportInsights(String reportData, String reportType) {
        return aiService.generateReportInsights(reportData, reportType).getInsightSummary();
    }
}
