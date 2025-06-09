package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.application.dto.BalanceSheetDetailedResponse;
import org.example.backend.application.service.BalanceSheetExportService;
import org.example.backend.application.service.BalanceSheetService;
import org.example.backend.model.Company;
import org.example.backend.repository.CompanyRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/balance-sheet")
@RequiredArgsConstructor
public class BalanceSheetController {

    private final BalanceSheetService balanceSheetService;
    private final CompanyRepository companyRepository;
    private final BalanceSheetExportService exportService;
    
    @GetMapping("/text")
    public ResponseEntity<String> getBalanceSheetAsText(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate
    ) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        BalanceSheetDetailedResponse response = balanceSheetService.generateBalanceSheet(company, asOfDate);
        String result = balanceSheetService.renderFullBalanceSheet(response);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/json")
    public ResponseEntity<BalanceSheetDetailedResponse> getBalanceSheetAsJson(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate
    ) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        BalanceSheetDetailedResponse response = balanceSheetService.generateBalanceSheet(company, asOfDate);
        return ResponseEntity.ok(response);
    }



    @GetMapping("/export")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {

        byte[] excelData = exportService.exportToExcel(companyId, asOfDate);

        String filename = "BalanceSheet_" + asOfDate + ".xlsx";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());

        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }
}
