// backend/src/main/java/org/example/backend/tenant/TenantAwareRepository.java
package org.example.backend.tenant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

/**
 * 租户感知的Repository基接口
 * 
 * 提供自动添加租户过滤的查询方法
 */
@NoRepositoryBean
public interface TenantAwareRepository<T, ID> extends JpaRepository<T, ID> {
    
    /**
     * 按租户查找所有记录
     */
    List<T> findAllByTenant();
    
    /**
     * 按租户和ID查找记录
     */
    Optional<T> findByIdAndTenant(ID id);
    
    /**
     * 按租户删除记录
     */
    void deleteByIdAndTenant(ID id);
    
    /**
     * 统计租户内的记录数
     */
    long countByTenant();
}
