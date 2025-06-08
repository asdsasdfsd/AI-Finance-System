// backend/src/main/java/org/example/backend/application/dto/IncomeStatementData.java
package org.example.backend.application.dto;

import org.example.backend.domain.valueobject.TenantId;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Income Statement Data
 * 
 * Contains structured data for income statement generation
 */
@Data
@Builder
public class IncomeStatementData {
    private TenantId tenantId;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<RevenueItem> revenues;
    private List<ExpenseItem> operatingExpenses;
    private List<ExpenseItem> administrativeExpenses;
    private List<ExpenseItem> financialExpenses;
    private List<IncomeItem> otherIncomes;
    private List<ExpenseItem> otherExpenses;
    
    // Calculation methods
    public BigDecimal getTotalRevenue() {
        return revenues.stream()
            .map(RevenueItem::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getTotalOperatingExpenses() {
        return operatingExpenses.stream()
            .map(ExpenseItem::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getTotalAdministrativeExpenses() {
        return administrativeExpenses.stream()
            .map(ExpenseItem::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getTotalFinancialExpenses() {
        return financialExpenses.stream()
            .map(ExpenseItem::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getTotalOtherIncomes() {
        return otherIncomes.stream()
            .map(IncomeItem::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getTotalOtherExpenses() {
        return otherExpenses.stream()
            .map(ExpenseItem::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getTotalExpenses() {
        return getTotalOperatingExpenses()
            .add(getTotalAdministrativeExpenses())
            .add(getTotalFinancialExpenses())
            .add(getTotalOtherExpenses());
    }
    
    public BigDecimal getNetProfit() {
        return getTotalRevenue()
            .add(getTotalOtherIncomes())
            .subtract(getTotalExpenses());
    }
    
    public String getPeriodDescription() {
        return String.format("From %s to %s", startDate, endDate);
    }
    
    @Data
    @AllArgsConstructor
    public static class RevenueItem {
        private String name;
        private BigDecimal amount;
    }
    
    @Data
    @AllArgsConstructor
    public static class ExpenseItem {
        private String name;
        private BigDecimal amount;
    }
    
    @Data
    @AllArgsConstructor
    public static class IncomeItem {
        private String name;
        private BigDecimal amount;
    }
}

