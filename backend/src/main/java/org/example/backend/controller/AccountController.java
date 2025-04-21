// src/main/java/org/example/backend/controller/AccountController.java
package org.example.backend.controller;

import org.example.backend.model.Account;
import org.example.backend.model.Company;
import org.example.backend.service.AccountService;
import org.example.backend.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    @Autowired
    private AccountService accountService;
    
    @Autowired
    private CompanyService companyService;

    @GetMapping
    public List<Account> getAll() {
        return accountService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getById(@PathVariable Integer id) {
        Account account = accountService.findById(id);
        return account != null ? ResponseEntity.ok(account) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/company/{companyId}")
    public List<Account> getByCompany(@PathVariable Integer companyId) {
        Company company = companyService.findById(companyId);
        return accountService.findByCompany(company);
    }
    
    @GetMapping("/company/{companyId}/type/{type}")
    public List<Account> getByCompanyAndType(
            @PathVariable Integer companyId, 
            @PathVariable Account.AccountType type) {
        Company company = companyService.findById(companyId);
        return accountService.findByCompanyAndType(company, type);
    }
    
    @GetMapping("/{id}/subaccounts")
    public List<Account> getSubaccounts(@PathVariable Integer id) {
        Account parentAccount = accountService.findById(id);
        return accountService.findSubaccounts(parentAccount);
    }

    @PostMapping
    public Account create(@RequestBody Account account) {
        return accountService.save(account);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Account> update(@PathVariable Integer id, @RequestBody Account account) {
        if (accountService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        account.setAccountId(id);
        return ResponseEntity.ok(accountService.save(account));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (accountService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        accountService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}