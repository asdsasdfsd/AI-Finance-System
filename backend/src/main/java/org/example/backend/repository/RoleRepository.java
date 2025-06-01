// backend/src/main/java/org/example/backend/repository/RoleRepository.java
package org.example.backend.repository;

import org.example.backend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Role Repository - 支持两种模式
 * 
 * 这个Repository在ORM和DDD模式下都会被使用，
 * 因为Role是一个共享实体，两种模式都需要访问角色信息
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    
    /**
     * 根据角色名查找角色
     * 
     * @param name 角色名称
     * @return 角色实体，返回Optional以支持DDD模式的类型安全
     */
    Optional<Role> findByName(String name);
    
    /**
     * 检查角色名是否存在
     * 
     * @param name 角色名称
     * @return 如果存在返回true
     */
    boolean existsByName(String name);
}