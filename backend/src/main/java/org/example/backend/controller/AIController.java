// backend/src/main/java/org/example/backend/controller/AIController.java
package org.example.backend.controller;

import org.example.backend.application.service.AIApplicationService;
import org.example.backend.application.dto.*;
import org.example.backend.util.JwtContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * FIXED AI Controller - Compatible with existing AIApplicationService
 * 
 * FIXES:
 * 1. Uses existing DTOs and method signatures
 * 2. Added missing single anomaly detection endpoint using existing structure
 * 3. Enhanced response formatting for better frontend display
 * 4. Maintains compatibility with existing AIApplicationService methods
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
@Slf4j
public class AIController {

    @Autowired
    private AIApplicationService aiApplicationService;

    /**
     * FIXED: Added missing single anomaly detection endpoint
     * Uses existing CreateTransactionCommand structure for compatibility
     */
    @PostMapping("/detect-anomaly")
    public ResponseEntity<?> detectSingleAnomaly(@RequestBody CreateTransactionCommand request) {
        try {
            log.info("Processing single anomaly detection for transaction: {}", request.getDescription());
            
            // Use existing enhanceTransactionCreation method which includes anomaly detection
            EnhancedTransactionDTO result = aiApplicationService.enhanceTransactionCreation(request);
            
            // Extract anomaly detection result
            AIAnomalyDetectionResult anomalyResult = result.getAnomalyDetection();
            
            // Enhanced response format for frontend
            Map<String, Object> response = new HashMap<>();
            response.put("anomalous", anomalyResult.isAnomalous());
            response.put("isAnomalous", anomalyResult.isAnomalous()); // Backward compatibility
            response.put("anomalyScore", anomalyResult.getAnomalyScore());
            response.put("anomalyType", anomalyResult.getAnomalyType());
            response.put("confidence", determineConfidence(anomalyResult.getAnomalyScore()));
            response.put("reason", generateReason(anomalyResult));
            response.put("recommendations", anomalyResult.getRecommendations());
            response.put("riskLevel", determineRiskLevel(anomalyResult.getAnomalyScore()));
            response.put("timestamp", java.time.LocalDateTime.now());
            
            log.info("Single anomaly detection completed. Anomalous: {}, Score: {}", 
                    anomalyResult.isAnomalous(), anomalyResult.getAnomalyScore());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Single anomaly detection failed for transaction: {}", request.getDescription(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("anomalous", false);
            errorResponse.put("isAnomalous", false);
            errorResponse.put("anomalyScore", 0.0);
            errorResponse.put("anomalyType", "error");
            errorResponse.put("confidence", "low");
            errorResponse.put("reason", "Analysis failed: " + e.getMessage());
            errorResponse.put("recommendations", List.of("Please try again or contact support"));
            errorResponse.put("riskLevel", "unknown");
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());
            
            return ResponseEntity.ok(errorResponse); // Return 200 to avoid frontend errors
        }
    }

    /**
     * Enhanced batch anomaly detection using existing method
     */
    @GetMapping("/detect-anomalies")
    public ResponseEntity<?> detectAnomalousTransactions(
            @RequestParam Integer companyId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            log.info("Processing batch anomaly detection for company {} from {} to {}", 
                    companyId, startDate, endDate);

            DateRange dateRange = new DateRange(
                    LocalDate.parse(startDate),
                    LocalDate.parse(endDate)
            );

            // Use existing method
            List<AnomalousTransactionDTO> results = aiApplicationService
                    .detectAnomalousTransactions(companyId, dateRange);

            // Enhanced formatting for frontend display
            List<Map<String, Object>> formattedResults = results.stream()
                    .map(this::formatAnomalousTransactionDTO)
                    .collect(java.util.stream.Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("results", formattedResults);
            response.put("totalAnalyzed", results.size());
            response.put("anomalousCount", results.size()); // All returned items are anomalous
            response.put("analysisDate", java.time.LocalDateTime.now());
            
            log.info("Batch anomaly detection completed. Analyzed: {}, Anomalous: {}", 
                    results.size(), results.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Batch anomaly detection failed for company {}", companyId, e);
            return ResponseEntity.ok(Map.of(
                    "results", List.of(),
                    "error", true,
                    "errorMessage", e.getMessage()
            ));
        }
    }

    /**
     * Enhanced transaction classification using existing method
     */
    @PostMapping("/enhance-transaction")
    public ResponseEntity<?> enhanceTransaction(@RequestBody CreateTransactionCommand request) {
        try {
            log.info("Processing transaction enhancement for: {}", request.getDescription());
            
            EnhancedTransactionDTO result = aiApplicationService.enhanceTransactionCreation(request);
            
            // Enhanced response formatting
            Map<String, Object> response = new HashMap<>();
            response.put("aiClassification", formatClassificationResult(result.getAiClassification()));
            response.put("anomalyDetection", formatAnomalyResult(result.getAnomalyDetection()));
            response.put("enhancementTimestamp", result.getEnhancementTimestamp());
            response.put("aiEnhanced", result.isAiEnhanced());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Transaction enhancement failed", e);
            return ResponseEntity.ok(createErrorResponse("Enhancement failed: " + e.getMessage()));
        }
    }

    /**
     * Enhanced financial Q&A using existing method
     */
    @PostMapping("/ask-financial-question")
    public ResponseEntity<?> askFinancialQuestion(@RequestBody FinancialQuestionCommand request) {
        try {
            log.info("Processing financial question: {}", request.getQuestion());
            
            FinancialQuestionAnswerDTO result = aiApplicationService.askFinancialQuestion(request);
            
            // Enhanced response formatting
            Map<String, Object> response = new HashMap<>();
            response.put("answer", result.getAnswer());
            response.put("confidence", result.getConfidence());
            response.put("hasNumericData", result.isHasNumericData());
            response.put("dataSources", result.getDataSources());
            response.put("relatedData", result.getRelatedData());
            response.put("responseTime", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Financial Q&A failed", e);
            return ResponseEntity.ok(Map.of(
                    "answer", "I apologize, but I encountered an error while processing your question: " + e.getMessage(),
                    "confidence", "low",
                    "hasNumericData", false,
                    "error", true
            ));
        }
    }

    /**
     * Enhanced category suggestions using existing method
     */
    @PostMapping("/category-suggestions")
    public ResponseEntity<?> getCategorySuggestions(@RequestBody CategorySuggestionCommand request) {
        try {
            log.info("Processing category suggestions for: {}", request.getDescription());
            
            List<CategorySuggestionDTO> suggestions = aiApplicationService
                    .getTransactionCategorySuggestions(request);
            
            // Enhanced formatting
            List<Map<String, Object>> formattedSuggestions = suggestions.stream()
                    .map(this::formatCategorySuggestion)
                    .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(formattedSuggestions);
            
        } catch (Exception e) {
            log.error("Category suggestions failed", e);
            return ResponseEntity.ok(List.of(Map.of(
                    "categoryCode", "GENERAL",
                    "categoryName", "General Expense",
                    "chineseName", "一般费用",
                    "confidence", "low",
                    "reason", "Default category due to analysis error: " + e.getMessage(),
                    "error", true
            )));
        }
    }

    /**
     * Enhanced report insights using existing method
     */
    @GetMapping("/report-insights")
    public ResponseEntity<?> getReportInsights(
            @RequestParam String reportData,
            @RequestParam String reportType) {
        try {
            log.info("Processing report insights for report type {}", reportType);
            
            String insights = aiApplicationService.generateReportInsights(reportData, reportType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("insights", List.of(insights));
            response.put("summary", insights);
            response.put("confidence", "medium");
            response.put("analysisDate", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Report insights failed", e);
            return ResponseEntity.ok(Map.of(
                    "insights", List.of("Analysis temporarily unavailable"),
                    "summary", "Unable to generate insights: " + e.getMessage(),
                    "error", true
            ));
        }
    }

    // Helper methods

    private String determineRiskLevel(Double anomalyScore) {
        if (anomalyScore == null) return "unknown";
        if (anomalyScore >= 0.8) return "high";
        if (anomalyScore >= 0.5) return "medium";
        if (anomalyScore >= 0.2) return "low";
        return "minimal";
    }

    private String determineConfidence(Double anomalyScore) {
        if (anomalyScore == null) return "low";
        if (anomalyScore >= 0.7) return "high";
        if (anomalyScore >= 0.4) return "medium";
        return "low";
    }

    private String generateReason(AIAnomalyDetectionResult result) {
        if (result.isAnomalous()) {
            return "Transaction shows unusual patterns based on " + result.getAnomalyType() + " analysis";
        } else {
            return "Transaction appears normal based on historical patterns";
        }
    }

    private Map<String, Object> formatAnomalyResult(AIAnomalyDetectionResult result) {
        Map<String, Object> formatted = new HashMap<>();
        formatted.put("anomalous", result.isAnomalous());
        formatted.put("isAnomalous", result.isAnomalous());
        formatted.put("anomalyScore", result.getAnomalyScore());
        formatted.put("anomalyType", result.getAnomalyType());
        formatted.put("confidence", determineConfidence(result.getAnomalyScore()));
        formatted.put("reason", generateReason(result));
        formatted.put("recommendations", result.getRecommendations());
        formatted.put("riskLevel", determineRiskLevel(result.getAnomalyScore()));
        return formatted;
    }

    private Map<String, Object> formatClassificationResult(AIClassificationResult result) {
        Map<String, Object> formatted = new HashMap<>();
        formatted.put("category", result.getCategory());
        formatted.put("confidence", result.getConfidence());
        formatted.put("reason", result.getReason());
        formatted.put("alternativeCategories", result.getAlternativeCategories());
        formatted.put("requireReview", result.isRequireReview());
        return formatted;
    }

    private Map<String, Object> formatCategorySuggestion(CategorySuggestionDTO suggestion) {
        Map<String, Object> formatted = new HashMap<>();
        formatted.put("categoryCode", suggestion.getCategoryCode());
        formatted.put("categoryName", suggestion.getCategoryName());
        formatted.put("chineseName", suggestion.getChineseName());
        formatted.put("confidence", suggestion.getConfidence());
        formatted.put("reason", suggestion.getReason());
        return formatted;
    }

    private Map<String, Object> formatAnomalousTransactionDTO(AnomalousTransactionDTO dto) {
        Map<String, Object> formatted = new HashMap<>();
        formatted.put("description", dto.getDescription());
        formatted.put("amount", dto.getAmount());
        formatted.put("transactionDate", dto.getTransactionDate());
        formatted.put("category", dto.getCategory());
        formatted.put("anomalyScore", dto.getAnomalyScore());
        formatted.put("anomalyType", dto.getAnomalyType());
        formatted.put("recommendations", List.of("Review transaction details", "Verify with originator"));
        formatted.put("anomalous", true);
        formatted.put("isAnomalous", true);
        return formatted;
    }

    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("errorMessage", errorMessage);
        response.put("timestamp", java.time.LocalDateTime.now());
        return response;
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        boolean isHealthy = aiApplicationService.isServiceAvailable();
        return ResponseEntity.ok(Map.of(
                "status", isHealthy ? "healthy" : "unhealthy",
                "service", "AI Analysis Service",
                "timestamp", java.time.LocalDateTime.now()
        ));
    }

    /**
     * Provider information endpoint
     */
    @GetMapping("/provider")
    public ResponseEntity<?> getProviderInfo() {
        String providerName = aiApplicationService.getProviderName();
        return ResponseEntity.ok(Map.of(
                "provider", providerName,
                "version", "1.0.0",
                "capabilities", List.of(
                    "Transaction Classification",
                    "Anomaly Detection", 
                    "Financial Q&A",
                    "Category Suggestions",
                    "Report Insights"
                )
        ));
    }
}