// backend/src/main/java/org/example/backend/domain/aggregate/fixedasset/FixedAssetAggregateRepository.java
package org.example.backend.domain.aggregate.fixedasset;

import org.example.backend.domain.valueobject.TenantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Fixed Asset Aggregate Repository
 */
@Repository
public interface FixedAssetAggregateRepository extends JpaRepository<FixedAssetAggregate, Integer> {
    
    /**
     * Find fixed asset by ID and tenant
     */
    @Query("SELECT f FROM FixedAssetAggregate f WHERE f.assetId = :assetId AND f.tenantId = :tenantId")
    Optional<FixedAssetAggregate> findByIdAndTenant(@Param("assetId") Integer assetId, 
                                                   @Param("tenantId") TenantId tenantId);
    
    /**
     * Find fixed assets by tenant
     */
    List<FixedAssetAggregate> findByTenantIdOrderByNameAsc(TenantId tenantId);
    
    /**
     * Find fixed assets by tenant and department
     */
    List<FixedAssetAggregate> findByTenantIdAndDepartmentId(TenantId tenantId, Integer departmentId);
    
    /**
     * Find fixed assets by tenant and status
     */
    @Query("SELECT f FROM FixedAssetAggregate f WHERE f.tenantId = :tenantId AND f.status = :status")
    List<FixedAssetAggregate> findByTenantIdAndStatus(@Param("tenantId") TenantId tenantId, 
                                                     @Param("status") String status);
    
    /**
     * Sum current value by tenant
     */
    @Query("SELECT COALESCE(SUM(f.currentValue.amount), 0) FROM FixedAssetAggregate f " +
           "WHERE f.tenantId = :tenantId AND f.status = 'ACTIVE'")
    BigDecimal sumCurrentValueByTenant(@Param("tenantId") TenantId tenantId);
    
    /**
     * Count assets by tenant and status
     */
    @Query("SELECT COUNT(f) FROM FixedAssetAggregate f WHERE f.tenantId = :tenantId AND f.status = :status")
    long countByTenantIdAndStatus(@Param("tenantId") TenantId tenantId, @Param("status") String status);
    
    /**
     * Find fixed assets by tenant (alias method)
     */
    default List<FixedAssetAggregate> findByTenantId(TenantId tenantId) {
        return findByTenantIdOrderByNameAsc(tenantId);
    }
}