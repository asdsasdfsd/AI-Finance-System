package org.example.backend.controller;

import org.example.backend.model.Company;
import org.example.backend.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {
    @Autowired
    private CompanyService companyService;

    @GetMapping
    public List<Company> getAll() { 
        return companyService.findAll(); 
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getById(@PathVariable Integer id) {
        Company company = companyService.findById(id);
        return company != null ? ResponseEntity.ok(company) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public Company create(@RequestBody Company company) { 
        return companyService.save(company); 
    }

    @PutMapping("/{id}")
    public ResponseEntity<Company> update(@PathVariable Integer id, @RequestBody Company company) {
        Company existingCompany = companyService.findById(id);
        if (existingCompany == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 设置ID和更新时间
        company.setCompanyId(id);
        company.setUpdatedAt(LocalDateTime.now());
        
        // 保持创建时间不变
        company.setCreatedAt(existingCompany.getCreatedAt());
        
        Company updatedCompany = companyService.save(company);
        return ResponseEntity.ok(updatedCompany);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        Company existingCompany = companyService.findById(id);
        if (existingCompany == null) {
            return ResponseEntity.notFound().build();
        }
        companyService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}