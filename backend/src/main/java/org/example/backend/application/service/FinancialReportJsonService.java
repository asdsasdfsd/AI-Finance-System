package org.example.backend.application.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.application.dto.IncomeExpenseReportRowDTO;
import org.example.backend.model.Transaction;
import org.example.backend.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialReportJsonService {

    private final TransactionRepository transactionRepository;

    public List<IncomeExpenseReportRowDTO> getIncomeExpenseReport(Integer companyId, LocalDate asOfDate) {
        List<Transaction> transactions = transactionRepository.findAllByCompanyId(companyId);

        List<IncomeExpenseReportRowDTO> rows = new ArrayList<>();

        Map<String, Map<String, List<Transaction>>> grouped = transactions.stream()
                .filter(txn -> txn.getTransactionDate() != null && !txn.getTransactionDate().isAfter(asOfDate))
                .collect(Collectors.groupingBy(
                        txn -> txn.getTransactionType().toString(),
                        Collectors.groupingBy(txn -> txn.getCategory().getName())
                ));

        for (String type : List.of("INCOME", "EXPENSE")) {
            Map<String, List<Transaction>> categories = grouped.getOrDefault(type, Collections.emptyMap());

            for (Map.Entry<String, List<Transaction>> entry : categories.entrySet()) {
                String categoryName = entry.getKey();
                List<Transaction> txns = entry.getValue();

                Map<String, List<Transaction>> byItem = txns.stream()
                        .collect(Collectors.groupingBy(Transaction::getDescription));

                for (Map.Entry<String, List<Transaction>> itemEntry : byItem.entrySet()) {
                    String item = itemEntry.getKey();
                    List<Transaction> itemTxns = itemEntry.getValue();

                    BigDecimal cm = sumByMonth(itemTxns, asOfDate.getMonthValue(), asOfDate.getYear());
                    BigDecimal pm = sumByMonth(itemTxns, asOfDate.minusMonths(1).getMonthValue(), asOfDate.minusMonths(1).getYear());
                    BigDecimal ytd = sumByYear(itemTxns, asOfDate.getYear());
                    BigDecimal budgetYtd = BigDecimal.ZERO;
                    BigDecimal fullBudget = BigDecimal.ZERO;
                    BigDecimal variance = ytd.subtract(budgetYtd);

                    IncomeExpenseReportRowDTO row = IncomeExpenseReportRowDTO.builder()
                            .type(type)
                            .category(categoryName)
                            .description(item)
                            .currentMonth(cm)
                            .previousMonth(pm)
                            .yearToDate(ytd)
                            .budgetYtd(budgetYtd)
                            .variance(variance)
                            .fullYearBudget(fullBudget)
                            .build();

                    rows.add(row);
                }
            }
        }

        return rows;
    }

    private BigDecimal sumByMonth(List<Transaction> txns, int month, int year) {
        return txns.stream()
                .filter(txn -> txn.getTransactionDate().getMonthValue() == month && txn.getTransactionDate().getYear() == year)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumByYear(List<Transaction> txns, int year) {
        return txns.stream()
                .filter(txn -> txn.getTransactionDate().getYear() == year)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
