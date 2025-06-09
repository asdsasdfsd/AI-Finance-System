package org.example.backend.application.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.backend.application.dto.IncomeExpenseReportRowDTO;
import org.example.backend.model.Company;
import org.example.backend.model.Transaction;
import org.example.backend.repository.CompanyRepository;
import org.example.backend.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialReportExportService {

    private final TransactionRepository transactionRepository;
    private final CompanyRepository companyRepository;

    public byte[] exportIncomeExpenseReport(Integer companyId, LocalDate asOfDate) {
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

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Income vs Expense Report");
            int rowNum = 0;

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setBold(true);
            titleStyle.setFont(titleFont);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.RIGHT);
            dataStyle.setBorderBottom(BorderStyle.DOTTED);
            dataStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Income vs Expense Report");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 7));

            Row header = sheet.createRow(rowNum++);
            String[] headers = {"Category", "Item", "Current Month", "Previous Month", "Year To-Date", "Budget YTD", "Variance", "Budget Full Year"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (IncomeExpenseReportRowDTO dto : rows) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(dto.getCategory());
                row.createCell(1).setCellValue(dto.getDescription());

                Cell cell2 = row.createCell(2);
                cell2.setCellValue(dto.getCurrentMonth().doubleValue());
                cell2.setCellStyle(dataStyle);

                Cell cell3 = row.createCell(3);
                cell3.setCellValue(dto.getPreviousMonth().doubleValue());
                cell3.setCellStyle(dataStyle);

                Cell cell4 = row.createCell(4);
                cell4.setCellValue(dto.getYearToDate().doubleValue());
                cell4.setCellStyle(dataStyle);

                Cell cell5 = row.createCell(5);
                cell5.setCellValue(dto.getBudgetYtd().doubleValue());
                cell5.setCellStyle(dataStyle);

                Cell cell6 = row.createCell(6);
                cell6.setCellValue(dto.getVariance().doubleValue());
                cell6.setCellStyle(dataStyle);

                Cell cell7 = row.createCell(7);
                cell7.setCellValue(dto.getFullYearBudget().doubleValue());
                cell7.setCellStyle(dataStyle);
            }

            for (int i = 0; i < 8; i++) sheet.autoSizeColumn(i);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to export income/expense report", e);
        }
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
