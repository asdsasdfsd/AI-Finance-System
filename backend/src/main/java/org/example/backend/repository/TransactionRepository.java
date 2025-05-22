// src/main/java/org/example/backend/repository/TransactionRepository.java
package org.example.backend.repository;

import org.example.backend.model.Company;
import org.example.backend.model.Department;
import org.example.backend.model.Transaction;
import org.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findByCompanyAndTransactionType(Company company, Transaction.TransactionType type);
    List<Transaction> findByUserAndTransactionType(User user, Transaction.TransactionType type);
    List<Transaction> findByDepartmentAndTransactionType(Department department, Transaction.TransactionType type);

    List<Transaction> findByCompany(Company company);

    List<Transaction> findByCompanyAndTransactionDateBetween(Company company, LocalDate startDate, LocalDate endDate);
    List<Transaction> findByCompanyOrderByTransactionDateDesc(Company company);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.company = ?1 AND t.transactionType = ?2")
    Double sumAmountByCompanyAndType(Company company, Transaction.TransactionType type);
}