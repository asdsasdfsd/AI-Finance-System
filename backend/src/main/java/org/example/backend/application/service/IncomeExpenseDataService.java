// backend/src/main/java/org/example/backend/application/service/IncomeExpenseDataService.java
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
 * 
 * Responsibilities:
 * 1. Generate income vs expense report using DDD aggregates only
 * 2. Group transactions by category and description using domain logic
 * 3. Calculate monthly, YTD, and variance analysis through domain calculations
 * 4. Ensure all business rules are enforced through domain layer
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
     * Generate income expense report using pure DDD approach
     */
    public IncomeExpenseReportData generateIncomeExpenseReportByTenant(
            TenantId tenantId, LocalDate asOfDate) {
        
        log.info("Generating income expense report for tenant {} as of {}", 
                 tenantId.getValue(), asOfDate);
        
        // DDD: Validate tenant exists using aggregate
        CompanyAggregate company = companyRepository.findById(tenantId.getValue())
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + tenantId.getValue()));
        
        // DDD: Get year-to-date transactions using aggregate repository
        LocalDate yearStart = asOfDate.withDayOfYear(1);
        List<TransactionAggregate> ytdTransactions = getApprovedTransactionsForPeriod(
                tenantId, yearStart, asOfDate);
        
        // DDD: Get current month transactions using aggregate repository
        LocalDate monthStart = asOfDate.withDayOfMonth(1);
        List<TransactionAggregate> monthTransactions = getApprovedTransactionsForPeriod(
                tenantId, monthStart, asOfDate);
        
        // DDD: Process transactions using domain logic
        List<IncomeExpenseReportRowDTO> incomeRows = processIncomeTransactions(
                ytdTransactions, monthTransactions, tenantId);
        List<IncomeExpenseReportRowDTO> expenseRows = processExpenseTransactions(
                ytdTransactions, monthTransactions, tenantId);
        
        // DDD: Calculate totals using domain calculation methods
        BigDecimal totalIncomeYTD = calculateTotalIncome(ytdTransactions);
        BigDecimal totalExpenseYTD = calculateTotalExpense(ytdTransactions);
        BigDecimal netIncomeYTD = calculateNetIncome(totalIncomeYTD, totalExpenseYTD);
        
        BigDecimal totalIncomeMonth = calculateTotalIncome(monthTransactions);
        BigDecimal totalExpenseMonth = calculateTotalExpense(monthTransactions);
        BigDecimal netIncomeMonth = calculateNetIncome(totalIncomeMonth, totalExpenseMonth);
        
        log.info("Income expense report generated - YTD Net Income: {}, Month Net Income: {}", 
                 netIncomeYTD, netIncomeMonth);
        
        return IncomeExpenseReportData.builder()
                .companyName(company.getCompanyName())
                .asOfDate(asOfDate)
                .incomeRows(incomeRows)
                .expenseRows(expenseRows)
                .totalIncomeYTD(totalIncomeYTD)
                .totalExpenseYTD(totalExpenseYTD)
                .netIncomeYTD(netIncomeYTD)
                .totalIncomeMonth(totalIncomeMonth)
                .totalExpenseMonth(totalExpenseMonth)
                .netIncomeMonth(netIncomeMonth)
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
     * Process income transactions using domain logic
     */
    private List<IncomeExpenseReportRowDTO> processIncomeTransactions(
            List<TransactionAggregate> ytdTransactions, 
            List<TransactionAggregate> monthTransactions,
            TenantId tenantId) {
        
        // Filter income transactions using domain logic
        List<TransactionAggregate> ytdIncome = ytdTransactions.stream()
                .filter(this::isIncomeTransaction)
                .collect(Collectors.toList());
        
        List<TransactionAggregate> monthIncome = monthTransactions.stream()
                .filter(this::isIncomeTransaction)
                .collect(Collectors.toList());
        
        return groupTransactionsByDescription(ytdIncome, monthIncome, "INCOME", tenantId);
    }

    /**
     * Process expense transactions using domain logic
     */
    private List<IncomeExpenseReportRowDTO> processExpenseTransactions(
            List<TransactionAggregate> ytdTransactions, 
            List<TransactionAggregate> monthTransactions,
            TenantId tenantId) {
        
        // Filter expense transactions using domain logic
        List<TransactionAggregate> ytdExpenses = ytdTransactions.stream()
                .filter(this::isExpenseTransaction)
                .collect(Collectors.toList());
        
        List<TransactionAggregate> monthExpenses = monthTransactions.stream()
                .filter(this::isExpenseTransaction)
                .collect(Collectors.toList());
        
        return groupTransactionsByDescription(ytdExpenses, monthExpenses, "EXPENSE", tenantId);
    }

    /**
     * Group transactions by description using domain logic
     */
    private List<IncomeExpenseReportRowDTO> groupTransactionsByDescription(
                List<TransactionAggregate> ytdTransactions,
                List<TransactionAggregate> monthTransactions,
                String type,
            	TenantId tenantId) {
        
        // Group YTD transactions by description
        Map<String, List<TransactionAggregate>> ytdByDescription = ytdTransactions.stream()
                .collect(Collectors.groupingBy(this::getTransactionDescription));
        
        // Group month transactions by description
        Map<String, List<TransactionAggregate>> monthByDescription = monthTransactions.stream()
                .collect(Collectors.groupingBy(this::getTransactionDescription));
        
        // Get all unique descriptions
        Set<String> allDescriptions = new HashSet<>();
        allDescriptions.addAll(ytdByDescription.keySet());
        allDescriptions.addAll(monthByDescription.keySet());
        
        // FIXED: Get all category IDs and fetch real names
        Set<Integer> allCategoryIds = new HashSet<>();
        ytdTransactions.stream().filter(t -> t.getCategoryId() != null).forEach(t -> allCategoryIds.add(t.getCategoryId()));
        monthTransactions.stream().filter(t -> t.getCategoryId() != null).forEach(t -> allCategoryIds.add(t.getCategoryId()));
        
        // Get real category names from database - 需要添加 tenantId 参数到方法签名
        Map<Integer, String> categoryIdToNameMap = getCategoryNames(allCategoryIds, tenantId);
        
        List<IncomeExpenseReportRowDTO> result = new ArrayList<>();
        
        for (String description : allDescriptions) {
                List<TransactionAggregate> ytdTxs = ytdByDescription.getOrDefault(description, Collections.emptyList());
                List<TransactionAggregate> monthTxs = monthByDescription.getOrDefault(description, Collections.emptyList());
                
                BigDecimal ytdAmount = calculateTotalAmount(ytdTxs);
                BigDecimal monthAmount = calculateTotalAmount(monthTxs);
                
                // FIXED: Use real category name from first transaction
                String categoryName = ytdTxs.isEmpty() ? 
                (monthTxs.isEmpty() ? "Unknown Category" : getCategoryName(monthTxs.get(0).getCategoryId(), categoryIdToNameMap)) :
                getCategoryName(ytdTxs.get(0).getCategoryId(), categoryIdToNameMap);
                
                IncomeExpenseReportRowDTO row = IncomeExpenseReportRowDTO.builder()
                        .category(categoryName) // FIXED: Use real category name
                        .description(description)
                        .type(type)
                        .currentMonth(monthAmount)
                        .yearToDate(ytdAmount)
                        .budgetYtd(BigDecimal.ZERO)
                        .variance(calculateVariance(ytdAmount, BigDecimal.ZERO))
                        .variancePercentage(calculateVariancePercentage(ytdAmount, BigDecimal.ZERO))
                        .build();
                
                result.add(row);
        }
        
        return result;
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
     * Calculate total amount using domain calculation
     */
    private BigDecimal calculateTotalAmount(List<TransactionAggregate> transactions) {
        return transactions.stream()
                .map(TransactionAggregate::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate total income using domain logic
     */
    private BigDecimal calculateTotalIncome(List<TransactionAggregate> transactions) {
        return transactions.stream()
                .filter(this::isIncomeTransaction)
                .map(TransactionAggregate::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate total expense using domain logic
     */
    private BigDecimal calculateTotalExpense(List<TransactionAggregate> transactions) {
        return transactions.stream()
                .filter(this::isExpenseTransaction)
                .map(TransactionAggregate::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate net income using domain business rule
     */
    private BigDecimal calculateNetIncome(BigDecimal totalIncome, BigDecimal totalExpense) {
        return totalIncome.subtract(totalExpense);
    }

    /**
     * Calculate variance using domain calculation
     */
    private BigDecimal calculateVariance(BigDecimal actual, BigDecimal budget) {
        return actual.subtract(budget);
    }

    /**
     * Calculate variance percentage using domain calculation
     */
    private BigDecimal calculateVariancePercentage(BigDecimal actual, BigDecimal budget) {
        if (budget.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return actual.subtract(budget)
                .divide(budget, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
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

    /**
	 * FIXED: Helper method to get category names
	 */
	private Map<Integer, String> getCategoryNames(Set<Integer> categoryIds, TenantId tenantId) {
		if (categoryIds.isEmpty()) {
				return new HashMap<>();
		}

		List<Category> categories = categoryRepository.findByIdInAndCompanyId(categoryIds, tenantId.getValue());

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
}