// src/main/java/org/example/backend/repository/BudgetLineRepository.java
package org.example.backend.repository;

import org.example.backend.model.Account;
import org.example.backend.model.Budget;
import org.example.backend.model.BudgetLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetLineRepository extends JpaRepository<BudgetLine, Integer> {
    List<BudgetLine> findByBudget(Budget budget);
    List<BudgetLine> findByAccount(Account account);
}