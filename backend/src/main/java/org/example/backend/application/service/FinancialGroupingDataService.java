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
 * Financial Grouping Data Service - Fixed DDD Implementation
 * 
 * All Repository method calls have been fixed to use existing or newly added methods
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
     * Generate financial grouping data using pure DDD approach - FIXED
     */
    public FinancialGroupingData getFinancialGroupingDataByTenant(
            TenantId tenantId, LocalDate startDate, LocalDate endDate) {
        
        log.info("Generating financial grouping for tenant {} from {} to {}", 
                 tenantId.getValue(), startDate, endDate);
        
        // DDD: Validate tenant exists using aggregate
        companyRepository.findById(tenantId.getValue())
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + tenantId.getValue()));
        
        // FIXED: Use the new method that exists in the repository
        List<TransactionAggregate> approvedTransactions = getApprovedTransactionsForPeriod(
                tenantId, startDate, endDate);
        
        if (approvedTransactions.isEmpty()) {
            log.warn("No approved transactions found for tenant {} in period", tenantId.getValue());
            return createEmptyGroupingData(tenantId, startDate, endDate);
        }
        
        // Generate groupings using domain logic
        Map<String, FinancialGroupingData.CategoryGrouping> categoryGroupings = 
                generateCategoryGroupings(approvedTransactions, tenantId);
        Map<String, FinancialGroupingData.DepartmentGrouping> departmentGroupings = 
                generateDepartmentGroupings(approvedTransactions, tenantId);
        Map<String, FinancialGroupingData.FundGrouping> fundGroupings = 
                generateFundGroupings(approvedTransactions, tenantId);
        Map<String, FinancialGroupingData.TransactionTypeGrouping> typeGroupings = 
                generateTransactionTypeGroupings(approvedTransactions);
        Map<String, FinancialGroupingData.MonthGrouping> monthGroupings = 
                generateMonthlyGroupings(approvedTransactions);
        
        return FinancialGroupingData.builder()
                .tenantId(tenantId)
                .startDate(startDate)
                .endDate(endDate)
                .periodDescription(String.format("From %s to %s", startDate, endDate))
                .byCategory(categoryGroupings)
                .byDepartment(departmentGroupings)
                .byFund(fundGroupings)
                .byTransactionType(typeGroupings)
                .byMonth(monthGroupings)
                .build();
    }

    /**
     * FIXED: Get approved transactions for the specified period
     */
    private List<TransactionAggregate> getApprovedTransactionsForPeriod(
            TenantId tenantId, LocalDate startDate, LocalDate endDate) {
        
        // Use the FIXED method name that now exists in TransactionAggregateRepository
        return transactionRepository.findByTenantIdAndDateRangeAndStatus(
                tenantId, startDate, endDate, TransactionStatus.Status.APPROVED);
    }

    /**
     * Generate category-based groupings - FIXED repository calls
     */
    private Map<String, FinancialGroupingData.CategoryGrouping> generateCategoryGroupings(
            List<TransactionAggregate> transactions, TenantId tenantId) {
        
        Map<String, FinancialGroupingData.CategoryGrouping> result = new LinkedHashMap<>();
        
        // Group transactions by category ID
        Map<Integer, List<TransactionAggregate>> transactionsByCategory = transactions.stream()
                .filter(t -> t.getCategoryId() != null)
                .collect(Collectors.groupingBy(TransactionAggregate::getCategoryId));
        
        // FIXED: Use the new method that exists in CategoryRepository
        Map<Integer, Category> categoryMap = getCategoriesByIds(
                transactionsByCategory.keySet(), tenantId);
        
        for (Map.Entry<Integer, List<TransactionAggregate>> entry : transactionsByCategory.entrySet()) {
            Integer categoryId = entry.getKey();
            List<TransactionAggregate> categoryTransactions = entry.getValue();
            Category category = categoryMap.get(categoryId);
            
            String categoryName = category != null ? category.getName() : "Unknown Category";
            BigDecimal totalAmount = calculateTotalAmount(categoryTransactions);
            int transactionCount = categoryTransactions.size();
            BigDecimal averageAmount = calculateAverageAmount(totalAmount, transactionCount);
            
            FinancialGroupingData.CategoryGrouping grouping = FinancialGroupingData.CategoryGrouping.builder()
                    .categoryName(categoryName)
                    .totalAmount(totalAmount)
                    .transactionCount(transactionCount)
                    .averageAmount(averageAmount)
                    .transactions(categoryTransactions)
                    .build();
            
            result.put(categoryName, grouping);
        }
        
        return result;
    }

    /**
     * Generate department-based groupings - FIXED repository calls
     */
    private Map<String, FinancialGroupingData.DepartmentGrouping> generateDepartmentGroupings(
            List<TransactionAggregate> transactions, TenantId tenantId) {
        
        Map<String, FinancialGroupingData.DepartmentGrouping> result = new LinkedHashMap<>();
        
        // Group transactions by department ID
        Map<Integer, List<TransactionAggregate>> transactionsByDepartment = transactions.stream()
                .filter(t -> t.getDepartmentId() != null)
                .collect(Collectors.groupingBy(TransactionAggregate::getDepartmentId));
        
        // FIXED: Use the new method that exists in DepartmentRepository
        Map<Integer, Department> departmentMap = getDepartmentsByIds(
                transactionsByDepartment.keySet(), tenantId);
        
        for (Map.Entry<Integer, List<TransactionAggregate>> entry : transactionsByDepartment.entrySet()) {
            Integer departmentId = entry.getKey();
            List<TransactionAggregate> deptTransactions = entry.getValue();
            Department department = departmentMap.get(departmentId);
            
            String departmentName = department != null ? department.getName() : "Unknown Department";
            BigDecimal totalAmount = calculateTotalAmount(deptTransactions);
            int transactionCount = deptTransactions.size();
            BigDecimal averageAmount = calculateAverageAmount(totalAmount, transactionCount);
            
            FinancialGroupingData.DepartmentGrouping grouping = FinancialGroupingData.DepartmentGrouping.builder()
                    .departmentName(departmentName)
                    .totalAmount(totalAmount)
                    .transactionCount(transactionCount)
                    .averageAmount(averageAmount)
                    .transactions(deptTransactions)
                    .build();
            
            result.put(departmentName, grouping);
        }
        
        return result;
    }

    /**
     * Generate fund-based groupings - FIXED repository calls
     */
    private Map<String, FinancialGroupingData.FundGrouping> generateFundGroupings(
            List<TransactionAggregate> transactions, TenantId tenantId) {
        
        Map<String, FinancialGroupingData.FundGrouping> result = new LinkedHashMap<>();
        
        // Group transactions by fund ID
        Map<Integer, List<TransactionAggregate>> transactionsByFund = transactions.stream()
                .filter(t -> t.getFundId() != null)
                .collect(Collectors.groupingBy(TransactionAggregate::getFundId));
        
        // FIXED: Use the new method that exists in FundRepository
        Map<Integer, Fund> fundMap = getFundsByIds(transactionsByFund.keySet(), tenantId);
        
        for (Map.Entry<Integer, List<TransactionAggregate>> entry : transactionsByFund.entrySet()) {
            Integer fundId = entry.getKey();
            List<TransactionAggregate> fundTransactions = entry.getValue();
            Fund fund = fundMap.get(fundId);
            
            String fundName = fund != null ? fund.getName() : "Unknown Fund";
            BigDecimal totalAmount = calculateTotalAmount(fundTransactions);
            int transactionCount = fundTransactions.size();
            BigDecimal averageAmount = calculateAverageAmount(totalAmount, transactionCount);
            
            FinancialGroupingData.FundGrouping grouping = FinancialGroupingData.FundGrouping.builder()
                    .fundName(fundName)
                    .totalAmount(totalAmount)
                    .transactionCount(transactionCount)
                    .averageAmount(averageAmount)
                    .transactions(fundTransactions)
                    .build();
            
            result.put(fundName, grouping);
        }
        
        return result;
    }

    /**
     * Generate transaction type groupings
     */
    private Map<String, FinancialGroupingData.TransactionTypeGrouping> generateTransactionTypeGroupings(
            List<TransactionAggregate> transactions) {
        
        Map<String, FinancialGroupingData.TransactionTypeGrouping> result = new LinkedHashMap<>();
        
        // Group transactions by type
        Map<TransactionAggregate.TransactionType, List<TransactionAggregate>> transactionsByType = 
                transactions.stream()
                    .collect(Collectors.groupingBy(TransactionAggregate::getTransactionType));
        
        for (Map.Entry<TransactionAggregate.TransactionType, List<TransactionAggregate>> entry : 
                transactionsByType.entrySet()) {
            
            TransactionAggregate.TransactionType type = entry.getKey();
            List<TransactionAggregate> typeTransactions = entry.getValue();
            
            String typeName = type.name();
            BigDecimal totalAmount = calculateTotalAmount(typeTransactions);
            int transactionCount = typeTransactions.size();
            BigDecimal averageAmount = calculateAverageAmount(totalAmount, transactionCount);
            
            FinancialGroupingData.TransactionTypeGrouping grouping = 
                    FinancialGroupingData.TransactionTypeGrouping.builder()
                    .typeName(typeName)
                    .totalAmount(totalAmount)
                    .transactionCount(transactionCount)
                    .averageAmount(averageAmount)
                    .transactions(typeTransactions)
                    .build();
            
            result.put(typeName, grouping);
        }
        
        return result;
    }

    /**
     * Generate monthly groupings
     */
    private Map<String, FinancialGroupingData.MonthGrouping> generateMonthlyGroupings(
            List<TransactionAggregate> transactions) {
        
        Map<String, FinancialGroupingData.MonthGrouping> result = new LinkedHashMap<>();
        
        // Group transactions by month
        Map<String, List<TransactionAggregate>> transactionsByMonth = transactions.stream()
                .collect(Collectors.groupingBy(t -> 
                    t.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))));
        
        for (Map.Entry<String, List<TransactionAggregate>> entry : transactionsByMonth.entrySet()) {
            String monthKey = entry.getKey();
            List<TransactionAggregate> monthTransactions = entry.getValue();
            
            BigDecimal totalAmount = calculateTotalAmount(monthTransactions);
            int transactionCount = monthTransactions.size();
            BigDecimal averageAmount = calculateAverageAmount(totalAmount, transactionCount);
            
            // Get first day of month for display
            LocalDate firstDayOfMonth = monthTransactions.stream()
                    .map(TransactionAggregate::getTransactionDate)
                    .min(LocalDate::compareTo)
                    .orElse(null);
            
            FinancialGroupingData.MonthGrouping grouping = FinancialGroupingData.MonthGrouping.builder()
                    .monthKey(monthKey)
                    .totalAmount(totalAmount)
                    .transactionCount(transactionCount)
                    .averageAmount(averageAmount)
                    .firstDayOfMonth(firstDayOfMonth)
                    .transactions(monthTransactions)
                    .build();
            
            result.put(monthKey, grouping);
        }
        
        return result;
    }

    /**
     * Create empty grouping data when no transactions found
     */
    private FinancialGroupingData createEmptyGroupingData(TenantId tenantId, LocalDate startDate, LocalDate endDate) {
        return FinancialGroupingData.builder()
                .tenantId(tenantId)
                .startDate(startDate)
                .endDate(endDate)
                .periodDescription(String.format("From %s to %s (No data)", startDate, endDate))
                .byCategory(new LinkedHashMap<>())
                .byDepartment(new LinkedHashMap<>())
                .byFund(new LinkedHashMap<>())
                .byTransactionType(new LinkedHashMap<>())
                .byMonth(new LinkedHashMap<>())
                .build();
    }

    // ========== Helper Methods - FIXED repository calls ==========

    private BigDecimal calculateTotalAmount(List<TransactionAggregate> transactions) {
        return transactions.stream()
                .map(TransactionAggregate::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateAverageAmount(BigDecimal totalAmount, int count) {
        return count > 0 ? 
                totalAmount.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
    }

    /**
     * FIXED: Get categories by IDs using the new repository method
     */
    private Map<Integer, Category> getCategoriesByIds(Set<Integer> categoryIds, TenantId tenantId) {
        return categoryRepository.findByIdInAndCompanyId(categoryIds, tenantId.getValue())
                .stream()
                .collect(Collectors.toMap(Category::getCategoryId, c -> c));
    }

    /**
     * FIXED: Get departments by IDs using the new repository method
     */
    private Map<Integer, Department> getDepartmentsByIds(Set<Integer> departmentIds, TenantId tenantId) {
        return departmentRepository.findByIdInAndCompanyId(departmentIds, tenantId.getValue())
                .stream()
                .collect(Collectors.toMap(Department::getDepartmentId, d -> d));
    }

    /**
     * FIXED: Get funds by IDs using the new repository method
     */
    private Map<Integer, Fund> getFundsByIds(Set<Integer> fundIds, TenantId tenantId) {
        return fundRepository.findByIdInAndCompanyId(fundIds, tenantId.getValue())
                .stream()
                .collect(Collectors.toMap(Fund::getFundId, f -> f));
    }
    /**
     * Alias method for backward compatibility
     */
    public FinancialGroupingData getFinancialGroupingData(TenantId tenantId, LocalDate startDate, LocalDate endDate) {
        return getFinancialGroupingDataByTenant(tenantId, startDate, endDate);
}
}