// backend/src/main/java/org/example/backend/repository/CategoryRepository.java
package org.example.backend.repository;

import org.example.backend.model.Account;
import org.example.backend.model.Category;
import org.example.backend.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    
    // ========== Existing Methods ==========
    
    List<Category> findByCompanyAndType(Company company, Category.CategoryType type);
    List<Category> findByCompanyAndParentCategoryIsNull(Company company);
    List<Category> findByParentCategory(Category parentCategory);
    
    @Query("""
        SELECT c FROM Category c
        WHERE c.company.companyId = :companyId
        AND c.account.accountType = :accountType
    """)
    List<Category> findByCompanyIdAndAccountType(@Param("companyId") Integer companyId,
                                                @Param("accountType") Account.AccountType accountType);

    /**
     * Find categories by company ID
     */
    List<Category> findByCompanyCompanyId(Integer companyId);

    /**
     * Find categories by company ID and statement section (type)
     */
    @Query("SELECT c FROM Category c WHERE c.company.companyId = :companyId AND c.type = :statementSection")
    List<Category> findByCompanyIdAndStatementSection(@Param("companyId") Integer companyId, 
                                                    @Param("statementSection") String statementSection);

    // ========== FIXED: Added Missing Methods ==========
    
    /**
     * FIXED: Find categories by ID set and company ID - This method was missing
     * Used by FinancialGroupingDataService for category lookups
     */
    @Query("SELECT c FROM Category c WHERE c.company.companyId = :companyId AND c.categoryId IN :categoryIds")
    List<Category> findByIdInAndCompanyId(@Param("categoryIds") Set<Integer> categoryIds, 
                                         @Param("companyId") Integer companyId);
    
    /**
     * Alternative method using company ID and category IDs
     */
    @Query("SELECT c FROM Category c WHERE c.company.companyId = :companyId AND c.categoryId IN :ids")
    List<Category> findByCompanyIdAndCategoryIdIn(@Param("companyId") Integer companyId, 
                                                 @Param("ids") Set<Integer> ids);
    
    /**
     * Find active categories by company ID
     */
    @Query("SELECT c FROM Category c WHERE c.company.companyId = :companyId AND c.isActive = true")
    List<Category> findActiveByCompanyId(@Param("companyId") Integer companyId);
    
    /**
     * Find categories by type and company ID
     */
    @Query("SELECT c FROM Category c WHERE c.company.companyId = :companyId AND c.type = :type")
    List<Category> findByCompanyIdAndType(@Param("companyId") Integer companyId, 
                                         @Param("type") Category.CategoryType type);
    
    /**
     * Count categories by company
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.company.companyId = :companyId")
    long countByCompanyId(@Param("companyId") Integer companyId);
    
    /**
     * Check if category exists in company
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.company.companyId = :companyId AND c.categoryId = :categoryId")
    boolean existsByCompanyIdAndCategoryId(@Param("companyId") Integer companyId, 
                                          @Param("categoryId") Integer categoryId);
}