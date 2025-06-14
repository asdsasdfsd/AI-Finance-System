// backend/src/main/java/org/example/backend/application/service/IncomeStatementDataService.java
package org.example.backend.application.service;

import org.example.backend.application.dto.IncomeStatementData;
import org.example.backend.domain.aggregate.transaction.TransactionAggregateRepository;
import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.TransactionStatus;
import org.example.backend.model.Category;
import org.example.backend.repository.CategoryRepository;
import org.example.backend.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.math.BigDecimal;

/**
 * Income Statement Data Service - Enhanced DDD Implementation
 * 
 * Responsibilities:
 * 1. Generate income statement data using DDD aggregates
 * 2. Calculate revenue, expenses, and net income
 * 3. Provide detailed breakdown by category and period
 * 4. Support multiple period comparisons
 */
@Service
@Transactional(readOnly = true)
public class IncomeStatementDataService {
    
    @Autowired
    private TransactionAggregateRepository transactionRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    /**
     * Generate comprehensive income statement data
     */
    public IncomeStatementData getIncomeStatementData(TenantId tenantId, LocalDate startDate, LocalDate endDate) {
        // Get all transactions for the period using DDD repository
        List<TransactionAggregate> allTransactions = transactionRepository
            .findByTenantIdAndTransactionDateBetween(tenantId, startDate, endDate);
        
        // Filter by status manually
        allTransactions = allTransactions.stream()
            .filter(t -> t.getTransactionStatus().getStatus() == TransactionStatus.Status.APPROVED)
            .collect(Collectors.toList());
        
        // Get all categories for proper labeling
        List<Category> categories = categoryRepository.findByCompanyAndParentCategoryIsNull(
                companyRepository.findById(tenantId.getValue())
                    .orElseThrow(() -> new IllegalArgumentException("Company not found")));
        Map<Integer, String> categoryMap = categories.stream()
            .collect(Collectors.toMap(c -> c.getCategoryId(), c -> c.getName()));
        
        // Separate revenue and expenses
        List<TransactionAggregate> revenueTransactions = allTransactions.stream()
            .filter(this::isRevenue)
            .collect(Collectors.toList());
            
        List<TransactionAggregate> expenseTransactions = allTransactions.stream()
            .filter(this::isExpense)
            .collect(Collectors.toList());
        
        // Calculate totals
        BigDecimal totalRevenue = revenueTransactions.stream()
            .map(t -> t.getMoney().getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal totalExpenses = expenseTransactions.stream()
            .map(t -> t.getMoney().getAmount().abs()) // Make expenses positive for display
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal grossProfit = totalRevenue.subtract(totalExpenses);
        
        // Group revenue by category
        Map<String, BigDecimal> revenueByCategory = revenueTransactions.stream()
            .collect(Collectors.groupingBy(
                t -> getCategoryName(t.getCategoryId(), categoryMap),
                Collectors.mapping(
                    t -> t.getMoney().getAmount(),
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                )
            ));
        
        // Group expenses by category
        Map<String, BigDecimal> expensesByCategory = expenseTransactions.stream()
            .collect(Collectors.groupingBy(
                t -> getCategoryName(t.getCategoryId(), categoryMap),
                Collectors.mapping(
                    t -> t.getMoney().getAmount().abs(),
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                )
            ));
        
        // Calculate operating expenses (excluding COGS if applicable)
        BigDecimal operatingExpenses = expensesByCategory.entrySet().stream()
            .filter(entry -> !entry.getKey().toLowerCase().contains("cost of goods"))
            .map(Map.Entry::getValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate operating income
        BigDecimal operatingIncome = totalRevenue.subtract(operatingExpenses);
        
        // Other income/expenses (simplified for now)
        BigDecimal otherIncome = revenueTransactions.stream()
            .filter(this::isOtherIncome)
            .map(t -> t.getMoney().getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal netIncome = grossProfit;
        
        // Calculate previous period for comparison
        LocalDate prevStartDate = startDate.minusMonths(1);
        LocalDate prevEndDate = endDate.minusMonths(1);
        IncomeStatementData.PeriodComparison previousPeriod = calculatePreviousPeriod(
            tenantId, prevStartDate, prevEndDate, categoryMap);
        
        return IncomeStatementData.builder()
            .periodStartDate(startDate)
            .periodEndDate(endDate)
            .totalRevenue(totalRevenue)
            .totalExpenses(totalExpenses)
            .grossProfit(grossProfit)
            .operatingExpenses(operatingExpenses)
            .operatingIncome(operatingIncome)
            .otherIncome(otherIncome)
            .netIncome(netIncome)
            .revenueByCategory(revenueByCategory)
            .expensesByCategory(expensesByCategory)
            .previousPeriod(previousPeriod)
            .transactionCount(allTransactions.size())
            .build();
    }
    
    /**
     * Calculate previous period data for comparison
     */
    private IncomeStatementData.PeriodComparison calculatePreviousPeriod(
            TenantId tenantId, LocalDate startDate, LocalDate endDate, Map<Integer, String> categoryMap) {
        
        List<TransactionAggregate> prevTransactions = transactionRepository
            .findByTenantIdAndTransactionDateBetween(tenantId, startDate, endDate);
        
        // Filter by status manually
        prevTransactions = prevTransactions.stream()
            .filter(t -> t.getTransactionStatus().getStatus() == TransactionStatus.Status.APPROVED)
            .collect(Collectors.toList());
        
        BigDecimal prevRevenue = prevTransactions.stream()
            .filter(this::isRevenue)
            .map(t -> t.getMoney().getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal prevExpenses = prevTransactions.stream()
            .filter(this::isExpense)
            .map(t -> t.getMoney().getAmount().abs())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal prevNetIncome = prevRevenue.subtract(prevExpenses);
        
        return IncomeStatementData.PeriodComparison.builder()
            .startDate(startDate)
            .endDate(endDate)
            .totalRevenue(prevRevenue)
            .totalExpenses(prevExpenses)
            .netIncome(prevNetIncome)
            .build();
    }
    
    /**
     * Determine if transaction is revenue (positive amounts, income categories)
     */
    private boolean isRevenue(TransactionAggregate transaction) {
        // Revenue transactions typically have positive amounts
        // and are associated with income categories
        return transaction.getMoney().getAmount().compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Determine if transaction is expense (negative amounts, expense categories)
     */
    private boolean isExpense(TransactionAggregate transaction) {
        // Expense transactions typically have negative amounts
        // and are associated with expense categories
        return transaction.getMoney().getAmount().compareTo(BigDecimal.ZERO) < 0;
    }
    
    /**
     * Get category name with fallback
     */
    private String getCategoryName(Integer categoryId, Map<Integer, String> categoryMap) {
        return categoryId != null ? 
            categoryMap.getOrDefault(categoryId, "Category " + categoryId) : 
            "Uncategorized";
    }
    
    /**
     * Determine if this is other income (non-operating revenue)
     */
    private boolean isOtherIncome(TransactionAggregate transaction) {
        // Simplified logic - in real implementation, this would check 
        // specific category types or account codes
        return false;
    }
}