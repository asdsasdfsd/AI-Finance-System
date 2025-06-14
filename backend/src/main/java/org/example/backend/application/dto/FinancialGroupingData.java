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
    
    public String getPeriodDescription() {
        if (periodDescription != null) {
            return periodDescription;
        }
        return String.format("From %s to %s", startDate, endDate);
    }
    
    public BigDecimal getGrandTotal() {
        if (byCategory == null) {
            return BigDecimal.ZERO;
        }
        return byCategory.values().stream()
            .map(CategoryGrouping::getTotalAmount)
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
     * Transaction Type Grouping Data
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TransactionTypeGrouping {
        private String typeName;
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
     * Monthly Grouping Data
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MonthGrouping {
        private String monthKey; // Format: yyyy-MM
        private BigDecimal totalAmount;
        private int transactionCount;
        private BigDecimal averageAmount;
        private LocalDate firstDayOfMonth;
        private List<TransactionAggregate> transactions;
        
        public BigDecimal getAverageAmount() {
            if (averageAmount != null) {
                return averageAmount;
            }
            return transactionCount > 0 ? 
                totalAmount.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
        }
        
        public String getDisplayName() {
            if (firstDayOfMonth == null) {
                return monthKey;
            }
            return firstDayOfMonth.getMonth().name() + " " + firstDayOfMonth.getYear();
        }
    }
}