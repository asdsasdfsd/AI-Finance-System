// src/main/java/org/example/backend/service/FundService.java
package org.example.backend.service;

import org.example.backend.model.Company;
import org.example.backend.model.Fund;
import org.example.backend.repository.FundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FundService {
    @Autowired
    private FundRepository fundRepository;

    public List<Fund> findAll() {
        return fundRepository.findAll();
    }

    public Fund findById(Integer id) {
        return fundRepository.findById(id).orElse(null);
    }
    
    public List<Fund> findByCompany(Company company) {
        return fundRepository.findByCompany(company);
    }

    public List<Fund> findByCompanyId(Integer companyId) {
        return fundRepository.findByCompanyCompanyId(companyId);
    }

    public List<Fund> findActiveByCompany(Company company) {
        return fundRepository.findByCompanyAndIsActive(company, true);
    }

    public Fund save(Fund fund) {
        if (fund.getCreatedAt() == null) {
            fund.setCreatedAt(LocalDateTime.now());
        }
        fund.setUpdatedAt(LocalDateTime.now());
        return fundRepository.save(fund);
    }

    public void deleteById(Integer id) {
        fundRepository.deleteById(id);
    }
}