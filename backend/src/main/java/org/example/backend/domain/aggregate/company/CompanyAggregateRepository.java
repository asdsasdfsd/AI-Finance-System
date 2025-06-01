// backend/src/main/java/org/example/backend/domain/aggregate/company/repository/CompanyAggregateRepository.java
package org.example.backend.domain.aggregate.company;

import org.example.backend.domain.aggregate.company.CompanyAggregate;
import org.example.backend.domain.valueobject.CompanyStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Company Aggregate Repository - DDD compliant
 * 
 * 注意：这个Repository接口需要放在正确的包结构中
 */
@Repository
@Profile("ddd")
public interface CompanyAggregateRepository extends JpaRepository<CompanyAggregate, Integer> {
    
    // ========== Tenant Discovery ==========
    
    /**
     * Find company by email domain for SSO auto-provisioning
     */
    @Query("SELECT c FROM CompanyAggregate c WHERE c.email LIKE CONCAT('%@', :domain) OR c.website LIKE CONCAT('%', :domain)")
    Optional<CompanyAggregate> findByEmailDomain(@Param("domain") String domain);
    
    /**
     * Find company by website domain as fallback
     */
    @Query("SELECT c FROM CompanyAggregate c WHERE c.website LIKE CONCAT('%', :domain)")
    Optional<CompanyAggregate> findByWebsiteDomain(@Param("domain") String domain);
    
    /**
     * Find company by exact email match
     */
    Optional<CompanyAggregate> findByEmail(String email);
    
    // ========== Business Validation ==========
    
    /**
     * Check if company name already exists
     */
    boolean existsByCompanyName(String companyName);
    
    /**
     * Check if email is already in use
     */
    boolean existsByEmail(String email);
    
    // ========== Status-based Queries ==========
    
    /**
     * Find companies by status
     */
    @Query("SELECT c FROM CompanyAggregate c WHERE c.companyStatus.status = :status")
    List<CompanyAggregate> findByCompanyStatus_Status(@Param("status") CompanyStatus.Status status);
    
    /**
     * Find active companies only
     */
    @Query("SELECT c FROM CompanyAggregate c WHERE c.companyStatus.status = 'ACTIVE'")
    List<CompanyAggregate> findActiveCompanies();
    
    // ========== Subscription Management ==========
    
    /**
     * Find companies with expired subscriptions
     */
    @Query("SELECT c FROM CompanyAggregate c WHERE c.subscriptionExpiresAt < :now AND c.companyStatus.status = 'ACTIVE'")
    List<CompanyAggregate> findWithExpiredSubscriptions(@Param("now") LocalDateTime now);
    
    /**
     * Find companies with subscriptions expiring soon
     */
    @Query("SELECT c FROM CompanyAggregate c WHERE c.subscriptionExpiresAt BETWEEN :now AND :warningDate AND c.companyStatus.status = 'ACTIVE'")
    List<CompanyAggregate> findWithExpiringSubscriptions(@Param("now") LocalDateTime now, 
                                                        @Param("warningDate") LocalDateTime warningDate);
    
    // ========== Search and Discovery ==========
    
    /**
     * Search companies by name (case-insensitive, partial match)
     */
    @Query("SELECT c FROM CompanyAggregate c WHERE LOWER(c.companyName) LIKE LOWER(CONCAT('%', :name, '%')) AND c.companyStatus.status = 'ACTIVE'")
    List<CompanyAggregate> searchByNameContaining(@Param("name") String name);
    
    // ========== Statistics ==========
    
    /**
     * Count companies by status
     */
    @Query("SELECT COUNT(c) FROM CompanyAggregate c WHERE c.companyStatus.status = :status")
    long countByStatus(@Param("status") CompanyStatus.Status status);
    
    /**
     * Count active companies
     */
    @Query("SELECT COUNT(c) FROM CompanyAggregate c WHERE c.companyStatus.status = 'ACTIVE'")
    long countActiveCompanies();
}