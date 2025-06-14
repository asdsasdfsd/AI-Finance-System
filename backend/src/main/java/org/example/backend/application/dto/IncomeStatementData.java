// backend/src/main/java/org/example/backend/application/dto/IncomeStatementData.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Income Statement Data DTO - Enhanced for comprehensive reporting
 * 
 * Contains all data required for income statement generation and display
 */
@Data
@Builder
public class IncomeStatementData {
    
    // Period information
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    
    // Revenue section
    private BigDecimal totalRevenue;
    private Map<String, BigDecimal> revenueByCategory;
    
    // Expense section
    private BigDecimal totalExpenses;
    private Map<String, BigDecimal> expensesByCategory;
    
    // Calculated values
    private BigDecimal grossProfit;
    private BigDecimal operatingExpenses;
    private BigDecimal operatingIncome;
    private BigDecimal otherIncome;
    private BigDecimal netIncome;
    
    // Comparison data
    private PeriodComparison previousPeriod;
    
    // Metadata
    private Integer transactionCount;
    
    /**
     * Get period description for Excel header
     */
    public String getPeriodDescription() {
        return String.format("From %s to %s", periodStartDate, periodEndDate);
    }
    
    /**
     * Get start date (for compatibility)
     */
    public LocalDate getStartDate() {
        return periodStartDate;
    }
    
    /**
     * Get end date (for compatibility)
     */
    public LocalDate getEndDate() {
        return periodEndDate;
    }
    
    /**
     * Get net profit (alias for net income)
     */
    public BigDecimal getNetProfit() {
        return netIncome;
    }
    
    /**
     * Get revenue items as list
     */
    public List<RevenueItem> getRevenues() {
        List<RevenueItem> items = new ArrayList<>();
        if (revenueByCategory != null) {
            for (Map.Entry<String, BigDecimal> entry : revenueByCategory.entrySet()) {
                items.add(new RevenueItem(entry.getKey(), entry.getValue()));
            }
        }
        return items;
    }
    
    /**
     * Get operating expenses as list (simplified)
     */
    public List<ExpenseItem> getOperatingExpenses() {
        List<ExpenseItem> items = new ArrayList<>();
        if (expensesByCategory != null) {
            // Take first half as "operating" expenses
            int count = 0;
            for (Map.Entry<String, BigDecimal> entry : expensesByCategory.entrySet()) {
                if (count++ < expensesByCategory.size() / 2) {
                    items.add(new ExpenseItem(entry.getKey(), entry.getValue()));
                }
            }
        }
        return items;
    }
    
    /**
     * Get total operating expenses
     */
    public BigDecimal getTotalOperatingExpenses() {
        return operatingExpenses != null ? operatingExpenses : BigDecimal.ZERO;
    }
    
    /**
     * Get administrative expenses (simplified)
     */
    public List<ExpenseItem> getAdministrativeExpenses() {
        List<ExpenseItem> items = new ArrayList<>();
        if (expensesByCategory != null) {
            // Take second half as "administrative" expenses
            int count = 0;
            int halfSize = expensesByCategory.size() / 2;
            for (Map.Entry<String, BigDecimal> entry : expensesByCategory.entrySet()) {
                if (count++ >= halfSize) {
                    items.add(new ExpenseItem(entry.getKey(), entry.getValue()));
                }
            }
        }
        return items;
    }
    
    /**
     * Get total administrative expenses
     */
    public BigDecimal getTotalAdministrativeExpenses() {
        return getAdministrativeExpenses().stream()
                .map(ExpenseItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Get financial expenses (empty for now)
     */
    public List<ExpenseItem> getFinancialExpenses() {
        return new ArrayList<>();
    }
    
    /**
     * Get total financial expenses
     */
    public BigDecimal getTotalFinancialExpenses() {
        return BigDecimal.ZERO;
    }
    
    /**
     * Get other incomes (empty for now)
     */
    public List<IncomeItem> getOtherIncomes() {
        return new ArrayList<>();
    }
    
    /**
     * Get total other incomes
     */
    public BigDecimal getTotalOtherIncomes() {
        return otherIncome != null ? otherIncome : BigDecimal.ZERO;
    }
    
    /**
     * Get other expenses (empty for now)
     */
    public List<ExpenseItem> getOtherExpenses() {
        return new ArrayList<>();
    }
    
    /**
     * Get total other expenses
     */
    public BigDecimal getTotalOtherExpenses() {
        return BigDecimal.ZERO;
    }
    
    /**
     * Revenue Item for Excel generation
     */
    @Data
    public static class RevenueItem {
        private String name;
        private BigDecimal amount;
        
        public RevenueItem(String name, BigDecimal amount) {
            this.name = name;
            this.amount = amount;
        }
    }
    
    /**
     * Expense Item for Excel generation
     */
    @Data
    public static class ExpenseItem {
        private String name;
        private BigDecimal amount;
        
        public ExpenseItem(String name, BigDecimal amount) {
            this.name = name;
            this.amount = amount;
        }
    }
    
    /**
     * Income Item for Excel generation
     */
    @Data
    public static class IncomeItem {
        private String name;
        private BigDecimal amount;
        
        public IncomeItem(String name, BigDecimal amount) {
            this.name = name;
            this.amount = amount;
        }
    }
    
    /**
     * Previous period comparison data
     */
    @Data
    @Builder
    public static class PeriodComparison {
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal totalRevenue;
        private BigDecimal totalExpenses;
        private BigDecimal netIncome;
        
        // Calculate variance percentages
        public BigDecimal getRevenueVariancePercent(BigDecimal currentRevenue) {
            if (totalRevenue.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            return currentRevenue.subtract(totalRevenue)
                .divide(totalRevenue, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
        }
        
        public BigDecimal getExpenseVariancePercent(BigDecimal currentExpenses) {
            if (totalExpenses.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            return currentExpenses.subtract(totalExpenses)
                .divide(totalExpenses, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
        }
        
        public BigDecimal getNetIncomeVariancePercent(BigDecimal currentNetIncome) {
            if (netIncome.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            return currentNetIncome.subtract(netIncome)
                .divide(netIncome, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
        }
    }
    
    // Calculated properties for display
    public BigDecimal getGrossProfitMargin() {
        if (totalRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return grossProfit.divide(totalRevenue, 4, BigDecimal.ROUND_HALF_UP)
            .multiply(new BigDecimal("100"));
    }
    
    public BigDecimal getNetProfitMargin() {
        if (totalRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return netIncome.divide(totalRevenue, 4, BigDecimal.ROUND_HALF_UP)
            .multiply(new BigDecimal("100"));
    }
    
    public BigDecimal getExpenseRatio() {
        if (totalRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalExpenses.divide(totalRevenue, 4, BigDecimal.ROUND_HALF_UP)
            .multiply(new BigDecimal("100"));
    }
}