// backend/src/main/java/org/example/backend/application/service/AIApplicationService.java
package org.example.backend.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.application.dto.*;
import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.aggregate.report.ReportAggregate;
import org.example.backend.domain.service.AIAnalysisDomainService;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.infrastructure.ai.AIService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * AI Application Service - Enhanced for better insights formatting
 * Fixed to use correct method names and existing DTOs
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AIApplicationService {

    private final AIAnalysisDomainService aiAnalysisDomainService;
    private final AIService aiService;

    /**
     * Generate structured report insights with proper formatting
     */
    public Map<String, Object> generateReportInsights(String reportData, String reportType) {
        try {
            log.info("Generating structured insights for report type: {}", reportType);
            
            // Get AI analysis result using existing method
            AIReportInsightResult result = aiService.generateReportInsights(reportData, reportType);
            String rawInsights = result.getInsightSummary();
            
            // Parse and structure the insights
            Map<String, Object> structuredInsights = parseAndStructureInsights(rawInsights, reportType);
            
            return structuredInsights;
            
        } catch (Exception e) {
            log.error("Failed to generate report insights", e);
            return createErrorInsightResponse(e.getMessage());
        }
    }

    /**
     * Parse raw AI response into structured format
     */
    private Map<String, Object> parseAndStructureInsights(String rawInsights, String reportType) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Extract different sections from AI response
            List<String> keyInsights = extractKeyInsights(rawInsights);
            List<String> anomalies = extractAnomalies(rawInsights);
            List<String> recommendations = extractRecommendations(rawInsights);
            String summary = extractSummary(rawInsights);
            
            // Build structured response
            result.put("summary", summary);
            result.put("insights", keyInsights);
            result.put("anomalies", anomalies);
            result.put("recommendations", recommendations);
            result.put("confidence", determineConfidence(rawInsights));
            result.put("analysisDate", LocalDateTime.now());
            result.put("reportType", reportType);
            result.put("status", "completed");
            
            // Add report-type specific analysis
            if ("BALANCE_SHEET".equals(reportType)) {
                result.put("financialHealth", analyzeBalanceSheetHealth(rawInsights));
            } else if ("INCOME_STATEMENT".equals(reportType)) {
                result.put("profitabilityAnalysis", analyzeProfitability(rawInsights));
            } else if ("INCOME_EXPENSE".equals(reportType)) {
                result.put("cashFlowAnalysis", analyzeCashFlow(rawInsights));
            }
            
        } catch (Exception e) {
            log.warn("Error parsing insights, using simplified format", e);
            result.put("summary", rawInsights.length() > 200 ? rawInsights.substring(0, 200) + "..." : rawInsights);
            result.put("insights", List.of(rawInsights));
            result.put("confidence", "medium");
            result.put("analysisDate", LocalDateTime.now());
        }
        
        return result;
    }

    /**
     * Extract key insights from raw AI response
     */
    private List<String> extractKeyInsights(String rawText) {
        List<String> insights = new java.util.ArrayList<>();
        
        // Look for numbered insights or bullet points
        String[] lines = rawText.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.matches("^\\d+\\..*") || line.startsWith("•") || line.startsWith("-")) {
                // Remove numbering and clean up
                String insight = line.replaceFirst("^\\d+\\.", "").replaceFirst("^[•-]", "").trim();
                if (insight.length() > 10) { // Filter out very short lines
                    insights.add(insight);
                }
            }
        }
        
        // If no structured insights found, extract sentences
        if (insights.isEmpty()) {
            String[] sentences = rawText.split("\\.");
            for (String sentence : sentences) {
                sentence = sentence.trim();
                if (sentence.length() > 20 && sentence.length() < 200) {
                    insights.add(sentence + ".");
                }
                if (insights.size() >= 5) break; // Limit to 5 key insights
            }
        }
        
        return insights.isEmpty() ? List.of("Analysis completed successfully") : insights;
    }

    /**
     * Extract anomalies from AI response
     */
    private List<String> extractAnomalies(String rawText) {
        List<String> anomalies = new java.util.ArrayList<>();
        String lowerText = rawText.toLowerCase();
        
        // Look for anomaly indicators
        if (lowerText.contains("anomaly") || lowerText.contains("unusual") || 
            lowerText.contains("irregular") || lowerText.contains("concerning")) {
            
            String[] lines = rawText.split("\n");
            for (String line : lines) {
                String lowerLine = line.toLowerCase();
                if (lowerLine.contains("anomaly") || lowerLine.contains("unusual") || 
                    lowerLine.contains("irregular") || lowerLine.contains("risk")) {
                    anomalies.add(line.trim());
                }
            }
        }
        
        return anomalies.isEmpty() ? List.of("No significant anomalies detected") : anomalies;
    }

    /**
     * Extract recommendations from AI response
     */
    private List<String> extractRecommendations(String rawText) {
        List<String> recommendations = new java.util.ArrayList<>();
        String lowerText = rawText.toLowerCase();
        
        // Look for recommendation sections
        if (lowerText.contains("recommend") || lowerText.contains("suggest") || 
            lowerText.contains("should") || lowerText.contains("action")) {
            
            String[] lines = rawText.split("\n");
            for (String line : lines) {
                String lowerLine = line.toLowerCase();
                if (lowerLine.contains("recommend") || lowerLine.contains("suggest") || 
                    lowerLine.contains("should consider") || lowerLine.contains("action")) {
                    recommendations.add(line.trim());
                }
            }
        }
        
        return recommendations.isEmpty() ? 
            List.of("Continue monitoring financial performance", "Review report data regularly") : 
            recommendations;
    }

    /**
     * Extract summary from AI response
     */
    private String extractSummary(String rawText) {
        // Look for summary section
        String[] lines = rawText.split("\n");
        for (String line : lines) {
            String lowerLine = line.toLowerCase();
            if (lowerLine.contains("summary") && line.length() > 50) {
                return line.replaceFirst("(?i).*summary:?", "").trim();
            }
        }
        
        // If no summary section, use first meaningful paragraph
        String[] paragraphs = rawText.split("\n\n");
        for (String paragraph : paragraphs) {
            if (paragraph.trim().length() > 50) {
                return paragraph.trim().length() > 300 ? 
                    paragraph.trim().substring(0, 300) + "..." : 
                    paragraph.trim();
            }
        }
        
        return rawText.length() > 200 ? rawText.substring(0, 200) + "..." : rawText;
    }

    /**
     * Determine confidence level based on content
     */
    private String determineConfidence(String rawText) {
        String lowerText = rawText.toLowerCase();
        
        if (lowerText.contains("high confidence") || lowerText.contains("certain")) {
            return "high";
        } else if (lowerText.contains("low confidence") || lowerText.contains("uncertain")) {
            return "low";
        } else {
            return "medium";
        }
    }

    /**
     * Analyze balance sheet health
     */
    private Map<String, Object> analyzeBalanceSheetHealth(String rawText) {
        Map<String, Object> health = new HashMap<>();
        String lowerText = rawText.toLowerCase();
        
        // Simple heuristics for balance sheet health
        if (lowerText.contains("strong") || lowerText.contains("healthy")) {
            health.put("overall", "good");
        } else if (lowerText.contains("weak") || lowerText.contains("concern")) {
            health.put("overall", "concerning");
        } else {
            health.put("overall", "stable");
        }
        
        health.put("liquidity", lowerText.contains("liquid") ? "adequate" : "needs_review");
        health.put("solvency", lowerText.contains("debt") ? "monitor" : "stable");
        
        return health;
    }

    /**
     * Analyze profitability
     */
    private Map<String, Object> analyzeProfitability(String rawText) {
        Map<String, Object> profitability = new HashMap<>();
        String lowerText = rawText.toLowerCase();
        
        if (lowerText.contains("profit") && lowerText.contains("increasing")) {
            profitability.put("trend", "improving");
        } else if (lowerText.contains("profit") && lowerText.contains("decreasing")) {
            profitability.put("trend", "declining");
        } else {
            profitability.put("trend", "stable");
        }
        
        profitability.put("margins", lowerText.contains("margin") ? "analyzed" : "needs_review");
        
        return profitability;
    }

    /**
     * Analyze cash flow
     */
    private Map<String, Object> analyzeCashFlow(String rawText) {
        Map<String, Object> cashFlow = new HashMap<>();
        String lowerText = rawText.toLowerCase();
        
        if (lowerText.contains("positive") || lowerText.contains("surplus")) {
            cashFlow.put("status", "positive");
        } else if (lowerText.contains("negative") || lowerText.contains("deficit")) {
            cashFlow.put("status", "negative");
        } else {
            cashFlow.put("status", "neutral");
        }
        
        return cashFlow;
    }

    /**
     * Create error response for insights
     */
    private Map<String, Object> createErrorInsightResponse(String errorMessage) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("summary", "Unable to generate insights at this time");
        errorResponse.put("insights", List.of("Analysis temporarily unavailable: " + errorMessage));
        errorResponse.put("anomalies", List.of("Unable to detect anomalies"));
        errorResponse.put("recommendations", List.of("Please try again later"));
        errorResponse.put("confidence", "low");
        errorResponse.put("analysisDate", LocalDateTime.now());
        errorResponse.put("status", "error");
        errorResponse.put("error", true);
        return errorResponse;
    }

    /**
     * Get category suggestions for transaction - using existing AI service methods
     */
    public List<Map<String, Object>> getCategorySuggestions(String description, Double amount) {
        try {
            log.info("Getting category suggestions for: {}", description);
            
            // Call existing AI service method
            AIClassificationResult classification = aiService.classifyTransaction(description, amount, "CNY");
            
            // Format response
            List<Map<String, Object>> formattedSuggestions = new java.util.ArrayList<>();
            
            if (classification != null) {
                Map<String, Object> suggestion = new HashMap<>();
                suggestion.put("categoryCode", classification.getCategory());
                suggestion.put("categoryName", classification.getCategory().replace("_", " "));
                suggestion.put("chineseName", getCategoryChineseName(classification.getCategory()));
                suggestion.put("confidence", classification.getConfidence());
                suggestion.put("reason", classification.getReason());
                suggestion.put("error", false);
                formattedSuggestions.add(suggestion);
            } else {
                // Default suggestion
                Map<String, Object> defaultSuggestion = new HashMap<>();
                defaultSuggestion.put("categoryCode", "GENERAL");
                defaultSuggestion.put("categoryName", "General Expense");
                defaultSuggestion.put("chineseName", "一般费用");
                defaultSuggestion.put("confidence", "medium");
                defaultSuggestion.put("reason", "Default category based on transaction pattern");
                defaultSuggestion.put("error", false);
                formattedSuggestions.add(defaultSuggestion);
            }
            
            return formattedSuggestions;
            
        } catch (Exception e) {
            log.error("Category suggestions failed", e);
            
            // Return error suggestion
            Map<String, Object> errorSuggestion = new HashMap<>();
            errorSuggestion.put("categoryCode", "GENERAL");
            errorSuggestion.put("categoryName", "General Expense");
            errorSuggestion.put("chineseName", "一般费用");
            errorSuggestion.put("confidence", "low");
            errorSuggestion.put("reason", "Analysis failed: " + e.getMessage());
            errorSuggestion.put("error", true);
            
            return List.of(errorSuggestion);
        }
    }

    /**
     * Enhance transaction with AI analysis - using existing AI service methods
     */
    public Map<String, Object> enhanceTransaction(String description, Double amount, String category) {
        try {
            log.info("Enhancing transaction: {}", description);
            
            // Call existing AI service method
            AIClassificationResult classification = aiService.classifyTransaction(description, amount, "CNY");
            
            Map<String, Object> result = new HashMap<>();
            
            if (classification != null) {
                result.put("description", description);
                result.put("category", classification.getCategory());
                result.put("confidence", classification.getConfidence());
                result.put("reason", classification.getReason());
                result.put("error", false);
            } else {
                result.put("description", description);
                result.put("category", category);
                result.put("confidence", "low");
                result.put("error", true);
                result.put("message", "Enhancement not available");
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Transaction enhancement failed", e);
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("description", description);
            errorResult.put("category", category);
            errorResult.put("confidence", "low");
            errorResult.put("error", true);
            errorResult.put("message", "Enhancement failed: " + e.getMessage());
            
            return errorResult;
        }
    }

    /**
     * Detect single transaction anomaly - using existing AI service methods
     */
    public Map<String, Object> detectSingleAnomaly(String description, Double amount, String category) {
        try {
            log.info("Detecting anomaly for transaction: {}", description);
            
            // Build AI transaction data
            AITransactionData transactionData = AITransactionData.builder()
                    .description(description)
                    .amount(amount)
                    .currency("CNY")
                    .transactionType("EXPENSE")
                    .category(category)
                    .build();
            
            // Call existing AI service method
            AIAnomalyDetectionResult anomalyResult = aiService.detectAnomalousTransaction(transactionData);
            
            Map<String, Object> result = new HashMap<>();
            
            if (anomalyResult != null) {
                result.put("anomalous", anomalyResult.isAnomalous());
                result.put("anomalyScore", anomalyResult.getAnomalyScore());
                result.put("anomalyType", anomalyResult.getAnomalyType());
                result.put("riskLevel", determineRiskLevel(anomalyResult.getAnomalyScore()));
                result.put("confidence", determineConfidenceFromScore(anomalyResult.getAnomalyScore())); // Use score to determine confidence
                result.put("reason", generateReasonFromResult(anomalyResult)); // Generate reason from result
                result.put("recommendations", anomalyResult.getRecommendations());
                result.put("error", false);
            } else {
                result.put("anomalous", false);
                result.put("anomalyScore", 0.0);
                result.put("confidence", "low");
                result.put("reason", "Analysis not available");
                result.put("error", true);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Anomaly detection failed", e);
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("anomalous", false);
            errorResult.put("anomalyScore", 0.0);
            errorResult.put("confidence", "low");
            errorResult.put("reason", "Analysis failed: " + e.getMessage());
            errorResult.put("error", true);
            
            return errorResult;
        }
    }

    /**
     * Detect batch anomalies - using existing domain service and correct entity methods
     */
    public List<Map<String, Object>> detectBatchAnomalies(Long companyId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        try {
            log.info("Detecting batch anomalies for company {} from {} to {}", companyId, startDate, endDate);
            
            TenantId tenantId = TenantId.of(companyId.intValue()); // Convert Long to Integer
            
            // Get transactions for analysis using existing domain service
            List<TransactionAggregate> transactions = aiAnalysisDomainService
                    .prepareTransactionDataForAI(tenantId, startDate, endDate, null);
            
            List<Map<String, Object>> anomalies = new java.util.ArrayList<>();
            
            for (TransactionAggregate transaction : transactions) {
                try {
                    // Build AI transaction data using correct entity methods
                    AITransactionData transactionData = AITransactionData.builder()
                            .description(transaction.getDescription())
                            .amount(transaction.getMoney().getAmount().doubleValue()) // Use getMoney().getAmount()
                            .currency(transaction.getMoney().getCurrencyCode()) // Use getMoney().getCurrencyCode()
                            .transactionType("EXPENSE") // Default type
                            .category("GENERAL") // Default category
                            .build();
                    
                    AIAnomalyDetectionResult anomalyResult = aiService.detectAnomalousTransaction(transactionData);
                    
                    if (anomalyResult != null && anomalyResult.isAnomalous()) {
                        Map<String, Object> anomaly = new HashMap<>();
                        anomaly.put("transactionId", transaction.getTransactionId()); // Use getTransactionId()
                        anomaly.put("description", transaction.getDescription());
                        anomaly.put("amount", transaction.getMoney().getAmount());
                        anomaly.put("transactionDate", transaction.getTransactionDate());
                        anomaly.put("anomalous", true);
                        anomaly.put("anomalyScore", anomalyResult.getAnomalyScore());
                        anomaly.put("anomalyType", anomalyResult.getAnomalyType());
                        anomaly.put("riskLevel", determineRiskLevel(anomalyResult.getAnomalyScore()));
                        anomaly.put("recommendations", anomalyResult.getRecommendations());
                        
                        anomalies.add(anomaly);
                    }
                } catch (Exception e) {
                    log.warn("Failed to check anomaly for transaction {}", transaction.getTransactionId(), e);
                }
            }
            
            log.info("Found {} anomalies out of {} transactions", anomalies.size(), transactions.size());
            return anomalies;
            
        } catch (Exception e) {
            log.error("Batch anomaly detection failed", e);
            return List.of();
        }
    }

    /**
     * Answer financial question - using existing AI service method
     */
    public String answerFinancialQuestion(String question, Long companyId) {
        try {
            log.info("Processing financial question: {}", question);
            
            // Get context data if company ID provided
            String contextData = "";
            if (companyId != null) {
                contextData = prepareCompanyContextForAI(companyId);
                
                // Call existing AI service method with correct parameters
                AIQuestionAnswerResult result = aiService.answerFinancialQuestion(question, contextData, companyId.intValue());
                String answer = result.getAnswer();
                
                return answer != null ? answer : "I'm unable to process your question at this time. Please try rephrasing or contact support.";
            } else {
                // Handle null companyId case
                return "Company information is required to provide accurate financial insights.";
            }
            
        } catch (Exception e) {
            log.error("Financial Q&A failed", e);
            return "I'm experiencing technical difficulties. Please try again later or contact support.";
        }
    }

    /**
     * Check AI service health
     */
    public boolean checkAIServiceHealth() {
        try {
            // Simple health check using existing method
            return aiService.isServiceAvailable();
        } catch (Exception e) {
            log.warn("AI service health check failed", e);
            return false;
        }
    }

    /**
     * Get AI provider information
     */
    public Map<String, Object> getAIProviderInfo() {
        try {
            String providerName = aiService.getProviderName();
            
            Map<String, Object> info = new HashMap<>();
            info.put("name", providerName);
            info.put("version", "1.0");
            info.put("model", "Unknown");
            info.put("available", aiService.isServiceAvailable());
            info.put("features", List.of(
                "Report Analysis",
                "Anomaly Detection",
                "Category Suggestions",
                "Transaction Enhancement",
                "Financial Q&A"
            ));
            
            return info;
            
        } catch (Exception e) {
            log.error("Failed to get AI provider info", e);
            
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("name", "Unknown");
            errorInfo.put("version", "Unknown");
            errorInfo.put("available", false);
            errorInfo.put("error", e.getMessage());
            
            return errorInfo;
        }
    }

    // Helper methods

    private String determineRiskLevel(Double anomalyScore) {
        if (anomalyScore == null) return "unknown";
        if (anomalyScore >= 0.8) return "high";
        if (anomalyScore >= 0.6) return "medium";
        if (anomalyScore >= 0.3) return "low";
        return "minimal";
    }

    /**
     * Determine confidence level from anomaly score
     */
    private String determineConfidenceFromScore(Double anomalyScore) {
        if (anomalyScore == null) return "low";
        if (anomalyScore >= 0.7) return "high";
        if (anomalyScore >= 0.4) return "medium";
        return "low";
    }

    /**
     * Generate reason from anomaly detection result
     */
    private String generateReasonFromResult(AIAnomalyDetectionResult result) {
        if (result.isAnomalous()) {
            return "Transaction shows unusual patterns based on " + result.getAnomalyType() + " analysis";
        } else {
            return "Transaction appears normal based on historical patterns";
        }
    }

    private String prepareCompanyContextForAI(Long companyId) {
        try {
            // Get recent financial data for context
            TenantId tenantId = TenantId.of(companyId.intValue());
            java.time.LocalDate endDate = java.time.LocalDate.now();
            java.time.LocalDate startDate = endDate.minusMonths(3);
            
            List<TransactionAggregate> recentTransactions = aiAnalysisDomainService
                    .prepareTransactionDataForAI(tenantId, startDate, endDate, null);
            
            // Build context summary
            StringBuilder context = new StringBuilder();
            context.append("Company financial context:\n");
            context.append("Recent transaction count: ").append(recentTransactions.size()).append("\n");
            
            if (!recentTransactions.isEmpty()) {
                double totalAmount = recentTransactions.stream()
                        .mapToDouble(t -> t.getMoney().getAmount().doubleValue())
                        .sum();
                context.append("Total transaction volume: ").append(totalAmount).append("\n");
            }
            
            return context.toString();
            
        } catch (Exception e) {
            log.warn("Failed to prepare company context", e);
            return "Limited context available due to data access issues.";
        }
    }

    private String getCategoryChineseName(String categoryCode) {
        // Simple mapping for common categories
        switch (categoryCode) {
            case "TRAVEL_EXPENSE": return "差旅费";
            case "FOOD_EXPENSE": return "餐饮费";
            case "OFFICE_SUPPLIES": return "办公用品";
            case "UTILITIES": return "水电费";
            case "RENT": return "租金";
            case "MARKETING": return "营销费用";
            default: return "一般费用";
        }
    }
}