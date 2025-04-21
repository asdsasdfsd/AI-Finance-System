// src/main/java/org/example/backend/model/User.java
package org.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "User")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;
    
    private String username;
    private String email;
    private String password;
    private String fullName;
    private Boolean enabled;
    private String externalId;
    
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
    
    // add Company joint
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
    
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // add User - Role joint
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "User_Role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // Default values for new fields
    public User() {
        this.enabled = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Check if user is Enabled
    public boolean isEnabled() {
        return this.enabled != null && this.enabled;
    }
    
    // Set Roles
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    
    // Get Roles
    public Set<Role> getRoles() {
        return this.roles;
    }
    
    // Set Company
    public void setCompany(Company company) {
        this.company = company;
    }
    
    // Get Company
    public Company getCompany() {
        return this.company;
    }
}