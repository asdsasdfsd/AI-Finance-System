// src/main/java/org/example/backend/controller/FiscalPeriodController.java
package org.example.backend.controller;

import org.example.backend.model.Company;
import org.example.backend.model.FiscalPeriod;
import org.example.backend.service.CompanyService;
import org.example.backend.service.FiscalPeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/fiscal-periods")
public class FiscalPeriodController {
    @Autowired
    private FiscalPeriodService fiscalPeriodService;
    
    @Autowired
    private CompanyService companyService;

    @GetMapping
    public List<FiscalPeriod> getAll() {
        return fiscalPeriodService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FiscalPeriod> getById(@PathVariable Integer id) {
        FiscalPeriod fiscalPeriod = fiscalPeriodService.findById(id);
        return fiscalPeriod != null ? ResponseEntity.ok(fiscalPeriod) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/company/{companyId}")
    public List<FiscalPeriod> getByCompany(@PathVariable Integer companyId) {
        Company company = companyService.findById(companyId);
        return fiscalPeriodService.findByCompany(company);
    }
    
    @GetMapping("/company/{companyId}/open")
    public List<FiscalPeriod> getOpenPeriods(@PathVariable Integer companyId) {
        Company company = companyService.findById(companyId);
        return fiscalPeriodService.findOpenPeriods(company);
    }
    
    @GetMapping("/company/{companyId}/for-date")
    public ResponseEntity<FiscalPeriod> getPeriodForDate(
            @PathVariable Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Company company = companyService.findById(companyId);
        FiscalPeriod period = fiscalPeriodService.findPeriodForDate(company, date);
        return period != null ? ResponseEntity.ok(period) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public FiscalPeriod create(@RequestBody FiscalPeriod fiscalPeriod) {
        return fiscalPeriodService.save(fiscalPeriod);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<FiscalPeriod> update(@PathVariable Integer id, @RequestBody FiscalPeriod fiscalPeriod) {
        if (fiscalPeriodService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        fiscalPeriod.setPeriodId(id);
        return ResponseEntity.ok(fiscalPeriodService.save(fiscalPeriod));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (fiscalPeriodService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        fiscalPeriodService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}