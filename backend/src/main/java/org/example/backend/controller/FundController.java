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

    // 获取所有基金
    @GetMapping
    public List<Fund> getAll() {
        return fundService.findAll();
    }

    // 根据 ID 获取基金
    @GetMapping("/{id}")
    public ResponseEntity<Fund> getById(@PathVariable Integer id) {
        Fund fund = fundService.findById(id);
        return fund != null ? ResponseEntity.ok(fund) : ResponseEntity.notFound().build();
    }

    // 获取公司所有基金
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<Fund>> getByCompany(@PathVariable Integer companyId) {
        Company company = companyService.findById(companyId);
        if (company == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(fundService.findByCompany(company));
    }

    // 获取公司启用基金
    @GetMapping("/company/{companyId}/active")
    public ResponseEntity<List<Fund>> getActiveByCompany(@PathVariable Integer companyId) {
        Company company = companyService.findById(companyId);
        if (company == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(fundService.findActiveByCompany(company));
    }

    // 创建基金
    @PostMapping
    public ResponseEntity<Fund> create(@RequestBody Fund fund) {
        if (fund.getCompany() == null || fund.getCompany().getCompanyId() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(fundService.save(fund));
    }

    // 更新基金
    @PutMapping("/{id}")
    public ResponseEntity<Fund> update(@PathVariable Integer id, @RequestBody Fund fund) {
        Fund existing = fundService.findById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        fund.setFundId(id);
        return ResponseEntity.ok(fundService.save(fund));
    }

    // 删除基金
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (fundService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        fundService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
