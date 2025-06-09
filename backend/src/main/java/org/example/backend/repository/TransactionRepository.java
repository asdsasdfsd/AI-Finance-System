// backend/src/main/java/org/example/backend/repository/TransactionRepository.java
package org.example.backend.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.example.backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 传统Transaction Repository - 与DDD TransactionAggregateRepository共存
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    // 基本CRUD方法即可，复杂业务使用DDD的Repository

    @Query("""
    SELECT SUM(t.amount) FROM Transaction t
    WHERE t.company.companyId = :companyId
      AND t.category.categoryId IN :categoryIds
      AND t.transactionDate <= :asOfDate
""")
BigDecimal sumByCompanyAndCategoriesBeforeDate(@Param("companyId") Integer companyId,
                                               @Param("categoryIds") List<Integer> categoryIds,
                                               @Param("asOfDate") LocalDate asOfDate);

    @Query("SELECT t FROM Transaction t WHERE t.company.companyId = :companyId")
    List<Transaction> findAllByCompanyId(@Param("companyId") Integer companyId);


}