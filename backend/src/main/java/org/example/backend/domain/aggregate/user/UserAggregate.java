// backend/src/main/java/org/example/backend/domain/aggregate/user/UserAggregate.java
package org.example.backend.domain.aggregate.user;

import org.example.backend.domain.event.UserCreatedEvent;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.model.Role; // 统一使用model.Role
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * User Aggregate Root - 统一Role实体版本
 * 
 * 统一使用org.example.backend.model.Role，避免类型冲突
 */
@Entity
@Table(name = "User", indexes = {
    @Index(name = "idx_user_tenant_username", columnList = "company_id, username", unique = true),
    @Index(name = "idx_user_email", columnList = "email", unique = true),
    @Index(name = "idx_user_external_id", columnList = "external_id"),
    @Index(name = "idx_user_tenant_enabled", columnList = "company_id, enabled")
})
@Profile("ddd")
public class UserAggregate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;
    
    @Column(nullable = false, length = 50)
    private String username;
    
    @Column(nullable = false, length = 100, unique = true)
    private String email;
    
    @Column(nullable = false, length = 255)
    private String password;
    
    @Column(nullable = false, length = 100)
    private String fullName;
    
    @Column(nullable = false)
    private Boolean enabled;
    
    @Column(length = 100)
    private String externalId; // SSO external ID
    
    // Multi-tenant isolation - user belongs to a tenant (company)
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "company_id", nullable = false))
    private TenantId tenantId;
    
    @Column(name = "department_id")
    private Integer departmentId;
    
    // User preferences
    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage;
    
    @Column(length = 50)
    private String timezone;
    
    // Audit fields
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;
    
    // Role associations - 统一使用model.Role
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "User_Role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
    // Security fields
    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts;
    
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;
    
    // Domain events
    @Transient
    private List<Object> domainEvents = new ArrayList<>();
    
    // ========== Constructors ==========
    
    protected UserAggregate() {
        // JPA requires default constructor
    }
    
    private UserAggregate(String username, String email, String encodedPassword, String fullName, 
                TenantId tenantId, Set<Role> roles) {
        validateUserCreation(username, email, encodedPassword, fullName, tenantId, roles);
        
        this.username = username;
        this.email = email;
        this.password = encodedPassword;
        this.fullName = fullName;
        this.tenantId = tenantId;
        this.enabled = true;
        this.preferredLanguage = "zh-CN";
        this.timezone = "Asia/Shanghai";
        this.roles = new HashSet<>(roles);
        this.failedLoginAttempts = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.passwordChangedAt = LocalDateTime.now();
        
        // Publish user creation event
        addDomainEvent(new UserCreatedEvent(this.userId, this.username, this.email, this.tenantId.getValue()));
    }
    
    // ========== Factory Methods ==========
    
    /**
     * Create regular user
     */
    public static UserAggregate createUser(String username, String email, String encodedPassword, 
                                 String fullName, TenantId tenantId, Role userRole) {
        return new UserAggregate(username, email, encodedPassword, fullName, tenantId, Set.of(userRole));
    }
    
    /**
     * Create admin user
     */
    public static UserAggregate createAdmin(String username, String email, String encodedPassword, 
                                  String fullName, TenantId tenantId, Role adminRole) {
        return new UserAggregate(username, email, encodedPassword, fullName, tenantId, Set.of(adminRole));
    }
    
    /**
     * Create user from SSO
     */
    public static UserAggregate createFromSso(String username, String email, String fullName, 
                                    String externalId, TenantId tenantId, Role defaultRole) {
        UserAggregate user = new UserAggregate(username, email, "SSO_MANAGED", fullName, tenantId, Set.of(defaultRole));
        user.externalId = externalId;
        return user;
    }
    
    // ========== Business Methods ==========
    
    /**
     * Update user basic information
     */
    public void updateBasicInfo(String fullName, String email) {
        if (fullName != null && !fullName.trim().isEmpty()) {
            this.fullName = fullName.trim();
        }
        
        if (email != null && !email.trim().isEmpty()) {
            validateEmail(email);
            this.email = email.toLowerCase().trim();
        }
        
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Change password
     */
    public void changePassword(String newEncodedPassword) {
        if (newEncodedPassword == null || newEncodedPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be empty");
        }
        
        if (this.externalId != null) {
            throw new IllegalStateException("SSO users cannot change password");
        }
        
        this.password = newEncodedPassword;
        this.passwordChangedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // Reset failed login attempts
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }
    
    /**
     * Assign role
     */
    public void assignRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        
        this.roles.add(role);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Remove role
     */
    public void removeRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        
        if (this.roles.size() <= 1) {
            throw new IllegalStateException("User must have at least one role");
        }
        
        this.roles.remove(role);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Replace all roles
     */
    public void replaceRoles(Set<Role> newRoles) {
        if (newRoles == null || newRoles.isEmpty()) {
            throw new IllegalArgumentException("User must have at least one role");
        }
        
        this.roles.clear();
        this.roles.addAll(newRoles);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Set department
     */
    public void setDepartment(Integer departmentId) {
        this.departmentId = departmentId;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Enable user
     */
    public void enable() {
        this.enabled = true;
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Disable user
     */
    public void disable() {
        this.enabled = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Record successful login
     */
    public void recordSuccessfulLogin() {
        this.lastLogin = LocalDateTime.now();
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Record failed login
     */
    public void recordFailedLogin() {
        this.failedLoginAttempts = (this.failedLoginAttempts == null ? 0 : this.failedLoginAttempts) + 1;
        
        // Lock account after 5 failed attempts for 30 minutes
        if (this.failedLoginAttempts >= 5) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(30);
        }
        
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Unlock account
     */
    public void unlock() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Update user preferences
     */
    public void updatePreferences(String language, String timezone) {
        if (language != null && !language.trim().isEmpty()) {
            this.preferredLanguage = language;
        }
        
        if (timezone != null && !timezone.trim().isEmpty()) {
            this.timezone = timezone;
        }
        
        this.updatedAt = LocalDateTime.now();
    }
    
    // ========== Query Methods ==========
    
    /**
     * Check if user is locked
     */
    public boolean isLocked() {
        return lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil);
    }
    
    /**
     * Check if user is active and unlocked
     */
    public boolean isActiveAndUnlocked() {
        return enabled && !isLocked();
    }
    
    /**
     * Check if user has specific role
     */
    public boolean hasRole(String roleName) {
        return roles.stream().anyMatch(role -> role.getName().equals(roleName));
    }
    
    /**
     * Check if user has any of the specified roles
     */
    public boolean hasAnyRole(String... roleNames) {
        return Arrays.stream(roleNames)
                .anyMatch(this::hasRole);
    }
    
    /**
     * Get role names
     */
    public Set<String> getRoleNames() {
        return roles.stream()
                .map(Role::getName)
                .collect(java.util.stream.Collectors.toSet());
    }
    
    /**
     * Check if user is SSO user
     */
    public boolean isSsoUser() {
        return externalId != null && !externalId.trim().isEmpty();
    }
    
    /**
     * Check if user belongs to specific tenant
     */
    public boolean belongsToTenant(TenantId tenantId) {
        return this.tenantId.equals(tenantId);
    }
    
    /**
     * Check if password is expired (90 days)
     */
    public boolean isPasswordExpired() {
        if (isSsoUser()) {
            return false; // SSO users' passwords don't expire
        }
        
        return passwordChangedAt != null && 
               passwordChangedAt.isBefore(LocalDateTime.now().minusDays(90));
    }
    
    // ========== Validation Methods ==========
    
    private void validateUserCreation(String username, String email, String encodedPassword, 
                                    String fullName, TenantId tenantId, Set<Role> roles) {
        validateUsername(username);
        validateEmail(email);
        validatePassword(encodedPassword);
        validateFullName(fullName);
        validateTenantId(tenantId);
        validateRoles(roles);
    }
    
    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        
        if (username.length() < 3 || username.length() > 50) {
            throw new IllegalArgumentException("Username must be between 3-50 characters");
        }
        
        if (!username.matches("^[a-zA-Z0-9_.-]+$")) {
            throw new IllegalArgumentException("Username can only contain letters, numbers, underscore, dot and hyphen");
        }
    }
    
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
    
    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
    }
    
    private void validateFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }
        
        if (fullName.length() > 100) {
            throw new IllegalArgumentException("Full name cannot exceed 100 characters");
        }
    }
    
    private void validateTenantId(TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }
    }
    
    private void validateRoles(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("User must have at least one role");
        }
    }
    
    // ========== Domain Events Management ==========
    
    private void addDomainEvent(Object event) {
        this.domainEvents.add(event);
    }
    
    @DomainEvents
    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    @AfterDomainEventPublication
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
    
    // ========== Getters ==========
    
    public Integer getUserId() {
        return userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public String getExternalId() {
        return externalId;
    }
    
    public TenantId getTenantId() {
        return tenantId;
    }
    
    public Integer getDepartmentId() {
        return departmentId;
    }
    
    public String getPreferredLanguage() {
        return preferredLanguage;
    }
    
    public String getTimezone() {
        return timezone;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public LocalDateTime getPasswordChangedAt() {
        return passwordChangedAt;
    }
    
    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }
    
    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }
    
    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }
    
    // ========== Object Methods ==========
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        UserAggregate user = (UserAggregate) obj;
        return Objects.equals(userId, user.userId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
    
    @Override
    public String toString() {
        return String.format("User{id=%d, username=%s, email=%s, tenantId=%s, enabled=%s}", 
                           userId, username, email, tenantId, enabled);
    }
}