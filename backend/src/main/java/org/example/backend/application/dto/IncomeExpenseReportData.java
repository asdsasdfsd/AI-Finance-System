// backend/src/main/java/org/example/backend/application/dto/IncomeExpenseReportData.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Income Expense Report Data DTO
 */
@Data
@Builder
public class IncomeExpenseReportData {
    private String companyName;
    private LocalDate asOfDate;
    private List<IncomeExpenseReportRowDTO> incomeRows;
    private List<IncomeExpenseReportRowDTO> expenseRows;
    private BigDecimal totalIncomeYTD;
    private BigDecimal totalExpenseYTD;
    private BigDecimal netIncomeYTD;
    private BigDecimal totalIncomeMonth;
    private BigDecimal totalExpenseMonth;
    private BigDecimal netIncomeMonth;
}