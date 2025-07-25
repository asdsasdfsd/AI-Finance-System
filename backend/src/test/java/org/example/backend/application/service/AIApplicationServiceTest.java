package org.example.backend.application.service;

import org.example.backend.application.dto.*;
import org.example.backend.infrastructure.ai.AIService;
import org.example.backend.infrastructure.ai.FinancialPromptBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AIApplicationServiceTest {

    @Mock
    private AIService aiService;
    @Mock
    private AIDataService aiDataService;
    @Mock
    private FinancialPromptBuilder promptBuilder;

    @InjectMocks
    private AIApplicationService aiApplicationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- enhanceTransactionCreation ---
    @Test
    void enhanceTransactionCreation_success() {
        CreateTransactionCommand cmd = CreateTransactionCommand.builder()
                .description("Lunch at restaurant")
                .amount(BigDecimal.valueOf(50))
                .currency("USD")
                .companyId(1)
                .transactionDate(LocalDate.now())
                .build();

        AIClassificationResult classification = AIClassificationResult.builder()
                .category("FOOD_EXPENSE")
                .confidence(0.95)
                .reason("Contains food keyword")
                .alternativeCategories(List.of("EXPENSE"))
                .requireReview(false)
                .build();

        AIAnomalyDetectionResult anomaly = AIAnomalyDetectionResult.builder()
                .anomalous(false)
                .anomalyScore(0.1)
                .anomalyType("none")
                .recommendations(List.of("No action needed"))
                .build();

        when(aiService.classifyTransaction(anyString(), anyDouble(), anyString())).thenReturn(classification);
        when(aiService.detectAnomalousTransaction(any())).thenReturn(anomaly);

        EnhancedTransactionDTO result = aiApplicationService.enhanceTransactionCreation(cmd);

        assertNotNull(result);
        assertTrue(result.isAiEnhanced());
        assertEquals("FOOD_EXPENSE", result.getAiClassification().getCategory());
        assertEquals(anomaly, result.getAnomalyDetection());
        assertNotNull(result.getEnhancementTimestamp());
    }

    @Test
    void enhanceTransactionCreation_aiServiceFails_returnsFallback() {
        CreateTransactionCommand cmd = CreateTransactionCommand.builder()
                .description("Taxi ride")
                .amount(BigDecimal.valueOf(20))
                .currency("USD")
                .companyId(2)
                .transactionDate(LocalDate.now())
                .build();

        when(aiService.classifyTransaction(anyString(), anyDouble(), anyString()))
                .thenThrow(new RuntimeException("AI down"));

        EnhancedTransactionDTO result = aiApplicationService.enhanceTransactionCreation(cmd);

        assertNotNull(result);
        assertFalse(result.isAiEnhanced());
        assertEquals("GENERAL_EXPENSE", result.getAiClassification().getCategory());
        assertEquals("AI service unavailable, using default classification", result.getAiClassification().getReason());
        assertEquals("unknown", result.getAnomalyDetection().getAnomalyType());
    }

    // --- askFinancialQuestion ---
    @Test
    void askFinancialQuestion_success() {
        FinancialQuestionCommand cmd = FinancialQuestionCommand.builder()
                .companyId(1)
                .question("What was the total revenue last month?")
                .startDate(LocalDate.now().minusMonths(1))
                .endDate(LocalDate.now())
                .build();

        when(aiDataService.buildFinancialContext(anyInt(), any(), any())).thenReturn("context");
        AIQuestionAnswerResult answerResult = AIQuestionAnswerResult.builder()
                .answer("Total revenue was $10,000")
                .confidence("HIGH")
                .hasNumericData(true)
                .dataSources(List.of("ERP"))
                .relatedData(Map.of("revenue", 10000))
                .build();
        when(aiService.answerFinancialQuestion(anyString(), anyString(), anyInt())).thenReturn(answerResult);

        FinancialQuestionAnswerDTO result = aiApplicationService.askFinancialQuestion(cmd);

        assertEquals("Total revenue was $10,000", result.getAnswer());
        assertEquals("HIGH", result.getConfidence());
        assertTrue(result.isHasNumericData());
        assertEquals(List.of("ERP"), result.getDataSources());
        assertEquals(Map.of("revenue", 10000), result.getRelatedData());
    }

    @Test
    void askFinancialQuestion_aiServiceFails_returnsFallback() {
        FinancialQuestionCommand cmd = FinancialQuestionCommand.builder()
                .companyId(1)
                .question("What was the total revenue last month?")
                .build();

        when(aiDataService.buildFinancialContext(anyInt(), any(), any())).thenReturn("context");
        when(aiService.answerFinancialQuestion(anyString(), anyString(), anyInt()))
                .thenThrow(new RuntimeException("AI down"));

        FinancialQuestionAnswerDTO result = aiApplicationService.askFinancialQuestion(cmd);

        assertEquals("I'm sorry, I'm unable to process your question at the moment. Please try again later.", result.getAnswer());
        assertEquals("LOW", result.getConfidence());
        assertFalse(result.isHasNumericData());
        assertEquals(List.of("Error"), result.getDataSources());
        assertEquals(Map.of(), result.getRelatedData());
    }

    // --- getTransactionCategorySuggestions ---
    @Test
    void getTransactionCategorySuggestions_success() {
        CategorySuggestionCommand cmd = CategorySuggestionCommand.builder()
                .description("Flight to Beijing")
                .amount(1200.0)
                .currency("CNY")
                .build();

        AIClassificationResult classification = AIClassificationResult.builder()
                .category("TRAVEL_EXPENSE")
                .confidence(0.92)
                .reason("描述包含'差旅'关键词")
                .build();

        when(aiService.classifyTransaction(anyString(), anyDouble(), anyString())).thenReturn(classification);

        List<CategorySuggestionDTO> result = aiApplicationService.getTransactionCategorySuggestions(cmd);

        assertEquals(1, result.size());
        assertEquals("TRAVEL_EXPENSE", result.get(0).getCategoryCode());
        assertEquals("差旅", result.get(0).getChineseName());
        assertEquals(0.92, result.get(0).getConfidence());
    }

    @Test
    void getTransactionCategorySuggestions_aiServiceFails_returnsFallback() {
        CategorySuggestionCommand cmd = CategorySuggestionCommand.builder()
                .description("Lunch")
                .amount(30.0)
                .currency("CNY")
                .build();

        when(aiService.classifyTransaction(anyString(), anyDouble(), anyString()))
                .thenThrow(new RuntimeException("AI down"));

        List<CategorySuggestionDTO> result = aiApplicationService.getTransactionCategorySuggestions(cmd);

        assertEquals(2, result.size());
        assertEquals("TRAVEL_EXPENSE", result.get(0).getCategoryCode());
        assertEquals("FOOD_EXPENSE", result.get(1).getCategoryCode());
    }

    // --- detectAnomalousTransactions ---
    @Test
    void detectAnomalousTransactions_success() {
        Integer companyId = 1;
        DateRange dateRange = new DateRange(LocalDate.now().minusDays(10), LocalDate.now());

        AITransactionData txn1 = AITransactionData.builder()
                .description("Taxi")
                .amount(100.0)
                .currency("CNY")
                .transactionType("EXPENSE")
                .companyId(companyId)
                .transactionDate(LocalDate.now().minusDays(5))
                .category("TRAVEL_EXPENSE")
                .build();

        AITransactionData txn2 = AITransactionData.builder()
                .description("Lunch")
                .amount(50.0)
                .currency("CNY")
                .transactionType("EXPENSE")
                .companyId(companyId)
                .transactionDate(LocalDate.now().minusDays(3))
                .category("FOOD_EXPENSE")
                .build();

        when(aiDataService.getTransactionsInRange(eq(companyId), eq(dateRange)))
                .thenReturn(List.of(txn1, txn2));

        AIAnomalyDetectionResult anomaly1 = AIAnomalyDetectionResult.builder()
                .anomalous(true)
                .anomalyScore(0.95)
                .anomalyType("outlier")
                .recommendations(List.of("Check taxi receipts"))
                .build();

        AIAnomalyDetectionResult anomaly2 = AIAnomalyDetectionResult.builder()
                .anomalous(false)
                .anomalyScore(0.1)
                .anomalyType("none")
                .recommendations(List.of())
                .build();

        when(aiService.detectAnomalousTransaction(eq(txn1))).thenReturn(anomaly1);
        when(aiService.detectAnomalousTransaction(eq(txn2))).thenReturn(anomaly2);

        List<AnomalousTransactionDTO> result = aiApplicationService.detectAnomalousTransactions(companyId, dateRange);

        assertEquals(1, result.size());
        assertEquals("Taxi", result.get(0).getDescription());
        assertEquals(0.95, result.get(0).getAnomalyScore());
        assertEquals("outlier", result.get(0).getAnomalyType());
    }

    @Test
    void detectAnomalousTransactions_aiDataServiceFails_returnsEmptyList() {
        Integer companyId = 1;
        DateRange dateRange = new DateRange(LocalDate.now().minusDays(10), LocalDate.now());

        when(aiDataService.getTransactionsInRange(anyInt(), any())).thenThrow(new RuntimeException("DB down"));

        List<AnomalousTransactionDTO> result = aiApplicationService.detectAnomalousTransactions(companyId, dateRange);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // --- generateReportInsights ---
    @Test
    void generateReportInsights_success() {
        String reportData = "data";
        String reportType = "summary";
        when(aiService.generateReportInsights(reportData, reportType))
                .thenReturn(AIReportInsightResult.builder().insightSummary("Insight!").build());

        String result = aiApplicationService.generateReportInsights(reportData, reportType);

        assertEquals("Insight!", result);
    }

    @Test
    void generateReportInsights_aiServiceFails_returnsFallback() {
        String reportData = "data";
        String reportType = "summary";
        when(aiService.generateReportInsights(anyString(), anyString()))
                .thenThrow(new RuntimeException("AI down"));

        String result = aiApplicationService.generateReportInsights(reportData, reportType);

        assertEquals("Unable to generate insights at this time. Please try again later.", result);
    }
}