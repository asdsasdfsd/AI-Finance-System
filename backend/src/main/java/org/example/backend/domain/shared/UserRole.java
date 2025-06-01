// backend/src/main/java/org/example/backend/domain/shared/UserRole.java
package org.example.backend.domain.shared;

import jakarta.persistence.*;
import lombok.Data;

/**
 * UserRole实体 - DDD模式专用
 * 
 * 保持与ORM模式相同的User-Role关系结构
 */
@Data
@Entity
@Table(name = "User_Role")
public class UserRole {
    @EmbeddedId
    private UserRoleId id;
    
    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private org.example.backend.domain.aggregate.user.UserAggregate user;
    
    @ManyToOne
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;
    
    // Constructor for easier creation
    public UserRole(org.example.backend.domain.aggregate.user.UserAggregate user, Role role) {
        this.id = new UserRoleId(user.getUserId(), role.getRoleId());
        this.user = user;
        this.role = role;
    }
    
    // Default constructor required by JPA
    public UserRole() {
    }
}