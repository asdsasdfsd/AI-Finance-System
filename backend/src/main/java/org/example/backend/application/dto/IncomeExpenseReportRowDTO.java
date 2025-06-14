package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class IncomeExpenseReportRowDTO {
    private String type;    // "INCOME" or "EXPENSE"
    private String category;            // 分类（如 Service Income, Utilities）
    private String description;         // 描述（Item，如 Bank Transfer, Electricity Bill）

    private BigDecimal currentMonth;    // 本月金额
    private BigDecimal previousMonth;   // 上月金额
    private BigDecimal yearToDate;      // 年初至今金额

    private BigDecimal budgetYtd;       // 年初至今预算金额（可拓展）
    private BigDecimal variance;        // 差异 = YTD - 预算
    private BigDecimal fullYearBudget;  // 年度预算总额（可拓展）
    private BigDecimal variancePercentage;
}
