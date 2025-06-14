// backend/src/main/java/org/example/backend/application/service/BalanceSheetDataService.java
package org.example.backend.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.application.dto.BalanceSheetDetailedResponse;
import org.example.backend.application.dto.AccountBalanceDTO;
import org.example.backend.domain.aggregate.transaction.TransactionAggregateRepository;
import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.aggregate.company.CompanyAggregateRepository;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.TransactionStatus;
import org.example.backend.repository.CategoryRepository;
import org.example.backend.model.Category;
import org.example.backend.model.Account;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Balance Sheet Data Service - DDD Implementation
 * 
 * Responsibilities:
 * 1. Generate balance sheet data using DDD aggregates
 * 2. Calculate account balances for different periods using domain logic
 * 3. Group accounts by financial statement categories
 * 4. Ensure data consistency and business rule compliance
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BalanceSheetDataService {

    private final TransactionAggregateRepository transactionRepository;
    private final CompanyAggregateRepository companyRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Generate balance sheet using DDD approach with TenantId
     */
    public BalanceSheetDetailedResponse generateBalanceSheetByTenant(TenantId tenantId, LocalDate asOfDate) {
        log.info("Generating balance sheet for tenant {} as of {}", tenantId.getValue(), asOfDate);
        
        // Validate tenant exists
        if (!companyRepository.existsById(tenantId.getValue())) {
            throw new IllegalArgumentException("Company not found: " + tenantId.getValue());
        }
        
        // Get balance sheet categories using domain logic
        Map<String, List<AccountBalanceDTO>> assets = generateAssetSection(tenantId, asOfDate);
        Map<String, List<AccountBalanceDTO>> liabilities = generateLiabilitySection(tenantId, asOfDate);
        Map<String, List<AccountBalanceDTO>> equity = generateEquitySection(tenantId, asOfDate);
        
        // Calculate totals using domain calculation
        BigDecimal totalAssets = calculateSectionTotal(assets);
        BigDecimal totalLiabilities = calculateSectionTotal(liabilities);
        BigDecimal totalEquity = calculateSectionTotal(equity);
        
        // Apply business rule: Assets = Liabilities + Equity
        boolean isBalanced = totalAssets.compareTo(totalLiabilities.add(totalEquity)) == 0;
        
        log.info("Balance sheet generated - Assets: {}, Liabilities: {}, Equity: {}, Balanced: {}", 
                 totalAssets, totalLiabilities, totalEquity, isBalanced);
        
        return BalanceSheetDetailedResponse.builder()
                .asOfDate(asOfDate)
                .assets(assets)
                .liabilities(liabilities)
                .equity(equity)
                .totalAssets(totalAssets)
                .totalLiabilities(totalLiabilities)
                .totalEquity(totalEquity)
                .isBalanced(isBalanced)
                .build();
    }

    /**
     * Generate asset section using DDD aggregates
     */
    private Map<String, List<AccountBalanceDTO>> generateAssetSection(TenantId tenantId, LocalDate asOfDate) {
        return generateBalanceSheetSection(tenantId, asOfDate, "ASSET");
    }

    /**
     * Generate liability section using DDD aggregates
     */
    private Map<String, List<AccountBalanceDTO>> generateLiabilitySection(TenantId tenantId, LocalDate asOfDate) {
        return generateBalanceSheetSection(tenantId, asOfDate, "LIABILITY");
    }

    /**
     * Generate equity section using DDD aggregates
     */
    private Map<String, List<AccountBalanceDTO>> generateEquitySection(TenantId tenantId, LocalDate asOfDate) {
        Map<String, List<AccountBalanceDTO>> equity = generateBalanceSheetSection(tenantId, asOfDate, "EQUITY");
        
        // Add retained earnings calculation using domain logic
        BigDecimal retainedEarnings = calculateRetainedEarnings(tenantId, asOfDate);
        if (retainedEarnings.compareTo(BigDecimal.ZERO) != 0) {
            AccountBalanceDTO retainedEarningsAccount = AccountBalanceDTO.builder()
                    .accountName("Retained Earnings")
                    .currentMonth(retainedEarnings)
                    .previousMonth(calculateRetainedEarnings(tenantId, asOfDate.minusMonths(1)))
                    .lastYearEnd(calculateRetainedEarnings(tenantId, asOfDate.withDayOfYear(1).minusDays(1)))
                    .build();
            
            equity.computeIfAbsent("Retained Earnings", k -> new ArrayList<>()).add(retainedEarningsAccount);
        }
        
        return equity;
    }

    /**
     * Generate balance sheet section using DDD aggregates and domain logic
     */
    private Map<String, List<AccountBalanceDTO>> generateBalanceSheetSection(
            TenantId tenantId, LocalDate asOfDate, String sectionType) {
        
        // 替换原来的方法调用
        List<Category> categories = categoryRepository.findByCompanyCompanyId(tenantId.getValue())
                .stream()
                .filter(c -> sectionType.equals(c.getType().name()))
                .collect(Collectors.toList());
        
        Map<String, List<AccountBalanceDTO>> section = new LinkedHashMap<>();
        
        for (Category category : categories) {
            // 修复方法调用：使用 getName() 而不是 getCategoryName()
            String categoryName = category.getName();
            
            // 由于Category不直接包含Account列表，我们需要不同的逻辑
            // 这里假设每个Category对应一个Account
            if (category.getAccount() != null) {
                AccountBalanceDTO balance = calculateAccountBalanceUsingAggregates(
                        tenantId, category.getAccount(), asOfDate);
                
                if (hasNonZeroBalance(balance)) {
                    section.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(balance);
                }
            }
        }
        
        return section;
    }

    /**
     * Calculate account balance using DDD aggregates
     */
    private AccountBalanceDTO calculateAccountBalanceUsingAggregates(
            TenantId tenantId, Account account, LocalDate asOfDate) {
        
        // Current month balance (up to asOfDate)
        BigDecimal currentMonth = calculateBalanceForPeriodUsingAggregates(
            tenantId, account.getAccountId(), asOfDate.withDayOfMonth(1), asOfDate
        );
        
        // Previous month balance
        LocalDate prevMonthStart = asOfDate.minusMonths(1).withDayOfMonth(1);
        LocalDate prevMonthEnd = asOfDate.minusMonths(1).withDayOfMonth(
            asOfDate.minusMonths(1).lengthOfMonth()
        );
        BigDecimal previousMonth = calculateBalanceForPeriodUsingAggregates(
            tenantId, account.getAccountId(), prevMonthStart, prevMonthEnd
        );
        
        // Last year end balance
        LocalDate lastYearEnd = asOfDate.withDayOfYear(1).minusDays(1);
        BigDecimal lastYearEndBalance = calculateBalanceUpToDateUsingAggregates(
            tenantId, account.getAccountId(), lastYearEnd
        );
        
        return AccountBalanceDTO.builder()
                .accountName(account.getName())
                .currentMonth(currentMonth != null ? currentMonth : BigDecimal.ZERO)
                .previousMonth(previousMonth != null ? previousMonth : BigDecimal.ZERO)
                .lastYearEnd(lastYearEndBalance != null ? lastYearEndBalance : BigDecimal.ZERO)
                .build();
    }

    /**
     * Calculate balance for period using transaction aggregates
     */
    private BigDecimal calculateBalanceForPeriodUsingAggregates(
            TenantId tenantId, Integer accountId, LocalDate startDate, LocalDate endDate) {
        
        // Get transactions using DDD aggregate repository
        List<TransactionAggregate> transactions = transactionRepository
                .findByTenantIdAndAccountIdAndDateRange(tenantId, accountId, startDate, endDate);
        
        // Apply domain logic for balance calculation
        return transactions.stream()
                .filter(tx -> tx.getTransactionStatus().getStatus() == TransactionStatus.Status.APPROVED)
                .map(tx -> calculateTransactionImpact(tx, accountId))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate balance up to date using transaction aggregates
     */
    private BigDecimal calculateBalanceUpToDateUsingAggregates(
            TenantId tenantId, Integer accountId, LocalDate asOfDate) {
        
        List<TransactionAggregate> transactions = transactionRepository
                .findByTenantIdAndAccountIdUpToDate(tenantId, accountId, asOfDate);
        
        return transactions.stream()
                .filter(tx -> tx.getTransactionStatus().getStatus() == TransactionStatus.Status.APPROVED)
                .map(tx -> calculateTransactionImpact(tx, accountId))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate transaction impact on account using domain logic
     */
    private BigDecimal calculateTransactionImpact(TransactionAggregate transaction, Integer accountId) {
        // Domain logic: Determine if this account is debited or credited
        if (transaction.getDebitAccountId().equals(accountId)) {
            return transaction.getAmount();
        } else if (transaction.getCreditAccountId().equals(accountId)) {
            return transaction.getAmount().negate();
        }
        return BigDecimal.ZERO;
    }

    /**
     * Calculate retained earnings using domain aggregates
     */
    private BigDecimal calculateRetainedEarnings(TenantId tenantId, LocalDate asOfDate) {
        // Get revenue transactions
        List<TransactionAggregate> revenueTransactions = transactionRepository
                .findByTenantIdAndTransactionTypeAndDateRange(
                    tenantId, 
                    TransactionAggregate.TransactionType.INCOME, 
                    asOfDate.withDayOfYear(1), 
                    asOfDate
                );
        
        // Get expense transactions
        List<TransactionAggregate> expenseTransactions = transactionRepository
                .findByTenantIdAndTransactionTypeAndDateRange(
                    tenantId, 
                    TransactionAggregate.TransactionType.EXPENSE, 
                    asOfDate.withDayOfYear(1), 
                    asOfDate
                );
        
        BigDecimal totalRevenue = revenueTransactions.stream()
                .filter(tx -> tx.getTransactionStatus().getStatus() == TransactionStatus.Status.APPROVED)
                .map(TransactionAggregate::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalExpenses = expenseTransactions.stream()
                .filter(tx -> tx.getTransactionStatus().getStatus() == TransactionStatus.Status.APPROVED)
                .map(TransactionAggregate::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return totalRevenue.subtract(totalExpenses);
    }

    /**
     * Calculate section total using domain logic
     */
    private BigDecimal calculateSectionTotal(Map<String, List<AccountBalanceDTO>> section) {
        return section.values().stream()
                .flatMap(List::stream)
                .map(AccountBalanceDTO::getCurrentMonth)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Check if account has non-zero balance
     */
    private boolean hasNonZeroBalance(AccountBalanceDTO balance) {
        return balance.getCurrentMonth().compareTo(BigDecimal.ZERO) != 0 ||
               balance.getPreviousMonth().compareTo(BigDecimal.ZERO) != 0 ||
               balance.getLastYearEnd().compareTo(BigDecimal.ZERO) != 0;
    }

    /**
     * Legacy method for backward compatibility - delegates to DDD implementation
     * @deprecated Use generateBalanceSheetByTenant instead
     */
    @Deprecated
    public BalanceSheetDetailedResponse generateBalanceSheet(Integer companyId, LocalDate asOfDate) {
        log.warn("Using deprecated generateBalanceSheet method. Consider migrating to generateBalanceSheetByTenant.");
        return generateBalanceSheetByTenant(TenantId.of(companyId), asOfDate);
    }
}