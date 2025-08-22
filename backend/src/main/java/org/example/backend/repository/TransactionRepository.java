// backend/src/main/java/org/example/backend/repository/TransactionRepository.java
package org.example.backend.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.example.backend.model.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 传统Transaction Repository - 与DDD TransactionAggregateRepository共存S
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

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


    List<Transaction> findByCompany_CompanyIdOrderByTransactionDateDesc(Integer companyId, Pageable pageable);

    @Query("""
        SELECT t FROM Transaction t 
        WHERE t.company.companyId = :companyId 
          AND t.transactionDate BETWEEN :start AND :end
    """)
    List<Transaction> findByCompanyIdAndDateRange(
            @Param("companyId") Integer companyId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    /**
     * Find transactions by company and date range - FIXED for JPA relations
     */
    @Query("SELECT t FROM Transaction t WHERE t.company.companyId = :companyId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findByCompanyIdAndTransactionDateBetween(
            @Param("companyId") Integer companyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * Find transactions by company, category and date range - FIXED for JPA relations
     */
    @Query("SELECT t FROM Transaction t WHERE t.company.companyId = :companyId " +
           "AND t.category.categoryId = :categoryId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findByCompanyIdAndCategoryIdAndTransactionDateBetween(
            @Param("companyId") Integer companyId,
            @Param("categoryId") Integer categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * Find transactions by company, department and date range - FIXED for JPA relations
     */
    @Query("SELECT t FROM Transaction t WHERE t.company.companyId = :companyId " +
           "AND t.department.departmentId = :departmentId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findByCompanyIdAndDepartmentIdAndTransactionDateBetween(
            @Param("companyId") Integer companyId,
            @Param("departmentId") Integer departmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * Find transactions by company and transaction type - FIXED for JPA relations
     */
    @Query("SELECT t FROM Transaction t WHERE t.company.companyId = :companyId " +
           "AND t.transactionType = :transactionType " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findByCompanyIdAndTransactionTypeAndTransactionDateBetween(
            @Param("companyId") Integer companyId,
            @Param("transactionType") org.example.backend.model.Transaction.TransactionType transactionType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * Alternative: Find all transactions by company - simpler approach
     */
    @Query("SELECT t FROM Transaction t WHERE t.company.companyId = :companyId ORDER BY t.transactionDate DESC")
    List<Transaction> findByCompanyId(@Param("companyId") Integer companyId);

    /**
     * Alternative: Find all transactions by company within date range - simpler approach  
     */
    @Query("SELECT t FROM Transaction t WHERE t.company.companyId = :companyId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findTransactionsByCompanyAndDateRange(
            @Param("companyId") Integer companyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}


