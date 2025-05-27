// backend/src/main/java/org/example/backend/domain/aggregate/user/User.java
package org.example.backend.domain.aggregate.user;

import org.example.backend.domain.event.UserCreatedEvent;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.model.Role;
import org.springframework.data.domain.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * User 聚合根
 * 
 * 聚合边界：
 * - User (聚合根)
 * - UserRole (内部实体)
 * - 外部引用：Company, Department通过ID引用
 * 
 * 业务不变量：
 * 1. 用户名在租户内必须唯一
 * 2. 邮箱在全系统内必须唯一
 * 3. 用户必须属于一个有效的公司（租户）
 * 4. 用户必须至少有一个角色
 */
@Entity
@Table(name = "User", indexes = {
    @Index(name = "idx_user_company_username", columnList = "company_id, username", unique = true),
    @Index(name = "idx_user_email", columnList = "email", unique = true),
    @Index(name = "idx_user_external_id", columnList = "external_id"),
    @Index(name = "idx_user_company_enabled", columnList = "company_id, enabled")
})
public class User {
    
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
    private String externalId; // SSO外部ID
    
    // 多租户隔离 - 用户所属公司（租户）
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "company_id", nullable = false))
    private TenantId tenantId;
    
    @Column(name = "department_id")
    private Integer departmentId;
    
    // 用户偏好设置
    @Column(length = 10)
    private String preferredLanguage;
    
    @Column(length = 50)
    private String timezone;
    
    // 审计字段
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime passwordChangedAt;
    
    // 角色关联 - 聚合内管理
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "User_Role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
    // 安全字段
    private Integer failedLoginAttempts;
    private LocalDateTime lockedUntil;
    
    // 领域事件
    @Transient
    private List<Object> domainEvents = new ArrayList<>();
    
    // ========== 构造函数 ==========
    
    protected User() {
        // JPA需要
    }
    
    private User(String username, String email, String encodedPassword, String fullName, 
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
        
        // 发布用户创建事件
        addDomainEvent(new UserCreatedEvent(this.userId, this.username, this.email, this.tenantId.getValue()));
    }
    
    // ========== 工厂方法 ==========
    
    /**
     * 创建普通用户
     */
    public static User createUser(String username, String email, String encodedPassword, 
                                 String fullName, TenantId tenantId, Role userRole) {
        return new User(username, email, encodedPassword, fullName, tenantId, Set.of(userRole));
    }
    
    /**
     * 创建管理员用户
     */
    public static User createAdmin(String username, String email, String encodedPassword, 
                                  String fullName, TenantId tenantId, Role adminRole) {
        return new User(username, email, encodedPassword, fullName, tenantId, Set.of(adminRole));
    }
    
    /**
     * 通过SSO创建用户
     */
    public static User createFromSso(String username, String email, String fullName, 
                                    String externalId, TenantId tenantId, Role defaultRole) {
        User user = new User(username, email, "SSO_MANAGED", fullName, tenantId, Set.of(defaultRole));
        user.externalId = externalId;
        return user;
    }
    
    // ========== 业务方法 ==========
    
    /**
     * 更新用户基本信息
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
     * 更改密码
     */
    public void changePassword(String newEncodedPassword) {
        if (newEncodedPassword == null || newEncodedPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("新密码不能为空");
        }
        
        if (this.externalId != null) {
            throw new IllegalStateException("SSO用户不能修改密码");
        }
        
        this.password = newEncodedPassword;
        this.passwordChangedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // 重置失败登录次数
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }
    
    /**
     * 分配角色
     */
    public void assignRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("角色不能为空");
        }
        
        this.roles.add(role);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 移除角色
     */
    public void removeRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("角色不能为空");
        }
        
        if (this.roles.size() <= 1) {
            throw new IllegalStateException("用户必须至少有一个角色");
        }
        
        this.roles.remove(role);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 替换所有角色
     */
    public void replaceRoles(Set<Role> newRoles) {
        if (newRoles == null || newRoles.isEmpty()) {
            throw new IllegalArgumentException("用户必须至少有一个角色");
        }
        
        this.roles.clear();
        this.roles.addAll(newRoles);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 设置部门
     */
    public void setDepartment(Integer departmentId) {
        this.departmentId = departmentId;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 启用用户
     */
    public void enable() {
        this.enabled = true;
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 禁用用户
     */
    public void disable() {
        this.enabled = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 记录登录成功
     */
    public void recordSuccessfulLogin() {
        this.lastLogin = LocalDateTime.now();
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 记录登录失败
     */
    public void recordFailedLogin() {
        this.failedLoginAttempts = (this.failedLoginAttempts == null ? 0 : this.failedLoginAttempts) + 1;
        
        // 超过5次失败登录，锁定账户30分钟
        if (this.failedLoginAttempts >= 5) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(30);
        }
        
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 解锁账户
     */
    public void unlock() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 更新用户偏好
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
    
    // ========== 查询方法 ==========
    
    /**
     * 检查用户是否被锁定
     */
    public boolean isLocked() {
        return lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil);
    }
    
    /**
     * 检查用户是否启用且未锁定
     */
    public boolean isActiveAndUnlocked() {
        return enabled && !isLocked();
    }
    
    /**
     * 检查用户是否有指定角色
     */
    public boolean hasRole(String roleName) {
        return roles.stream().anyMatch(role -> role.getName().equals(roleName));
    }
    
    /**
     * 检查用户是否有任意指定角色
     */
    public boolean hasAnyRole(String... roleNames) {
        return Arrays.stream(roleNames)
                .anyMatch(this::hasRole);
    }
    
    /**
     * 获取角色名称集合
     */
    public Set<String> getRoleNames() {
        return roles.stream()
                .map(Role::getName)
                .collect(java.util.stream.Collectors.toSet());
    }
    
    /**
     * 检查是否是SSO用户
     */
    public boolean isSsoUser() {
        return externalId != null && !externalId.trim().isEmpty();
    }
    
    /**
     * 检查是否属于指定租户
     */
    public boolean belongsToTenant(TenantId tenantId) {
        return this.tenantId.equals(tenantId);
    }
    
    /**
     * 检查密码是否需要更新（超过90天）
     */
    public boolean isPasswordExpired() {
        if (isSsoUser()) {
            return false; // SSO用户密码不会过期
        }
        
        return passwordChangedAt != null && 
               passwordChangedAt.isBefore(LocalDateTime.now().minusDays(90));
    }
    
    // ========== 验证方法 ==========
    
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
            throw new IllegalArgumentException("用户名不能为空");
        }
        
        if (username.length() < 3 || username.length() > 50) {
            throw new IllegalArgumentException("用户名长度必须在3-50个字符之间");
        }
        
        if (!username.matches("^[a-zA-Z0-9_.-]+$")) {
            throw new IllegalArgumentException("用户名只能包含字母、数字、下划线、点和连字符");
        }
    }
    
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
    }
    
    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        // 这里假设传入的是已加密的密码，在实际项目中可能需要更复杂的验证
    }
    
    private void validateFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("姓名不能为空");
        }
        
        if (fullName.length() > 100) {
            throw new IllegalArgumentException("姓名长度不能超过100个字符");
        }
    }
    
    private void validateTenantId(TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("租户ID不能为空");
        }
    }
    
    private void validateRoles(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("用户必须至少有一个角色");
        }
    }
    
    // ========== 领域事件管理 ==========
    
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
    
    // ========== Getter方法 ==========
    
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
    
    // ========== Object方法重写 ==========
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        User user = (User) obj;
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
