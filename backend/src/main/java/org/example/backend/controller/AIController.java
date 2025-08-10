// backend/src/main/java/org/example/backend/controller/AIController.java
package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.application.service.AIApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enhanced AI Controller with better response formatting
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AIController {

    private final AIApplicationService aiApplicationService;

    /**
     * Enhanced report insights endpoint with structured response
     */
    @GetMapping("/report-insights")
    public ResponseEntity<Map<String, Object>> getReportInsights(
            @RequestParam String reportData,
            @RequestParam String reportType) {
        try {
            log.info("Processing enhanced report insights for report type: {}", reportType);
            
            // Get structured insights from application service
            Map<String, Object> structuredInsights = aiApplicationService.generateReportInsights(reportData, reportType);
            
            log.info("Successfully generated structured insights for report type: {}", reportType);
            return ResponseEntity.ok(structuredInsights);
            
        } catch (Exception e) {
            log.error("Enhanced report insights failed for report type: {}", reportType, e);
            
            // Return structured error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("summary", "Analysis temporarily unavailable");
            errorResponse.put("insights", List.of("Unable to generate insights: " + e.getMessage()));
            errorResponse.put("anomalies", List.of("Unable to detect anomalies"));
            errorResponse.put("recommendations", List.of("Please check report data and try again"));
            errorResponse.put("confidence", "low");
            errorResponse.put("analysisDate", java.time.LocalDateTime.now());
            errorResponse.put("status", "error");
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());
            
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Category suggestions endpoint
     */
    @PostMapping("/category-suggestions")
    public ResponseEntity<?> getCategorySuggestions(@RequestBody Map<String, Object> request) {
        try {
            String description = (String) request.get("description");
            Double amount = request.get("amount") instanceof Number ? 
                ((Number) request.get("amount")).doubleValue() : 0.0;
            
            log.info("Processing category suggestions for description: {}", description);
            
            // Call application service for category suggestions
            List<Map<String, Object>> suggestions = aiApplicationService.getCategorySuggestions(description, amount);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "suggestions", suggestions,
                "timestamp", java.time.LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            log.error("Category suggestions failed", e);
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "suggestions", List.of(Map.of(
                    "categoryCode", "GENERAL",
                    "categoryName", "General Expense",
                    "chineseName", "一般费用",
                    "confidence", "low",
                    "reason", "Default category due to analysis error",
                    "error", true
                )),
                "errorMessage", e.getMessage()
            ));
        }
    }

    /**
     * Enhanced transaction classification endpoint
     */
    @PostMapping("/enhance-transaction")
    public ResponseEntity<Map<String, Object>> enhanceTransaction(@RequestBody Map<String, Object> request) {
        try {
            String description = (String) request.get("description");
            Double amount = request.get("amount") instanceof Number ? 
                ((Number) request.get("amount")).doubleValue() : 0.0;
            String category = (String) request.get("category");
            
            log.info("Enhancing transaction: {} with amount: {}", description, amount);
            
            // Get enhanced transaction data
            Map<String, Object> enhancement = aiApplicationService.enhanceTransaction(description, amount, category);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "enhancement", enhancement,
                "originalDescription", description,
                "timestamp", java.time.LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            log.error("Transaction enhancement failed", e);
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "enhancement", Map.of(
                    "description", request.get("description"),
                    "confidence", "low",
                    "error", true
                ),
                "errorMessage", e.getMessage()
            ));
        }
    }

    /**
     * Anomaly detection endpoint
     */
    @PostMapping("/detect-anomaly")
    public ResponseEntity<Map<String, Object>> detectAnomaly(@RequestBody Map<String, Object> request) {
        try {
            String description = (String) request.get("description");
            Double amount = request.get("amount") instanceof Number ? 
                ((Number) request.get("amount")).doubleValue() : 0.0;
            String category = (String) request.get("category");
            
            log.info("Detecting anomaly for transaction: {}", description);
            
            // Detect anomaly
            Map<String, Object> anomalyResult = aiApplicationService.detectSingleAnomaly(description, amount, category);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "result", anomalyResult,
                "timestamp", java.time.LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            log.error("Anomaly detection failed", e);
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "result", Map.of(
                    "anomalous", false,
                    "anomalyScore", 0.0,
                    "confidence", "low",
                    "reason", "Analysis failed: " + e.getMessage(),
                    "error", true
                ),
                "errorMessage", e.getMessage()
            ));
        }
    }

    /**
     * Batch anomaly detection endpoint
     */
    @GetMapping("/detect-anomalies")
    public ResponseEntity<Map<String, Object>> detectAnomalies(
            @RequestParam Long companyId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            log.info("Detecting anomalies for company {} from {} to {}", companyId, startDate, endDate);
            
            // Parse dates
            java.time.LocalDate start = java.time.LocalDate.parse(startDate);
            java.time.LocalDate end = java.time.LocalDate.parse(endDate);
            
            // Detect anomalies
            List<Map<String, Object>> anomalies = aiApplicationService.detectBatchAnomalies(companyId, start, end);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "anomalies", anomalies,
                "totalCount", anomalies.size(),
                "period", Map.of("start", startDate, "end", endDate),
                "timestamp", java.time.LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            log.error("Batch anomaly detection failed", e);
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "anomalies", List.of(),
                "totalCount", 0,
                "errorMessage", e.getMessage()
            ));
        }
    }

    /**
     * Financial Q&A endpoint
     */
    @PostMapping("/ask-financial-question")
    public ResponseEntity<Map<String, Object>> askFinancialQuestion(@RequestBody Map<String, Object> request) {
        try {
            String question = (String) request.get("question");
            Long companyId = request.get("companyId") instanceof Number ? 
                ((Number) request.get("companyId")).longValue() : null;
            
            log.info("Processing financial question: {}", question);
            
            // Process financial question
            String answer = aiApplicationService.answerFinancialQuestion(question, companyId);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "question", question,
                "answer", answer,
                "confidence", "medium",
                "timestamp", java.time.LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            log.error("Financial Q&A failed", e);
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "question", request.get("question"),
                "answer", "I'm unable to process your question at this time. Please try again later.",
                "confidence", "low",
                "errorMessage", e.getMessage()
            ));
        }
    }

    /**
     * AI health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            // Check AI service availability
            boolean isHealthy = aiApplicationService.checkAIServiceHealth();
            
            return ResponseEntity.ok(Map.of(
                "status", isHealthy ? "healthy" : "unhealthy",
                "service", "AI Analysis Service",
                "timestamp", java.time.LocalDateTime.now(),
                "features", List.of(
                    "Report Insights",
                    "Anomaly Detection", 
                    "Category Suggestions",
                    "Transaction Enhancement",
                    "Financial Q&A"
                )
            ));
            
        } catch (Exception e) {
            log.error("AI health check failed", e);
            return ResponseEntity.ok(Map.of(
                "status", "unhealthy",
                "error", e.getMessage(),
                "timestamp", java.time.LocalDateTime.now()
            ));
        }
    }

    /**
     * Get AI provider information
     */
    @GetMapping("/provider")
    public ResponseEntity<Map<String, Object>> getProviderInfo() {
        try {
            Map<String, Object> providerInfo = aiApplicationService.getAIProviderInfo();
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "provider", providerInfo,
                "timestamp", java.time.LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            log.error("Failed to get AI provider info", e);
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "provider", Map.of(
                    "name", "Unknown",
                    "version", "Unknown",
                    "available", false
                ),
                "errorMessage", e.getMessage()
            ));
        }
    }
}