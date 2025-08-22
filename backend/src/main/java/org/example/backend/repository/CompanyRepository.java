// backend/src/main/java/org/example/backend/repository/CompanyRepository.java
package org.example.backend.repository;

import org.example.backend.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 传统Company Repository - 与DDD CompanyAggregateRepository共存
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {
    Optional<Company> findByCompanyName(String companyName);
    Optional<Company> findByEmail(String email);
    boolean existsByCompanyName(String companyName);
    boolean existsByEmail(String email);

    /**
     * Find company by ID with all details
     */
    @Query("SELECT c FROM Company c WHERE c.companyId = :companyId AND c.status = 'ACTIVE'")
    Optional<Company> findActiveCompanyById(@Param("companyId") Integer companyId);
    
    /**
     * Check if company exists and is active
     */
    @Query("SELECT COUNT(c) > 0 FROM Company c WHERE c.companyId = :companyId AND c.status = 'ACTIVE'")
    boolean existsActiveCompanyById(@Param("companyId") Integer companyId);
}

