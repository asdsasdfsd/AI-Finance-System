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
 * Transaction Aggregate Repository - Native SQL Version
 * 
 * Using native SQL queries to avoid JPQL value object mapping issues
 */
@Repository
public interface TransactionAggregateRepository extends JpaRepository<TransactionAggregate, Integer> {
    
    // ========== Basic Queries using Native SQL ==========
    
    /**
     * Find transaction by ID within tenant boundary
     */
    @Query(value = "SELECT * FROM Transaction t WHERE t.transaction_id = :transactionId AND t.company_id = :companyId", 
           nativeQuery = true)
    Optional<TransactionAggregate> findByIdAndTenant(@Param("transactionId") Integer transactionId, 
                                                    @Param("companyId") Integer companyId);
    
    /**
     * Find all transactions for a tenant
     */
    @Query(value = "SELECT * FROM Transaction t WHERE t.company_id = :companyId ORDER BY t.transaction_date DESC", 
           nativeQuery = true)
    List<TransactionAggregate> findByTenantIdOrderByTransactionDateDesc(@Param("companyId") Integer companyId);
    
    /**
     * Find transactions by tenant and type
     */
    @Query(value = "SELECT * FROM Transaction t WHERE t.company_id = :companyId AND t.transaction_type = :type", 
           nativeQuery = true)
    List<TransactionAggregate> findByTenantIdAndTransactionType(@Param("companyId") Integer companyId, 
                                                              @Param("type") String transactionType);
    
    /**
     * Find transactions by tenant and status
     */
    @Query(value = "SELECT * FROM Transaction t WHERE t.company_id = :companyId AND t.status = :status", 
           nativeQuery = true)
    List<TransactionAggregate> findByTenantIdAndTransactionStatus_Status(@Param("companyId") Integer companyId, 
                                                                        @Param("status") String status);
    
    // ========== Date Range Queries ==========
    
    /**
     * Find transactions within date range for tenant
     */
    @Query(value = "SELECT * FROM Transaction t WHERE t.company_id = :companyId " +
                   "AND t.transaction_date BETWEEN :startDate AND :endDate", 
           nativeQuery = true)
    List<TransactionAggregate> findByTenantIdAndTransactionDateBetween(@Param("companyId") Integer companyId, 
                                                                     @Param("startDate") LocalDate startDate, 
                                                                     @Param("endDate") LocalDate endDate);

    /**
     * Find transactions by tenant, date range and status
     */
    @Query(value = "SELECT * FROM Transaction t WHERE t.company_id = :companyId " +
                   "AND t.transaction_date BETWEEN :startDate AND :endDate " +
                   "AND t.status = :status " +
                   "ORDER BY t.transaction_date", 
           nativeQuery = true)
    List<TransactionAggregate> findByTenantIdAndDateRangeAndStatus(
            @Param("companyId") Integer companyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") String status);

    /**
     * Find transactions by date range, type and status
     */
    @Query(value = "SELECT * FROM Transaction t WHERE t.company_id = :companyId " +
                   "AND t.transaction_date BETWEEN :startDate AND :endDate " +
                   "AND t.transaction_type = :type " +
                   "AND t.status = :status", 
           nativeQuery = true)
    List<TransactionAggregate> findByDateRangeTypeAndStatus(@Param("companyId") Integer companyId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate,
                                                          @Param("type") String type,
                                                          @Param("status") String status);
    
    // ========== Balance Sheet Specific Queries ==========

    /**
     * Find transactions by tenant, account and date range for balance calculations
     */
    @Query(value = "SELECT * FROM Transaction t WHERE t.company_id = :companyId " +
                   "AND t.category_id = :accountId " +
                   "AND t.transaction_date BETWEEN :startDate AND :endDate " +
                   "AND t.status = 'APPROVED' " +
                   "ORDER BY t.transaction_date", 
           nativeQuery = true)
    List<TransactionAggregate> findByTenantIdAndAccountIdAndDateRange(
            @Param("companyId") Integer companyId,
            @Param("accountId") Integer accountId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find transactions by tenant and account up to a specific date
     */
    @Query(value = "SELECT * FROM Transaction t WHERE t.company_id = :companyId " +
                   "AND t.category_id = :accountId " +
                   "AND t.transaction_date <= :asOfDate " +
                   "AND t.status = 'APPROVED' " +
                   "ORDER BY t.transaction_date", 
           nativeQuery = true)
    List<TransactionAggregate> findByTenantIdAndAccountIdUpToDate(
            @Param("companyId") Integer companyId,
            @Param("accountId") Integer accountId,
            @Param("asOfDate") LocalDate asOfDate);

    /**
     * Find transactions by tenant, type and date range
     */
    @Query(value = "SELECT * FROM Transaction t WHERE t.company_id = :companyId " +
                   "AND t.transaction_type = :type " +
                   "AND t.transaction_date BETWEEN :startDate AND :endDate " +
                   "AND t.status = 'APPROVED' " +
                   "ORDER BY t.transaction_date", 
           nativeQuery = true)
    List<TransactionAggregate> findByTenantIdAndTransactionTypeAndDateRange(
            @Param("companyId") Integer companyId,
            @Param("type") String type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find pending approval transactions by tenant
     */
    @Query(value = "SELECT * FROM Transaction t WHERE t.company_id = :companyId " +
                   "AND t.status = 'PENDING' " +
                   "ORDER BY t.created_at DESC", 
           nativeQuery = true)
    List<TransactionAggregate> findPendingApprovalByTenant(@Param("companyId") Integer companyId);

    // ========== Helper methods for TenantId conversion ==========
    
    /**
     * Helper method to work with TenantId - delegates to company ID version
     */
    default Optional<TransactionAggregate> findByIdAndTenant(Integer transactionId, TenantId tenantId) {
        return findByIdAndTenant(transactionId, tenantId.getValue());
    }
    
    default List<TransactionAggregate> findByTenantIdOrderByTransactionDateDesc(TenantId tenantId) {
        return findByTenantIdOrderByTransactionDateDesc(tenantId.getValue());
    }
    
    default List<TransactionAggregate> findByTenantIdAndTransactionType(TenantId tenantId, 
                                                                      TransactionAggregate.TransactionType transactionType) {
        return findByTenantIdAndTransactionType(tenantId.getValue(), transactionType.name());
    }
    
    default List<TransactionAggregate> findByTenantIdAndTransactionStatus_Status(TenantId tenantId, 
                                                                                TransactionStatus.Status status) {
        return findByTenantIdAndTransactionStatus_Status(tenantId.getValue(), status.name());
    }
    
    default List<TransactionAggregate> findByTenantIdAndTransactionDateBetween(TenantId tenantId, 
                                                                             LocalDate startDate, 
                                                                             LocalDate endDate) {
        return findByTenantIdAndTransactionDateBetween(tenantId.getValue(), startDate, endDate);
    }
    
    default List<TransactionAggregate> findByTenantIdAndDateRangeAndStatus(TenantId tenantId,
                                                                         LocalDate startDate,
                                                                         LocalDate endDate,
                                                                         TransactionStatus.Status status) {
        return findByTenantIdAndDateRangeAndStatus(tenantId.getValue(), startDate, endDate, status.name());
    }
    
    default List<TransactionAggregate> findByTenantIdAndAccountIdAndDateRange(TenantId tenantId,
                                                                            Integer accountId,
                                                                            LocalDate startDate,
                                                                            LocalDate endDate) {
        return findByTenantIdAndAccountIdAndDateRange(tenantId.getValue(), accountId, startDate, endDate);
    }
    
    default List<TransactionAggregate> findByTenantIdAndAccountIdUpToDate(TenantId tenantId,
                                                                         Integer accountId,
                                                                         LocalDate asOfDate) {
        return findByTenantIdAndAccountIdUpToDate(tenantId.getValue(), accountId, asOfDate);
    }
    
    default List<TransactionAggregate> findByTenantIdAndTransactionTypeAndDateRange(TenantId tenantId,
                                                                                  TransactionAggregate.TransactionType type,
                                                                                  LocalDate startDate,
                                                                                  LocalDate endDate) {
        return findByTenantIdAndTransactionTypeAndDateRange(tenantId.getValue(), type.name(), startDate, endDate);
    }
    
    default List<TransactionAggregate> findPendingApprovalByTenant(TenantId tenantId) {
        return findPendingApprovalByTenant(tenantId.getValue());
    }
    // ========== User-specific Queries ==========

       /**
        * Find transactions by user within tenant
        */
       @Query(value = "SELECT * FROM Transaction t WHERE t.company_id = :companyId AND t.user_id = :userId " +
                     "ORDER BY t.transaction_date DESC", 
              nativeQuery = true)
       List<TransactionAggregate> findByTenantAndUser(@Param("companyId") Integer companyId, 
                                                 @Param("userId") Integer userId);

       // ========== Business Analytics Queries ==========

       /**
        * Calculate total amount by tenant, type and status
        */
       @Query(value = "SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
                     "WHERE t.company_id = :companyId " +
                     "AND t.transaction_type = :type " +
                     "AND t.status = :status", 
              nativeQuery = true)
       BigDecimal sumAmountByTenantTypeAndStatus(@Param("companyId") Integer companyId,
                                          @Param("type") String type,
                                          @Param("status") String status);

       /**
        * Calculate total amount by tenant and status
        */
       @Query(value = "SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
                     "WHERE t.company_id = :companyId " +
                     "AND t.status = :status", 
              nativeQuery = true)
       BigDecimal sumAmountByTenantAndStatus(@Param("companyId") Integer companyId,
                                          @Param("status") String status);

       /**
        * Find transactions by category
        */
       @Query(value = "SELECT * FROM Transaction t WHERE t.company_id = :companyId AND t.category_id = :categoryId", 
              nativeQuery = true)
       List<TransactionAggregate> findByTenantAndCategory(@Param("companyId") Integer companyId, 
                                                        @Param("categoryId") Integer categoryId);

       /**
        * Find transactions by fund
        */
       @Query(value = "SELECT * FROM Transaction t WHERE t.company_id = :companyId AND t.fund_id = :fundId", 
              nativeQuery = true)
       List<TransactionAggregate> findByTenantAndFund(@Param("companyId") Integer companyId, 
                                                 @Param("fundId") Integer fundId);

       /**
        * Check if user has any transactions in tenant
        */
       @Query(value = "SELECT COUNT(*) > 0 FROM Transaction t WHERE t.company_id = :companyId AND t.user_id = :userId", 
              nativeQuery = true)
       boolean existsByTenantAndUser(@Param("companyId") Integer companyId, 
                                   @Param("userId") Integer userId);

       /**
        * Count transactions by status for tenant
        */
       @Query(value = "SELECT COUNT(*) FROM Transaction t WHERE t.company_id = :companyId AND t.status = :status", 
              nativeQuery = true)
       long countByTenantAndStatus(@Param("companyId") Integer companyId, 
                            @Param("status") String status);

       // ========== Additional Helper methods for TenantId conversion ==========

       /**
        * Helper method to work with TenantId - find by tenant and user
        */
       default List<TransactionAggregate> findByTenantAndUser(TenantId tenantId, Integer userId) {
       return findByTenantAndUser(tenantId.getValue(), userId);
       }

       /**
        * Helper method to work with TenantId - sum amount by tenant, type and status
        */
       default BigDecimal sumAmountByTenantTypeAndStatus(TenantId tenantId,
                                                 TransactionAggregate.TransactionType type,
                                                 TransactionStatus.Status status) {
       return sumAmountByTenantTypeAndStatus(tenantId.getValue(), type.name(), status.name());
       }

       /**
        * Helper method to work with TenantId - sum amount by tenant and status
        */
       default BigDecimal sumAmountByTenantAndStatus(TenantId tenantId, TransactionStatus.Status status) {
       return sumAmountByTenantAndStatus(tenantId.getValue(), status.name());
       }

       /**
        * Helper method to work with TenantId - find by tenant and category
        */
       default List<TransactionAggregate> findByTenantAndCategory(TenantId tenantId, Integer categoryId) {
       return findByTenantAndCategory(tenantId.getValue(), categoryId);
       }

       /**
        * Helper method to work with TenantId - find by tenant and fund
        */
       default List<TransactionAggregate> findByTenantAndFund(TenantId tenantId, Integer fundId) {
       return findByTenantAndFund(tenantId.getValue(), fundId);
       }

       /**
        * Helper method to work with TenantId - check if user has transactions
        */
       default boolean existsByTenantAndUser(TenantId tenantId, Integer userId) {
       return existsByTenantAndUser(tenantId.getValue(), userId);
       }

       /**
        * Helper method to work with TenantId - count by tenant and status
        */
       default long countByTenantAndStatus(TenantId tenantId, TransactionStatus.Status status) {
       return countByTenantAndStatus(tenantId.getValue(), status.name());
       }
}