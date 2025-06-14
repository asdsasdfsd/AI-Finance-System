// backend/src/main/java/org/example/backend/application/service/IncomeStatementDataService.java
package org.example.backend.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.application.dto.IncomeStatementData;
import org.example.backend.domain.aggregate.transaction.TransactionAggregateRepository;
import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.aggregate.company.CompanyAggregateRepository;
import org.example.backend.domain.aggregate.company.CompanyAggregate;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.TransactionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Income Statement Data Service - DDD Compliant Implementation
 * 
 * Responsibilities:
 * 1. Generate income statement data using DDD aggregates only
 * 2. Calculate revenue, expenses, and net income through domain logic
 * 3. Provide detailed breakdown by category and period
 * 4. Ensure all business rules are enforced through domain layer
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IncomeStatementDataService {

    private final TransactionAggregateRepository transactionRepository;
    private final CompanyAggregateRepository companyRepository;

    /**
     * Generate income statement using pure DDD approach
     */
    public IncomeStatementData getIncomeStatementDataByTenant(
            TenantId tenantId, LocalDate startDate, LocalDate endDate) {
        
        log.info("Generating income statement for tenant {} from {} to {}", 
                 tenantId.getValue(), startDate, endDate);
        
        // DDD: Validate tenant exists using aggregate
        CompanyAggregate company = companyRepository.findById(tenantId.getValue())
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + tenantId.getValue()));
        
        // DDD: Get transactions using aggregate repository
        List<TransactionAggregate> approvedTransactions = getApprovedTransactionsForPeriod(
                tenantId, startDate, endDate);
        
        if (approvedTransactions.isEmpty()) {
            log.warn("No approved transactions found for tenant {} in period", tenantId.getValue());
            return createEmptyIncomeStatement(company, startDate, endDate);
        }
        
        // DDD: Use domain logic to separate revenue and expenses
        List<TransactionAggregate> revenueTransactions = separateRevenueTransactions(approvedTransactions);
        List<TransactionAggregate> expenseTransactions = separateExpenseTransactions(approvedTransactions);
        
        // DDD: Calculate totals using domain calculation methods
        BigDecimal totalRevenue = calculateTotalRevenue(revenueTransactions);
        BigDecimal totalExpenses = calculateTotalExpenses(expenseTransactions);
        BigDecimal netIncome = calculateNetIncome(totalRevenue, totalExpenses);
        
        // DDD: Generate category breakdowns using domain logic
        Map<String, BigDecimal> revenueByCategory = groupTransactionsByCategory(revenueTransactions);
        Map<String, BigDecimal> expensesByCategory = groupTransactionsByCategory(expenseTransactions);
        
        log.info("Income statement generated - Revenue: {}, Expenses: {}, Net Income: {}", 
                 totalRevenue, totalExpenses, netIncome);
        
        return IncomeStatementData.builder()
                .companyName(company.getCompanyName())
                .periodStartDate(startDate)
                .periodEndDate(endDate)
                .totalRevenue(totalRevenue)
                .totalExpenses(totalExpenses)
                .netIncome(netIncome)
                .revenueByCategory(revenueByCategory)
                .expensesByCategory(expensesByCategory)
                .transactionCount(approvedTransactions.size())
                .build();
    }

    /**
     * Get approved transactions for period using DDD aggregate repository
     */
    private List<TransactionAggregate> getApprovedTransactionsForPeriod(
            TenantId tenantId, LocalDate startDate, LocalDate endDate) {
        
        return transactionRepository
                .findByTenantIdAndTransactionDateBetween(tenantId, startDate, endDate)
                .stream()
                .filter(tx -> tx.getTransactionStatus().getStatus() == TransactionStatus.Status.APPROVED)
                .collect(Collectors.toList());
    }

    /**
     * Separate revenue transactions using domain logic
     */
    private List<TransactionAggregate> separateRevenueTransactions(List<TransactionAggregate> transactions) {
        return transactions.stream()
                .filter(this::isRevenueTransaction)
                .collect(Collectors.toList());
    }

    /**
     * Separate expense transactions using domain logic
     */
    private List<TransactionAggregate> separateExpenseTransactions(List<TransactionAggregate> transactions) {
        return transactions.stream()
                .filter(this::isExpenseTransaction)
                .collect(Collectors.toList());
    }

    /**
     * Domain logic: Determine if transaction is revenue
     */
    private boolean isRevenueTransaction(TransactionAggregate transaction) {
        // Domain rule: Revenue transactions have type INCOME or REVENUE
        return transaction.getTransactionType() == TransactionAggregate.TransactionType.INCOME;
    }

    /**
     * Domain logic: Determine if transaction is expense
     */
    private boolean isExpenseTransaction(TransactionAggregate transaction) {
        // Domain rule: Expense transactions have type EXPENSE
        return transaction.getTransactionType() == TransactionAggregate.TransactionType.EXPENSE;
    }

    /**
     * Calculate total revenue using domain calculation
     */
    private BigDecimal calculateTotalRevenue(List<TransactionAggregate> revenueTransactions) {
        return revenueTransactions.stream()
                .map(TransactionAggregate::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate total expenses using domain calculation
     */
    private BigDecimal calculateTotalExpenses(List<TransactionAggregate> expenseTransactions) {
        return expenseTransactions.stream()
                .map(TransactionAggregate::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate net income using domain business rule
     */
    private BigDecimal calculateNetIncome(BigDecimal totalRevenue, BigDecimal totalExpenses) {
        // Domain rule: Net Income = Revenue - Expenses
        return totalRevenue.subtract(totalExpenses);
    }

    /**
     * Group transactions by category using domain logic
     * Note: This implementation avoids direct entity access
     */
    private Map<String, BigDecimal> groupTransactionsByCategory(List<TransactionAggregate> transactions) {
        Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();
        
        // Group transactions by category ID first
        Map<Integer, List<TransactionAggregate>> transactionsByCategory = transactions.stream()
                .filter(tx -> tx.getCategoryId() != null)
                .collect(Collectors.groupingBy(TransactionAggregate::getCategoryId));
        
        // Calculate totals for each category
        for (Map.Entry<Integer, List<TransactionAggregate>> entry : transactionsByCategory.entrySet()) {
            Integer categoryId = entry.getKey();
            List<TransactionAggregate> categoryTransactions = entry.getValue();
            
            // Use category ID as key for now to avoid entity access
            // In a full DDD implementation, we'd have a CategoryAggregate
            String categoryKey = "Category_" + categoryId;
            
            BigDecimal categoryTotal = categoryTransactions.stream()
                    .map(TransactionAggregate::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            categoryTotals.put(categoryKey, categoryTotal);
        }
        
        return categoryTotals;
    }

    /**
     * Create empty income statement for periods with no data
     */
    private IncomeStatementData createEmptyIncomeStatement(
            CompanyAggregate company, LocalDate startDate, LocalDate endDate) {
        
        return IncomeStatementData.builder()
                .companyName(company.getCompanyName())
                .periodStartDate(startDate)
                .periodEndDate(endDate)
                .totalRevenue(BigDecimal.ZERO)
                .totalExpenses(BigDecimal.ZERO)
                .netIncome(BigDecimal.ZERO)
                .revenueByCategory(new LinkedHashMap<>())
                .expensesByCategory(new LinkedHashMap<>())
                .transactionCount(0)
                .build();
    }

    /**
     * Legacy method for backward compatibility - delegates to DDD implementation
     * @deprecated Use getIncomeStatementDataByTenant instead
     */
    @Deprecated
    public IncomeStatementData getIncomeStatementData(TenantId tenantId, LocalDate startDate, LocalDate endDate) {
        log.warn("Using deprecated getIncomeStatementData method. Consider migrating to getIncomeStatementDataByTenant.");
        return getIncomeStatementDataByTenant(tenantId, startDate, endDate);
    }
}