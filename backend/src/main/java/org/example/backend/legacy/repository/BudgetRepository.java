// src/main/java/org/example/backend/repository/BudgetRepository.java
package org.example.backend.repository;

import org.example.backend.model.Budget;
import org.example.backend.model.Company;
import org.example.backend.model.Department;
import org.example.backend.model.FiscalPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Integer> {
    List<Budget> findByCompany(Company company);
    List<Budget> findByDepartment(Department department);
    List<Budget> findByFiscalPeriod(FiscalPeriod fiscalPeriod);
}