// backend/src/main/java/org/example/backend/application/service/IncomeStatementDataService.java
package org.example.backend.application.service;

import org.example.backend.application.dto.IncomeStatementData;
import org.example.backend.domain.aggregate.transaction.TransactionAggregateRepository;
import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.TransactionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.math.BigDecimal;

/**
 * Income Statement Data Service
 * 
 * Responsibilities:
 * 1. Query and aggregate financial data for income statements
 * 2. Categorize transactions by income/expense types
 * 3. Calculate financial metrics and totals
 */
@Service
@Transactional(readOnly = true)
public class IncomeStatementDataService {
    
    @Autowired
    private TransactionAggregateRepository transactionRepository;
    
/**
     * Get income statement data for specified period
     */
    public IncomeStatementData getIncomeStatementData(TenantId tenantId, LocalDate startDate, LocalDate endDate) {
        // Get all approved transactions in the period
        List<TransactionAggregate> transactions = transactionRepository.findByDateRangeTypeAndStatus(
            tenantId, startDate, endDate, null, TransactionStatus.Status.APPROVED
        );
        
        // Separate income and expense transactions
        List<TransactionAggregate> incomeTransactions = transactions.stream()
            .filter(t -> t.getTransactionType() == TransactionAggregate.TransactionType.INCOME)
            .collect(Collectors.toList());
            
        List<TransactionAggregate> expenseTransactions = transactions.stream()
            .filter(t -> t.getTransactionType() == TransactionAggregate.TransactionType.EXPENSE)
            .collect(Collectors.toList());
        
        return IncomeStatementData.builder()
            .tenantId(tenantId)
            .startDate(startDate)
            .endDate(endDate)
            .revenues(categorizeIncomeTransactions(incomeTransactions))
            .operatingExpenses(categorizeExpenseTransactions(expenseTransactions, "OPERATING"))
            .administrativeExpenses(categorizeExpenseTransactions(expenseTransactions, "ADMINISTRATIVE"))
            .financialExpenses(categorizeExpenseTransactions(expenseTransactions, "FINANCIAL"))
            .otherIncomes(categorizeOtherIncomes(incomeTransactions))
            .otherExpenses(categorizeExpenseTransactions(expenseTransactions, "OTHER"))
            .build();
    }
    
    private List<IncomeStatementData.RevenueItem> categorizeIncomeTransactions(List<TransactionAggregate> transactions) {
        Map<String, List<TransactionAggregate>> grouped = transactions.stream()
            .collect(Collectors.groupingBy(t -> getCategoryName(t.getCategoryId())));
            
        return grouped.entrySet().stream()
            .map(entry -> {
                BigDecimal total = entry.getValue().stream()
                    .map(t -> t.getMoney().getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                return new IncomeStatementData.RevenueItem(entry.getKey(), total);
            })
            .collect(Collectors.toList());
    }
    
    private List<IncomeStatementData.ExpenseItem> categorizeExpenseTransactions(
            List<TransactionAggregate> transactions, String expenseType) {
        // Filter by expense type logic would go here
        // For now, we'll use a simple categorization
        Map<String, List<TransactionAggregate>> grouped = transactions.stream()
            .filter(t -> matchesExpenseType(t, expenseType))
            .collect(Collectors.groupingBy(t -> getCategoryName(t.getCategoryId())));
            
        return grouped.entrySet().stream()
            .map(entry -> {
                BigDecimal total = entry.getValue().stream()
                    .map(t -> t.getMoney().getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                return new IncomeStatementData.ExpenseItem(entry.getKey(), total);
            })
            .collect(Collectors.toList());
    }
    
    private List<IncomeStatementData.IncomeItem> categorizeOtherIncomes(List<TransactionAggregate> transactions) {
        // Filter other incomes (non-operating revenue)
        return transactions.stream()
            .filter(this::isOtherIncome)
            .map(t -> new IncomeStatementData.IncomeItem(
                getCategoryName(t.getCategoryId()), 
                t.getMoney().getAmount()
            ))
            .collect(Collectors.toList());
    }
    
    // Helper methods (simplified for demo)
    private String getCategoryName(Integer categoryId) {
        return categoryId != null ? "Category " + categoryId : "Uncategorized";
    }
    
    private boolean matchesExpenseType(TransactionAggregate transaction, String expenseType) {
        // Simplified logic - in real implementation, this would check category types
        return true;
    }
    
    private boolean isOtherIncome(TransactionAggregate transaction) {
        // Simplified logic - check if this is non-operating income
        return false;
    }
}

