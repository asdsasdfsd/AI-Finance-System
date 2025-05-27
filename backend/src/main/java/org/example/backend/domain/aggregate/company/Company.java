
// backend/src/main/java/org/example/backend/domain/aggregate/company/Company.java
package org.example.backend.domain.aggregate.company;

import org.example.backend.domain.event.CompanyCreatedEvent;
import org.example.backend.domain.valueobject.TenantId;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Company 聚合根
 * 
 * 职责：
 * 1. 作为租户的根实体
 * 2. 管理公司基本信息
 * 3. 提供多租户隔离的边界
 */
@Entity
@Table(name = "Company", indexes = {
    @Index(name = "idx_company_email", columnList = "email", unique = true),
    @Index(name = "idx_company_registration", columnList = "registration_number"),
    @Index(name = "idx_company_status", columnList = "status")
})
public class Company {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer companyId;
    
    @Column(nullable = false, length = 200)
    private String companyName;
    
    @Column(length = 500)
    private String address;
    
    @Column(length = 100)
    private String city;
    
    @Column(length = 100)
    private String stateProvince;
    
    @Column(length = 20)
    private String postalCode;
    
    @Column(length = 100, unique = true)
    private String email;
    
    @Column(length = 200)
    private String website;
    
    @Column(length = 100)
    private String registrationNumber;
    
    @Column(length = 100)
    private String taxId;
    
    @Column(length = 10)
    private String fiscalYearStart;
    
    @Column(length = 3)
    private String defaultCurrency;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyStatus status;
    
    // 审计字段
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 公司配置
    @Column(name = "max_users")
    private Integer maxUsers; // 最大用户数限制
    
    @Column(name = "subscription_expires_at")
    private LocalDateTime subscriptionExpiresAt; // 订阅到期时间
    
    // 领域事件
    @Transient
    private List<Object> domainEvents = new ArrayList<>();
    
    // ========== 构造函数 ==========
    
    protected Company() {
        // JPA需要
    }
    
    private Company(String companyName, String email, String address, String city, 
                   String stateProvince, String postalCode, Integer createdBy) {
        validateCompanyCreation(companyName, email);
        
        this.companyName = companyName;
        this.email = email.toLowerCase().trim();
        this.address = address;
        this.city = city;
        this.stateProvince = stateProvince;
        this.postalCode = postalCode;
        this.status = CompanyStatus.ACTIVE;
        this.defaultCurrency = "CNY";
        this.fiscalYearStart = "01-01"; // 默认1月1日
        this.maxUsers = 100; // 默认最大用户数
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // 发布公司创建事件
        addDomainEvent(new CompanyCreatedEvent(this.companyId, this.companyName, this.email, createdBy));
    }
    
    // ========== 工厂方法 ==========
    
    /**
     * 创建新公司
     */
    public static Company create(String companyName, String email, String address, 
                               String city, String stateProvince, String postalCode, 
                               Integer createdBy) {
        return new Company(companyName, email, address, city, stateProvince, postalCode, createdBy);
    }
    
    // ========== 业务方法 ==========
    
    /**
     * 更新公司基本信息
     */
    public void updateBasicInfo(String companyName, String address, String city, 
                              String stateProvince, String postalCode, String website) {
        if (companyName != null && !companyName.trim().isEmpty()) {
            this.companyName = companyName.trim();
        }
        
        this.address = address;
        this.city = city;
        this.stateProvince = stateProvince;
        this.postalCode = postalCode;
        this.website = website;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 更新财务配置
     */
    public void updateFinancialSettings(String fiscalYearStart, String defaultCurrency) {
        if (fiscalYearStart != null && fiscalYearStart.matches("\\d{2}-\\d{2}")) {
            this.fiscalYearStart = fiscalYearStart;
        }
        
        if (defaultCurrency != null && defaultCurrency.matches("[A-Z]{3}")) {
            this.defaultCurrency = defaultCurrency;
        }
        
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 更新注册信息
     */
    public void updateRegistrationInfo(String registrationNumber, String taxId) {
        this.registrationNumber = registrationNumber;
        this.taxId = taxId;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 激活公司
     */
    public void activate() {
        this.status = CompanyStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 暂停公司
     */
    public void suspend() {
        this.status = CompanyStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 删除公司（软删除）
     */
    public void delete() {
        this.status = CompanyStatus.DELETED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 更新用户限制
     */
    public void updateUserLimit(Integer maxUsers) {
        if (maxUsers != null && maxUsers > 0) {
            this.maxUsers = maxUsers;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * 更新订阅到期时间
     */
    public void updateSubscription(LocalDateTime expiresAt) {
        this.subscriptionExpiresAt = expiresAt;
        this.updatedAt = LocalDateTime.now();
    }
    
    // ========== 查询方法 ==========
    
    /**
     * 检查公司是否激活
     */
    public boolean isActive() {
        return status == CompanyStatus.ACTIVE;
    }
    
    /**
     * 检查订阅是否有效
     */
    public boolean isSubscriptionValid() {
        return subscriptionExpiresAt == null || 
               LocalDateTime.now().isBefore(subscriptionExpiresAt);
    }
    
    /**
     * 检查是否可以添加新用户
     */
    public boolean canAddUser(int currentUserCount) {
        return maxUsers == null || currentUserCount < maxUsers;
    }
    
    /**
     * 获取租户ID
     */
    public TenantId getTenantId() {
        return TenantId.of(companyId);
    }
    
    // ========== 验证方法 ==========
    
    private void validateCompanyCreation(String companyName, String email) {
        if (companyName == null || companyName.trim().isEmpty()) {
            throw new IllegalArgumentException("公司名称不能为空");
        }
        
        if (companyName.length() > 200) {
            throw new IllegalArgumentException("公司名称不能超过200个字符");
        }
        
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("公司邮箱不能为空");
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
            throw new IllegalArgumentException("邮箱格式不正确");
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
    
    public Integer getCompanyId() {
        return companyId;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public String getAddress() {
        return address;
    }
    
    public String getCity() {
        return city;
    }
    
    public String getStateProvince() {
        return stateProvince;
    }
    
    public String getPostalCode() {
        return postalCode;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getWebsite() {
        return website;
    }
    
    public String getRegistrationNumber() {
        return registrationNumber;
    }
    
    public String getTaxId() {
        return taxId;
    }
    
    public String getFiscalYearStart() {
        return fiscalYearStart;
    }
    
    public String getDefaultCurrency() {
        return defaultCurrency;
    }
    
    public CompanyStatus getStatus() {
        return status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public Integer getMaxUsers() {
        return maxUsers;
    }
    
    public LocalDateTime getSubscriptionExpiresAt() {
        return subscriptionExpiresAt;
    }
    
    // ========== 枚举定义 ==========
    
    public enum CompanyStatus {
        ACTIVE("激活"),
        SUSPENDED("暂停"),
        DELETED("已删除");
        
        private final String displayName;
        
        CompanyStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // ========== Object方法重写 ==========
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Company company = (Company) obj;
        return Objects.equals(companyId, company.companyId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(companyId);
    }
    
    @Override
    public String toString() {
        return String.format("Company{id=%d, name=%s, email=%s, status=%s}", 
                           companyId, companyName, email, status);
    }
}