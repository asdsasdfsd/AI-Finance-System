// backend/src/main/java/org/example/backend/repository/FundRepository.java
package org.example.backend.repository;

import org.example.backend.model.Company;
import org.example.backend.model.Fund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface FundRepository extends JpaRepository<Fund, Integer> {
    
    // ========== Existing Methods ==========
    
    List<Fund> findByCompanyCompanyId(Integer companyId);
    List<Fund> findByCompany(Company company);
    List<Fund> findByCompanyAndIsActive(Company company, Boolean isActive);

    // ========== FIXED: Added Missing Methods ==========
    
    /**
     * FIXED: Find funds by ID set and company ID - This method was missing
     * Used by FinancialGroupingDataService for fund lookups
     */
    @Query("SELECT f FROM Fund f WHERE f.company.companyId = :companyId AND f.fundId IN :fundIds")
    List<Fund> findByIdInAndCompanyId(@Param("fundIds") Set<Integer> fundIds, 
                                     @Param("companyId") Integer companyId);
    
    /**
     * Alternative method using company ID and fund IDs
     */
    @Query("SELECT f FROM Fund f WHERE f.company.companyId = :companyId AND f.fundId IN :ids")
    List<Fund> findByCompanyIdAndFundIdIn(@Param("companyId") Integer companyId, 
                                         @Param("ids") Set<Integer> ids);
    
    /**
     * Find active funds by company ID
     */
    @Query("SELECT f FROM Fund f WHERE f.company.companyId = :companyId AND f.isActive = true")
    List<Fund> findActiveByCompanyId(@Param("companyId") Integer companyId);
    
    /**
     * Find funds by type and company ID
     */
    @Query("SELECT f FROM Fund f WHERE f.company.companyId = :companyId AND f.fundType = :fundType")
    List<Fund> findByCompanyIdAndFundType(@Param("companyId") Integer companyId, 
                                         @Param("fundType") String fundType);
    
    /**
     * Find funds with positive balance
     */
    @Query("SELECT f FROM Fund f WHERE f.company.companyId = :companyId AND f.balance > 0")
    List<Fund> findFundsWithPositiveBalanceByCompanyId(@Param("companyId") Integer companyId);
    
    /**
     * Count funds by company
     */
    @Query("SELECT COUNT(f) FROM Fund f WHERE f.company.companyId = :companyId")
    long countByCompanyId(@Param("companyId") Integer companyId);
    
    /**
     * Check if fund exists in company
     */
    @Query("SELECT COUNT(f) > 0 FROM Fund f WHERE f.company.companyId = :companyId AND f.fundId = :fundId")
    boolean existsByCompanyIdAndFundId(@Param("companyId") Integer companyId, 
                                      @Param("fundId") Integer fundId);
    
    /**
     * Calculate total fund balance for company
     */
    @Query("SELECT COALESCE(SUM(f.balance), 0) FROM Fund f WHERE f.company.companyId = :companyId AND f.isActive = true")
    java.math.BigDecimal calculateTotalBalanceByCompanyId(@Param("companyId") Integer companyId);
}