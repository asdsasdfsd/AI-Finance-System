// backend/src/main/java/org/example/backend/application/service/BalanceSheetDataService.java
package org.example.backend.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.application.dto.BalanceSheetDetailedResponse;
import org.example.backend.application.dto.AccountBalanceDTO;
import org.example.backend.domain.aggregate.company.CompanyAggregateRepository;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Fixed Balance Sheet Data Service - Using Direct SQL Queries
 * 
 * Problem: The original DDD implementation was not getting data from account_balance table.
 * Solution: Use JdbcTemplate to directly query account_balance table for actual balance data.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BalanceSheetDataService {

    private final CompanyAggregateRepository companyRepository;
    private final AccountRepository accountRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Generate balance sheet using account_balance table data
     */
    public BalanceSheetDetailedResponse generateBalanceSheetByTenant(TenantId tenantId, LocalDate asOfDate) {
        log.info("Generating balance sheet for tenant {} as of {}", tenantId.getValue(), asOfDate);
        
        // Validate tenant exists
        if (!companyRepository.existsById(tenantId.getValue())) {
            throw new IllegalArgumentException("Company not found: " + tenantId.getValue());
        }
        
        // Generate sections using account_balance table
        Map<String, List<AccountBalanceDTO>> assets = generateSectionFromAccountBalance(tenantId, asOfDate, "ASSET");
        Map<String, List<AccountBalanceDTO>> liabilities = generateSectionFromAccountBalance(tenantId, asOfDate, "LIABILITY");
        Map<String, List<AccountBalanceDTO>> equity = generateSectionFromAccountBalance(tenantId, asOfDate, "EQUITY");
        
        // Calculate totals
        BigDecimal totalAssets = calculateSectionTotal(assets);
        BigDecimal totalLiabilities = calculateSectionTotal(liabilities);
        BigDecimal totalEquity = calculateSectionTotal(equity);
        
        // Check if balanced
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
     * Generate balance sheet section using account_balance table
     */
    private Map<String, List<AccountBalanceDTO>> generateSectionFromAccountBalance(
            TenantId tenantId, LocalDate asOfDate, String accountType) {
        
        Map<String, List<AccountBalanceDTO>> section = new LinkedHashMap<>();
        
        try {
            // Query account_balance table directly
            String sql = """
                SELECT 
                    a.account_id,
                    a.account_code,
                    a.name as account_name,
                    a.account_type,
                    COALESCE(ab.current_month, 0) as current_month,
                    COALESCE(ab.previous_month, 0) as previous_month,
                    COALESCE(ab.last_year_end, 0) as last_year_end
                FROM Account a
                LEFT JOIN account_balance ab ON a.account_id = ab.account_id 
                    AND ab.as_of_date = ?
                WHERE a.company_id = ? 
                    AND a.account_type = ?
                    AND a.is_active = true
                ORDER BY a.account_code
                """;
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                sql, asOfDate, tenantId.getValue(), accountType);
            
            log.info("Found {} {} accounts for company {}", results.size(), accountType, tenantId.getValue());
            
            // Group by account type (can be enhanced to group by categories later)
            String sectionName = accountType + " Accounts";
            List<AccountBalanceDTO> accountBalances = new ArrayList<>();
            
            for (Map<String, Object> row : results) {
                BigDecimal currentMonth = new BigDecimal(row.get("current_month").toString());
                BigDecimal previousMonth = new BigDecimal(row.get("previous_month").toString());
                BigDecimal lastYearEnd = new BigDecimal(row.get("last_year_end").toString());
                
                // Create AccountBalanceDTO with correct field names
                AccountBalanceDTO balance = AccountBalanceDTO.builder()
                        .accountName(row.get("account_name").toString())
                        .currentMonth(currentMonth)
                        .previousMonth(previousMonth)
                        .lastYearEnd(lastYearEnd)
                        .build();
                
                accountBalances.add(balance);
            }
            
            if (!accountBalances.isEmpty()) {
                section.put(sectionName, accountBalances);
            } else {
                log.warn("No account balances found for {} section", accountType);
                // Add empty section to maintain structure
                section.put(sectionName, new ArrayList<>());
            }
            
        } catch (Exception e) {
            log.error("Error generating {} section: {}", accountType, e.getMessage(), e);
            // Return empty section instead of failing
            section.put(accountType + " Accounts", new ArrayList<>());
        }
        
        return section;
    }
    
    /**
     * Calculate section total
     */
    private BigDecimal calculateSectionTotal(Map<String, List<AccountBalanceDTO>> section) {
        return section.values().stream()
                .flatMap(List::stream)
                .map(AccountBalanceDTO::getCurrentMonth)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Legacy method for backward compatibility
     */
    @Deprecated
    public BalanceSheetDetailedResponse generateBalanceSheet(Integer companyId, LocalDate asOfDate) {
        log.warn("Using deprecated generateBalanceSheet method. Consider migrating to generateBalanceSheetByTenant.");
        return generateBalanceSheetByTenant(TenantId.of(companyId), asOfDate);
    }
}