// src/main/java/org/example/backend/repository/CategoryRepository.java
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

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
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
}

