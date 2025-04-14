// src/main/java/org/example/backend/model/UserRole.java
package org.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "User_Role")
public class UserRole {
    @EmbeddedId
    private UserRoleId id;
    
    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;
    
    // Constructor for easier creation
    public UserRole(User user, Role role) {
        this.id = new UserRoleId(user.getUserId(), role.getRoleId());
        this.user = user;
        this.role = role;
    }
    
    // Default constructor required by JPA
    public UserRole() {
    }
}