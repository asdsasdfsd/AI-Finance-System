package org.example.backend.application.service;

import lombok.RequiredArgsConstructor;

import org.example.backend.application.dto.AccountBalanceDTO;
import org.example.backend.application.dto.BalanceSheetDetailedResponse;
import org.example.backend.model.Account;
import org.example.backend.model.Category;
import org.example.backend.model.Company;

import org.example.backend.repository.CategoryRepository;
import org.example.backend.repository.TransactionRepository;

import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BalanceSheetService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    

        public BalanceSheetDetailedResponse generateBalanceSheet(Company company, LocalDate asOfDate) {
        Map<String, List<AccountBalanceDTO>> assets = new LinkedHashMap<>();
        Map<String, List<AccountBalanceDTO>> liabilities = new LinkedHashMap<>();
        Map<String, List<AccountBalanceDTO>> equity = new LinkedHashMap<>();

        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        BigDecimal totalEquity = BigDecimal.ZERO;

        List<Category> categories = categoryRepository.findByCompanyAndParentCategoryIsNull(company);

        for (Category category : categories) {
            if (category.getAccount() == null || category.getAccount().getAccountType() == null) continue;

            Account account = category.getAccount();
            Account.AccountType type = account.getAccountType();
            String categoryName = category.getName();
            Integer categoryId = category.getCategoryId();

            BigDecimal current = transactionRepository.sumByCompanyAndCategoriesBeforeDate(
                    company.getCompanyId(), List.of(categoryId), asOfDate);
            BigDecimal previous = transactionRepository.sumByCompanyAndCategoriesBeforeDate(
                    company.getCompanyId(), List.of(categoryId), asOfDate.minusMonths(1));
            BigDecimal lastYear = transactionRepository.sumByCompanyAndCategoriesBeforeDate(
                    company.getCompanyId(), List.of(categoryId), asOfDate.withDayOfYear(1).minusDays(1));

            AccountBalanceDTO dto = AccountBalanceDTO.builder()
                    .accountName(account.getName())
                    .currentMonth(current != null ? current : BigDecimal.ZERO)
                    .previousMonth(previous != null ? previous : BigDecimal.ZERO)
                    .lastYearEnd(lastYear != null ? lastYear : BigDecimal.ZERO)
                    .build();

            switch (type) {
                case ASSET -> {
                    assets.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(dto);
                    totalAssets = totalAssets.add(dto.getCurrentMonth());
                }
                case LIABILITY -> {
                    liabilities.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(dto);
                    totalLiabilities = totalLiabilities.add(dto.getCurrentMonth());
                }
                case EQUITY -> {
                    equity.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(dto);
                    totalEquity = totalEquity.add(dto.getCurrentMonth());
                }
            }
        }

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

    public String renderSection(String sectionName, Map<String, List<AccountBalanceDTO>> sectionMap, BigDecimal total) {
        StringBuilder sb = new StringBuilder();
        sb.append(sectionName.toUpperCase()).append("\n");

        for (Map.Entry<String, List<AccountBalanceDTO>> entry : sectionMap.entrySet()) {
            sb.append(entry.getKey()).append("\n");

            for (AccountBalanceDTO dto : entry.getValue()) {
                sb.append(String.format("  %-30s %12.2f %12.2f %12.2f\n",
                        dto.getAccountName(),
                        dto.getCurrentMonth(),
                        dto.getPreviousMonth(),
                        dto.getLastYearEnd()));
            }
        }

        sb.append(String.format("TOTAL %s: %,12.2f\n", sectionName.toUpperCase(), total));
        return sb.toString();
    }

    public String renderFullBalanceSheet(BalanceSheetDetailedResponse response) {
        StringBuilder full = new StringBuilder();
        full.append("BALANCE SHEET AS AT ").append(response.getAsOfDate()).append("\n\n");

        full.append(renderSection("Assets", response.getAssets(), response.getTotalAssets())).append("\n");
        full.append(renderSection("Liabilities", response.getLiabilities(), response.getTotalLiabilities())).append("\n");
        full.append(renderSection("Equity", response.getEquity(), response.getTotalEquity())).append("\n");

        full.append("IS BALANCED: ").append(response.isBalanced()).append("\n");

        return full.toString();
    }

}
