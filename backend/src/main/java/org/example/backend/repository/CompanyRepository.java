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
     * This method improves domain matching to find companies by their email domain
     */
    @Query("SELECT c FROM Company c WHERE c.email LIKE CONCAT('%', :domain) OR c.email LIKE CONCAT('%@', :domain)")
    Optional<Company> findByEmailDomain(@Param("domain") String domain);
    
    /**
     * Find a company by website domain
     * Alternative method to find company when email matching fails
     */
    @Query("SELECT c FROM Company c WHERE c.website LIKE CONCAT('%', :domain)")
    Optional<Company> findByWebsiteDomain(@Param("domain") String domain);
    
    /**
     * Check if a company exists with the given name
     */
    boolean existsByCompanyName(String companyName);
}