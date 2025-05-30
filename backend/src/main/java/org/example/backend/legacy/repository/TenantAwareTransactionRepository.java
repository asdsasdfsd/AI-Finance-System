// backend/src/main/java/org/example/backend/repository/TenantAwareTransactionRepository.java
package org.example.backend.repository;

import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.tenant.TenantAwareRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 租户感知的Transaction Repository
 */
@Repository
public interface TenantAwareTransactionRepository extends TenantAwareRepository<TransactionAggregate, Integer> {
    
    /**
     * 查找当前租户的所有交易
     */
    @Query("SELECT t FROM Transaction t WHERE t.companyId = :#{T(org.example.backend.tenant.TenantContext).currentTenant}")
    List<TransactionAggregate> findAllByTenant();
    
    /**
     * 按ID和租户查找交易
     */
    @Query("SELECT t FROM Transaction t WHERE t.transactionId = :id AND t.companyId = :#{T(org.example.backend.tenant.TenantContext).currentTenant}")
    Optional<TransactionAggregate> findByIdAndTenant(@Param("id") Integer id);
    
    /**
     * 按租户和类型查找交易
     */
    @Query("SELECT t FROM Transaction t WHERE t.companyId = :#{T(org.example.backend.tenant.TenantContext).currentTenant} AND t.transactionType = :type")
    List<TransactionAggregate> findByTenantAndType(@Param("type") TransactionAggregate.TransactionType type);
    
    /**
     * 按租户统计交易数量
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.companyId = :#{T(org.example.backend.tenant.TenantContext).currentTenant}")
    long countByTenant();
    
    /**
     * 按租户和日期范围查询
     */
    @Query("SELECT t FROM Transaction t WHERE t.companyId = :#{T(org.example.backend.tenant.TenantContext).currentTenant} " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate")
    List<TransactionAggregate> findByTenantAndDateRange(@Param("startDate") LocalDate startDate, 
                                             @Param("endDate") LocalDate endDate);
    
    /**
     * 删除指定ID和租户的交易
     */
    @Query("DELETE FROM Transaction t WHERE t.transactionId = :id AND t.companyId = :#{T(org.example.backend.tenant.TenantContext).currentTenant}")
    void deleteByIdAndTenant(@Param("id") Integer id);
}
