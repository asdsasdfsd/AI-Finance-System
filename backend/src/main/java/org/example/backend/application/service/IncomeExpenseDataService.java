// backend/src/main/java/org/example/backend/application/service/IncomeExpenseDataService.java
// FIXED IncomeExpenseDataService with correct status mapping and debug logging

package org.example.backend.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.application.dto.IncomeExpenseReportData;
import org.example.backend.application.dto.IncomeExpenseReportRowDTO;
import org.example.backend.domain.aggregate.transaction.TransactionAggregateRepository;
import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.aggregate.company.CompanyAggregateRepository;
import org.example.backend.domain.aggregate.company.CompanyAggregate;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.TransactionStatus;
import org.example.backend.model.Category;
import org.example.backend.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Income Expense Data Service - DDD Compliant Implementation
 * FIXED: Uses correct status values and improved logging
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IncomeExpenseDataService {

    private final TransactionAggregateRepository transactionRepository;
    private final CompanyAggregateRepository companyRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Generate Income Expense Report by Tenant using DDD
     * FIXED: Enhanced debugging and correct status handling
     */
    public IncomeExpenseReportData generateIncomeExpenseReportByTenant(TenantId tenantId, LocalDate asOfDate) {
        try {
            log.info("=== Starting Income Expense Report Generation ===");
            log.info("Tenant ID: {}, As of Date: {}", tenantId.getValue(), asOfDate);

            // Validate inputs
            if (tenantId == null || tenantId.getValue() == null) {
                throw new IllegalArgumentException("Tenant ID cannot be null");
            }
            if (asOfDate == null) {
                throw new IllegalArgumentException("As-of date cannot be null");
            }

            // Get company using DDD repository
            Optional<CompanyAggregate> companyOpt = companyRepository.findById(tenantId.getValue());
            if (companyOpt.isEmpty()) {
                throw new IllegalArgumentException("Company not found for tenant: " + tenantId.getValue());
            }
            CompanyAggregate company = companyOpt.get();
            log.info("Found company: {}", company.getCompanyName());

            // FIXED: Get ALL transactions first, then filter by status and date
            List<TransactionAggregate> allTransactions = transactionRepository
                    .findByTenantIdOrderByTransactionDateDesc(tenantId);
            
            log.info("Total transactions found for tenant {}: {}", tenantId.getValue(), allTransactions.size());

            // FIXED: Filter by APPROVED status (status = 2) and date range
            List<TransactionAggregate> validTransactions = allTransactions.stream()
                    .filter(tx -> {
                        boolean dateValid = tx.getTransactionDate() != null && 
                                          !tx.getTransactionDate().isAfter(asOfDate);
                        // FIXED: Check status using the actual status field value
                        boolean statusValid = isApprovedTransaction(tx);
                        
                        if (!dateValid) {
                            log.debug("Transaction {} excluded: date {} after {}", 
                                     tx.getTransactionId(), tx.getTransactionDate(), asOfDate);
                        }
                        if (!statusValid) {
                            log.debug("Transaction {} excluded: status not approved", tx.getTransactionId());
                        }
                        
                        return dateValid && statusValid;
                    })
                    .collect(Collectors.toList());

            log.info("Valid approved transactions up to {}: {}", asOfDate, validTransactions.size());

            if (validTransactions.isEmpty()) {
                log.warn("No valid transactions found for tenant {} up to date {}", 
                         tenantId.getValue(), asOfDate);
                return createEmptyReport(company.getCompanyName(), asOfDate);
            }

            // Split transactions by type
            List<TransactionAggregate> incomeTransactions = validTransactions.stream()
                    .filter(this::isIncomeTransaction)
                    .collect(Collectors.toList());

            List<TransactionAggregate> expenseTransactions = validTransactions.stream()
                    .filter(this::isExpenseTransaction)
                    .collect(Collectors.toList());

            log.info("Income transactions: {}, Expense transactions: {}", 
                     incomeTransactions.size(), expenseTransactions.size());

            // Get category names for mapping
            Set<Integer> categoryIds = validTransactions.stream()
                    .map(TransactionAggregate::getCategoryId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            
            Map<Integer, String> categoryIdToNameMap = getCategoryNames(categoryIds, tenantId);
            log.info("Category mapping loaded: {} categories", categoryIdToNameMap.size());

            // Generate income and expense rows with NULL SAFETY
            List<IncomeExpenseReportRowDTO> incomeRows = generateReportRows(
                    incomeTransactions, "INCOME", asOfDate, categoryIdToNameMap);
            List<IncomeExpenseReportRowDTO> expenseRows = generateReportRows(
                    expenseTransactions, "EXPENSE", asOfDate, categoryIdToNameMap);

            log.info("Generated {} income rows and {} expense rows", 
                     incomeRows.size(), expenseRows.size());

            // Calculate totals with NULL SAFETY
            BigDecimal totalIncomeMonth = calculateCurrentMonthTotal(incomeRows);
            BigDecimal totalExpenseMonth = calculateCurrentMonthTotal(expenseRows);
            BigDecimal netIncomeMonth = totalIncomeMonth.subtract(totalExpenseMonth);

            BigDecimal totalIncomeYTD = calculateYearToDateTotal(incomeRows);
            BigDecimal totalExpenseYTD = calculateYearToDateTotal(expenseRows);
            BigDecimal netIncomeYTD = totalIncomeYTD.subtract(totalExpenseYTD);

            log.info("Totals - Income Month: {}, Expense Month: {}, Net Month: {}", 
                     totalIncomeMonth, totalExpenseMonth, netIncomeMonth);

            // Build final report data
            IncomeExpenseReportData reportData = IncomeExpenseReportData.builder()
                    .companyName(company.getCompanyName())
                    .asOfDate(asOfDate)
                    .incomeRows(incomeRows)
                    .expenseRows(expenseRows)
                    .totalIncomeMonth(totalIncomeMonth)
                    .totalExpenseMonth(totalExpenseMonth)
                    .netIncomeMonth(netIncomeMonth)
                    .totalIncomeYTD(totalIncomeYTD)
                    .totalExpenseYTD(totalExpenseYTD)
                    .netIncomeYTD(netIncomeYTD)
                    .build();

            log.info("=== Income Expense Report Generation Completed Successfully ===");
            return reportData;

        } catch (Exception e) {
            log.error("Failed to generate income expense report for tenant {}: {}", 
                     tenantId.getValue(), e.getMessage(), e);
            throw new RuntimeException("Failed to generate income expense report: " + e.getMessage(), e);
        }
    }

    /**
     * FIXED: Check if transaction is approved by looking at the actual status value
     * Database stores status as integer: 2 = APPROVED
     */
    private boolean isApprovedTransaction(TransactionAggregate transaction) {
        try {
            // Check if transaction has status field and it equals 2 (APPROVED)
            // This matches the database structure where status = 2
            return transaction.getTransactionStatus() != null && 
                   transaction.getTransactionStatus().getStatus() == TransactionStatus.Status.APPROVED;
        } catch (Exception e) {
            log.debug("Error checking transaction status for {}: {}", 
                     transaction.getTransactionId(), e.getMessage());
            return false;
        }
    }

    /**
     * Create empty report when no data is found
     */
    private IncomeExpenseReportData createEmptyReport(String companyName, LocalDate asOfDate) {
        return IncomeExpenseReportData.builder()
                .companyName(companyName)
                .asOfDate(asOfDate)
                .incomeRows(new ArrayList<>())
                .expenseRows(new ArrayList<>())
                .totalIncomeMonth(BigDecimal.ZERO)
                .totalExpenseMonth(BigDecimal.ZERO)
                .netIncomeMonth(BigDecimal.ZERO)
                .totalIncomeYTD(BigDecimal.ZERO)
                .totalExpenseYTD(BigDecimal.ZERO)
                .netIncomeYTD(BigDecimal.ZERO)
                .build();
    }

    /**
     * FIXED: Generate report rows with comprehensive null safety
     */
    private List<IncomeExpenseReportRowDTO> generateReportRows(
            List<TransactionAggregate> transactions, String type, LocalDate asOfDate,
            Map<Integer, String> categoryIdToNameMap) {

        List<IncomeExpenseReportRowDTO> result = new ArrayList<>();

        if (transactions.isEmpty()) {
            log.info("No transactions found for type: {}", type);
            return result;
        }

        // Group by category + description for aggregation
        Map<String, List<TransactionAggregate>> groupedTransactions = transactions.stream()
                .collect(Collectors.groupingBy(tx -> {
                    String categoryName = getCategoryName(tx.getCategoryId(), categoryIdToNameMap);
                    String description = getTransactionDescription(tx);
                    return categoryName + "|" + description;
                }));

        log.info("Grouped {} transactions into {} groups for type {}", 
                 transactions.size(), groupedTransactions.size(), type);

        // Generate rows for each group
        for (Map.Entry<String, List<TransactionAggregate>> entry : groupedTransactions.entrySet()) {
            String[] keyParts = entry.getKey().split("\\|", 2);
            String categoryName = keyParts.length > 0 ? keyParts[0] : "Unknown Category";
            String description = keyParts.length > 1 ? keyParts[1] : "Unknown Description";
            
            List<TransactionAggregate> groupTransactions = entry.getValue();

            // Calculate amounts for different periods with NULL SAFETY
            BigDecimal currentMonth = calculateAmountForMonth(groupTransactions, asOfDate);
            BigDecimal previousMonth = calculateAmountForPreviousMonth(groupTransactions, asOfDate);
            BigDecimal yearToDate = calculateAmountForYearToDate(groupTransactions, asOfDate);

            // FIXED: Ensure all BigDecimal fields are never null
            IncomeExpenseReportRowDTO row = IncomeExpenseReportRowDTO.builder()
                    .category(categoryName)
                    .description(description)
                    .type(type)
                    .currentMonth(safeDecimal(currentMonth))
                    .previousMonth(safeDecimal(previousMonth))  // FIXED: Never null
                    .yearToDate(safeDecimal(yearToDate))
                    .budgetYtd(safeDecimal(BigDecimal.ZERO))     // FIXED: Default to zero
                    .variance(safeDecimal(yearToDate))           // FIXED: YTD as variance for now
                    .fullYearBudget(safeDecimal(BigDecimal.ZERO)) // FIXED: Default to zero
                    .variancePercentage(safeDecimal(BigDecimal.ZERO)) // FIXED: Default to zero
                    .build();

            result.add(row);
            log.debug("Added {} row: {} - {} = {}", type, categoryName, description, currentMonth);
        }

        return result;
    }

    /**
     * FIXED: Safe BigDecimal that never returns null
     */
    private BigDecimal safeDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    /**
     * Calculate amount for the current month (as-of date's month)
     */
    private BigDecimal calculateAmountForMonth(List<TransactionAggregate> transactions, LocalDate asOfDate) {
        return transactions.stream()
                .filter(tx -> tx.getTransactionDate() != null &&
                             tx.getTransactionDate().getYear() == asOfDate.getYear() &&
                             tx.getTransactionDate().getMonth() == asOfDate.getMonth())
                .map(TransactionAggregate::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate amount for the previous month
     */
    private BigDecimal calculateAmountForPreviousMonth(List<TransactionAggregate> transactions, LocalDate asOfDate) {
        LocalDate previousMonth = asOfDate.minusMonths(1);
        return transactions.stream()
                .filter(tx -> tx.getTransactionDate() != null &&
                             tx.getTransactionDate().getYear() == previousMonth.getYear() &&
                             tx.getTransactionDate().getMonth() == previousMonth.getMonth())
                .map(TransactionAggregate::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate amount for year-to-date (from Jan 1 to as-of date)
     */
    private BigDecimal calculateAmountForYearToDate(List<TransactionAggregate> transactions, LocalDate asOfDate) {
        LocalDate yearStart = LocalDate.of(asOfDate.getYear(), 1, 1);
        return transactions.stream()
                .filter(tx -> tx.getTransactionDate() != null &&
                             !tx.getTransactionDate().isBefore(yearStart) &&
                             !tx.getTransactionDate().isAfter(asOfDate))
                .map(TransactionAggregate::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate current month total from rows
     */
    private BigDecimal calculateCurrentMonthTotal(List<IncomeExpenseReportRowDTO> rows) {
        return rows.stream()
                .map(IncomeExpenseReportRowDTO::getCurrentMonth)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate year-to-date total from rows
     */
    private BigDecimal calculateYearToDateTotal(List<IncomeExpenseReportRowDTO> rows) {
        return rows.stream()
                .map(IncomeExpenseReportRowDTO::getYearToDate)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Domain logic: Determine if transaction is income
     */
    private boolean isIncomeTransaction(TransactionAggregate transaction) {
        return transaction.getTransactionType() == TransactionAggregate.TransactionType.INCOME;
    }

    /**
     * Domain logic: Determine if transaction is expense
     */
    private boolean isExpenseTransaction(TransactionAggregate transaction) {
        return transaction.getTransactionType() == TransactionAggregate.TransactionType.EXPENSE;
    }

    /**
     * Get transaction description using domain logic
     */
    private String getTransactionDescription(TransactionAggregate transaction) {
        String description = transaction.getDescription();
        return (description != null && !description.trim().isEmpty()) ? 
               description.trim() : "Unspecified";
    }

    /**
     * FIXED: Helper method to get category names
     */
    private Map<Integer, String> getCategoryNames(Set<Integer> categoryIds, TenantId tenantId) {
        if (categoryIds.isEmpty()) {
            return new HashMap<>();
        }

        List<Category> categories = categoryRepository.findByIdInAndCompanyId(categoryIds, tenantId.getValue());
        log.info("Found {} categories for {} IDs", categories.size(), categoryIds.size());

        return categories.stream()
                .collect(Collectors.toMap(
                        Category::getCategoryId,
                        Category::getName,
                        (existing, replacement) -> existing
                ));
    }

    /**
     * FIXED: Helper method to get single category name
     */
    private String getCategoryName(Integer categoryId, Map<Integer, String> categoryIdToNameMap) {
        if (categoryId == null) {
            return "Unknown Category";
        }
        return categoryIdToNameMap.getOrDefault(categoryId, "Unknown Category (ID: " + categoryId + ")");
    }

    /**
     * Legacy method for backward compatibility - delegates to DDD implementation
     * @deprecated Use generateIncomeExpenseReportByTenant instead
     */
    @Deprecated
    public List<IncomeExpenseReportRowDTO> generateIncomeExpenseReport(TenantId tenantId, LocalDate asOfDate) {
        log.warn("Using deprecated generateIncomeExpenseReport method. Consider migrating to generateIncomeExpenseReportByTenant.");
        IncomeExpenseReportData data = generateIncomeExpenseReportByTenant(tenantId, asOfDate);
        List<IncomeExpenseReportRowDTO> result = new ArrayList<>();
        result.addAll(data.getIncomeRows());
        result.addAll(data.getExpenseRows());
        return result;
    }
}