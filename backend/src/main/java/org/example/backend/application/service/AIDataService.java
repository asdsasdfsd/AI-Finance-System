package org.example.backend.application.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.application.dto.AITransactionData;
import org.example.backend.application.dto.DateRange;
import org.example.backend.model.Transaction;
import org.example.backend.repository.TransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIDataService {

    private final TransactionRepository transactionRepository;

    /**
     * 获取公司最近交易（用于分类/问答上下文）
     */
    public List<AITransactionData> getRecentTransactions(Integer companyId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return transactionRepository.findByCompany_CompanyIdOrderByTransactionDateDesc(companyId, pageable)
                .stream()
                .map(this::toAITransactionData)
                .collect(Collectors.toList());
    }

    /**
     * 获取公司指定时间范围内的交易（用于异常检测）
     */
    public List<AITransactionData> getTransactionsInRange(Integer companyId, DateRange dateRange) {
        List<Transaction> txns = transactionRepository.findByCompanyIdAndDateRange(
                companyId, dateRange.getStartDate(), dateRange.getEndDate());

        return txns.stream()
                .map(this::toAITransactionData)
                .collect(Collectors.toList());
    }

    /**
     * 构建指定公司在时间段内的财务上下文摘要（用于问答）
     */
    public String buildFinancialContext(Integer companyId, LocalDate start, LocalDate end) {
        List<Transaction> txns = transactionRepository.findByCompanyIdAndDateRange(companyId, start, end);

        double income = txns.stream()
                .filter(t -> "INCOME".equalsIgnoreCase(t.getTransactionType().name()))
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();

        double expense = txns.stream()
                .filter(t -> "EXPENSE".equalsIgnoreCase(t.getTransactionType().name()))
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();

        return String.format("From %s to %s: total income = %.2f, total expenses = %.2f.",
                start.toString(), end.toString(), income, expense);
    }

    /**
     * Entity 转 AI 数据结构
     */
    private AITransactionData toAITransactionData(Transaction t) {
        return AITransactionData.builder()
                .description(t.getDescription())
                .amount(t.getAmount().doubleValue())
                .currency(t.getCurrency())
                .category(t.getCategory() != null ? t.getCategory().getName() : null)
                .transactionDate(t.getTransactionDate())
                .transactionType(t.getTransactionType().name())
                .companyId(t.getCompany() != null ? t.getCompany().getCompanyId() : null)
                .metadata(Map.of("source", "system"))
                .build();
    }
}
