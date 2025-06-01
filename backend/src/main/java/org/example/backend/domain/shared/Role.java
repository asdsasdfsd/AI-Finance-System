// backend/src/main/java/org/example/backend/domain/shared/Role.java
package org.example.backend.domain.shared;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Role实体 - DDD模式专用
 * 
 * 保持与ORM模式相同的简单结构，不使用多对多关系
 * 角色关系通过UserRole中间表管理
 */
@Data
@Entity
@Table(name = "Role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roleId;
    
    private String name;
    private String description;
    
    // 不定义与User的直接关联，通过UserRole中间表管理
    
    // 确保有getName方法
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(roleId, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Role role = (Role) obj;
        return Objects.equals(roleId, role.roleId);
    }
}