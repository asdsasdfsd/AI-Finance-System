// src/main/java/src/main/java/org/example/backend/model/User.java
package org.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "User")
// 避免循环引用的toString问题
@ToString(exclude = {"department", "company", "roles"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;
    
    @Column(name = "username", nullable = false, length = 50)
    private String username;
    
    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;
    
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled;
    
    @Column(name = "external_id", length = 100)
    private String externalId;

    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage;
    
    @Column(name = "timezone", length = 50)
    private String timezone;
    
    @ManyToOne
    @JoinColumn(name = "department_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "manager", "company"})
    private Department department;
    
    // add Company joint
    @ManyToOne
    @JoinColumn(name = "company_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Company company;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // add User - Role joint
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "User_Role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "users"})
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

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}