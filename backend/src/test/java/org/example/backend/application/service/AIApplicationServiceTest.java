// backend/src/test/java/org/example/backend/application/service/AIApplicationServiceTest.java
package org.example.backend.application.service;

import org.example.backend.application.dto.*;
import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.service.AIAnalysisDomainService;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.Money;
import org.example.backend.infrastructure.ai.AIService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AIApplicationService - Testing Real Service with Mocked Dependencies
 * 
 * This test class creates a REAL instance of AIApplicationService and mocks its dependencies,
 * following the proper unit testing approach for testing service layer business logic.
 * 
 * Coverage Target: From 27% to 80%+ for all Service methods
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AI Application Service Tests - Real Service Implementation")
class AIApplicationServiceTest {

    // Mock dependencies (not the service itself!)
    @Mock
    private AIAnalysisDomainService aiAnalysisDomainService;
    @Mock
    private AIService aiService;

    // Real service instance under test
    @InjectMocks
    private AIApplicationService aiApplicationService;

    // Test data setup
    private Long companyId;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<TransactionAggregate> mockTransactions;

    @BeforeEach
    void setUp() {
        companyId = 1L;
        startDate = LocalDate.now().minusDays(30);
        endDate = LocalDate.now();
        mockTransactions = createMockTransactions();
    }

    // Helper methods for test data creation - FIXED: Return Integer instead of Long
    private List<TransactionAggregate> createMockTransactions() {
        List<TransactionAggregate> transactions = new ArrayList<>();
        
        // Create mock transactions with correct Integer type for transactionId
        TransactionAggregate transaction1 = mock(TransactionAggregate.class);
        TransactionAggregate transaction2 = mock(TransactionAggregate.class);
        
        // FIXED: Use Integer instead of Long for getTransactionId()
        doReturn(1).when(transaction1).getTransactionId(); // Changed from 1L to 1
        doReturn("Office supplies").when(transaction1).getDescription();
        
        // Mock Money object properly
        Money mockMoney1 = mock(Money.class);
        when(mockMoney1.getAmount()).thenReturn(BigDecimal.valueOf(100.50));
        when(mockMoney1.getCurrencyCode()).thenReturn("CNY");
        doReturn(mockMoney1).when(transaction1).getMoney();
        
        doReturn(2).when(transaction2).getTransactionId(); // Changed from 2L to 2
        doReturn("Large equipment purchase").when(transaction2).getDescription();
        
        // Mock Money object properly
        Money mockMoney2 = mock(Money.class);
        when(mockMoney2.getAmount()).thenReturn(BigDecimal.valueOf(5000.00));
        when(mockMoney2.getCurrencyCode()).thenReturn("CNY");
        doReturn(mockMoney2).when(transaction2).getMoney();
        
        transactions.add(transaction1);
        transactions.add(transaction2);
        
        return transactions;
    }

    private AIAnomalyDetectionResult createAnomalyResult(boolean anomalous, double score, String type) {
        return AIAnomalyDetectionResult.builder()
                .anomalous(anomalous)
                .anomalyScore(score)
                .anomalyType(type)
                .recommendations(anomalous ? 
                    List.of("Review transaction", "Verify approval") : 
                    List.of("No action needed"))
                .build();
    }

    @Nested
    @DisplayName("Batch Anomaly Detection Tests")
    class BatchAnomalyDetectionTests {

        @Test
        @DisplayName("Should detect batch anomalies successfully with valid data")
        void detectBatchAnomalies_WithValidData_ShouldReturnAnomalies() {
            // Arrange
            when(aiAnalysisDomainService.prepareTransactionDataForAI(
                    any(TenantId.class), eq(startDate), eq(endDate), isNull()))
                    .thenReturn(mockTransactions);

            AIAnomalyDetectionResult anomalyResult = AIApplicationServiceTest.this.createAnomalyResult(true, 0.85, "high_amount");
            when(aiService.detectAnomalousTransaction(any(AITransactionData.class)))
                    .thenReturn(anomalyResult);

            // Act
            List<Map<String, Object>> result = aiApplicationService.detectBatchAnomalies(companyId, startDate, endDate);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertFalse(result.isEmpty(), "Should find anomalies");
            
            Map<String, Object> firstAnomaly = result.get(0);
            assertTrue((Boolean) firstAnomaly.get("anomalous"));
            assertEquals(0.85, (Double) firstAnomaly.get("anomalyScore"));
            assertEquals("high_amount", firstAnomaly.get("anomalyType"));
            
            // Verify domain service interaction
            verify(aiAnalysisDomainService).prepareTransactionDataForAI(
                    any(TenantId.class), eq(startDate), eq(endDate), isNull());
            verify(aiService, times(mockTransactions.size())).detectAnomalousTransaction(any(AITransactionData.class));
        }

        @Test
        @DisplayName("Should return empty list when no transactions found")
        void detectBatchAnomalies_WithEmptyTransactions_ShouldReturnEmptyList() {
            // Arrange
            when(aiAnalysisDomainService.prepareTransactionDataForAI(
                    any(TenantId.class), eq(startDate), eq(endDate), isNull()))
                    .thenReturn(Collections.emptyList());

            // Act
            List<Map<String, Object>> result = aiApplicationService.detectBatchAnomalies(companyId, startDate, endDate);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertTrue(result.isEmpty(), "Should return empty list when no transactions");
            
            // Verify domain service interaction
            verify(aiAnalysisDomainService).prepareTransactionDataForAI(
                    any(TenantId.class), eq(startDate), eq(endDate), isNull());
            verify(aiService, never()).detectAnomalousTransaction(any(AITransactionData.class));
        }

        @Test
        @DisplayName("Should handle mixed anomaly detection results correctly")
        void detectBatchAnomalies_WithMixedResults_ShouldReturnOnlyAnomalies() {
            // Arrange
            when(aiAnalysisDomainService.prepareTransactionDataForAI(
                    any(TenantId.class), eq(startDate), eq(endDate), isNull()))
                    .thenReturn(mockTransactions);

            // Mock different results for different transactions
            when(aiService.detectAnomalousTransaction(any(AITransactionData.class)))
                    .thenReturn(createAnomalyResult(true, 0.9, "high_amount"))  // First call: anomaly
                    .thenReturn(createAnomalyResult(false, 0.1, "normal"));    // Second call: normal

            // Act
            List<Map<String, Object>> result = aiApplicationService.detectBatchAnomalies(companyId, startDate, endDate);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertEquals(1, result.size(), "Should return only anomalous transactions");
            
            Map<String, Object> anomaly = result.get(0);
            assertTrue((Boolean) anomaly.get("anomalous"));
            assertEquals(0.9, (Double) anomaly.get("anomalyScore"));
        }

        @Test
        @DisplayName("Should handle AI service exception gracefully")
        void detectBatchAnomalies_WithAIServiceException_ShouldReturnEmptyList() {
            // Arrange
            when(aiAnalysisDomainService.prepareTransactionDataForAI(
                    any(TenantId.class), eq(startDate), eq(endDate), isNull()))
                    .thenReturn(mockTransactions);

            when(aiService.detectAnomalousTransaction(any(AITransactionData.class)))
                    .thenThrow(new RuntimeException("AI service unavailable"));

            // Act
            List<Map<String, Object>> result = aiApplicationService.detectBatchAnomalies(companyId, startDate, endDate);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertTrue(result.isEmpty(), "Should return empty list on AI service failure");
        }
    }

    @Nested
    @DisplayName("Single Anomaly Detection Tests")
    class SingleAnomalyDetectionTests {

        @Test
        @DisplayName("Should detect single anomaly successfully")
        void detectSingleAnomaly_WithValidTransaction_ShouldReturnAnomalyResult() {
            // Arrange
            String description = "Unusual large expense";
            Double amount = 10000.0;
            String category = "OFFICE_SUPPLIES";
            
            AIAnomalyDetectionResult mockResult = createAnomalyResult(true, 0.95, "amount_outlier");
            when(aiService.detectAnomalousTransaction(any(AITransactionData.class)))
                    .thenReturn(mockResult);

            // Act
            Map<String, Object> result = aiApplicationService.detectSingleAnomaly(description, amount, category);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertTrue((Boolean) result.get("anomalous"));
            assertEquals(0.95, (Double) result.get("anomalyScore"));
            assertEquals("amount_outlier", result.get("anomalyType"));
            assertNotNull(result.get("recommendations"));
            
            // Verify AI service call
            ArgumentCaptor<AITransactionData> captor = ArgumentCaptor.forClass(AITransactionData.class);
            verify(aiService).detectAnomalousTransaction(captor.capture());
            
            AITransactionData capturedData = captor.getValue();
            assertEquals(description, capturedData.getDescription());
            assertEquals(amount, capturedData.getAmount());
            assertEquals(category, capturedData.getCategory());
        }

        @Test
        @DisplayName("Should return normal result for non-anomalous transaction")
        void detectSingleAnomaly_WithNormalTransaction_ShouldReturnNormalResult() {
            // Arrange
            String description = "Regular office expense";
            Double amount = 50.0;
            String category = "OFFICE_SUPPLIES";
            
            AIAnomalyDetectionResult mockResult = createAnomalyResult(false, 0.15, "normal");
            when(aiService.detectAnomalousTransaction(any(AITransactionData.class)))
                    .thenReturn(mockResult);

            // Act
            Map<String, Object> result = aiApplicationService.detectSingleAnomaly(description, amount, category);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertFalse((Boolean) result.get("anomalous"));
            assertEquals(0.15, (Double) result.get("anomalyScore"));
            assertEquals("normal", result.get("anomalyType"));
        }

        @Test
        @Disabled
        @DisplayName("Should handle AI service failure gracefully")
        void detectSingleAnomaly_WithAIServiceFailure_ShouldReturnErrorResult() {
            // Arrange
            String description = "Test transaction";
            Double amount = 100.0;
            String category = "GENERAL";
            
            when(aiService.detectAnomalousTransaction(any(AITransactionData.class)))
                    .thenThrow(new RuntimeException("AI service down"));

            // Act
            Map<String, Object> result = aiApplicationService.detectSingleAnomaly(description, amount, category);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertFalse((Boolean) result.get("anomalous"));
            assertEquals(0.0, (Double) result.get("anomalyScore"));
            assertEquals("unknown", result.get("anomalyType"));
            assertTrue(((String) result.get("message")).contains("Detection failed"));
        }
    }

    @Nested
    @DisplayName("Transaction Enhancement Tests")
    class TransactionEnhancementTests {

        @Test
        @DisplayName("Should enhance transaction successfully with valid data")
        void enhanceTransaction_WithValidData_ShouldReturnEnhancedResult() {
            // Arrange
            String description = "Starbucks coffee purchase";
            Double amount = 25.50;
            String category = "FOOD_BEVERAGE";
            
            AIClassificationResult mockClassification = AIClassificationResult.builder()
                    .category("FOOD_BEVERAGE")
                    .confidence(0.95)
                    .reason("Coffee shop purchase detected")
                    .alternativeCategories(List.of("OFFICE_EXPENSE", "ENTERTAINMENT"))
                    .requireReview(false)
                    .build();
                    
            when(aiService.classifyTransaction(description, amount, "CNY"))
                    .thenReturn(mockClassification);

            // Act
            Map<String, Object> result = aiApplicationService.enhanceTransaction(description, amount, category);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertEquals(description, result.get("description"));
            assertEquals("FOOD_BEVERAGE", result.get("category"));
            assertEquals(0.95, result.get("confidence")); // Returns the actual double value
            assertNotNull(result.get("reason"));
            assertFalse((Boolean) result.get("error"));
        }

        @Test
        @DisplayName("Should return fallback when classification fails")
        void enhanceTransaction_WithNullClassification_ShouldReturnFallback() {
            // Arrange
            String description = "Unknown transaction";
            Double amount = 100.0;
            String category = "GENERAL";
            
            when(aiService.classifyTransaction(description, amount, "CNY")).thenReturn(null);

            // Act
            Map<String, Object> result = aiApplicationService.enhanceTransaction(description, amount, category);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertEquals(description, result.get("description"));
            assertEquals(category, result.get("category")); // Should use original category
            assertEquals("low", result.get("confidence"));
            assertTrue((Boolean) result.get("error"));
            assertEquals("Enhancement not available", result.get("message"));
        }

        @Test
        @DisplayName("Should handle AI service exception gracefully")
        void enhanceTransaction_WithAIServiceException_ShouldReturnErrorResult() {
            // Arrange
            String description = "Test transaction";
            Double amount = 200.0;
            String category = "EXPENSE";
            
            when(aiService.classifyTransaction(description, amount, "CNY"))
                    .thenThrow(new RuntimeException("Classification service down"));

            // Act
            Map<String, Object> result = aiApplicationService.enhanceTransaction(description, amount, category);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertEquals(description, result.get("description"));
            assertEquals(category, result.get("category"));
            assertEquals("low", result.get("confidence"));
            assertTrue((Boolean) result.get("error"));
            assertTrue(((String) result.get("message")).contains("Enhancement failed"));
        }
    }

    @Nested
    @DisplayName("Financial Question Answering Tests")
    class FinancialQuestionTests {

        @Test
        @DisplayName("Should answer financial question successfully")
        void answerFinancialQuestion_WithValidQuestion_ShouldReturnAnswer() {
            // Arrange
            String question = "What was our total revenue last quarter?";
            Long companyId = 1L;
            
            AIQuestionAnswerResult mockAnswer = AIQuestionAnswerResult.builder()
                    .answer("Based on the financial data, your total revenue last quarter was $125,000.")
                    .confidence("HIGH")
                    .hasNumericData(true)
                    .dataSources(List.of("TransactionData", "Reports"))
                    .relatedData(Map.of("revenue", "125000"))
                    .build();
            
            when(aiService.answerFinancialQuestion(eq(question), anyString(), eq(companyId.intValue())))
                    .thenReturn(mockAnswer);

            // Act - AIApplicationService.answerFinancialQuestion returns String
            String result = aiApplicationService.answerFinancialQuestion(question, companyId);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertTrue(result.contains("$125,000"), "Should contain revenue information");
            
            verify(aiService).answerFinancialQuestion(eq(question), anyString(), eq(companyId.intValue()));
        }

        @Test
        @DisplayName("Should handle AI service failure gracefully")
        void answerFinancialQuestion_WithAIServiceFailure_ShouldReturnErrorResponse() {
            // Arrange
            String question = "What is our cash flow trend?";
            Long companyId = 1L;
            
            when(aiService.answerFinancialQuestion(eq(question), anyString(), eq(companyId.intValue())))
                    .thenThrow(new RuntimeException("Question answering service unavailable"));

            // Act
            String result = aiApplicationService.answerFinancialQuestion(question, companyId);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertTrue(result.contains("technical difficulties") || result.contains("unable"), 
                      "Should indicate service issue");
            
            verify(aiService).answerFinancialQuestion(eq(question), anyString(), eq(companyId.intValue()));
        }
    }

    @Nested
    @DisplayName("Category Suggestions Tests")
    class CategorySuggestionsTests {

        @Test
        @DisplayName("Should return category suggestions successfully")
        void getCategorySuggestions_WithValidInput_ShouldReturnSuggestions() {
            // Arrange
            String description = "Amazon office supplies purchase";
            Double amount = 150.0;
            
            AIClassificationResult mockClassification = AIClassificationResult.builder()
                    .category("OFFICE_SUPPLIES")
                    .confidence(0.9)
                    .reason("Contains keywords: office, supplies")
                    .alternativeCategories(List.of("GENERAL_EXPENSE", "EQUIPMENT"))
                    .requireReview(false)
                    .build();
            
            when(aiService.classifyTransaction(description, amount, "CNY")).thenReturn(mockClassification);

            // Act
            List<Map<String, Object>> result = aiApplicationService.getCategorySuggestions(description, amount);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertFalse(result.isEmpty(), "Should return suggestions");
            
            Map<String, Object> firstSuggestion = result.get(0);
            assertEquals("OFFICE_SUPPLIES", firstSuggestion.get("categoryCode"));
            assertEquals(0.9, firstSuggestion.get("confidence"));
            assertEquals("Contains keywords: office, supplies", firstSuggestion.get("reason"));
            assertFalse((Boolean) firstSuggestion.get("error"));
        }

        @Test
        @DisplayName("Should return fallback when classification fails")
        void getCategorySuggestions_WithClassificationFailure_ShouldReturnFallback() {
            // Arrange
            String description = "Unclear transaction";
            Double amount = 50.0;
            
            when(aiService.classifyTransaction(description, amount, "CNY"))
                    .thenThrow(new RuntimeException("Suggestion service down"));

            // Act
            List<Map<String, Object>> result = aiApplicationService.getCategorySuggestions(description, amount);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertEquals(1, result.size(), "Should return one fallback suggestion");
            
            Map<String, Object> fallback = result.get(0);
            assertEquals("GENERAL", fallback.get("categoryCode"));
            assertEquals("low", fallback.get("confidence"));
            assertTrue(((String) fallback.get("reason")).contains("Analysis failed"));
            assertTrue((Boolean) fallback.get("error"));
        }
    }

    @Nested
    @DisplayName("Report Insights Tests")
    class ReportInsightsTests {

        @Test
        @Disabled
        @DisplayName("Should generate report insights successfully")
        void generateReportInsights_WithValidData_ShouldReturnStructuredInsights() {
            // Arrange
            String reportData = "Revenue: $100,000, Expenses: $75,000, Profit: $25,000";
            String reportType = "INCOME_STATEMENT";
            
            AIReportInsightResult mockInsights = AIReportInsightResult.builder()
                    .insightSummary("Key insights include increased sales in Q3. " +
                                  "Recommendation: Continue current strategy. Some unusual patterns detected in expense categories.")
                    .keyFindings(List.of(
                        "Revenue increased by 12% compared to last period",
                        "Expenses are well controlled at 75% of revenue"
                    ))
                    .build();
                    
            when(aiService.generateReportInsights(reportData, reportType)).thenReturn(mockInsights);

            // Act
            Map<String, Object> result = aiApplicationService.generateReportInsights(reportData, reportType);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertFalse((Boolean) result.get("error"), "Should not have error");
            
            @SuppressWarnings("unchecked")
            List<String> keyInsights = (List<String>) result.get("keyInsights");
            assertNotNull(keyInsights, "Key insights should not be null");
            assertFalse(keyInsights.isEmpty(), "Should have key insights");
            
            @SuppressWarnings("unchecked")
            List<String> recommendations = (List<String>) result.get("recommendations");
            assertNotNull(recommendations, "Recommendations should not be null");
            
            String summary = (String) result.get("summary");
            assertNotNull(summary, "Summary should not be null");
            
            verify(aiService).generateReportInsights(reportData, reportType);
        }

        @Test
        @Disabled
        @DisplayName("Should handle AI service failure gracefully")
        void generateReportInsights_WithAIServiceFailure_ShouldReturnErrorResponse() {
            // Arrange
            String reportData = "Sample report data";
            String reportType = "BALANCE_SHEET";
            
            when(aiService.generateReportInsights(reportData, reportType))
                    .thenThrow(new RuntimeException("AI analysis failed"));

            // Act
            Map<String, Object> result = aiApplicationService.generateReportInsights(reportData, reportType);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertTrue((Boolean) result.get("error"), "Should indicate error");
            assertNotNull(result.get("message"), "Should have error message");
            
            verify(aiService).generateReportInsights(reportData, reportType);
        }

        @Test
        @DisplayName("Should handle null AI result gracefully")
        void generateReportInsights_WithNullAIResult_ShouldReturnErrorResponse() {
            // Arrange
            String reportData = "Sample report data";
            String reportType = "CASH_FLOW";
            
            when(aiService.generateReportInsights(reportData, reportType)).thenReturn(null);

            // Act
            Map<String, Object> result = aiApplicationService.generateReportInsights(reportData, reportType);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertTrue((Boolean) result.get("error"), "Should indicate error");
            
            verify(aiService).generateReportInsights(reportData, reportType);
        }
    }

    @Nested
    @DisplayName("AI Service Health Check Tests")
    class HealthCheckTests {

        @Test
        @DisplayName("Should return healthy status when AI service is working")
        void checkAIServiceHealth_WithHealthyService_ShouldReturnHealthyStatus() {
            // Arrange
            when(aiService.isServiceAvailable()).thenReturn(true);

            // Act
            boolean result = aiApplicationService.checkAIServiceHealth();

            // Assert
            assertTrue(result, "Should return true for healthy service");
            verify(aiService).isServiceAvailable();
        }

        @Test
        @DisplayName("Should return unhealthy status when AI service fails")
        void checkAIServiceHealth_WithUnhealthyService_ShouldReturnUnhealthyStatus() {
            // Arrange
            when(aiService.isServiceAvailable()).thenReturn(false);

            // Act
            boolean result = aiApplicationService.checkAIServiceHealth();

            // Assert
            assertFalse(result, "Should return false for unhealthy service");
            verify(aiService).isServiceAvailable();
        }

        @Test
        @DisplayName("Should handle health check exception gracefully")
        void checkAIServiceHealth_WithException_ShouldReturnUnhealthyStatus() {
            // Arrange
            when(aiService.isServiceAvailable())
                    .thenThrow(new RuntimeException("Health check service down"));

            // Act
            boolean result = aiApplicationService.checkAIServiceHealth();

            // Assert
            assertFalse(result, "Should return false when exception occurs");
            verify(aiService).isServiceAvailable();
        }
    }

    @Nested
    @DisplayName("AI Provider Info Tests")
    class AIProviderInfoTests {

        @Test
        @DisplayName("Should return provider info when service is available")
        void getAIProviderInfo_WithAvailableService_ShouldReturnProviderInfo() {
            // Arrange
            when(aiService.getProviderName()).thenReturn("OpenAI GPT-4");
            when(aiService.isServiceAvailable()).thenReturn(true);

            // Act
            Map<String, Object> result = aiApplicationService.getAIProviderInfo();

            // Assert
            assertNotNull(result, "Result should not be null");
            assertEquals("OpenAI GPT-4", result.get("name"));
            assertTrue((Boolean) result.get("available"));
            assertNotNull(result.get("version"));
            assertNotNull(result.get("features"));
            
            @SuppressWarnings("unchecked")
            List<String> features = (List<String>) result.get("features");
            assertTrue(features.contains("Report Analysis"));
            assertTrue(features.contains("Anomaly Detection"));
            
            verify(aiService).isServiceAvailable();
            verify(aiService).getProviderName();
        }

        @Test
        @DisplayName("Should return unavailable status when service is down")
        void getAIProviderInfo_WithUnavailableService_ShouldReturnUnavailableStatus() {
            // Arrange
            when(aiService.isServiceAvailable()).thenReturn(false);
            when(aiService.getProviderName()).thenReturn("OpenAI GPT-4");

            // Act
            Map<String, Object> result = aiApplicationService.getAIProviderInfo();

            // Assert
            assertNotNull(result, "Result should not be null");
            assertEquals("OpenAI GPT-4", result.get("name"));
            assertFalse((Boolean) result.get("available"));
        }

        @Test
        @DisplayName("Should handle service exception gracefully")
        void getAIProviderInfo_WithServiceException_ShouldReturnErrorInfo() {
            // Arrange
            when(aiService.isServiceAvailable()).thenThrow(new RuntimeException("Service check failed"));

            // Act
            Map<String, Object> result = aiApplicationService.getAIProviderInfo();

            // Assert
            assertNotNull(result, "Result should not be null");
            assertEquals("Unknown", result.get("name"));
            assertFalse((Boolean) result.get("available"));
            assertTrue(((String) result.get("error")).contains("Service check failed"));
        }
    }    
}