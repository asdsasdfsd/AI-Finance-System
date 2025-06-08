// backend/src/main/java/org/example/backend/application/dto/FinancialGroupingData.java
package org.example.backend.application.dto;

import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.valueobject.TenantId;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Financial Grouping Data
 * 
 * Contains structured data for financial grouping report generation
 */
@Data
@Builder
public class FinancialGroupingData {
    private TenantId tenantId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Map<String, CategoryGrouping> byCategory;
    private Map<String, DepartmentGrouping> byDepartment;
    private Map<String, FundGrouping> byFund;
    private Map<String, TransactionTypeGrouping> byTransactionType;
    private Map<String, MonthlyGrouping> byMonth;
    
    public String getPeriodDescription() {
        return String.format("From %s to %s", startDate, endDate);
    }
    
    public BigDecimal getGrandTotal() {
        return byCategory.values().stream()
            .map(CategoryGrouping::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public int getTotalTransactionCount() {
        return byCategory.values().stream()
            .mapToInt(CategoryGrouping::getTransactionCount)
            .sum();
    }
    
    @Data
    @AllArgsConstructor
    public static class CategoryGrouping {
        private String categoryName;
        private BigDecimal totalAmount;
        private int transactionCount;
        private List<TransactionAggregate> transactions;
        
        public BigDecimal getAverageAmount() {
            return transactionCount > 0 ? 
                totalAmount.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
        }
        
        public double getPercentage(BigDecimal grandTotal) {
            return grandTotal.compareTo(BigDecimal.ZERO) > 0 ?
                totalAmount.divide(grandTotal, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).doubleValue() :
                0.0;
        }
    }
    
    @Data
    @AllArgsConstructor
    public static class DepartmentGrouping {
        private String departmentName;
        private BigDecimal totalAmount;
        private int transactionCount;
        
        public BigDecimal getAverageAmount() {
            return transactionCount > 0 ? 
                totalAmount.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
        }
    }
    
    @Data
    @AllArgsConstructor
    public static class FundGrouping {
        private String fundName;
        private BigDecimal totalAmount;
        private int transactionCount;
        
        public BigDecimal getAverageAmount() {
            return transactionCount > 0 ? 
                totalAmount.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
        }
    }
    
    @Data
    @AllArgsConstructor
    public static class TransactionTypeGrouping {
        private String typeName;
        private BigDecimal totalAmount;
        private int transactionCount;
        
        public BigDecimal getAverageAmount() {
            return transactionCount > 0 ? 
                totalAmount.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
        }
    }
    
    @Data
    @AllArgsConstructor
    public static class MonthlyGrouping {
        private String monthKey; // Format: yyyy-MM
        private BigDecimal totalAmount;
        private int transactionCount;
        private LocalDate firstDayOfMonth;
        
        public BigDecimal getAverageAmount() {
            return transactionCount > 0 ? 
                totalAmount.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
        }
        
        public String getDisplayName() {
            return firstDayOfMonth.getMonth().name() + " " + firstDayOfMonth.getYear();
        }
    }
}