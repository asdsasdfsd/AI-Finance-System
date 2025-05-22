// src/main/java/org/example/backend/controller/TransactionController.java
package org.example.backend.controller;

import org.example.backend.model.Company;
import org.example.backend.model.Department;
import org.example.backend.model.Transaction;
import org.example.backend.model.User;
import org.example.backend.service.CompanyService;
import org.example.backend.service.DepartmentService;
import org.example.backend.service.TransactionService;
import org.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor; 
import java.time.LocalDate;
import java.util.List;
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/transactions")

public class TransactionController {
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private CompanyService companyService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private DepartmentService departmentService; 

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping
    public List<Transaction> getAll() {
        return transactionService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getById(@PathVariable Integer id) {
        Transaction transaction = transactionService.findById(id);
        return transaction != null ? ResponseEntity.ok(transaction) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/company/{companyId}/type/{type}")
    public List<Transaction> getByCompanyAndType(
            @PathVariable Integer companyId,
            @PathVariable Transaction.TransactionType type) {
        Company company = companyService.findById(companyId);
        return transactionService.findByCompanyAndType(company, type);
    }
    
    @GetMapping("/user/{userId}/type/{type}")
    public List<Transaction> getByUserAndType(
            @PathVariable Integer userId,
            @PathVariable Transaction.TransactionType type) {
        User user = userService.findById(userId);
        return transactionService.findByUserAndType(user, type);
    }
    
    @GetMapping("/department/{departmentId}/type/{type}")
    public List<Transaction> getByDepartmentAndType(
            @PathVariable Integer departmentId,
            @PathVariable Transaction.TransactionType type) {
        Department department = departmentService.findById(departmentId);
        return transactionService.findByDepartmentAndType(department, type);
    }
    
    @GetMapping("/company/{companyId}/date-range")
    public List<Transaction> getByDateRange(
            @PathVariable Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Company company = companyService.findById(companyId);
        return transactionService.findByDateRange(company, startDate, endDate);
    }
    
    @GetMapping("/company/{companyId}/type/{type}/sum")
    public ResponseEntity<Double> getSumByCompanyAndType(
            @PathVariable Integer companyId,
            @PathVariable Transaction.TransactionType type) {
        Company company = companyService.findById(companyId);
        Double sum = transactionService.getSum(company, type);
        return ResponseEntity.ok(sum != null ? sum : 0.0);
    }

    @PostMapping
    public Transaction create(@RequestBody Transaction transaction) {
        return transactionService.save(transaction);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Transaction> update(@PathVariable Integer id, @RequestBody Transaction transaction) {
        if (transactionService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        transaction.setTransactionId(id);
        return ResponseEntity.ok(transactionService.save(transaction));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (transactionService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        transactionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/company/{companyId}")
    public List<Transaction> getByCompany(@PathVariable Integer companyId) {
        Company company = companyService.findById(companyId);
        return transactionService.findByCompany(company);
    }

    @GetMapping("/company/{companyId}/sorted")
    public List<Transaction> getByCompanySorted(@PathVariable Integer companyId) {
        Company company = companyService.findById(companyId);
        return transactionService.findByCompanySortedByDate(company);
    }


}