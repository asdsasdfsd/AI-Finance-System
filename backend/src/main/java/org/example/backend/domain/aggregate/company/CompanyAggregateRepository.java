// backend/src/main/java/org/example/backend/domain/aggregate/company/CompanyAggregateRepository.java
package org.example.backend.domain.aggregate.company;

import org.example.backend.domain.valueobject.CompanyStatus;
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
 * Responsibilities:
 * 1. Manage company aggregate persistence
 * 2. Support tenant discovery and validation
 * 3. Handle company status and subscription queries
 * 4. Provide business-focused query methods
 */
@Repository
public interface CompanyAggregateRepository extends JpaRepository<CompanyAggregate, Integer> {
    
    // ========== Tenant Discovery ==========
    
    /**
     * Find company by email domain for SSO auto-provisioning
     * Supports finding companies by their email domain
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
    
    /**
     * Check if registration number is already in use
     */
    boolean existsByRegistrationNumber(String registrationNumber);
    
    // ========== Status-based Queries ==========
    
    /**
     * Find companies by status
     */
    List<CompanyAggregate> findByCompanyStatus_Status(CompanyStatus.Status status);
    
    /**
     * Find active companies only
     */
    @Query("SELECT c FROM CompanyAggregate c WHERE c.companyStatus.status = 'ACTIVE'")
    List<CompanyAggregate> findActiveCompanies();
    
    /**
     * Find companies with operational status (active)
     */
    @Query("SELECT c FROM CompanyAggregate c WHERE c.companyStatus.status = 'ACTIVE' ORDER BY c.companyName")
    List<CompanyAggregate> findOperationalCompanies();
    
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
    
    /**
     * Find companies without subscription expiration (lifetime/unlimited)
     */
    @Query("SELECT c FROM CompanyAggregate c WHERE c.subscriptionExpiresAt IS NULL AND c.companyStatus.status = 'ACTIVE'")
    List<CompanyAggregate> findWithUnlimitedSubscriptions();
    
    // ========== User Limit Management ==========
    
    /**
     * Find companies by user limit range
     */
    @Query("SELECT c FROM CompanyAggregate c WHERE c.maxUsers BETWEEN :minUsers AND :maxUsers")
    List<CompanyAggregate> findByUserLimitRange(@Param("minUsers") Integer minUsers, 
                                              @Param("maxUsers") Integer maxUsers);
    
    /**
     * Find companies without user limits
     */
    @Query("SELECT c FROM CompanyAggregate c WHERE c.maxUsers IS NULL")
    List<CompanyAggregate> findWithUnlimitedUsers();
    
    // ========== Geographic Queries ==========
    
    /**
     * Find companies by city
     */
    List<CompanyAggregate> findByCityIgnoreCase(String city);
    
    /**
     * Find companies by state/province
     */
    List<CompanyAggregate> findByStateProvinceIgnoreCase(String stateProvince);
    
    /**
     * Find companies by country (derived from postal code pattern)
     */
    @Query("SELECT c FROM CompanyAggregate c WHERE c.postalCode LIKE :pattern")
    List<CompanyAggregate> findByPostalCodePattern(@Param("pattern") String pattern);
    
    // ========== Financial Configuration ==========
    
    /**
     * Find companies by default currency
     */
    List<CompanyAggregate> findByDefaultCurrency(String currency);
    
    /**
     * Find companies by fiscal year start
     */
    List<CompanyAggregate> findByFiscalYearStart(String fiscalYearStart);
    
    // ========== Audit and Reporting ==========
    
    /**
     * Find companies created within date range
     */
    @Query("SELECT c FROM CompanyAggregate c WHERE c.createdAt BETWEEN :startDate AND :endDate ORDER BY c.createdAt DESC")
    List<CompanyAggregate> findCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find companies created by specific user
     */
    List<CompanyAggregate> findByCreatedBy(Integer createdBy);
    
    /**
     * Find recently updated companies
     */
    @Query("SELECT c FROM CompanyAggregate c WHERE c.updatedAt > :since ORDER BY c.updatedAt DESC")
    List<CompanyAggregate> findUpdatedSince(@Param("since") LocalDateTime since);
    
    // ========== Statistics and Analytics ==========
    
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
    
    /**
     * Get company creation statistics by month
     */
    @Query("SELECT YEAR(c.createdAt), MONTH(c.createdAt), COUNT(c) " +
           "FROM CompanyAggregate c " +
           "WHERE c.createdAt >= :startDate " +
           "GROUP BY YEAR(c.createdAt), MONTH(c.createdAt) " +
           "ORDER BY YEAR(c.createdAt), MONTH(c.createdAt)")
    List<Object[]> getMonthlyCreationStats(@Param("startDate") LocalDateTime startDate);
    
    // ========== Search and Discovery ==========
    
    /**
     * Search companies by name (case-insensitive, partial match)
     */
    @Query("SELECT c FROM CompanyAggregate c WHERE LOWER(c.companyName) LIKE LOWER(CONCAT('%', :name, '%')) AND c.companyStatus.status = 'ACTIVE'")
    List<CompanyAggregate> searchByNameContaining(@Param("name") String name);
    
    /**
     * Find companies matching multiple criteria
     */
    @Query("SELECT c FROM CompanyAggregate c WHERE " +
           "(:name IS NULL OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:city IS NULL OR LOWER(c.city) = LOWER(:city)) AND " +
           "(:status IS NULL OR c.companyStatus.status = :status)")
    List<CompanyAggregate> findByMultipleCriteria(@Param("name") String name,
                                                @Param("city") String city,
                                                @Param("status") CompanyStatus.Status status);
}