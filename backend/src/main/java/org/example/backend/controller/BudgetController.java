// src/main/java/org/example/backend/controller/BudgetController.java
package org.example.backend.controller;

import org.example.backend.model.Budget;
import org.example.backend.model.BudgetLine;
import org.example.backend.model.Company;
import org.example.backend.model.Department;
import org.example.backend.service.BudgetService;
import org.example.backend.service.CompanyService;
import org.example.backend.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {
    @Autowired
    private BudgetService budgetService;
    
    @Autowired
    private CompanyService companyService;
    
    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public List<Budget> getAll() {
        return budgetService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Budget> getById(@PathVariable Integer id) {
        Budget budget = budgetService.findById(id);
        return budget != null ? ResponseEntity.ok(budget) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/company/{companyId}")
    public List<Budget> getByCompany(@PathVariable Integer companyId) {
        Company company = companyService.findById(companyId);
        return budgetService.findByCompany(company);
    }
    
    @GetMapping("/department/{departmentId}")
    public List<Budget> getByDepartment(@PathVariable Integer departmentId) {
        Department department = departmentService.findById(departmentId);
        return budgetService.findByDepartment(department);
    }

    @PostMapping
    public Budget create(@RequestBody Budget budget) {
        return budgetService.save(budget);
    }
    
    @PostMapping("/with-lines")
    public Budget createWithLines(@RequestBody Map<String, Object> payload) {
        Budget budget = new Budget();
        // Map payload fields to budget
        
        List<BudgetLine> lines = null;
        // Extract lines from payload
        
        return budgetService.saveWithLines(budget, lines);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Budget> update(@PathVariable Integer id, @RequestBody Budget budget) {
        if (budgetService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        budget.setBudgetId(id);
        return ResponseEntity.ok(budgetService.save(budget));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (budgetService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        budgetService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}