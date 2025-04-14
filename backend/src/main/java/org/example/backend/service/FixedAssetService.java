// src/main/java/org/example/backend/service/FixedAssetService.java
package org.example.backend.service;

import org.example.backend.model.Company;
import org.example.backend.model.Department;
import org.example.backend.model.FixedAsset;
import org.example.backend.repository.FixedAssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FixedAssetService {
    @Autowired
    private FixedAssetRepository fixedAssetRepository;

    public List<FixedAsset> findAll() {
        return fixedAssetRepository.findAll();
    }

    public FixedAsset findById(Integer id) {
        return fixedAssetRepository.findById(id).orElse(null);
    }
    
    public List<FixedAsset> findByCompany(Company company) {
        return fixedAssetRepository.findByCompany(company);
    }
    
    public List<FixedAsset> findByDepartment(Department department) {
        return fixedAssetRepository.findByDepartment(department);
    }
    
    public List<FixedAsset> findByStatus(FixedAsset.AssetStatus status) {
        return fixedAssetRepository.findByStatus(status);
    }

    public FixedAsset save(FixedAsset fixedAsset) {
        if (fixedAsset.getCreatedAt() == null) {
            fixedAsset.setCreatedAt(LocalDateTime.now());
        }
        fixedAsset.setUpdatedAt(LocalDateTime.now());
        return fixedAssetRepository.save(fixedAsset);
    }

    public void deleteById(Integer id) {
        fixedAssetRepository.deleteById(id);
    }
}