package org.example.backend.application.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.backend.application.dto.AccountBalanceDTO;
import org.example.backend.application.dto.BalanceSheetDetailedResponse;
import org.example.backend.model.Company;
import org.example.backend.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BalanceSheetExportService {

    private final BalanceSheetService balanceSheetService;
    private final CompanyRepository companyRepository;

    public byte[] exportToExcel(Integer companyId, LocalDate asOfDate) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid companyId: " + companyId));

        BalanceSheetDetailedResponse sheet = balanceSheetService.generateBalanceSheet(company, asOfDate);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet1 = workbook.createSheet("Balance Sheet");
            int rowNum = 0;

            Row title = sheet1.createRow(rowNum++);
            title.createCell(0).setCellValue("Balance Sheet as of " + asOfDate);

            rowNum++; // 空行

            rowNum = appendSection(sheet1, rowNum, "Assets", sheet.getAssets());
            rowNum = appendSection(sheet1, rowNum, "Liabilities", sheet.getLiabilities());
            rowNum = appendSection(sheet1, rowNum, "Equity", sheet.getEquity());

            rowNum++;
            Row finalRow = sheet1.createRow(rowNum++);
            finalRow.createCell(0).setCellValue("IS BALANCED:");
            finalRow.createCell(1).setCellValue(sheet.isBalanced() ? "YES" : "NO");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to export balance sheet", e);
        }
    }

private int appendSection(Sheet sheet, int rowNum, String title, Map<String, List<AccountBalanceDTO>> section) {
    // 分类总标题（如 ASSETS、LIABILITIES）
    Row header = sheet.createRow(rowNum++);
    header.createCell(0).setCellValue(title.toUpperCase());

    for (Map.Entry<String, List<AccountBalanceDTO>> entry : section.entrySet()) {
        // 子分类标题（如 Operating Income）
        Row catRow = sheet.createRow(rowNum++);
        catRow.createCell(0).setCellValue(entry.getKey());

        //  添加列标题行
        Row columnHeader = sheet.createRow(rowNum++);
        columnHeader.createCell(0).setCellValue("Account");
        columnHeader.createCell(1).setCellValue("Current Month");
        columnHeader.createCell(2).setCellValue("Previous Month");
        columnHeader.createCell(3).setCellValue("Last Year End");

        // 填入每一行账户数据
        for (AccountBalanceDTO dto : entry.getValue()) {
            Row r = sheet.createRow(rowNum++);
            r.createCell(0).setCellValue(dto.getAccountName());
            r.createCell(1).setCellValue(dto.getCurrentMonth().doubleValue());
            r.createCell(2).setCellValue(dto.getPreviousMonth().doubleValue());
            r.createCell(3).setCellValue(dto.getLastYearEnd().doubleValue());
        }

        rowNum++; // 子分类之间空一行
    }

    return rowNum;
}

}
