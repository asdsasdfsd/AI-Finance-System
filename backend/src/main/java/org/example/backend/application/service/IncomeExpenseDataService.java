// backend/src/main/java/org/example/backend/application/service/IncomeExpenseDataService.java
package org.example.backend.application.service;

import org.example.backend.application.dto.IncomeExpenseReportRowDTO;
import org.example.backend.domain.aggregate.transaction.TransactionAggregateRepository;
import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.TransactionStatus;
import org.example.backend.repository.CategoryRepository;
import org.example.backend.model.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Income Expense Data Service - DDD Implementation
 * 
 * Responsibilities:
 * 1. Generate income vs expense report data using DDD aggregates
 * 2. Group transactions by category and description
 * 3. Calculate monthly, YTD, and budget comparisons
 * 4. Provide variance analysis
 */
@Service
@Transactional(readOnly = true)
public class IncomeExpenseDataService {
    
    @Autowired
    private TransactionAggregateRepository transactionRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    /**
     * Generate income expense report data for specified period
     */
    public List<IncomeExpenseReportRowDTO> generateIncomeExpenseReport(TenantId tenantId, LocalDate asOfDate) {
        List<IncomeExpenseReportRowDTO> result = new ArrayList<>();
        
        // Get all approved transactions up to the specified date
        List<TransactionAggregate> allTransactions = transactionRepository.findByDateRangeTypeAndStatus(
            tenantId, LocalDate.of(asOfDate.getYear(), 1, 1), asOfDate, null, TransactionStatus.Status.APPROVED
        );
        
        // Group transactions by type and category
        Map<String, Map<String, List<TransactionAggregate>>> groupedTransactions = allTransactions.stream()
                .collect(Collectors.groupingBy(
                    t -> t.getTransactionType().name(),
                    Collectors.groupingBy(this::getCategoryName)
                ));
        
        // Process INCOME transactions
        processTransactionsByType(groupedTransactions.getOrDefault("INCOME", Collections.emptyMap()), 
                                "INCOME", asOfDate, result);
        
        // Process EXPENSE transactions
        processTransactionsByType(groupedTransactions.getOrDefault("EXPENSE", Collections.emptyMap()), 
                                "EXPENSE", asOfDate, result);
        
        return result;
    }
    
    /**
     * Process transactions by type (INCOME or EXPENSE)
     */
    private void processTransactionsByType(Map<String, List<TransactionAggregate>> transactionsByCategory,
                                         String type, LocalDate asOfDate, 
                                         List<IncomeExpenseReportRowDTO> result) {
        
        for (Map.Entry<String, List<TransactionAggregate>> categoryEntry : transactionsByCategory.entrySet()) {
            String categoryName = categoryEntry.getKey();
            List<TransactionAggregate> categoryTransactions = categoryEntry.getValue();
            
            // Group by description (item level)
            Map<String, List<TransactionAggregate>> transactionsByDescription = categoryTransactions.stream()
                    .collect(Collectors.groupingBy(
                        t -> t.getDescription() != null ? t.getDescription() : "No Description"
                    ));
            
            for (Map.Entry<String, List<TransactionAggregate>> descEntry : transactionsByDescription.entrySet()) {
                String description = descEntry.getKey();
                List<TransactionAggregate> itemTransactions = descEntry.getValue();
                
                // Calculate amounts for different periods
                BigDecimal currentMonth = calculateAmountForMonth(itemTransactions, asOfDate);
                BigDecimal previousMonth = calculateAmountForMonth(itemTransactions, asOfDate.minusMonths(1));
                BigDecimal yearToDate = calculateYearToDateAmount(itemTransactions, asOfDate);
                
                // For now, budget amounts are zero (can be enhanced later)
                BigDecimal budgetYtd = BigDecimal.ZERO;
                BigDecimal fullYearBudget = BigDecimal.ZERO;
                BigDecimal variance = yearToDate.subtract(budgetYtd);
                
                IncomeExpenseReportRowDTO row = IncomeExpenseReportRowDTO.builder()
                        .type(type)
                        .category(categoryName)
                        .description(description)
                        .currentMonth(currentMonth)
                        .previousMonth(previousMonth)
                        .yearToDate(yearToDate)
                        .budgetYtd(budgetYtd)
                        .variance(variance)
                        .fullYearBudget(fullYearBudget)
                        .build();
                
                result.add(row);
            }
        }
    }
    
    /**
     * Calculate amount for specific month
     */
    private BigDecimal calculateAmountForMonth(List<TransactionAggregate> transactions, LocalDate monthDate) {
        return transactions.stream()
                .filter(t -> t.getTransactionDate().getYear() == monthDate.getYear() &&
                           t.getTransactionDate().getMonth() == monthDate.getMonth())
                .map(t -> t.getMoney().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Calculate year-to-date amount
     */
    private BigDecimal calculateYearToDateAmount(List<TransactionAggregate> transactions, LocalDate asOfDate) {
        LocalDate yearStart = asOfDate.withDayOfYear(1);
        
        return transactions.stream()
                .filter(t -> !t.getTransactionDate().isBefore(yearStart) && 
                           !t.getTransactionDate().isAfter(asOfDate))
                .map(t -> t.getMoney().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Get category name for transaction (simplified version)
     */
    private String getCategoryName(TransactionAggregate transaction) {
        Integer categoryId = transaction.getCategoryId();
        if (categoryId == null) {
            return "Uncategorized";
        }
        
        // Simple category name mapping - can be enhanced with actual category lookup
        return "Category " + categoryId;
    }
    
    /**
     * Generate summary statistics for income expense report
     */
    public Map<String, BigDecimal> generateIncomeExpenseSummary(TenantId tenantId, LocalDate asOfDate) {
        LocalDate yearStart = asOfDate.withDayOfYear(1);
        
        List<TransactionAggregate> transactions = transactionRepository.findByDateRangeTypeAndStatus(
            tenantId, yearStart, asOfDate, null, TransactionStatus.Status.APPROVED
        );
        
        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getTransactionType() == TransactionAggregate.TransactionType.INCOME)
                .map(t -> t.getMoney().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> t.getTransactionType() == TransactionAggregate.TransactionType.EXPENSE)
                .map(t -> t.getMoney().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal netIncome = totalIncome.subtract(totalExpenses);
        
        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpenses", totalExpenses);
        summary.put("netIncome", netIncome);
        summary.put("incomeExpenseRatio", 
                   totalExpenses.compareTo(BigDecimal.ZERO) != 0 ? 
                   totalIncome.divide(totalExpenses, 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO);
        
        return summary;
    }
}