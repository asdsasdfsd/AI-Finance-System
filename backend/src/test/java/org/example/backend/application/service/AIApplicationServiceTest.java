// backend/src/test/java/org/example/backend/application/service/AIApplicationServiceTest.java
package org.example.backend.application.service;

import org.example.backend.application.dto.*;
import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.service.AIAnalysisDomainService;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.infrastructure.ai.AIService;

import org.junit.jupiter.api.BeforeEach;
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

    // Helper methods for test data creation - moved to class level
    private List<TransactionAggregate> createMockTransactions() {
        List<TransactionAggregate> transactions = new ArrayList<>();
        
        // Create mock transactions - simplified to avoid type issues
        TransactionAggregate transaction1 = mock(TransactionAggregate.class);
        TransactionAggregate transaction2 = mock(TransactionAggregate.class);
        
        // Configure basic mock behavior without complex type matching
        doReturn(1L).when(transaction1).getTransactionId();
        doReturn("Office supplies").when(transaction1).getDescription();
        doReturn(mock(Object.class)).when(transaction1).getMoney();
        
        doReturn(2L).when(transaction2).getTransactionId();
        doReturn("Large equipment purchase").when(transaction2).getDescription();
        doReturn(mock(Object.class)).when(transaction2).getMoney();
        
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

    @SuppressWarnings("unused")
    private Object createMockMoney(Double amount, String currency) {
        return mock(Object.class, "MockMoney_" + amount + "_" + currency);
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
        @DisplayName("Should handle empty transaction list gracefully")
        void detectBatchAnomalies_WithEmptyTransactions_ShouldReturnEmptyList() {
            // Arrange
            when(aiAnalysisDomainService.prepareTransactionDataForAI(
                    any(TenantId.class), eq(startDate), eq(endDate), isNull()))
                    .thenReturn(Collections.emptyList());

            // Act
            List<Map<String, Object>> result = aiApplicationService.detectBatchAnomalies(companyId, startDate, endDate);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertTrue(result.isEmpty(), "Should return empty list for no transactions");
            
            verify(aiAnalysisDomainService).prepareTransactionDataForAI(
                    any(TenantId.class), eq(startDate), eq(endDate), isNull());
            verify(aiService, never()).detectAnomalousTransaction(any(AITransactionData.class));
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
            assertTrue(result.isEmpty(), "Should return empty list on AI service error");
            
            verify(aiAnalysisDomainService).prepareTransactionDataForAI(
                    any(TenantId.class), eq(startDate), eq(endDate), isNull());
        }

        @Test
        @DisplayName("Should filter non-anomalous transactions correctly")
        void detectBatchAnomalies_WithMixedResults_ShouldReturnOnlyAnomalies() {
            // Arrange
            when(aiAnalysisDomainService.prepareTransactionDataForAI(
                    any(TenantId.class), eq(startDate), eq(endDate), isNull()))
                    .thenReturn(mockTransactions);

            // First transaction is anomalous, second is not
            AIAnomalyDetectionResult anomalyResult = AIApplicationServiceTest.this.createAnomalyResult(true, 0.90, "suspicious_pattern");
            AIAnomalyDetectionResult normalResult = AIApplicationServiceTest.this.createAnomalyResult(false, 0.10, "normal");
            
            when(aiService.detectAnomalousTransaction(any(AITransactionData.class)))
                    .thenReturn(anomalyResult, normalResult);

            // Act
            List<Map<String, Object>> result = aiApplicationService.detectBatchAnomalies(companyId, startDate, endDate);

            // Assert
            assertEquals(1, result.size(), "Should return only anomalous transactions");
            assertTrue((Boolean) result.get(0).get("anomalous"));
            assertEquals(0.90, (Double) result.get(0).get("anomalyScore"));
        }
    }

    @Nested
    @DisplayName("Report Insights Generation Tests")
    class ReportInsightsTests {

        @Test
        @DisplayName("Should generate structured report insights successfully")
        void generateReportInsights_WithValidData_ShouldReturnStructuredInsights() {
            // Arrange
            String reportData = "Sample financial report with revenue and expenses";
            String reportType = "INCOME_STATEMENT";
            
            AIReportInsightResult mockResult = AIReportInsightResult.builder()
                    .insightSummary("This report shows strong revenue growth. Key insights include increased sales in Q3. " +
                                  "Recommendation: Continue current strategy. Some unusual patterns detected in expense categories.")
                    .build();
            
            when(aiService.generateReportInsights(reportData, reportType)).thenReturn(mockResult);

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
    @DisplayName("Single Transaction Anomaly Detection Tests")
    class SingleAnomalyDetectionTests {

        @Test
        @DisplayName("Should detect single transaction anomaly successfully")
        void detectSingleAnomaly_WithValidTransaction_ShouldReturnAnomalyResult() {
            // Arrange
            String description = "Suspicious large payment";
            Double amount = 50000.0;
            String category = "MISC_EXPENSE";
            
            AIAnomalyDetectionResult anomalyResult = AIApplicationServiceTest.this.createAnomalyResult(true, 0.92, "unusually_high_amount");
            when(aiService.detectAnomalousTransaction(any(AITransactionData.class))).thenReturn(anomalyResult);

            // Act
            Map<String, Object> result = aiApplicationService.detectSingleAnomaly(description, amount, category);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertTrue((Boolean) result.get("anomalous"), "Should detect anomaly");
            assertEquals(0.92, (Double) result.get("anomalyScore"));
            assertEquals("unusually_high_amount", result.get("anomalyType"));
            assertEquals("high", result.get("riskLevel"));
            assertEquals("high", result.get("confidence"));
            assertFalse((Boolean) result.get("error"));
            
            // Verify AI service was called with correct data
            ArgumentCaptor<AITransactionData> captor = ArgumentCaptor.forClass(AITransactionData.class);
            verify(aiService).detectAnomalousTransaction(captor.capture());
            
            AITransactionData capturedData = captor.getValue();
            assertEquals(description, capturedData.getDescription());
            assertEquals(amount, capturedData.getAmount());
            assertEquals(category, capturedData.getCategory());
        }

        @Test
        @DisplayName("Should handle normal transaction correctly")
        void detectSingleAnomaly_WithNormalTransaction_ShouldReturnNormalResult() {
            // Arrange
            String description = "Office supplies";
            Double amount = 25.0;
            String category = "OFFICE_EXPENSE";
            
            AIAnomalyDetectionResult normalResult = AIApplicationServiceTest.this.createAnomalyResult(false, 0.05, "normal");
            when(aiService.detectAnomalousTransaction(any(AITransactionData.class))).thenReturn(normalResult);

            // Act
            Map<String, Object> result = aiApplicationService.detectSingleAnomaly(description, amount, category);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertFalse((Boolean) result.get("anomalous"), "Should not detect anomaly");
            assertEquals(0.05, (Double) result.get("anomalyScore"));
            assertEquals("minimal", result.get("riskLevel"));
            assertEquals("low", result.get("confidence"));
            assertFalse((Boolean) result.get("error"));
        }

        @Test
        @DisplayName("Should handle AI service failure gracefully")
        void detectSingleAnomaly_WithAIServiceFailure_ShouldReturnErrorResult() {
            // Arrange
            String description = "Test transaction";
            Double amount = 100.0;
            String category = "EXPENSE";
            
            when(aiService.detectAnomalousTransaction(any(AITransactionData.class)))
                    .thenThrow(new RuntimeException("Service unavailable"));

            // Act
            Map<String, Object> result = aiApplicationService.detectSingleAnomaly(description, amount, category);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertFalse((Boolean) result.get("anomalous"), "Should default to not anomalous");
            assertEquals(0.0, (Double) result.get("anomalyScore"));
            assertEquals("low", result.get("confidence"));
            assertTrue((Boolean) result.get("error"));
            assertTrue(((String) result.get("reason")).contains("Analysis failed"));
        }
    }

    @Nested
    @DisplayName("Transaction Enhancement Tests")
    class TransactionEnhancementTests {

        @Test
        @DisplayName("Should enhance transaction successfully")
        void enhanceTransaction_WithValidData_ShouldReturnEnhancedResult() {
            // Arrange
            String description = "Restaurant lunch meeting";
            Double amount = 45.0;
            String category = "MEAL";
            
            AIClassificationResult classification = AIClassificationResult.builder()
                    .category("BUSINESS_MEAL")
                    .confidence(0.88)
                    .reason("Business meeting detected")
                    .build();
            
            when(aiService.classifyTransaction(description, amount, "CNY")).thenReturn(classification);

            // Act
            Map<String, Object> result = aiApplicationService.enhanceTransaction(description, amount, category);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertEquals(description, result.get("description"));
            assertEquals("BUSINESS_MEAL", result.get("category"));
            assertEquals(0.88, result.get("confidence"));
            assertEquals("Business meeting detected", result.get("reason"));
            assertFalse((Boolean) result.get("error"));
            
            verify(aiService).classifyTransaction(description, amount, "CNY");
        }

        @Test
        @DisplayName("Should handle null classification result gracefully")
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
            
            AIQuestionAnswerResult mockAnswer = AIQuestionAnswerResult.builder()
                    .answer("Based on the financial data, your total revenue last quarter was $125,000.")
                    .confidence("HIGH")
                    .hasNumericData(true)
                    .dataSources(List.of("TransactionData", "Reports"))
                    .relatedData(Map.of("revenue", "125000"))
                    .build();
            
            when(aiService.answerFinancialQuestion(eq(question), anyString(), eq(companyId.intValue())))
                    .thenReturn(mockAnswer);

            // Act - AIApplicationService.answerFinancialQuestion returns String, not AIQuestionAnswerResult
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
        @DisplayName("Should get category suggestions successfully")
        void getCategorySuggestions_WithValidInput_ShouldReturnSuggestions() {
            // Arrange
            String description = "Coffee shop purchase";
            Double amount = 15.0;
            
            AIClassificationResult classification = AIClassificationResult.builder()
                    .category("FOOD_BEVERAGE")
                    .confidence(0.95)
                    .alternativeCategories(List.of("OFFICE_EXPENSE", "ENTERTAINMENT"))
                    .build();
            
            when(aiService.classifyTransaction(description, amount, "CNY")).thenReturn(classification);

            // Act - getCategorySuggestions returns List<Map<String,Object>>, not Map<String,Object>
            List<Map<String, Object>> result = aiApplicationService.getCategorySuggestions(description, amount);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertFalse(result.isEmpty(), "Should have suggestions");
            
            Map<String, Object> firstSuggestion = result.get(0);
            assertEquals("FOOD_BEVERAGE", firstSuggestion.get("categoryCode"));
            assertEquals(0.95, firstSuggestion.get("confidence"));
            assertFalse((Boolean) firstSuggestion.get("error"));
        }

        @Test
        @DisplayName("Should handle classification failure gracefully")
        void getCategorySuggestions_WithClassificationFailure_ShouldReturnFallback() {
            // Arrange
            String description = "Unknown expense";
            Double amount = 100.0;
            
            when(aiService.classifyTransaction(description, amount, "CNY"))
                    .thenThrow(new RuntimeException("Classification failed"));

            // Act
            List<Map<String, Object>> result = aiApplicationService.getCategorySuggestions(description, amount);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertFalse(result.isEmpty(), "Should have fallback suggestions");
            
            Map<String, Object> firstSuggestion = result.get(0);
            assertNotNull(firstSuggestion.get("categoryCode"));
            assertTrue((Boolean) firstSuggestion.get("error"));
        }
    }

    @Nested
    @DisplayName("AI Provider Info Tests")
    class AIProviderInfoTests {

        @Test
        @DisplayName("Should get AI provider info successfully")
        void getAIProviderInfo_WithAvailableService_ShouldReturnProviderInfo() {
            // Arrange
            when(aiService.isServiceAvailable()).thenReturn(true);
            when(aiService.getProviderName()).thenReturn("OpenAI GPT-4");

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
        @DisplayName("Should handle unavailable service gracefully")
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

    @Nested
    @DisplayName("Health Check Tests")
    class HealthCheckTests {

        @Test
        @DisplayName("Should perform health check successfully")
        void checkAIServiceHealth_WithHealthyService_ShouldReturnHealthyStatus() {
            // Arrange
            when(aiService.isServiceAvailable()).thenReturn(true);

            // Act - checkAIServiceHealth returns boolean, not Map<String,Object>
            boolean result = aiApplicationService.checkAIServiceHealth();

            // Assert
            assertTrue(result, "Should return true for healthy service");
            
            verify(aiService).isServiceAvailable();
        }

        @Test
        @DisplayName("Should detect unhealthy service")
        void checkAIServiceHealth_WithUnhealthyService_ShouldReturnUnhealthyStatus() {
            // Arrange
            when(aiService.isServiceAvailable()).thenReturn(false);

            // Act
            boolean result = aiApplicationService.checkAIServiceHealth();

            // Assert
            assertFalse(result, "Should return false for unhealthy service");
        }

        @Test
        @DisplayName("Should handle health check exception gracefully")
        void checkAIServiceHealth_WithException_ShouldReturnErrorStatus() {
            // Arrange
            when(aiService.isServiceAvailable()).thenThrow(new RuntimeException("Health check failed"));

            // Act
            boolean result = aiApplicationService.checkAIServiceHealth();

            // Assert
            assertFalse(result, "Should return false when exception occurs");
        }
    }
}