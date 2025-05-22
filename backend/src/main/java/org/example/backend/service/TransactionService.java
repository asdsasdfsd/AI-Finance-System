// src/main/java/org/example/backend/service/TransactionService.java
package org.example.backend.service;

import org.example.backend.model.Company;
import org.example.backend.model.Department;
import org.example.backend.model.Transaction;
import org.example.backend.model.User;
import org.example.backend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    public Transaction findById(Integer id) {
        return transactionRepository.findById(id).orElse(null);
    }

    public List<Transaction> findByCompany(Company company) {
        return transactionRepository.findByCompany(company);
    }

    public List<Transaction> findByCompanySortedByDate(Company company) {
        return transactionRepository.findByCompanyOrderByTransactionDateDesc(company);
    }

    public List<Transaction> findByCompanyAndType(Company company, Transaction.TransactionType type) {
        return transactionRepository.findByCompanyAndTransactionType(company, type);
    }
    
    public List<Transaction> findByUserAndType(User user, Transaction.TransactionType type) {
        return transactionRepository.findByUserAndTransactionType(user, type);
    }
    
    public List<Transaction> findByDepartmentAndType(Department department, Transaction.TransactionType type) {
        return transactionRepository.findByDepartmentAndTransactionType(department, type);
    }
    
    public List<Transaction> findByDateRange(Company company, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByCompanyAndTransactionDateBetween(company, startDate, endDate);
    }
    
    public Double getSum(Company company, Transaction.TransactionType type) {
        return transactionRepository.sumAmountByCompanyAndType(company, type);
    }

    public Transaction save(Transaction transaction) {
        if (transaction.getCreatedAt() == null) {
            transaction.setCreatedAt(LocalDateTime.now());
        }
        transaction.setUpdatedAt(LocalDateTime.now());
        return transactionRepository.save(transaction);
    }

    public void deleteById(Integer id) {
        transactionRepository.deleteById(id);
    }
}