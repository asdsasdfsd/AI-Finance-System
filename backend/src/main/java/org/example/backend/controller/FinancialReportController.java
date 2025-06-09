package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.application.service.FinancialReportExportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.example.backend.application.dto.IncomeExpenseReportRowDTO;
import org.example.backend.application.service.FinancialReportJsonService;


import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/financial-report")
@RequiredArgsConstructor
public class FinancialReportController {

    private final FinancialReportExportService exportService;
    private final FinancialReportJsonService financialReportJsonService;

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportFinancialReport(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {

        byte[] excel = exportService.exportIncomeExpenseReport(companyId, asOfDate);
        String filename = "Financial_Report_" + asOfDate + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType
                .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());

        return new ResponseEntity<>(excel, headers, HttpStatus.OK);
    }

    @GetMapping("/json")
    public ResponseEntity<List<IncomeExpenseReportRowDTO>> getIncomeExpenseReport(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate
    ) {
        List<IncomeExpenseReportRowDTO> rows = financialReportJsonService.getIncomeExpenseReport(companyId, asOfDate);
        return ResponseEntity.ok(rows);
    }
    
}

