// src/main/java/org/example/backend/service/AccountService.java
package org.example.backend.service;

import org.example.backend.model.Account;
import org.example.backend.model.Company;
import org.example.backend.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public Account findById(Integer id) {
        return accountRepository.findById(id).orElse(null);
    }
    
    public List<Account> findByCompany(Company company) {
        return accountRepository.findByCompany(company);
    }
    
    public List<Account> findByCompanyAndType(Company company, Account.AccountType type) {
        return accountRepository.findByCompanyAndAccountType(company, type);
    }
    
    public List<Account> findSubaccounts(Account parentAccount) {
        return accountRepository.findByParentAccount(parentAccount);
    }

    public Account save(Account account) {
        if (account.getCreatedAt() == null) {
            account.setCreatedAt(LocalDateTime.now());
        }
        account.setUpdatedAt(LocalDateTime.now());
        return accountRepository.save(account);
    }

    public void deleteById(Integer id) {
        accountRepository.deleteById(id);
    }
}