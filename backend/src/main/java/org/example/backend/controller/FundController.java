// src/main/java/org/example/backend/controller/FundController.java
package org.example.backend.controller;

import org.example.backend.model.Company;
import org.example.backend.model.Fund;
import org.example.backend.service.CompanyService;
import org.example.backend.service.FundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/funds")
public class FundController {
    @Autowired
    private FundService fundService;
    
    @Autowired
    private CompanyService companyService;

    @GetMapping
    public List<Fund> getAll() {
        return fundService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fund> getById(@PathVariable Integer id) {
        Fund fund = fundService.findById(id);
        return fund != null ? ResponseEntity.ok(fund) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/company/{companyId}")
    public List<Fund> getByCompany(@PathVariable Integer companyId) {
        Company company = companyService.findById(companyId);
        return fundService.findByCompany(company);
    }
    
    @GetMapping("/company/{companyId}/active")
    public List<Fund> getActiveByCompany(@PathVariable Integer companyId) {
        Company company = companyService.findById(companyId);
        return fundService.findActiveByCompany(company);
    }

    @PostMapping
    public Fund create(@RequestBody Fund fund) {
        return fundService.save(fund);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Fund> update(@PathVariable Integer id, @RequestBody Fund fund) {
        if (fundService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        fund.setFundId(id);
        return ResponseEntity.ok(fundService.save(fund));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (fundService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        fundService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}