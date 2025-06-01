// backend/src/main/java/org/example/backend/service/CompanyService.java
package org.example.backend.service;

import org.example.backend.model.Company;
import org.example.backend.repository.CompanyRepository;
import org.example.backend.application.service.CompanyApplicationService;
import org.example.backend.application.dto.CompanyDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 简单Company Service - 为传统Controller提供兼容性
 * 
 * 注意：这是为了兼容现有传统Controller而保留的简单Service
 * 新业务逻辑应该使用CompanyApplicationService
 */
@Service
public class CompanyService {
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private CompanyApplicationService companyApplicationService;

    public List<Company> findAll() {
        return companyRepository.findAll();
    }

    public Company findById(Integer id) {
        return companyRepository.findById(id).orElse(null);
    }

    public Company save(Company company) {
        if (company.getCreatedAt() == null) {
            company.setCreatedAt(LocalDateTime.now());
        }
        company.setUpdatedAt(LocalDateTime.now());
        return companyRepository.save(company);
    }

    public void deleteById(Integer id) {
        companyRepository.deleteById(id);
    }
    
    /**
     * 将DDD的CompanyDTO转换为传统Company实体
     * 用于兼容传统Controller
     */
    public Company convertFromDTO(CompanyDTO dto) {
        if (dto == null) return null;
        
        Company company = new Company();
        company.setCompanyId(dto.getCompanyId());
        company.setCompanyName(dto.getCompanyName());
        company.setAddress(dto.getAddress());
        company.setCity(dto.getCity());
        company.setStateProvince(dto.getStateProvince());
        company.setPostalCode(dto.getPostalCode());
        company.setEmail(dto.getEmail());
        company.setWebsite(dto.getWebsite());
        company.setRegistrationNumber(dto.getRegistrationNumber());
        company.setTaxId(dto.getTaxId());
        company.setFiscalYearStart(dto.getFiscalYearStart());
        company.setDefaultCurrency(dto.getDefaultCurrency());
        company.setStatus(dto.getStatus().name());
        company.setCreatedAt(dto.getCreatedAt());
        company.setUpdatedAt(dto.getUpdatedAt());
        
        return company;
    }
}