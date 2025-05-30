// src/main/java/org/example/backend/service/FiscalPeriodService.java
package org.example.backend.service;

import org.example.backend.model.Company;
import org.example.backend.model.FiscalPeriod;
import org.example.backend.repository.FiscalPeriodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FiscalPeriodService {
    @Autowired
    private FiscalPeriodRepository fiscalPeriodRepository;

    public List<FiscalPeriod> findAll() {
        return fiscalPeriodRepository.findAll();
    }

    public FiscalPeriod findById(Integer id) {
        return fiscalPeriodRepository.findById(id).orElse(null);
    }
    
    public List<FiscalPeriod> findByCompany(Company company) {
        return fiscalPeriodRepository.findByCompany(company);
    }
    
    public List<FiscalPeriod> findOpenPeriods(Company company) {
        return fiscalPeriodRepository.findByCompanyAndStatus(company, FiscalPeriod.PeriodStatus.OPEN);
    }
    
    public FiscalPeriod findPeriodForDate(Company company, LocalDate date) {
        return fiscalPeriodRepository.findByCompanyAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                company, date, date);
    }

    public FiscalPeriod save(FiscalPeriod fiscalPeriod) {
        if (fiscalPeriod.getCreatedAt() == null) {
            fiscalPeriod.setCreatedAt(LocalDateTime.now());
        }
        fiscalPeriod.setUpdatedAt(LocalDateTime.now());
        return fiscalPeriodRepository.save(fiscalPeriod);
    }

    public void deleteById(Integer id) {
        fiscalPeriodRepository.deleteById(id);
    }
}