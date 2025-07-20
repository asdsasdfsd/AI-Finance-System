// backend/src/main/java/org/example/backend/application/dto/FinancialGroupingData.java
package org.example.backend.application.dto;

import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.valueobject.TenantId;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Financial Grouping Data DTO - Fixed Lombok Issues
 * FIXED: Added missing getter methods for export functionality
 * 
 * Contains structured data for financial grouping report generation
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FinancialGroupingData {
    private TenantId tenantId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String periodDescription;
    private Map<String, CategoryGrouping> byCategory;
    private Map<String, DepartmentGrouping> byDepartment;
    private Map<String, FundGrouping> byFund;
    private Map<String, TransactionTypeGrouping> byTransactionType;
    private Map<String, MonthGrouping> byMonth;
    
    // FIXED: Add compatibility methods for export service
    public Map<String, BigDecimal> getCategoryGrouping() {
        if (byCategory == null) {
            return Map.of();
        }
        return byCategory.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().getTotalAmount()
                ));
    }
    
    public Map<String, BigDecimal> getDepartmentGrouping() {
        if (byDepartment == null) {
            return Map.of();
        }
        return byDepartment.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().getTotalAmount()
                ));
    }
    
    public Map<String, BigDecimal> getFundGrouping() {
        if (byFund == null) {
            return Map.of();
        }
        return byFund.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().getTotalAmount()
                ));
    }
    
    public Map<String, BigDecimal> getTransactionTypeGrouping() {
        if (byTransactionType == null) {
            return Map.of();
        }
        return byTransactionType.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().getTotalAmount()
                ));
    }
    
    public Map<String, BigDecimal> getMonthlyGrouping() {
        if (byMonth == null) {
            return Map.of();
        }
        return byMonth.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().getTotalAmount()
                ));
    }
    
    public String getPeriodDescription() {
        if (periodDescription != null) {
            return periodDescription;
        }
        return String.format("From %s to %s", startDate, endDate);
    }
    
    public BigDecimal getGrandTotal() {
        return getCategoryGrouping().values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public int getTotalTransactionCount() {
        if (byCategory == null) {
            return 0;
        }
        return byCategory.values().stream()
            .mapToInt(CategoryGrouping::getTransactionCount)
            .sum();
    }
    
    /**
     * Category Grouping Data
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryGrouping {
        private String categoryName;
        private BigDecimal totalAmount;
        private int transactionCount;
        private BigDecimal averageAmount;
        private List<TransactionAggregate> transactions;
        
        public BigDecimal getAverageAmount() {
            if (averageAmount != null) {
                return averageAmount;
            }
            return transactionCount > 0 ?
                totalAmount.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
        }
        
        public double getPercentage(BigDecimal grandTotal) {
            if (grandTotal == null || grandTotal.compareTo(BigDecimal.ZERO) <= 0) {
                return 0.0;
            }
            return totalAmount.divide(grandTotal, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue();
        }
    }
    
    /**
     * Department Grouping Data
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DepartmentGrouping {
        private String departmentName;
        private BigDecimal totalAmount;
        private int transactionCount;
        private BigDecimal averageAmount;
        private List<TransactionAggregate> transactions;
        
        public BigDecimal getAverageAmount() {
            if (averageAmount != null) {
                return averageAmount;
            }
            return transactionCount > 0 ?
                totalAmount.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
        }
    }
    
    /**
     * Fund Grouping Data
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FundGrouping {
        private String fundName;
        private BigDecimal totalAmount;
        private int transactionCount;
        private BigDecimal averageAmount;
        private BigDecimal budgetAllocation;
        private BigDecimal variance;
        private List<TransactionAggregate> transactions;
        
        public BigDecimal getAverageAmount() {
            if (averageAmount != null) {
                return averageAmount;
            }
            return transactionCount > 0 ? 
                totalAmount.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
        }
        
        public BigDecimal getVariance() {
            if (variance != null) {
                return variance;
            }
            return totalAmount.subtract(budgetAllocation != null ? budgetAllocation : BigDecimal.ZERO);
        }
    }
    
    /**
     * Transaction Type Grouping Data
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TransactionTypeGrouping {
        private String typeName;
        private String transactionType; // Alternative field name for compatibility
        private BigDecimal totalAmount;
        private int transactionCount;
        private BigDecimal minAmount;
        private BigDecimal maxAmount;
        private BigDecimal averageAmount;
        private List<TransactionAggregate> transactions;
        
        public String getTypeName() {
            return typeName != null ? typeName : transactionType;
        }
        
        public BigDecimal getAverageAmount() {
            if (averageAmount != null) {
                return averageAmount;
            }
            return transactionCount > 0 ?
                totalAmount.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
        }
    }
    
    /**
     * Monthly Grouping Data
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MonthGrouping {
        private String monthName;
        private String monthKey; // Format: yyyy-MM
        private BigDecimal totalAmount;
        private BigDecimal incomeAmount;
        private BigDecimal expenseAmount;
        private int transactionCount;
        private BigDecimal growthRate;
        private BigDecimal averageAmount;
        private LocalDate firstDayOfMonth;
        private List<TransactionAggregate> transactions;
        
        public BigDecimal getNetAmount() {
            BigDecimal income = incomeAmount != null ? incomeAmount : BigDecimal.ZERO;
            BigDecimal expense = expenseAmount != null ? expenseAmount : BigDecimal.ZERO;
            return income.subtract(expense);
        }
        
        public String getDisplayName() {
            if (monthName != null) {
                return monthName;
            }
            if (firstDayOfMonth != null) {
                return firstDayOfMonth.getMonth().name() + " " + firstDayOfMonth.getYear();
            }
            return monthKey != null ? monthKey : "Unknown Month";
        }
        
        public LocalDate getFirstDayOfMonth() {
            return firstDayOfMonth;
        }
        
        public BigDecimal getAverageAmount() {
            if (averageAmount != null) {
                return averageAmount;
            }
            return transactionCount > 0 ? 
                totalAmount.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
        }
    }
}