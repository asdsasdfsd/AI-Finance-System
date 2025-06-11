// backend/src/main/java/org/example/backend/application/service/BalanceSheetDataService.java
package org.example.backend.application.service;

import org.example.backend.application.dto.BalanceSheetDetailedResponse;
import org.example.backend.application.dto.AccountBalanceDTO;
import org.example.backend.domain.aggregate.transaction.TransactionAggregateRepository;
import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.TransactionStatus;
import org.example.backend.repository.CategoryRepository;
import org.example.backend.repository.CompanyRepository;
import org.example.backend.model.Category;
import org.example.backend.model.Account;
import org.example.backend.model.Company;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Balance Sheet Data Service - DDD Implementation
 * 
 * Responsibilities:
 * 1. Generate balance sheet data using DDD aggregates
 * 2. Calculate account balances for different periods
 * 3. Group accounts by financial statement categories
 * 4. Ensure proper balance sheet equation validation
 */
@Service
@Transactional(readOnly = true)
public class BalanceSheetDataService {
    
    @Autowired
    private TransactionAggregateRepository transactionRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    /**
     * Generate detailed balance sheet data for specified date
     */
    public BalanceSheetDetailedResponse generateBalanceSheet(Integer companyId, LocalDate asOfDate) {
        // Validate company exists
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));
        
        TenantId tenantId = TenantId.of(companyId);
        
        // Initialize collections for different account types
        Map<String, List<AccountBalanceDTO>> assets = new LinkedHashMap<>();
        Map<String, List<AccountBalanceDTO>> liabilities = new LinkedHashMap<>();
        Map<String, List<AccountBalanceDTO>> equity = new LinkedHashMap<>();
        
        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        BigDecimal totalEquity = BigDecimal.ZERO;
        
        // Get all categories with accounts for this company
        List<Category> categories = categoryRepository.findByCompanyAndParentCategoryIsNull(company);
        
        for (Category category : categories) {
            if (category.getAccount() == null || category.getAccount().getAccountType() == null) {
                continue;
            }
            
            Account account = category.getAccount();
            Account.AccountType accountType = account.getAccountType();
            
            // Calculate balances using DDD repository
            AccountBalanceDTO balanceDto = calculateAccountBalance(
                tenantId, category.getCategoryId(), account, asOfDate
            );
            
            // Group by account type
            switch (accountType) {
                case ASSET:
                    assets.computeIfAbsent(category.getName(), k -> new ArrayList<>()).add(balanceDto);
                    totalAssets = totalAssets.add(balanceDto.getCurrentMonth());
                    break;
                case LIABILITY:
                    liabilities.computeIfAbsent(category.getName(), k -> new ArrayList<>()).add(balanceDto);
                    totalLiabilities = totalLiabilities.add(balanceDto.getCurrentMonth());
                    break;
                case EQUITY:
                    equity.computeIfAbsent(category.getName(), k -> new ArrayList<>()).add(balanceDto);
                    totalEquity = totalEquity.add(balanceDto.getCurrentMonth());
                    break;
                default:
                    // REVENUE and EXPENSE accounts don't appear in balance sheet
                    break;
            }
        }
        
        // Calculate net income and add to equity
        BigDecimal netIncome = calculateNetIncome(tenantId, asOfDate);
        if (netIncome.compareTo(BigDecimal.ZERO) != 0) {
            AccountBalanceDTO retainedEarnings = AccountBalanceDTO.builder()
                    .accountName("Retained Earnings (Net Income)")
                    .currentMonth(netIncome)
                    .previousMonth(BigDecimal.ZERO)
                    .lastYearEnd(BigDecimal.ZERO)
                    .build();
            
            equity.computeIfAbsent("Retained Earnings", k -> new ArrayList<>()).add(retainedEarnings);
            totalEquity = totalEquity.add(netIncome);
        }
        
        // Check if balance sheet balances
        boolean isBalanced = totalAssets.compareTo(totalLiabilities.add(totalEquity)) == 0;
        
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
     * Calculate account balance for different periods using DDD aggregates
     */
    private AccountBalanceDTO calculateAccountBalance(TenantId tenantId, Integer categoryId, 
                                                     Account account, LocalDate asOfDate) {
        
        // Current month balance (up to asOfDate)
        BigDecimal currentMonth = calculateBalanceForPeriod(
            tenantId, categoryId, asOfDate.withDayOfMonth(1), asOfDate
        );
        
        // Previous month balance
        LocalDate prevMonthStart = asOfDate.minusMonths(1).withDayOfMonth(1);
        LocalDate prevMonthEnd = asOfDate.minusMonths(1).withDayOfMonth(
            asOfDate.minusMonths(1).lengthOfMonth()
        );
        BigDecimal previousMonth = calculateBalanceForPeriod(
            tenantId, categoryId, prevMonthStart, prevMonthEnd
        );
        
        // Last year end balance
        LocalDate lastYearEnd = asOfDate.withDayOfYear(1).minusDays(1);
        BigDecimal lastYearEndBalance = calculateBalanceUpToDate(tenantId, categoryId, lastYearEnd);
        
        return AccountBalanceDTO.builder()
                .accountName(account.getName())
                .currentMonth(currentMonth != null ? currentMonth : BigDecimal.ZERO)
                .previousMonth(previousMonth != null ? previousMonth : BigDecimal.ZERO)
                .lastYearEnd(lastYearEndBalance != null ? lastYearEndBalance : BigDecimal.ZERO)
                .build();
    }
    
    /**
     * Calculate balance for specific period using DDD transaction aggregates
     */
    private BigDecimal calculateBalanceForPeriod(TenantId tenantId, Integer categoryId, 
                                                LocalDate startDate, LocalDate endDate) {
        List<TransactionAggregate> transactions = transactionRepository.findByDateRangeTypeAndStatus(
            tenantId, startDate, endDate, null, TransactionStatus.Status.APPROVED
        );
        
        return transactions.stream()
                .filter(t -> Objects.equals(t.getCategoryId(), categoryId))
                .map(t -> t.getMoney().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Calculate cumulative balance up to specific date
     */
    private BigDecimal calculateBalanceUpToDate(TenantId tenantId, Integer categoryId, LocalDate upToDate) {
        // For simplicity, using a very early start date
        LocalDate startDate = LocalDate.of(2020, 1, 1);
        return calculateBalanceForPeriod(tenantId, categoryId, startDate, upToDate);
    }
    
    /**
     * Calculate net income for the period (for retained earnings)
     */
    private BigDecimal calculateNetIncome(TenantId tenantId, LocalDate asOfDate) {
        LocalDate yearStart = asOfDate.withDayOfYear(1);
        
        // Get all transactions for the year
        List<TransactionAggregate> transactions = transactionRepository.findByDateRangeTypeAndStatus(
            tenantId, yearStart, asOfDate, null, TransactionStatus.Status.APPROVED
        );
        
        // Calculate income
        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getTransactionType() == TransactionAggregate.TransactionType.INCOME)
                .map(t -> t.getMoney().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate expenses
        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> t.getTransactionType() == TransactionAggregate.TransactionType.EXPENSE)
                .map(t -> t.getMoney().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return totalIncome.subtract(totalExpenses);
    }
    
    /**
     * Generate balance sheet for specific company and period
     */
    public BalanceSheetDetailedResponse generateBalanceSheetForPeriod(TenantId tenantId, 
                                                                     LocalDate startDate, 
                                                                     LocalDate endDate) {
        // For balance sheet, we typically use the end date
        return generateBalanceSheet(tenantId.getValue(), endDate);
    }
}