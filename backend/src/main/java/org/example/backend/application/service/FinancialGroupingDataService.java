// backend/src/main/java/org/example/backend/application/service/FinancialGroupingDataService.java
package org.example.backend.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.application.dto.FinancialGroupingData;
import org.example.backend.domain.aggregate.transaction.TransactionAggregateRepository;
import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.aggregate.company.CompanyAggregateRepository;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.TransactionStatus;
import org.example.backend.repository.CategoryRepository;
import org.example.backend.repository.DepartmentRepository;
import org.example.backend.repository.FundRepository;
import org.example.backend.model.Category;
import org.example.backend.model.Department;
import org.example.backend.model.Fund;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Financial Grouping Data Service - DDD Implementation
 * 
 * Responsibilities:
 * 1. Generate financial grouping data using DDD aggregates
 * 2. Group transactions by various criteria (category, department, fund, month)
 * 3. Calculate summaries and statistics using domain logic
 * 4. Ensure data consistency and business rule compliance
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinancialGroupingDataService {

    private final TransactionAggregateRepository transactionRepository;
    private final CompanyAggregateRepository companyRepository;
    private final CategoryRepository categoryRepository;
    private final DepartmentRepository departmentRepository;
    private final FundRepository fundRepository;

    /**
     * Generate financial grouping data using DDD approach with TenantId
     */
    public FinancialGroupingData getFinancialGroupingDataByTenant(
            TenantId tenantId, LocalDate startDate, LocalDate endDate) {
        
        log.info("Generating financial grouping for tenant {} from {} to {}", 
                 tenantId.getValue(), startDate, endDate);
        
        // Validate tenant exists
        if (!companyRepository.existsById(tenantId.getValue())) {
            throw new IllegalArgumentException("Company not found: " + tenantId.getValue());
        }
        
        // Get approved transactions using DDD aggregates
        List<TransactionAggregate> transactions = getApprovedTransactions(tenantId, startDate, endDate);
        
        if (transactions.isEmpty()) {
            log.warn("No approved transactions found for tenant {} in period {} to {}", 
                     tenantId.getValue(), startDate, endDate);
            return createEmptyFinancialGroupingData(startDate, endDate);
        }
        
        // Generate groupings using domain logic
        Map<String, FinancialGroupingData.CategoryGrouping> byCategory = 
            generateCategoryGroupings(tenantId, transactions);
        
        Map<String, FinancialGroupingData.DepartmentGrouping> byDepartment = 
            generateDepartmentGroupings(tenantId, transactions);
        
        Map<String, FinancialGroupingData.FundGrouping> byFund = 
            generateFundGroupings(tenantId, transactions);
        
        Map<String, FinancialGroupingData.MonthGrouping> byMonth = 
            generateMonthGroupings(transactions);
        
        // Calculate totals using domain calculation
        BigDecimal grandTotal = calculateGrandTotal(transactions);
        int totalTransactionCount = transactions.size();
        
        String periodDescription = String.format("Period: %s to %s", 
                                                startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                                endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        log.info("Financial grouping generated - {} transactions, total amount: {}", 
                 totalTransactionCount, grandTotal);
        
        return FinancialGroupingData.builder()
                .periodDescription(periodDescription)
                .byCategory(byCategory)
                .byDepartment(byDepartment)
                .byFund(byFund)
                .byMonth(byMonth)
                .grandTotal(grandTotal)
                .totalTransactionCount(totalTransactionCount)
                .build();
    }

    /**
     * Get approved transactions using DDD aggregates
     */
    private List<TransactionAggregate> getApprovedTransactions(
            TenantId tenantId, LocalDate startDate, LocalDate endDate) {
        
        return transactionRepository.findByTenantIdAndDateRangeAndTypeAndStatus(
                tenantId, 
                startDate, 
                endDate, 
                null, // All types
                TransactionStatus.Status.APPROVED
        ).stream()
        .filter(tx -> tx.getTransactionStatus().getStatus() == TransactionStatus.Status.APPROVED)
        .collect(Collectors.toList());
    }

    /**
     * Generate category groupings using domain logic
     */
    private Map<String, FinancialGroupingData.CategoryGrouping> generateCategoryGroupings(
            TenantId tenantId, List<TransactionAggregate> transactions) {
        
        // 修复Repository调用
        Map<Integer, Category> categoryMap = categoryRepository
                .findByCompanyCompanyId(tenantId.getValue())  // 使用修复后的方法名
                .stream()
                .collect(Collectors.toMap(Category::getCategoryId, c -> c));
        
        // 修复类型转换问题
        Map<String, List<TransactionAggregate>> groupedByCategory = transactions.stream()
                .filter(tx -> tx.getCategoryId() != null)
                .collect(Collectors.groupingBy(tx -> {
                    Category category = categoryMap.get(tx.getCategoryId());
                    return category != null ? category.getName() : "Unknown Category";  // 使用 getName()
                }));
        
        Map<String, FinancialGroupingData.CategoryGrouping> result = new LinkedHashMap<>();
        
        for (Map.Entry<String, List<TransactionAggregate>> entry : groupedByCategory.entrySet()) {
            String categoryName = entry.getKey();
            List<TransactionAggregate> categoryTransactions = entry.getValue();
            
            BigDecimal totalAmount = calculateTotalAmount(categoryTransactions);
            int transactionCount = categoryTransactions.size();
            BigDecimal averageAmount = calculateAverageAmount(totalAmount, transactionCount);
            
            // 使用 builder() 创建对象
            FinancialGroupingData.CategoryGrouping grouping = 
                FinancialGroupingData.CategoryGrouping.builder()
                    .categoryName(categoryName)
                    .totalAmount(totalAmount)
                    .transactionCount(transactionCount)
                    .averageAmount(averageAmount)
                    .build();
            
            result.put(categoryName, grouping);
        }
        
        return result;
    }

    /**
     * Generate department groupings using domain logic
     */
    private Map<String, FinancialGroupingData.DepartmentGrouping> generateDepartmentGroupings(
        TenantId tenantId, List<TransactionAggregate> transactions) {
    
        // 修复Repository调用
        Map<Integer, Department> departmentMap = departmentRepository
                .findByCompanyCompanyId(tenantId.getValue())  // 使用修复后的方法名
                .stream()
                .collect(Collectors.toMap(Department::getDepartmentId, d -> d));
        
        // 修复类型转换问题
        Map<String, List<TransactionAggregate>> groupedByDepartment = transactions.stream()
                .filter(tx -> tx.getDepartmentId() != null)
                .collect(Collectors.groupingBy(tx -> {
                    Department department = departmentMap.get(tx.getDepartmentId());
                    return department != null ? department.getName() : "Unknown Department";  // 使用 getName()
                }));
        
        Map<String, FinancialGroupingData.DepartmentGrouping> result = new LinkedHashMap<>();
        
        for (Map.Entry<String, List<TransactionAggregate>> entry : groupedByDepartment.entrySet()) {
            String departmentName = entry.getKey();
            List<TransactionAggregate> departmentTransactions = entry.getValue();
            
            BigDecimal totalAmount = calculateTotalAmount(departmentTransactions);
            int transactionCount = departmentTransactions.size();
            BigDecimal averageAmount = calculateAverageAmount(totalAmount, transactionCount);
            
            // 使用 builder() 创建对象
            FinancialGroupingData.DepartmentGrouping grouping = 
                FinancialGroupingData.DepartmentGrouping.builder()
                    .departmentName(departmentName)
                    .totalAmount(totalAmount)
                    .transactionCount(transactionCount)
                    .averageAmount(averageAmount)
                    .build();
            
            result.put(departmentName, grouping);
        }
        
        return result;
    }

    /**
     * Generate fund groupings using domain logic
     */
    private Map<String, FinancialGroupingData.FundGrouping> generateFundGroupings(
        TenantId tenantId, List<TransactionAggregate> transactions) {
    
        // 修复Repository调用
        Map<Integer, Fund> fundMap = fundRepository
                .findByCompanyCompanyId(tenantId.getValue())  // 使用修复后的方法名
                .stream()
                .collect(Collectors.toMap(Fund::getFundId, f -> f));
        
        // 修复类型转换问题
        Map<String, List<TransactionAggregate>> groupedByFund = transactions.stream()
                .filter(tx -> tx.getFundId() != null)
                .collect(Collectors.groupingBy(tx -> {
                    Fund fund = fundMap.get(tx.getFundId());
                    return fund != null ? fund.getName() : "Unknown Fund";  // 使用 getName()
                }));
        
        Map<String, FinancialGroupingData.FundGrouping> result = new LinkedHashMap<>();
        
        for (Map.Entry<String, List<TransactionAggregate>> entry : groupedByFund.entrySet()) {
            String fundName = entry.getKey();
            List<TransactionAggregate> fundTransactions = entry.getValue();
            
            BigDecimal totalAmount = calculateTotalAmount(fundTransactions);
            int transactionCount = fundTransactions.size();
            BigDecimal averageAmount = calculateAverageAmount(totalAmount, transactionCount);
            
            // 使用 builder() 创建对象
            FinancialGroupingData.FundGrouping grouping = 
                FinancialGroupingData.FundGrouping.builder()
                    .fundName(fundName)
                    .totalAmount(totalAmount)
                    .transactionCount(transactionCount)
                    .averageAmount(averageAmount)
                    .build();
            
            result.put(fundName, grouping);
        }
        
        return result;
    }

    /**
     * Generate month groupings using domain logic
     */
    private Map<String, FinancialGroupingData.MonthGrouping> generateMonthGroupings(
        List<TransactionAggregate> transactions) {
        
        Map<String, List<TransactionAggregate>> groupedByMonth = transactions.stream()
                .collect(Collectors.groupingBy(tx -> 
                    tx.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))));
        
        Map<String, MonthGrouping> result = new LinkedHashMap<>();
        
        for (Map.Entry<String, List<TransactionAggregate>> entry : groupedByMonth.entrySet()) {
            String monthKey = entry.getKey();
            List<TransactionAggregate> monthTransactions = entry.getValue();
            
            BigDecimal totalAmount = calculateTotalAmount(monthTransactions);
            int transactionCount = monthTransactions.size();
            BigDecimal averageAmount = calculateAverageAmount(totalAmount, transactionCount);
            
            // 获取第一天用于排序和显示
            LocalDate firstDayOfMonth = monthTransactions.get(0).getTransactionDate().withDayOfMonth(1);
            String displayName = firstDayOfMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
            
            // 使用 builder() 创建对象
            FinancialGroupingData.MonthGrouping grouping = 
                FinancialGroupingData.MonthGrouping.builder()
                    .monthKey(monthKey)
                    .displayName(displayName)
                    .firstDayOfMonth(firstDayOfMonth)
                    .totalAmount(totalAmount)
                    .transactionCount(transactionCount)
                    .averageAmount(averageAmount)
                    .build();
            
            result.put(monthKey, grouping);
        }
        
        return result;
    }

    /**
     * Calculate total amount using domain logic
     */
    private BigDecimal calculateTotalAmount(List<TransactionAggregate> transactions) {
        return transactions.stream()
                .map(TransactionAggregate::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate grand total using domain logic
     */
    private BigDecimal calculateGrandTotal(List<TransactionAggregate> transactions) {
        return calculateTotalAmount(transactions);
    }

    /**
     * Calculate average amount using domain logic
     */
    private BigDecimal calculateAverageAmount(BigDecimal totalAmount, int transactionCount) {
        if (transactionCount == 0) {
            return BigDecimal.ZERO;
        }
        return totalAmount.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP);
    }

    /**
     * Create empty financial grouping data for cases with no transactions
     */
    private FinancialGroupingData createEmptyFinancialGroupingData(LocalDate startDate, LocalDate endDate) {
        String periodDescription = String.format("Period: %s to %s (No Data)", 
                                                startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                                endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        return FinancialGroupingData.builder()
                .periodDescription(periodDescription)
                .byCategory(new LinkedHashMap<>())
                .byDepartment(new LinkedHashMap<>())
                .byFund(new LinkedHashMap<>())
                .byMonth(new LinkedHashMap<>())
                .grandTotal(BigDecimal.ZERO)
                .totalTransactionCount(0)
                .build();
    }

    /**
     * Legacy method for backward compatibility - delegates to DDD implementation
     * @deprecated Use getFinancialGroupingDataByTenant instead
     */
    @Deprecated
    public FinancialGroupingData getFinancialGroupingData(TenantId tenantId, LocalDate startDate, LocalDate endDate) {
        log.warn("Using deprecated getFinancialGroupingData method. Consider migrating to getFinancialGroupingDataByTenant.");
        return getFinancialGroupingDataByTenant(tenantId, startDate, endDate);
    }
}