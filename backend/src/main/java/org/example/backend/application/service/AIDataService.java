// backend/src/main/java/org/example/backend/application/service/AIDataService.java
package org.example.backend.application.service;

import lombok.extern.slf4j.Slf4j;
import org.example.backend.application.dto.AITransactionData;
import org.example.backend.application.dto.DateRange;
import org.example.backend.model.Transaction;
import org.example.backend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Collections;

/**
 * AI Data Service - Enhanced with error handling and fallback
 * Provides financial data context for AI operations
 */
@Slf4j
@Service
public class AIDataService {

    @Autowired(required = false)
    private TransactionRepository transactionRepository;

    /**
     * Get recent transactions for a company (used for classification/Q&A context)
     */
    public List<AITransactionData> getRecentTransactions(Integer companyId, int limit) {
        try {
            if (transactionRepository == null) {
                log.warn("TransactionRepository not available, returning empty list");
                return Collections.emptyList();
            }

            Pageable pageable = PageRequest.of(0, limit);
            List<Transaction> transactions = transactionRepository
                    .findByCompany_CompanyIdOrderByTransactionDateDesc(companyId, pageable);
            
            return transactions.stream()
                    .map(this::toAITransactionData)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching recent transactions for company {}: {}", companyId, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Get transactions within a date range (used for anomaly detection)
     */
    public List<AITransactionData> getTransactionsInRange(Integer companyId, DateRange dateRange) {
        try {
            if (transactionRepository == null) {
                log.warn("TransactionRepository not available, returning empty list");
                return Collections.emptyList();
            }

            List<Transaction> txns = transactionRepository.findByCompanyIdAndDateRange(
                    companyId, dateRange.getStartDate(), dateRange.getEndDate());

            return txns.stream()
                    .map(this::toAITransactionData)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching transactions in range for company {}: {}", companyId, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Build financial context summary for a company within time period (used for Q&A)
     */
    public String buildFinancialContext(Integer companyId, LocalDate start, LocalDate end) {
        try {
            if (transactionRepository == null) {
                log.warn("TransactionRepository not available, returning mock context");
                return String.format("Financial data for company %d from %s to %s is temporarily unavailable.", 
                    companyId, start.toString(), end.toString());
            }

            List<Transaction> txns = transactionRepository.findByCompanyIdAndDateRange(companyId, start, end);
            
            if (txns.isEmpty()) {
                return String.format("No transactions found for company %d from %s to %s.", 
                    companyId, start.toString(), end.toString());
            }

            double income = txns.stream()
                    .filter(t -> Transaction.TransactionType.INCOME.equals(t.getTransactionType()))
                    .mapToDouble(t -> t.getAmount() != null ? t.getAmount().doubleValue() : 0.0)
                    .sum();

            double expense = txns.stream()
                    .filter(t -> Transaction.TransactionType.EXPENSE.equals(t.getTransactionType()))
                    .mapToDouble(t -> t.getAmount() != null ? t.getAmount().doubleValue() : 0.0)
                    .sum();

            return String.format("From %s to %s: total income = %.2f, total expenses = %.2f, net = %.2f.",
                    start.toString(), end.toString(), income, expense, income - expense);
        } catch (Exception e) {
            log.error("Error building financial context for company {}: {}", companyId, e.getMessage());
            return String.format("Error retrieving financial data for company %d from %s to %s.", 
                companyId, start.toString(), end.toString());
        }
    }

    /**
     * Convert Transaction entity to AI data structure
     */
    private AITransactionData toAITransactionData(Transaction t) {
        try {
            return AITransactionData.builder()
                    .description(t.getDescription())
                    .amount(t.getAmount() != null ? t.getAmount().doubleValue() : 0.0)
                    .currency(t.getCurrency() != null ? t.getCurrency() : "CNY")
                    .category(t.getCategory() != null ? t.getCategory().getName() : "Unknown")
                    .transactionDate(t.getTransactionDate())
                    .transactionType(t.getTransactionType() != null ? t.getTransactionType().name() : "EXPENSE")
                    .companyId(t.getCompany() != null ? t.getCompany().getCompanyId() : null)
                    .userId(t.getUser() != null ? t.getUser().getUserId() : null)
                    .paymentMethod(t.getPaymentMethod())
                    .referenceNumber(t.getReferenceNumber())
                    .isRecurring(t.getIsRecurring())
                    .isTaxable(t.getIsTaxable())
                    .metadata(Map.of("source", "database", "entityId", t.getTransactionId()))
                    .build();
        } catch (Exception e) {
            log.error("Error converting transaction to AI data: {}", e.getMessage());
            // Return a safe fallback
            return AITransactionData.builder()
                    .description("Error loading transaction")
                    .amount(0.0)
                    .currency("CNY")
                    .category("Unknown")
                    .transactionDate(LocalDate.now())
                    .transactionType("EXPENSE")
                    .companyId(t.getCompany() != null ? t.getCompany().getCompanyId() : null)
                    .metadata(Map.of("source", "error"))
                    .build();
        }
    }
}