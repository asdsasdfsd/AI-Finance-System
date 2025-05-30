// src/main/java/org/example/backend/repository/FundRepository.java
package org.example.backend.repository;

import org.example.backend.model.Company;
import org.example.backend.model.Fund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FundRepository extends JpaRepository<Fund, Integer> {
    List<Fund> findByCompanyCompanyId(Integer companyId);
    List<Fund> findByCompany(Company company);
    List<Fund> findByCompanyAndIsActive(Company company, Boolean isActive);
}