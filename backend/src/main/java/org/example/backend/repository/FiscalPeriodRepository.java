// src/main/java/org/example/backend/repository/FiscalPeriodRepository.java
package org.example.backend.repository;

import org.example.backend.model.Company;
import org.example.backend.model.FiscalPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FiscalPeriodRepository extends JpaRepository<FiscalPeriod, Integer> {
    List<FiscalPeriod> findByCompany(Company company);
    List<FiscalPeriod> findByCompanyAndStatus(Company company, FiscalPeriod.PeriodStatus status);
    FiscalPeriod findByCompanyAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Company company, LocalDate date, LocalDate sameDate);
}