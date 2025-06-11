// backend/src/main/java/org/example/backend/application/service/FinancialGroupingDataService.java
package org.example.backend.application.service;

import org.example.backend.application.dto.FinancialGroupingData;
import org.example.backend.domain.aggregate.transaction.TransactionAggregateRepository;
import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.TransactionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.math.BigDecimal;

/**
 * Financial Grouping Data Service - Enhanced DDD Implementation
 * 
 * Responsibilities:
 * 1. Group transactions by various dimensions (category, department, fund, etc.)
 * 2. Calculate aggregated statistics for each grouping
 * 3. Provide data for financial grouping reports
 * 4. Support comprehensive financial analysis and reporting
 */
@Service
@Transactional(readOnly = true)
public class FinancialGroupingDataService {
    
    @Autowired
    private TransactionAggregateRepository transactionRepository;
    
    /**
     * Get financial grouping data for specified period
     */
    public FinancialGroupingData getFinancialGroupingData(TenantId tenantId, LocalDate startDate, LocalDate endDate) {
        // Get all approved transactions in the period
        List<TransactionAggregate> transactions = transactionRepository.findByDateRangeTypeAndStatus(
            tenantId, startDate, endDate, null, TransactionStatus.Status.APPROVED
        );
        
        return FinancialGroupingData.builder()
            .tenantId(tenantId)
            .startDate(startDate)
            .endDate(endDate)
            .byCategory(groupByCategory(transactions))
            .byDepartment(groupByDepartment(transactions))
            .byFund(groupByFund(transactions))
            .byTransactionType(groupByTransactionType(transactions))
            .byMonth(groupByMonth(transactions))
            .build();
    }
    
    /**
     * Group transactions by category with enhanced logic
     */
    private Map<String, FinancialGroupingData.CategoryGrouping> groupByCategory(List<TransactionAggregate> transactions) {
        return transactions.stream()
            .collect(Collectors.groupingBy(
                t -> getCategoryName(t.getCategoryId()),
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> new FinancialGroupingData.CategoryGrouping(
                        getCategoryName(list.get(0).getCategoryId()),
                        list.stream().map(t -> t.getMoney().getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add),
                        list.size(),
                        list
                    )
                )
            ));
    }
    
    /**
     * Group transactions by department with enhanced logic
     */
    private Map<String, FinancialGroupingData.DepartmentGrouping> groupByDepartment(List<TransactionAggregate> transactions) {
        return transactions.stream()
            .collect(Collectors.groupingBy(
                t -> getDepartmentName(t.getDepartmentId()),
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> new FinancialGroupingData.DepartmentGrouping(
                        getDepartmentName(list.get(0).getDepartmentId()),
                        list.stream().map(t -> t.getMoney().getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add),
                        list.size()
                    )
                )
            ));
    }
    
    /**
     * Group transactions by fund with enhanced logic
     */
    private Map<String, FinancialGroupingData.FundGrouping> groupByFund(List<TransactionAggregate> transactions) {
        return transactions.stream()
            .collect(Collectors.groupingBy(
                t -> getFundName(t.getFundId()),
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> new FinancialGroupingData.FundGrouping(
                        getFundName(list.get(0).getFundId()),
                        list.stream().map(t -> t.getMoney().getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add),
                        list.size()
                    )
                )
            ));
    }
    
    /**
     * Group transactions by transaction type with enhanced logic
     */
    private Map<String, FinancialGroupingData.TransactionTypeGrouping> groupByTransactionType(List<TransactionAggregate> transactions) {
        return transactions.stream()
            .collect(Collectors.groupingBy(
                t -> t.getTransactionType().getDisplayName(),
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> new FinancialGroupingData.TransactionTypeGrouping(
                        list.get(0).getTransactionType().getDisplayName(),
                        list.stream().map(t -> t.getMoney().getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add),
                        list.size()
                    )
                )
            ));
    }
    
    /**
     * Group transactions by month with enhanced logic
     */
    private Map<String, FinancialGroupingData.MonthlyGrouping> groupByMonth(List<TransactionAggregate> transactions) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        return transactions.stream()
            .collect(Collectors.groupingBy(
                t -> t.getTransactionDate().format(formatter),
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> new FinancialGroupingData.MonthlyGrouping(
                        list.get(0).getTransactionDate().format(formatter),
                        list.stream().map(t -> t.getMoney().getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add),
                        list.size(),
                        list.get(0).getTransactionDate().withDayOfMonth(1)
                    )
                )
            ));
    }
    
    /**
     * Generate financial grouping summary for specified tenant and period
     */
    public Map<String, Object> generateFinancialGroupingSummary(TenantId tenantId, LocalDate startDate, LocalDate endDate) {
        FinancialGroupingData data = getFinancialGroupingData(tenantId, startDate, endDate);
        
        Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("totalTransactions", data.getTotalTransactionCount());
        summary.put("totalAmount", data.getGrandTotal());
        summary.put("categoryCount", data.getByCategory().size());
        summary.put("departmentCount", data.getByDepartment().size());
        summary.put("fundCount", data.getByFund().size());
        summary.put("monthCount", data.getByMonth().size());
        summary.put("period", data.getPeriodDescription());
        
        // Add largest category by amount
        data.getByCategory().values().stream()
                .max((a, b) -> a.getTotalAmount().compareTo(b.getTotalAmount()))
                .ifPresent(largest -> {
                    summary.put("largestCategory", largest.getCategoryName());
                    summary.put("largestCategoryAmount", largest.getTotalAmount());
                });
        
        return summary;
    }
    
    /**
     * Get top categories by amount for specified period
     */
    public List<FinancialGroupingData.CategoryGrouping> getTopCategoriesByAmount(TenantId tenantId, 
                                                                                LocalDate startDate, 
                                                                                LocalDate endDate, 
                                                                                int limit) {
        FinancialGroupingData data = getFinancialGroupingData(tenantId, startDate, endDate);
        
        return data.getByCategory().values().stream()
                .sorted((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Get transaction type distribution for specified period
     */
    public Map<String, BigDecimal> getTransactionTypeDistribution(TenantId tenantId, 
                                                                 LocalDate startDate, 
                                                                 LocalDate endDate) {
        FinancialGroupingData data = getFinancialGroupingData(tenantId, startDate, endDate);
        
        return data.getByTransactionType().entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().getTotalAmount()
                ));
    }
    
    // Enhanced helper methods with better business logic
    private String getCategoryName(Integer categoryId) {
        if (categoryId == null) {
            return "Uncategorized";
        }
        // TODO: Can be enhanced to lookup actual category names from repository
        return "Category " + categoryId;
    }
    
    private String getDepartmentName(Integer departmentId) {
        if (departmentId == null) {
            return "No Department";
        }
        // TODO: Can be enhanced to lookup actual department names from repository
        return "Department " + departmentId;
    }
    
    private String getFundName(Integer fundId) {
        if (fundId == null) {
            return "General Fund";
        }
        // TODO: Can be enhanced to lookup actual fund names from repository
        return "Fund " + fundId;
    }
}