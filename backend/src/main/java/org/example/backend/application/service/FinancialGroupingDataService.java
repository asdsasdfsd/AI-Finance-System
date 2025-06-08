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
 * Financial Grouping Data Service
 * 
 * Responsibilities:
 * 1. Group transactions by various dimensions (category, department, fund, etc.)
 * 2. Calculate aggregated statistics for each grouping
 * 3. Provide data for financial grouping reports
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
                        list.get(0).getTransactionDate()
                    )
                )
            ));
    }
    
    // Helper methods (simplified for demo)
    private String getCategoryName(Integer categoryId) {
        return categoryId != null ? "Category " + categoryId : "Uncategorized";
    }
    
    private String getDepartmentName(Integer departmentId) {
        return departmentId != null ? "Department " + departmentId : "No Department";
    }
    
    private String getFundName(Integer fundId) {
        return fundId != null ? "Fund " + fundId : "General Fund";
    }
}