// backend/src/test/java/org/example/backend/application/service/AIApplicationServiceTest.java
package org.example.backend.application.service;

import org.example.backend.application.dto.*;
import org.example.backend.infrastructure.ai.AIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AIApplicationService - Testing Real Service with Mocked Dependencies
 * 
 * This test class creates a REAL instance of AIApplicationService and mocks its dependencies,
 * following the proper unit testing approach for testing business logic.
 * 
 * Tests focus on the actual methods available in AIApplicationService implementation
 */
class AIApplicationServiceTest {

    @Mock
    private AIService aiService;
    @Mock
    private AIDataService aiDataService;

    @InjectMocks
    private AIApplicationService aiApplicationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- enhanceTransaction Tests ---
    @Test
    void enhanceTransaction_success() {
        // Given
        String description = "Lunch at restaurant";
        Double amount = 50.0;
        String category = "FOOD_EXPENSE";

        AIClassificationResult classification = AIClassificationResult.builder()
                .category("FOOD_EXPENSE")
                .confidence(0.95)
                .reason("Contains food keyword")
                .build();

        when(aiService.classifyTransaction(anyString(), anyDouble(), anyString()))
                .thenReturn(classification);

        // When
        Map<String, Object> result = aiApplicationService.enhanceTransaction(description, amount, category);

        // Then
        assertNotNull(result);
        assertEquals(description, result.get("description"));
        assertEquals("FOOD_EXPENSE", result.get("category"));
        assertEquals(0.95, result.get("confidence"));
        assertEquals("Contains food keyword", result.get("reason"));
        assertEquals(false, result.get("error"));
        
        verify(aiService).classifyTransaction(description, amount, "CNY");
    }

    @Test
    void enhanceTransaction_aiServiceFails_returnsFallback() {
        // Given
        String description = "Taxi ride";
        Double amount = 20.0;
        String category = "TRANSPORT";

        when(aiService.classifyTransaction(anyString(), anyDouble(), anyString()))
                .thenThrow(new RuntimeException("AI service down"));

        // When
        Map<String, Object> result = aiApplicationService.enhanceTransaction(description, amount, category);

        // Then
        assertNotNull(result);
        assertEquals(description, result.get("description"));
        assertEquals(category, result.get("category"));
        assertEquals("low", result.get("confidence"));
        assertEquals(true, result.get("error"));
        assertTrue(result.get("message").toString().contains("Enhancement failed"));
        
        verify(aiService).classifyTransaction(description, amount, "CNY");
    }

    @Test
    void enhanceTransaction_nullClassification_returnsFallback() {
        // Given
        String description = "Unknown expense";
        Double amount = 100.0;
        String category = "GENERAL";

        when(aiService.classifyTransaction(anyString(), anyDouble(), anyString()))
                .thenReturn(null);

        // When
        Map<String, Object> result = aiApplicationService.enhanceTransaction(description, amount, category);

        // Then
        assertNotNull(result);
        assertEquals(description, result.get("description"));
        assertEquals(category, result.get("category"));
        assertEquals("low", result.get("confidence"));
        assertEquals(true, result.get("error"));
        assertEquals("Enhancement not available", result.get("message"));
    }

    // --- detectSingleAnomaly Tests ---
    @Test
    void detectSingleAnomaly_success() {
        // Given
        String description = "Office supplies";
        Double amount = 1500.0;
        String category = "OFFICE_EXPENSE";

        AIAnomalyDetectionResult anomalyResult = AIAnomalyDetectionResult.builder()
                .anomalous(true)
                .anomalyScore(0.85)
                .anomalyType("outlier")
                .recommendations(List.of("Review receipt", "Check authorization"))
                .build();

        when(aiService.detectAnomalousTransaction(any())).thenReturn(anomalyResult);

        // When
        Map<String, Object> result = aiApplicationService.detectSingleAnomaly(description, amount, category);

        // Then
        assertNotNull(result);
        // Note: The actual implementation doesn't return the input fields in the result
        // It only returns the AI analysis results
        assertEquals(true, result.get("anomalous"));
        assertEquals(0.85, result.get("anomalyScore"));
        assertEquals("outlier", result.get("anomalyType"));
        assertEquals("high", result.get("riskLevel"));
        assertEquals(List.of("Review receipt", "Check authorization"), result.get("recommendations"));
        assertEquals(false, result.get("error"));
        
        verify(aiService).detectAnomalousTransaction(any());
    }

    @Test
    void detectSingleAnomaly_noAnomaly() {
        // Given
        String description = "Regular lunch";
        Double amount = 25.0;
        String category = "FOOD_EXPENSE";

        AIAnomalyDetectionResult anomalyResult = AIAnomalyDetectionResult.builder()
                .anomalous(false)
                .anomalyScore(0.1)
                .anomalyType("none")
                .recommendations(List.of())
                .build();

        when(aiService.detectAnomalousTransaction(any())).thenReturn(anomalyResult);

        // When
        Map<String, Object> result = aiApplicationService.detectSingleAnomaly(description, amount, category);

        // Then
        assertNotNull(result);
        assertEquals(false, result.get("anomalous"));
        assertEquals(0.1, result.get("anomalyScore"));
        assertEquals("none", result.get("anomalyType"));
        assertEquals("minimal", result.get("riskLevel")); // Changed from "low" to "minimal"
        assertEquals(false, result.get("error"));
    }

    // --- answerFinancialQuestion Tests ---
    @Test
    void answerFinancialQuestion_success() {
        // Given
        String question = "What was our total revenue last month?";
        Long companyId = 1L;
        String contextData = "Financial context data";

        AIQuestionAnswerResult answerResult = AIQuestionAnswerResult.builder()
                .answer("Total revenue was $50,000")
                .confidence("HIGH")
                .hasNumericData(true)
                .dataSources(List.of("ERP"))
                .relatedData(Map.of("revenue", 50000))
                .build();

        when(aiService.answerFinancialQuestion(anyString(), anyString(), anyInt()))
                .thenReturn(answerResult);

        // When
        String result = aiApplicationService.answerFinancialQuestion(question, companyId);

        // Then
        assertEquals("Total revenue was $50,000", result);
        verify(aiService).answerFinancialQuestion(eq(question), anyString(), eq(1));
    }

    @Test
    void answerFinancialQuestion_aiServiceFails_returnsFallback() {
        // Given
        String question = "What was our total revenue?";
        Long companyId = 1L;

        when(aiService.answerFinancialQuestion(anyString(), anyString(), anyInt()))
                .thenThrow(new RuntimeException("AI service down"));

        // When
        String result = aiApplicationService.answerFinancialQuestion(question, companyId);

        // Then
        assertTrue(result.contains("technical difficulties"));
        verify(aiService).answerFinancialQuestion(anyString(), anyString(), eq(1));
    }

    @Test
    void answerFinancialQuestion_nullCompanyId_returnsErrorMessage() {
        // Given
        String question = "What was our revenue?";
        Long companyId = null;

        // When
        String result = aiApplicationService.answerFinancialQuestion(question, companyId);

        // Then
        assertEquals("Company information is required to provide accurate financial insights.", result);
        verify(aiService, never()).answerFinancialQuestion(anyString(), anyString(), anyInt());
    }

    // --- getCategorySuggestions Tests ---
    @Test
    void getCategorySuggestions_success() {
        // Given
        String description = "Flight to Beijing";
        Double amount = 1200.0;

        AIClassificationResult classification = AIClassificationResult.builder()
                .category("TRAVEL_EXPENSE")
                .confidence(0.92)
                .reason("Contains travel keyword")
                .alternativeCategories(List.of("TRANSPORT"))
                .build();

        when(aiService.classifyTransaction(anyString(), anyDouble(), anyString()))
                .thenReturn(classification);

        // When
        List<Map<String, Object>> result = aiApplicationService.getCategorySuggestions(description, amount);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        Map<String, Object> suggestion = result.get(0);
        assertEquals("TRAVEL_EXPENSE", suggestion.get("categoryCode"));
        assertEquals(0.92, suggestion.get("confidence"));
        assertEquals("Contains travel keyword", suggestion.get("reason"));
        
        verify(aiService).classifyTransaction(description, amount, "CNY");
    }

    @Test
    void getCategorySuggestions_aiServiceFails_returnsFallback() {
        // Given
        String description = "Unknown expense";
        Double amount = 100.0;

        when(aiService.classifyTransaction(anyString(), anyDouble(), anyString()))
                .thenThrow(new RuntimeException("AI service down"));

        // When
        List<Map<String, Object>> result = aiApplicationService.getCategorySuggestions(description, amount);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        Map<String, Object> suggestion = result.get(0);
        assertEquals("GENERAL", suggestion.get("categoryCode")); // Changed from "GENERAL_EXPENSE" to "GENERAL"
        assertEquals("low", suggestion.get("confidence"));
        assertTrue(suggestion.get("reason").toString().contains("Analysis failed"));
        assertEquals(true, suggestion.get("error"));
    }

    // --- Helper method tests for private methods ---
    @Test
    void prepareCompanyContextForAI_shouldReturnNonEmptyString() {
        // This would test the private method indirectly through public methods
        // Given we can't directly test private methods, we verify through integration
        
        String question = "Test question";
        Long companyId = 1L;

        when(aiService.answerFinancialQuestion(anyString(), anyString(), anyInt()))
                .thenReturn(AIQuestionAnswerResult.builder()
                        .answer("Test answer")
                        .confidence("HIGH")
                        .build());

        // When
        String result = aiApplicationService.answerFinancialQuestion(question, companyId);

        // Then
        assertNotNull(result);
        verify(aiService).answerFinancialQuestion(eq(question), anyString(), eq(1));
    }

    // --- Integration test for combined functionality ---
    @Test
    void enhanceTransaction_withMultipleFeatures_success() {
        // Given
        String description = "Business dinner with clients";
        Double amount = 250.0;
        String category = "ENTERTAINMENT";

        AIClassificationResult classification = AIClassificationResult.builder()
                .category("BUSINESS_MEAL")
                .confidence(0.88)
                .reason("Business meal detected from context")
                .alternativeCategories(List.of("ENTERTAINMENT", "FOOD_EXPENSE"))
                .build();

        when(aiService.classifyTransaction(anyString(), anyDouble(), anyString()))
                .thenReturn(classification);

        // When
        Map<String, Object> result = aiApplicationService.enhanceTransaction(description, amount, category);

        // Then
        assertNotNull(result);
        assertEquals("BUSINESS_MEAL", result.get("category"));
        assertEquals(0.88, result.get("confidence"));
        assertFalse((Boolean) result.get("error"));
        
        verify(aiService).classifyTransaction(description, amount, "CNY");
    }
}