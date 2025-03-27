// backend/src/main/java/org/example/backend/repository/CompanyRepository.java
package org.example.backend.repository;

import java.util.Optional;

import org.example.backend.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {
    
    /**
     * Find a company by email domain (e.g., "example.com")
     * This is used for SSO auto-provisioning of users
     */
    @Query("SELECT c FROM Company c WHERE c.email LIKE %:domain")
    Optional<Company> findByEmailDomain(@Param("domain") String domain);
    
    /**
     * Check if a company exists with the given name
     */
    boolean existsByCompanyName(String companyName);
}