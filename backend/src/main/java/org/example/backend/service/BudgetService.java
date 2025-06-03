// src/main/java/org/example/backend/service/BudgetService.java
package org.example.backend.service;

import org.example.backend.model.Budget;
import org.example.backend.model.BudgetLine;
import org.example.backend.model.Company;
import org.example.backend.model.Department;
import org.example.backend.repository.BudgetLineRepository;
import org.example.backend.repository.BudgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BudgetService {
    @Autowired
    private BudgetRepository budgetRepository;
    
    @Autowired
    private BudgetLineRepository budgetLineRepository;

    public List<Budget> findAll() {
        return budgetRepository.findAll();
    }

    public Budget findById(Integer id) {
        return budgetRepository.findById(id).orElse(null);
    }
    
    public List<Budget> findByCompany(Company company) {
        return budgetRepository.findByCompany(company);
    }
    
    public List<Budget> findByDepartment(Department department) {
        return budgetRepository.findByDepartment(department);
    }

    @Transactional
    public Budget save(Budget budget) {
        if (budget.getCreatedAt() == null) {
            budget.setCreatedAt(LocalDateTime.now());
        }
        budget.setUpdatedAt(LocalDateTime.now());
        return budgetRepository.save(budget);
    }
    
    @Transactional
    public Budget saveWithLines(Budget budget, List<BudgetLine> lines) {
        Budget savedBudget = save(budget);
        
        if (lines != null) {
            lines.forEach(line -> {
                line.setBudget(savedBudget);
                budgetLineRepository.save(line);
            });
        }
        
        return savedBudget;
    }

    @Transactional
    public void deleteById(Integer id) {
        budgetRepository.deleteById(id);
    }
}