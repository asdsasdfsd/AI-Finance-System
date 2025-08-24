// backend/src/main/java/org/example/backend/controller/FundController.java
package org.example.backend.controller;

import org.example.backend.model.Company;
import org.example.backend.model.Fund;
import org.example.backend.service.CompanyService;
import org.example.backend.service.FundService;
import org.example.backend.util.JwtContextUtil;
import org.example.backend.exception.MissingCompanyIdException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/api/funds")
@CrossOrigin(origins = "http://localhost:3000")
public class FundController {

    @Autowired
    private FundService fundService;

    @Autowired
    private CompanyService companyService;
    
    @Autowired
    private JwtContextUtil jwtContextUtil;

    // Get all funds for current user's company (from JWT)
    @GetMapping
    public ResponseEntity<List<Fund>> getAll() {
        Integer companyId = getCurrentCompanyId();
        Company company = companyService.findById(companyId);
        if (company == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(fundService.findByCompany(company));
    }

    // Get fund by ID (with company validation)
    @GetMapping("/{id}")
    public ResponseEntity<Fund> getById(@PathVariable Integer id) {
        Integer companyId = getCurrentCompanyId();
        Fund fund = fundService.findById(id);
        
        if (fund == null || !fund.getCompany().getCompanyId().equals(companyId)) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(fund);
    }

    // Get all funds for current user's company
    @GetMapping("/company")
    public ResponseEntity<List<Fund>> getByCompany() {
        Integer companyId = getCurrentCompanyId();
        Company company = companyService.findById(companyId);
        if (company == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(fundService.findByCompany(company));
    }

    // Backward compatibility - allow companyId parameter but validate against JWT
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<Fund>> getByCompanyId(@PathVariable Integer companyId) {
        Integer jwtCompanyId = getCurrentCompanyId();
        
        // Security check: ensure requested company matches user's company
        if (!jwtCompanyId.equals(companyId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        Company company = companyService.findById(companyId);
        if (company == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(fundService.findByCompany(company));
    }

    // Get active funds for current user's company
    @GetMapping("/active")
    public ResponseEntity<List<Fund>> getActive() {
        Integer companyId = getCurrentCompanyId();
        Company company = companyService.findById(companyId);
        if (company == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(fundService.findActiveByCompany(company));
    }

    // Backward compatibility for active funds
    @GetMapping("/company/{companyId}/active")
    public ResponseEntity<List<Fund>> getActiveByCompanyId(@PathVariable Integer companyId) {
        Integer jwtCompanyId = getCurrentCompanyId();
        
        // Security check: ensure requested company matches user's company
        if (!jwtCompanyId.equals(companyId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        Company company = companyService.findById(companyId);
        if (company == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(fundService.findActiveByCompany(company));
    }

    // Create fund (auto-assign to current user's company)
    @PostMapping
    public ResponseEntity<Fund> create(@RequestBody Fund fund) {
        Integer companyId = getCurrentCompanyId();
        Company company = companyService.findById(companyId);
        
        if (company == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // Auto-assign company from JWT
        fund.setCompany(company);
        
        return ResponseEntity.ok(fundService.save(fund));
    }

    // Update fund (with company validation)
    @PutMapping("/{id}")
    public ResponseEntity<Fund> update(@PathVariable Integer id, @RequestBody Fund fund) {
        Integer companyId = getCurrentCompanyId();
        Fund existing = fundService.findById(id);
        
        if (existing == null || !existing.getCompany().getCompanyId().equals(companyId)) {
            return ResponseEntity.notFound().build();
        }
        
        fund.setFundId(id);
        fund.setCompany(existing.getCompany()); // Preserve original company
        
        return ResponseEntity.ok(fundService.save(fund));
    }

    // Delete fund (with company validation)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        Integer companyId = getCurrentCompanyId();
        Fund existing = fundService.findById(id);
        
        if (existing == null || !existing.getCompany().getCompanyId().equals(companyId)) {
            return ResponseEntity.notFound().build();
        }
        
        fundService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Helper method to get company ID from JWT with proper error handling
    private Integer getCurrentCompanyId() {
        Integer companyId = jwtContextUtil.getCurrentCompanyId();
        if (companyId == null) {
            throw new MissingCompanyIdException(
                "Company ID not found in JWT token. Please ensure you are properly authenticated."
            );
        }
        return companyId;
    }
}