// backend/src/main/java/org/example/backend/application/service/AIAnalysisApplicationService.java
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

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI Analysis Application Service - DDD Compliant
 * 
 * This Application Service orchestrates AI analysis workflows by:
 * 1. Coordinating with Domain Service for data preparation
 * 2. Calling Infrastructure Services for AI processing
 * 3. Handling application-level concerns (transactions, DTOs)
 * 4. NOT containing business logic (that's in Domain Service)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AIAnalysisApplicationService {

    private final AIAnalysisDomainService aiAnalysisDomainService;
    private final AIService aiService;

    /**
     * Get available data summary for AI analysis
     * 
     * Application Service responsibility: DTO conversion and coordination
     */
    @Transactional(readOnly = true)
    public AIAnalysisDataSummaryDTO getAvailableDataSummary(AIDataFilterCommand filterCommand) {
        log.info("Getting available data summary for AI analysis - Company: {}, Period: {} to {}", 
                filterCommand.getCompanyId(), filterCommand.getStartDate(), filterCommand.getEndDate());
        
        TenantId tenantId = TenantId.of(filterCommand.getCompanyId());
        
        // Use Domain Service to prepare data according to business rules
        List<TransactionAggregate> transactions = aiAnalysisDomainService
                .prepareTransactionDataForAI(tenantId, filterCommand.getStartDate(), 
                                           filterCommand.getEndDate(), filterCommand.getDepartmentId());
        
        List<ReportAggregate> reports = aiAnalysisDomainService
                .prepareReportDataForAI(tenantId, filterCommand.getStartDate(), filterCommand.getEndDate());
        
        // Convert domain objects to DTOs (Application Service responsibility)
        return AIAnalysisDataSummaryDTO.builder()
                .totalTransactions(transactions.size())
                .totalReports(reports.size())
                .dateRange(new DateRangeDTO(filterCommand.getStartDate(), filterCommand.getEndDate()))
                .transactionSummary(buildTransactionSummaryDTO(transactions))
                .reportSummary(buildReportSummaryDTO(reports))
                .availableAnalysisTypes(getAvailableAnalysisTypes(transactions, reports))
                .estimatedComplexity(aiAnalysisDomainService.estimateAnalysisComplexity(transactions, reports))
                .dataSufficient(aiAnalysisDomainService.validateDataSufficiencyForAI(tenantId, 
                        filterCommand.getStartDate(), filterCommand.getEndDate()))
                .build();
    }

    /**
     * Perform comprehensive AI analysis
     * 
     * Application Service responsibility: Orchestrate the workflow
     */
    public AIAnalysisResultDTO performAIAnalysis(AIAnalysisRequestCommand requestCommand) {
        log.info("Starting AI analysis for company {} from {} to {}", 
                requestCommand.getCompanyId(), requestCommand.getStartDate(), requestCommand.getEndDate());
        
        TenantId tenantId = TenantId.of(requestCommand.getCompanyId());
        
        // 1. Validate data sufficiency using Domain Service
        if (!aiAnalysisDomainService.validateDataSufficiencyForAI(tenantId, 
                requestCommand.getStartDate(), requestCommand.getEndDate())) {
            throw new IllegalArgumentException("Insufficient data for meaningful AI analysis");
        }
        
        // 2. Prepare data using Domain Service
        List<TransactionAggregate> transactions = aiAnalysisDomainService
                .prepareTransactionDataForAI(tenantId, requestCommand.getStartDate(), 
                                           requestCommand.getEndDate(), requestCommand.getDepartmentId());
        
        List<ReportAggregate> reports = aiAnalysisDomainService
                .prepareReportDataForAI(tenantId, requestCommand.getStartDate(), requestCommand.getEndDate());
        
        // 3. Generate structured data using Domain Service
        String transactionData = aiAnalysisDomainService.generateTransactionAnalysisData(transactions);
        String reportData = aiAnalysisDomainService.generateReportAnalysisData(reports);
        
        // 4. Call Infrastructure Service for AI processing
        try {
            AIAnalysisResultDTO result = performAnalysisByType(requestCommand.getAnalysisType(), 
                    transactionData, reportData, requestCommand);
            
            log.info("Completed AI analysis for company {}, generated {} insights", 
                    requestCommand.getCompanyId(), result.getInsights().size());
            
            return result;
        } catch (Exception e) {
            log.error("AI analysis failed for company {}: {}", requestCommand.getCompanyId(), e.getMessage());
            throw new RuntimeException("AI analysis failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get transaction data for AI selection interface
     */
    @Transactional(readOnly = true)
    public List<AITransactionDataDTO> getTransactionDataForSelection(AIDataFilterCommand filterCommand) {
        TenantId tenantId = TenantId.of(filterCommand.getCompanyId());
        
        List<TransactionAggregate> transactions = aiAnalysisDomainService
                .prepareTransactionDataForAI(tenantId, filterCommand.getStartDate(), 
                                           filterCommand.getEndDate(), filterCommand.getDepartmentId());
        
        return transactions.stream()
                .map(this::convertToAITransactionDataDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get report data for AI selection interface
     */
    @Transactional(readOnly = true)
    public List<AIReportDataDTO> getReportDataForSelection(AIDataFilterCommand filterCommand) {
        TenantId tenantId = TenantId.of(filterCommand.getCompanyId());
        
        List<ReportAggregate> reports = aiAnalysisDomainService
                .prepareReportDataForAI(tenantId, filterCommand.getStartDate(), filterCommand.getEndDate());
        
        return reports.stream()
                .map(this::convertToAIReportDataDTO)
                .collect(Collectors.toList());
    }

    // Private helper methods (Application Service concerns)

    private AIAnalysisResultDTO performAnalysisByType(String analysisType, 
                                                     String transactionData, 
                                                     String reportData, 
                                                     AIAnalysisRequestCommand requestCommand) {
        
        return switch (analysisType) {
            case "TREND_ANALYSIS" -> performTrendAnalysis(transactionData, reportData, requestCommand);
            case "ANOMALY_DETECTION" -> performAnomalyDetection(transactionData, requestCommand);
            case "FINANCIAL_INSIGHTS" -> performFinancialInsights(transactionData, reportData, requestCommand);
            case "COMPARATIVE_ANALYSIS" -> performComparativeAnalysis(transactionData, reportData, requestCommand);
            case "COMPREHENSIVE" -> performComprehensiveAnalysis(transactionData, reportData, requestCommand);
            default -> throw new IllegalArgumentException("Unsupported analysis type: " + analysisType);
        };
    }

    private AIAnalysisResultDTO performTrendAnalysis(String transactionData, String reportData, 
                                                   AIAnalysisRequestCommand requestCommand) {
        try {
            String prompt = buildTrendAnalysisPrompt(transactionData, reportData, requestCommand);
            String aiResponse = aiService.call(prompt);
            
            return AIAnalysisResultDTO.builder()
                    .analysisType("TREND_ANALYSIS")
                    .summary("Trend analysis completed successfully")
                    .insights(parseAIInsights(aiResponse))
                    .confidence("HIGH")
                    .dataPoints(countDataPoints(transactionData, reportData))
                    .generatedAt(java.time.LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            return createErrorResult("TREND_ANALYSIS", e.getMessage());
        }
    }

    private AIAnalysisResultDTO performAnomalyDetection(String transactionData, 
                                                       AIAnalysisRequestCommand requestCommand) {
        try {
            String prompt = buildAnomalyDetectionPrompt(transactionData, requestCommand);
            String aiResponse = aiService.call(prompt);
            
            return AIAnalysisResultDTO.builder()
                    .analysisType("ANOMALY_DETECTION")
                    .summary("Anomaly detection completed successfully")
                    .insights(parseAIInsights(aiResponse))
                    .confidence("MEDIUM")
                    .dataPoints(countDataPoints(transactionData, ""))
                    .generatedAt(java.time.LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            return createErrorResult("ANOMALY_DETECTION", e.getMessage());
        }
    }

    private AIAnalysisResultDTO performFinancialInsights(String transactionData, String reportData, 
                                                        AIAnalysisRequestCommand requestCommand) {
        try {
            String prompt = buildFinancialInsightsPrompt(transactionData, reportData, requestCommand);
            String aiResponse = aiService.call(prompt);
            
            return AIAnalysisResultDTO.builder()
                    .analysisType("FINANCIAL_INSIGHTS")
                    .summary("Financial insights generated successfully")
                    .insights(parseAIInsights(aiResponse))
                    .confidence("HIGH")
                    .dataPoints(countDataPoints(transactionData, reportData))
                    .generatedAt(java.time.LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            return createErrorResult("FINANCIAL_INSIGHTS", e.getMessage());
        }
    }

    private AIAnalysisResultDTO performComparativeAnalysis(String transactionData, String reportData, 
                                                          AIAnalysisRequestCommand requestCommand) {
        try {
            String prompt = buildComparativeAnalysisPrompt(transactionData, reportData, requestCommand);
            String aiResponse = aiService.call(prompt);
            
            return AIAnalysisResultDTO.builder()
                    .analysisType("COMPARATIVE_ANALYSIS")
                    .summary("Comparative analysis completed successfully")
                    .insights(parseAIInsights(aiResponse))
                    .confidence("MEDIUM")
                    .dataPoints(countDataPoints(transactionData, reportData))
                    .generatedAt(java.time.LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            return createErrorResult("COMPARATIVE_ANALYSIS", e.getMessage());
        }
    }

    private AIAnalysisResultDTO performComprehensiveAnalysis(String transactionData, String reportData, 
                                                           AIAnalysisRequestCommand requestCommand) {
        try {
            String prompt = buildComprehensiveAnalysisPrompt(transactionData, reportData, requestCommand);
            String aiResponse = aiService.call(prompt);
            
            return AIAnalysisResultDTO.builder()
                    .analysisType("COMPREHENSIVE")
                    .summary("Comprehensive analysis completed successfully")
                    .insights(parseAIInsights(aiResponse))
                    .confidence("HIGH")
                    .dataPoints(countDataPoints(transactionData, reportData))
                    .generatedAt(java.time.LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            return createErrorResult("COMPREHENSIVE", e.getMessage());
        }
    }

    // DTO conversion methods (Application Service responsibility)

    private TransactionSummaryDTO buildTransactionSummaryDTO(List<TransactionAggregate> transactions) {
        double totalIncome = transactions.stream()
                .filter(t -> "INCOME".equals(t.getTransactionType().toString()))
                .mapToDouble(t -> t.getMoney().getAmount().doubleValue())
                .sum();
        
        double totalExpense = transactions.stream()
                .filter(t -> "EXPENSE".equals(t.getTransactionType().toString()))
                .mapToDouble(t -> t.getMoney().getAmount().doubleValue())
                .sum();
        
        return TransactionSummaryDTO.builder()
                .totalTransactions(transactions.size())
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netAmount(totalIncome - totalExpense)
                .averageAmount(transactions.isEmpty() ? 0 : 
                    transactions.stream().mapToDouble(t -> t.getMoney().getAmount().doubleValue()).average().orElse(0))
                .build();
    }

    private ReportSummaryDTO buildReportSummaryDTO(List<ReportAggregate> reports) {
        long completedReports = reports.stream()
                .filter(ReportAggregate::canBeViewed)
                .count();
        
        return ReportSummaryDTO.builder()
                .totalReports(reports.size())
                .completedReports((int) completedReports)
                .reportsWithAI((int) reports.stream().filter(r -> r.getAiAnalysisEnabled()).count())
                .reportTypes(reports.stream().map(r -> r.getReportType().toString()).distinct().collect(Collectors.toList()))
                .build();
    }

    private AITransactionDataDTO convertToAITransactionDataDTO(TransactionAggregate transaction) {
        return AITransactionDataDTO.builder()
                .transactionId(transaction.getTransactionId())
                .description(transaction.getDescription())
                .amount(transaction.getAmount().doubleValue())
                .transactionType(transaction.getTransactionType().toString())
                .category("Unknown") // TransactionAggregate doesn't have getCategory method
                .transactionDate(transaction.getTransactionDate())
                .departmentId(transaction.getDepartmentId())
                .status(transaction.getTransactionStatus().getStatus().toString())
                .build();
    }

    private AIReportDataDTO convertToAIReportDataDTO(ReportAggregate report) {
        return AIReportDataDTO.builder()
                .reportId(report.getReportId())
                .reportName(report.getReportName())
                .reportType(report.getReportType().toString())
                .periodDescription(report.getPeriodDescription())
                .hasContent(report.hasContentData())
                .contentSize(report.getContent() != null ? report.getContent().getSize() : 0L)
                .aiAnalysisEnabled(report.getAiAnalysisEnabled())
                .status(report.getStatus().toString())
                .createdAt(report.getCreatedAt())
                .build();
    }

    private List<String> getAvailableAnalysisTypes(List<TransactionAggregate> transactions, 
                                                  List<ReportAggregate> reports) {
        List<String> types = List.of("FINANCIAL_INSIGHTS");
        
        if (transactions.size() >= 10) {
            types = List.of("TREND_ANALYSIS", "FINANCIAL_INSIGHTS", "ANOMALY_DETECTION");
        }
        
        if (!reports.isEmpty()) {
            types = List.of("TREND_ANALYSIS", "FINANCIAL_INSIGHTS", "ANOMALY_DETECTION", "COMPARATIVE_ANALYSIS");
        }
        
        if (transactions.size() >= 20 && !reports.isEmpty()) {
            types = List.of("TREND_ANALYSIS", "FINANCIAL_INSIGHTS", "ANOMALY_DETECTION", 
                           "COMPARATIVE_ANALYSIS", "COMPREHENSIVE");
        }
        
        return types;
    }

    // AI prompt building methods

    private String buildTrendAnalysisPrompt(String transactionData, String reportData, 
                                           AIAnalysisRequestCommand requestCommand) {
        return String.format("""
            You are a financial analyst. Analyze the following transaction and report data to identify trends.
            
            Analysis Period: %s to %s
            Company ID: %s
            
            Transaction Data:
            %s
            
            Report Data:
            %s
            
            Please provide:
            1. Key financial trends identified
            2. Notable patterns in spending/income
            3. Period-over-period changes
            4. Actionable recommendations
            
            Respond with clear, actionable insights in JSON format with fields: insights, trends, recommendations.
            """, 
            requestCommand.getStartDate(), requestCommand.getEndDate(), 
            requestCommand.getCompanyId(), transactionData, reportData);
    }

    private String buildAnomalyDetectionPrompt(String transactionData, AIAnalysisRequestCommand requestCommand) {
        return String.format("""
            You are a financial fraud analyst. Analyze the following transaction data to detect anomalies.
            
            Analysis Period: %s to %s
            Company ID: %s
            
            Transaction Data:
            %s
            
            Please identify:
            1. Unusual transaction amounts
            2. Irregular timing patterns
            3. Suspicious transaction descriptions
            4. Potential fraud indicators
            
            Respond with anomaly detection results in JSON format with fields: anomalies, riskLevel, recommendations.
            """, 
            requestCommand.getStartDate(), requestCommand.getEndDate(), 
            requestCommand.getCompanyId(), transactionData);
    }

    private String buildFinancialInsightsPrompt(String transactionData, String reportData, 
                                              AIAnalysisRequestCommand requestCommand) {
        return String.format("""
            You are a financial advisor. Analyze the following financial data to provide insights.
            
            Analysis Period: %s to %s
            Company ID: %s
            
            Transaction Data:
            %s
            
            Report Data:
            %s
            
            Please provide:
            1. Financial health assessment
            2. Cash flow analysis
            3. Cost optimization opportunities
            4. Strategic recommendations
            
            Respond with financial insights in JSON format with fields: healthScore, insights, opportunities, recommendations.
            """, 
            requestCommand.getStartDate(), requestCommand.getEndDate(), 
            requestCommand.getCompanyId(), transactionData, reportData);
    }

    private String buildComparativeAnalysisPrompt(String transactionData, String reportData, 
                                                 AIAnalysisRequestCommand requestCommand) {
        return String.format("""
            You are a financial analyst. Perform comparative analysis on the following financial data.
            
            Analysis Period: %s to %s
            Company ID: %s
            
            Transaction Data:
            %s
            
            Report Data:
            %s
            
            Please compare:
            1. Current period vs previous periods (if data available)
            2. Income vs expense patterns
            3. Category-wise spending comparison
            4. Performance benchmarks
            
            Respond with comparative analysis in JSON format with fields: comparisons, variances, insights, recommendations.
            """, 
            requestCommand.getStartDate(), requestCommand.getEndDate(), 
            requestCommand.getCompanyId(), transactionData, reportData);
    }

    private String buildComprehensiveAnalysisPrompt(String transactionData, String reportData, 
                                                   AIAnalysisRequestCommand requestCommand) {
        return String.format("""
            You are a senior financial consultant. Perform comprehensive financial analysis.
            
            Analysis Period: %s to %s
            Company ID: %s
            
            Transaction Data:
            %s
            
            Report Data:
            %s
            
            Please provide a comprehensive analysis including:
            1. Financial performance overview
            2. Trend analysis and patterns
            3. Risk assessment and anomalies
            4. Comparative insights
            5. Strategic recommendations
            6. Executive summary
            
            Respond with comprehensive analysis in JSON format with fields: executive_summary, performance, trends, risks, recommendations.
            """, 
            requestCommand.getStartDate(), requestCommand.getEndDate(), 
            requestCommand.getCompanyId(), transactionData, reportData);
    }

    // Helper methods

    private List<String> parseAIInsights(String aiResponse) {
        // Simple implementation - in production, you'd parse JSON properly
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return List.of("AI analysis completed, but no specific insights were generated.");
        }
        
        // Extract insights from AI response (simplified)
        return List.of(
            "AI analysis insight 1: " + aiResponse.substring(0, Math.min(100, aiResponse.length())),
            "AI analysis insight 2: Financial patterns identified",
            "AI analysis insight 3: Recommendations generated"
        );
    }

    private int countDataPoints(String transactionData, String reportData) {
        // Simple count of data points
        int transactionCount = transactionData.split("\"id\":").length - 1;
        int reportCount = reportData.split("\"id\":").length - 1;
        return transactionCount + reportCount;
    }

    private AIAnalysisResultDTO createErrorResult(String analysisType, String errorMessage) {
        return AIAnalysisResultDTO.builder()
                .analysisType(analysisType)
                .summary("Analysis failed: " + errorMessage)
                .insights(List.of("Unable to complete analysis due to error: " + errorMessage))
                .confidence("LOW")
                .dataPoints(0)
                .generatedAt(java.time.LocalDateTime.now())
                .build();
    }
}