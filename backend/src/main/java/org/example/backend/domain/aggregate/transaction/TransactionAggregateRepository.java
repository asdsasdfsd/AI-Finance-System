// backend/src/main/java/org/example/backend/domain/aggregate/transaction/TransactionAggregateRepository.java
package org.example.backend.domain.aggregate.transaction;

import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Transaction Aggregate Repository - DDD compliant
 * 
 * Responsibilities:
 * 1. Provide aggregate-focused persistence operations
 * 2. Maintain aggregate boundaries
 * 3. Support domain queries for business operations
 * 4. Ensure transaction consistency
 */
@Repository
public interface TransactionAggregateRepository extends JpaRepository<TransactionAggregate, Integer> {
    
    // ========== Basic Queries ==========
    
    /**
     * Find transaction by ID within tenant boundary
     */
    @Query("SELECT t FROM TransactionAggregate t WHERE t.transactionId = :transactionId AND t.tenantId = :tenantId")
    Optional<TransactionAggregate> findByIdAndTenant(@Param("transactionId") Integer transactionId, 
                                                    @Param("tenantId") TenantId tenantId);
    
    /**
     * Find all transactions for a tenant
     */
    List<TransactionAggregate> findByTenantIdOrderByTransactionDateDesc(TenantId tenantId);
    
    /**
     * Find transactions by tenant and type
     */
    List<TransactionAggregate> findByTenantIdAndTransactionType(TenantId tenantId, 
                                                              TransactionAggregate.TransactionType transactionType);
    
    /**
     * Find transactions by tenant and status
     */
    List<TransactionAggregate> findByTenantIdAndTransactionStatus_Status(TenantId tenantId, 
                                                                        TransactionStatus.Status status);
    
    // ========== Date Range Queries ==========
    
    /**
     * Find transactions within date range for tenant
     */
    List<TransactionAggregate> findByTenantIdAndTransactionDateBetween(TenantId tenantId, 
                                                                     LocalDate startDate, 
                                                                     LocalDate endDate);
    
    /**
     * Find transactions by date range, type and status
     */
    @Query("SELECT t FROM TransactionAggregate t WHERE t.tenantId = :tenantId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "AND t.transactionType = :type " +
           "AND t.transactionStatus.status = :status")
    List<TransactionAggregate> findByDateRangeTypeAndStatus(@Param("tenantId") TenantId tenantId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate,
                                                          @Param("type") TransactionAggregate.TransactionType type,
                                                          @Param("status") TransactionStatus.Status status);
    
    // ========== User-specific Queries ==========
    
    /**
     * Find transactions by user within tenant
     */
    @Query("SELECT t FROM TransactionAggregate t WHERE t.tenantId = :tenantId AND t.userId = :userId " +
           "ORDER BY t.transactionDate DESC")
    List<TransactionAggregate> findByTenantAndUser(@Param("tenantId") TenantId tenantId, 
                                                 @Param("userId") Integer userId);
    
    /**
     * Find user's transactions by type
     */
    List<TransactionAggregate> findByTenantIdAndUserIdAndTransactionType(TenantId tenantId, 
                                                                        Integer userId,
                                                                        TransactionAggregate.TransactionType type);
    
    // ========== Department-specific Queries ==========
    
    /**
     * Find transactions by department
     */
    @Query("SELECT t FROM TransactionAggregate t WHERE t.tenantId = :tenantId AND t.departmentId = :departmentId")
    List<TransactionAggregate> findByTenantAndDepartment(@Param("tenantId") TenantId tenantId, 
                                                       @Param("departmentId") Integer departmentId);
    
    // ========== Business Analytics Queries ==========
    
    /**
     * Calculate total amount by tenant, type and status
     */
    @Query("SELECT COALESCE(SUM(t.money.amount), 0) FROM TransactionAggregate t " +
           "WHERE t.tenantId = :tenantId " +
           "AND t.transactionType = :type " +
           "AND t.transactionStatus.status = :status")
    BigDecimal sumAmountByTenantTypeAndStatus(@Param("tenantId") TenantId tenantId,
                                            @Param("type") TransactionAggregate.TransactionType type,
                                            @Param("status") TransactionStatus.Status status);
    
    /**
     * Calculate monthly statistics for tenant
     */
    @Query("SELECT YEAR(t.transactionDate), MONTH(t.transactionDate), " +
           "COUNT(t), SUM(t.money.amount) " +
           "FROM TransactionAggregate t " +
           "WHERE t.tenantId = :tenantId " +
           "AND t.transactionStatus.status = :status " +
           "GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate) " +
           "ORDER BY YEAR(t.transactionDate), MONTH(t.transactionDate)")
    List<Object[]> findMonthlyStatistics(@Param("tenantId") TenantId tenantId, 
                                       @Param("status") TransactionStatus.Status status);
    
    /**
     * Find transactions pending approval for tenant
     */
    @Query("SELECT t FROM TransactionAggregate t " +
           "WHERE t.tenantId = :tenantId " +
           "AND t.transactionStatus.status IN ('DRAFT', 'PENDING_APPROVAL') " +
           "ORDER BY t.createdAt ASC")
    List<TransactionAggregate> findPendingApprovalByTenant(@Param("tenantId") TenantId tenantId);
    
    /**
     * Find transactions by amount range
     */
    @Query("SELECT t FROM TransactionAggregate t " +
           "WHERE t.tenantId = :tenantId " +
           "AND t.money.amount BETWEEN :minAmount AND :maxAmount")
    List<TransactionAggregate> findByAmountRange(@Param("tenantId") TenantId tenantId,
                                               @Param("minAmount") BigDecimal minAmount,
                                               @Param("maxAmount") BigDecimal maxAmount);
    
    // ========== Category and Fund Queries ==========
    
    /**
     * Find transactions by category
     */
    @Query("SELECT t FROM TransactionAggregate t WHERE t.tenantId = :tenantId AND t.categoryId = :categoryId")
    List<TransactionAggregate> findByTenantAndCategory(@Param("tenantId") TenantId tenantId, 
                                                     @Param("categoryId") Integer categoryId);
    
    /**
     * Find transactions by fund
     */
    @Query("SELECT t FROM TransactionAggregate t WHERE t.tenantId = :tenantId AND t.fundId = :fundId")
    List<TransactionAggregate> findByTenantAndFund(@Param("tenantId") TenantId tenantId, 
                                                 @Param("fundId") Integer fundId);
    
    // ========== Audit and Compliance Queries ==========
    
    /**
     * Find transactions modified after specific date
     */
    @Query("SELECT t FROM TransactionAggregate t " +
           "WHERE t.tenantId = :tenantId " +
           "AND t.updatedAt > :since " +
           "ORDER BY t.updatedAt DESC")
    List<TransactionAggregate> findModifiedSince(@Param("tenantId") TenantId tenantId, 
                                               @Param("since") LocalDate since);
    
    /**
     * Check if user has any transactions in tenant
     */
    @Query("SELECT COUNT(t) > 0 FROM TransactionAggregate t " +
           "WHERE t.tenantId = :tenantId AND t.userId = :userId")
    boolean existsByTenantAndUser(@Param("tenantId") TenantId tenantId, 
                                @Param("userId") Integer userId);
    
    /**
     * Count transactions by status for tenant
     */
    @Query("SELECT COUNT(t) FROM TransactionAggregate t " +
           "WHERE t.tenantId = :tenantId " +
           "AND t.transactionStatus.status = :status")
    long countByTenantAndStatus(@Param("tenantId") TenantId tenantId, 
                              @Param("status") TransactionStatus.Status status);
}