// backend/src/main/java/org/example/backend/repository/TenantAwareUserRepository.java
package org.example.backend.repository;

import org.example.backend.domain.aggregate.user.User;
import org.example.backend.tenant.TenantAwareRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 租户感知的User Repository
 */
@Repository
public interface TenantAwareUserRepository extends TenantAwareRepository<User, Integer> {
    
    /**
     * 查找当前租户的所有用户
     */
    @Query("SELECT u FROM User u WHERE u.tenantId.value = :#{T(org.example.backend.tenant.TenantContext).currentTenant}")
    List<User> findAllByTenant();
    
    /**
     * 按ID和租户查找用户
     */
    @Query("SELECT u FROM User u WHERE u.userId = :id AND u.tenantId.value = :#{T(org.example.backend.tenant.TenantContext).currentTenant}")
    Optional<User> findByIdAndTenant(@Param("id") Integer id);
    
    /**
     * 按用户名和租户查找用户（租户内用户名唯一）
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.tenantId.value = :#{T(org.example.backend.tenant.TenantContext).currentTenant}")
    Optional<User> findByUsernameAndTenant(@Param("username") String username);
    
    /**
     * 检查用户名在租户内是否已存在
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.tenantId.value = :#{T(org.example.backend.tenant.TenantContext).currentTenant}")
    boolean existsByUsernameAndTenant(@Param("username") String username);
    
    /**
     * 统计租户内的用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantId.value = :#{T(org.example.backend.tenant.TenantContext).currentTenant}")
    long countByTenant();
    
    /**
     * 查找租户内启用的用户
     */
    @Query("SELECT u FROM User u WHERE u.tenantId.value = :#{T(org.example.backend.tenant.TenantContext).currentTenant} AND u.enabled = true")
    List<User> findEnabledByTenant();
    
    /**
     * 删除指定ID和租户的用户
     */
    @Query("DELETE FROM User u WHERE u.userId = :id AND u.tenantId.value = :#{T(org.example.backend.tenant.TenantContext).currentTenant}")
    void deleteByIdAndTenant(@Param("id") Integer id);
}
