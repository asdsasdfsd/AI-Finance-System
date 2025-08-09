// backend/src/main/java/org/example/backend/domain/service/AIAnalysisDomainService.java
package org.example.backend.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.aggregate.transaction.TransactionAggregateRepository;
import org.example.backend.domain.aggregate.report.ReportAggregateRepository;
import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.aggregate.report.ReportAggregate;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.TransactionStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI Analysis Domain Service - DDD Compliant
 * 
 * This is a Domain Service that coordinates cross-aggregate business logic
 * for AI analysis. It follows DDD principles by:
 * 1. Operating on domain objects (aggregates)
 * 2. Implementing business logic that spans multiple aggregates
 * 3. Being stateless and focused on domain concepts
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIAnalysisDomainService {

    private final TransactionAggregateRepository transactionRepository;
    private final ReportAggregateRepository reportRepository;

    /**
     * Prepare transaction data for AI analysis within a time period
     * 
     * Domain Rule: Only approved transactions should be included in AI analysis
     */
    public List<TransactionAggregate> prepareTransactionDataForAI(TenantId tenantId, 
                                                                LocalDate startDate, 
                                                                LocalDate endDate,
                                                                Integer departmentId) {
        
        log.debug("Preparing transaction data for AI analysis - Tenant: {}, Period: {} to {}", 
                 tenantId.getValue(), startDate, endDate);
        
        // Domain Rule: Get all approved transactions in the period
        List<TransactionAggregate> transactions = transactionRepository
                .findByTenantIdAndTransactionDateBetween(tenantId, startDate, endDate)
                .stream()
                .filter(t -> t.getTransactionStatus().getStatus() == TransactionStatus.Status.APPROVED)
                .collect(Collectors.toList());
        
        // Domain Rule: Filter by department if specified
        if (departmentId != null) {
            transactions = transactions.stream()
                    .filter(t -> departmentId.equals(t.getDepartmentId()))
                    .collect(Collectors.toList());
        }
        
        log.debug("Found {} approved transactions for AI analysis", transactions.size());
        return transactions;
    }

    /**
     * Prepare report data for AI analysis within a time period
     * 
     * Domain Rule: Only completed reports with content can be analyzed
     */
    public List<ReportAggregate> prepareReportDataForAI(TenantId tenantId, 
                                                       LocalDate startDate, 
                                                       LocalDate endDate) {
        
        log.debug("Preparing report data for AI analysis - Tenant: {}, Period: {} to {}", 
                 tenantId.getValue(), startDate, endDate);
        
        // Domain Rule: Only get reports that can be viewed and are ready for AI
        List<ReportAggregate> reports = reportRepository
                .findByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream()
                .filter(r -> !r.getStartDate().isAfter(endDate) && !r.getEndDate().isBefore(startDate))
                .filter(ReportAggregate::canBeViewed)
                .filter(r -> r.getContent() != null && r.getContent().isSuitableForAI())
                .collect(Collectors.toList());
        
        log.debug("Found {} reports suitable for AI analysis", reports.size());
        return reports;
    }

    /**
     * Generate structured data for AI analysis from transactions
     * 
     * Domain Rule: Convert domain objects to AI-readable format while preserving business meaning
     */
    public String generateTransactionAnalysisData(List<TransactionAggregate> transactions) {
        if (transactions.isEmpty()) {
            return "{\"transactions\": [], \"summary\": \"No transactions available for analysis\"}";
        }
        
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\"transactions\":[");
        
        for (int i = 0; i < transactions.size(); i++) {
            TransactionAggregate t = transactions.get(i);
            if (i > 0) jsonBuilder.append(",");
            
            // Use domain methods to extract business-relevant data
            jsonBuilder.append(String.format("""
                {
                    "id": %d,
                    "description": "%s",
                    "amount": %.2f,
                    "type": "%s",
                    "category": "%s",
                    "date": "%s",
                    "departmentId": %s,
                    "status": "%s"
                }
                """, 
                t.getTransactionId(),
                escapeJson(t.getDescription()),
                t.getMoney().getAmount().doubleValue(),
                t.getTransactionType(),
                "Unknown", // Category not available in aggregate
                t.getTransactionDate(),
                t.getDepartmentId(),
                t.getTransactionStatus().getStatus()
            ));
        }
        
        // Add summary statistics using domain calculations
        double totalAmount = transactions.stream()
                .mapToDouble(t -> t.getMoney().getAmount().doubleValue())
                .sum();
        
        long incomeCount = transactions.stream()
                .filter(t -> "INCOME".equals(t.getTransactionType().toString()))
                .count();
        
        long expenseCount = transactions.stream()
                .filter(t -> "EXPENSE".equals(t.getTransactionType().toString()))
                .count();
        
        jsonBuilder.append(String.format("""
            ],
            "summary": {
                "totalTransactions": %d,
                "totalAmount": %.2f,
                "incomeTransactions": %d,
                "expenseTransactions": %d,
                "period": "%s to %s"
            }}
            """,
            transactions.size(),
            totalAmount,
            incomeCount,
            expenseCount,
            transactions.stream().map(t -> t.getTransactionDate()).min(LocalDate::compareTo).orElse(LocalDate.now()),
            transactions.stream().map(t -> t.getTransactionDate()).max(LocalDate::compareTo).orElse(LocalDate.now())
        ));
        
        return jsonBuilder.toString();
    }

    /**
     * Generate structured data for AI analysis from reports
     * 
     * Domain Rule: Extract report content while maintaining business context
     */
    public String generateReportAnalysisData(List<ReportAggregate> reports) {
        if (reports.isEmpty()) {
            return "{\"reports\": [], \"summary\": \"No reports available for analysis\"}";
        }
        
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\"reports\":[");
        
        for (int i = 0; i < reports.size(); i++) {
            ReportAggregate r = reports.get(i);
            if (i > 0) jsonBuilder.append(",");
            
            jsonBuilder.append(String.format("""
                {
                    "id": %d,
                    "name": "%s",
                    "type": "%s",
                    "period": "%s",
                    "contentSize": %d,
                    "aiAnalysisEnabled": %s,
                    "contentData": "%s"
                }
                """,
                r.getReportId(),
                escapeJson(r.getReportName()),
                r.getReportType(),
                r.getPeriodDescription(),
                r.getContent().getSize(),
                r.getAiAnalysisEnabled(),
                escapeJson(r.getContent().getSummary()) // Use summary for large content
            ));
        }
        
        jsonBuilder.append(String.format("""
            ],
            "summary": {
                "totalReports": %d,
                "reportTypes": %s,
                "totalContentSize": %d
            }}
            """,
            reports.size(),
            reports.stream().map(r -> "\"" + r.getReportType() + "\"").distinct().collect(Collectors.joining(",")),
            reports.stream().mapToLong(r -> r.getContent().getSize()).sum()
        ));
        
        return jsonBuilder.toString();
    }

    /**
     * Validate that the tenant has sufficient data for meaningful AI analysis
     * 
     * Domain Rule: AI analysis requires minimum data threshold
     */
    public boolean validateDataSufficiencyForAI(TenantId tenantId, 
                                               LocalDate startDate, 
                                               LocalDate endDate) {
        
        // Business Rule: Need at least 5 transactions or 1 report for meaningful analysis
        List<TransactionAggregate> transactions = transactionRepository
                .findByTenantIdAndTransactionDateBetween(tenantId, startDate, endDate)
                .stream()
                .filter(t -> t.getTransactionStatus().getStatus() == TransactionStatus.Status.APPROVED)
                .collect(Collectors.toList());
                
        List<ReportAggregate> reports = reportRepository
                .findByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream()
                .filter(r -> !r.getStartDate().isAfter(endDate) && !r.getEndDate().isBefore(startDate))
                .filter(ReportAggregate::canBeViewed)
                .collect(Collectors.toList());
        
        boolean hasSufficientTransactions = transactions.size() >= 5;
        boolean hasSufficientReports = !reports.isEmpty();
        
        log.debug("Data sufficiency check - Transactions: {}, Reports: {}, Sufficient: {}", 
                 transactions.size(), reports.size(), hasSufficientTransactions || hasSufficientReports);
        
        return hasSufficientTransactions || hasSufficientReports;
    }

    /**
     * Calculate estimated analysis complexity
     * 
     * Domain Rule: Analysis complexity depends on data volume and type diversity
     */
    public String estimateAnalysisComplexity(List<TransactionAggregate> transactions, 
                                           List<ReportAggregate> reports) {
        
        int totalDataPoints = transactions.size() + reports.size();
        
        // Count unique categories and types for complexity estimation
        long uniqueCategories = transactions.stream()
                .map(t -> "Unknown") // Category not available
                .distinct()
                .count();
        
        long uniqueReportTypes = reports.stream()
                .map(ReportAggregate::getReportType)
                .distinct()
                .count();
        
        if (totalDataPoints < 20 && uniqueCategories < 5 && uniqueReportTypes <= 1) {
            return "SIMPLE";
        } else if (totalDataPoints < 100 && uniqueCategories < 10 && uniqueReportTypes <= 2) {
            return "MODERATE";
        } else {
            return "COMPLEX";
        }
    }

    /**
     * Helper method to escape JSON strings
     */
    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}