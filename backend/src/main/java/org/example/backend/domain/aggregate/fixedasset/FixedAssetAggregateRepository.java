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
 * Fixed Asset Aggregate Repository - 修复版本
 */
@Repository
public interface FixedAssetAggregateRepository extends JpaRepository<FixedAssetAggregate, Integer> {
    
    /**
     * Find fixed asset by ID and tenant
     */
    @Query("SELECT f FROM FixedAssetAggregate f WHERE f.assetId = :assetId AND f.companyId = :companyId")
    Optional<FixedAssetAggregate> findByIdAndTenant(@Param("assetId") Integer assetId, 
                                                   @Param("companyId") Integer companyId);
    
    /**
     * Find fixed assets by tenant
     */
    List<FixedAssetAggregate> findByCompanyIdOrderByNameAsc(Integer companyId);
    
    /**
     * Find fixed assets by tenant and department
     */
    List<FixedAssetAggregate> findByCompanyIdAndDepartmentId(Integer companyId, Integer departmentId);
    
    /**
     * Find fixed assets by tenant and status
     */
    @Query("SELECT f FROM FixedAssetAggregate f WHERE f.companyId = :companyId AND f.status = :status")
    List<FixedAssetAggregate> findByCompanyIdAndStatus(@Param("companyId") Integer companyId, 
                                                      @Param("status") String status);
    
    /**
     * Sum current value by tenant - 修复版本
     */
    @Query("SELECT COALESCE(SUM(f.currentValue), 0) FROM FixedAssetAggregate f " +
           "WHERE f.companyId = :companyId AND f.status = 'ACTIVE'")
    BigDecimal sumCurrentValueByTenant(@Param("companyId") Integer companyId);
    
    /**
     * Count assets by tenant and status
     */
    @Query("SELECT COUNT(f) FROM FixedAssetAggregate f WHERE f.companyId = :companyId AND f.status = :status")
    long countByTenantIdAndStatus(@Param("companyId") Integer companyId, @Param("status") String status);
    
    /**
     * Find fixed assets by tenant (alias method)
     */
    default List<FixedAssetAggregate> findByTenantId(TenantId tenantId) {
        return findByCompanyIdOrderByNameAsc(tenantId.getValue());
    }
    
    /**
     * Additional convenience method for TenantId compatibility
     */
    default Optional<FixedAssetAggregate> findByIdAndTenant(Integer assetId, TenantId tenantId) {
        return findByIdAndTenant(assetId, tenantId.getValue());
    }
    
    default List<FixedAssetAggregate> findByTenantIdAndDepartmentId(TenantId tenantId, Integer departmentId) {
        return findByCompanyIdAndDepartmentId(tenantId.getValue(), departmentId);
    }
    
    default List<FixedAssetAggregate> findByTenantIdAndStatus(TenantId tenantId, String status) {
        return findByCompanyIdAndStatus(tenantId.getValue(), status);
    }
    
    default BigDecimal sumCurrentValueByTenant(TenantId tenantId) {
        return sumCurrentValueByTenant(tenantId.getValue());
    }
}