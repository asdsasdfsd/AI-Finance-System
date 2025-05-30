package org.example.backend.service;

import org.example.backend.model.Company;
import org.example.backend.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyService {
    @Autowired
    private CompanyRepository companyRepository;

    public List<Company> findAll() { return companyRepository.findAll(); }
    public Company findById(Integer id) { return companyRepository.findById(id).orElse(null); }
    public Company save(Company company) { return companyRepository.save(company); }
    public void deleteById(Integer id) { companyRepository.deleteById(id); }
}

