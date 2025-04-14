// src/main/java/org/example/backend/repository/AccountRepository.java
package org.example.backend.repository;

import org.example.backend.model.Account;
import org.example.backend.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    List<Account> findByCompany(Company company);
    List<Account> findByCompanyAndAccountType(Company company, Account.AccountType accountType);
    List<Account> findByParentAccount(Account parentAccount);
}