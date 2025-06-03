// backend/src/main/java/org/example/backend/domain/aggregate/company/CompanyAggregate.java
package org.example.backend.domain.aggregate.company;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.example.backend.domain.event.CompanyCreatedEvent;
import org.example.backend.domain.valueobject.CompanyStatus;
import org.example.backend.domain.valueobject.TenantId;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Company Aggregate Root - 修复字段映射冲突
 * 
 * 修复了与传统Company实体的字段映射冲突问题
 */
@Entity
@Table(name = "Company", indexes = {
    @Index(name = "idx_company_email", columnList = "email", unique = true),
    @Index(name = "idx_company_registration", columnList = "registration_number"),
    @Index(name = "idx_company_status", columnList = "status")
})

public class CompanyAggregate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")  // 明确指定数据库字段名
    private Integer companyId;
    
    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;
    
    @Column(name = "address", length = 500)
    private String address;
    
    @Column(name = "city", length = 100)
    private String city;
    
    @Column(name = "state_province", length = 100)
    private String stateProvince;
    
    @Column(name = "postal_code", length = 20)
    private String postalCode;
    
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(name = "website", length = 200)
    private String website;
    
    @Column(name = "registration_number", length = 100)
    private String registrationNumber;
    
    @Column(name = "tax_id", length = 100)
    private String taxId;
    
    // Financial configuration
    @Column(name = "fiscal_year_start", length = 10)
    private String fiscalYearStart;
    
    @Column(name = "default_currency", length = 3)
    private String defaultCurrency;
    
    // Company status value object - embedded
    @Embedded
    private CompanyStatus companyStatus;
    
    // Subscription and limits
    @Column(name = "max_users")
    private Integer maxUsers;
    
    @Column(name = "subscription_expires_at")
    private LocalDateTime subscriptionExpiresAt;
    
    // Audit fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private Integer createdBy;
    
    // Domain events
    @Transient
    private List<Object> domainEvents = new ArrayList<>();
    
    // ========== Constructors ==========
    
    protected CompanyAggregate() {
        // JPA requires default constructor
    }
    
    private CompanyAggregate(String companyName, String email, String address, String city, 
                   String stateProvince, String postalCode, Integer createdBy) {
        validateCompanyCreation(companyName, email);
        
        this.companyName = companyName.trim();
        this.email = email.toLowerCase().trim();
        this.address = address;
        this.city = city;
        this.stateProvince = stateProvince;
        this.postalCode = postalCode;
        this.companyStatus = CompanyStatus.active();
        this.defaultCurrency = "CNY";
        this.fiscalYearStart = "01-01"; // Default January 1st
        this.maxUsers = 100; // Default max users
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // Publish company creation event
        addDomainEvent(new CompanyCreatedEvent(this.companyId, this.companyName, this.email, createdBy));
    }
    
    // ========== Factory Methods ==========
    
    /**
     * Create new company
     */
    public static CompanyAggregate create(String companyName, String email, String address, 
                               String city, String stateProvince, String postalCode, 
                               Integer createdBy) {
        return new CompanyAggregate(companyName, email, address, city, stateProvince, postalCode, createdBy);
    }
    
    // ========== Business Methods ==========
    
    /**
     * Update company basic information
     */
    public void updateBasicInfo(String companyName, String address, String city, 
                              String stateProvince, String postalCode, String website) {
        ensureCanBeModified();
        
        if (companyName != null && !companyName.trim().isEmpty()) {
            validateCompanyName(companyName);
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
     * Update financial settings
     */
    public void updateFinancialSettings(String fiscalYearStart, String defaultCurrency) {
        ensureCanBeModified();
        
        if (fiscalYearStart != null && fiscalYearStart.matches("\\d{2}-\\d{2}")) {
            this.fiscalYearStart = fiscalYearStart;
        }
        
        if (defaultCurrency != null && defaultCurrency.matches("[A-Z]{3}")) {
            this.defaultCurrency = defaultCurrency;
        }
        
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Update registration information
     */
    public void updateRegistrationInfo(String registrationNumber, String taxId) {
        ensureCanBeModified();
        this.registrationNumber = registrationNumber;
        this.taxId = taxId;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Activate company
     */
    public void activate() {
        this.companyStatus = CompanyStatus.active();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Deactivate company operations (替代原来的 suspend)
     */
    public void deactivate() {
        this.companyStatus = CompanyStatus.inactive();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Update user limit
     */
    public void updateUserLimit(Integer maxUsers) {
        ensureCanBeModified();
        if (maxUsers != null && maxUsers > 0) {
            this.maxUsers = maxUsers;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * Update subscription expiration
     */
    public void updateSubscription(LocalDateTime expiresAt) {
        this.subscriptionExpiresAt = expiresAt;
        this.updatedAt = LocalDateTime.now();
    }
    
    // ========== Query Methods ==========
    
    /**
     * Check if company is active and operational
     */
    public boolean isActive() {
        return companyStatus.isOperational();
    }
    
    /**
     * Check if subscription is valid
     */
    public boolean isSubscriptionValid() {
        return subscriptionExpiresAt == null || 
               LocalDateTime.now().isBefore(subscriptionExpiresAt);
    }
    
    /**
     * Check if company can add new users
     */
    public boolean canAddUser(int currentUserCount) {
        return companyStatus.canAcceptNewUsers() && 
               (maxUsers == null || currentUserCount < maxUsers);
    }
    
    /**
     * Get tenant ID for multi-tenancy
     */
    public TenantId getTenantId() {
        return TenantId.of(companyId);
    }
    
    /**
     * Check if company can be modified
     */
    public boolean canBeModified() {
        return companyStatus.canBeModified();
    }
    
    /**
     * Get company status
     */
    public CompanyStatus.Status getStatus() {
        return companyStatus.getStatus();
    }
    
    // ========== Validation Methods ==========
    
    private void validateCompanyCreation(String companyName, String email) {
        validateCompanyName(companyName);
        validateEmail(email);
    }
    
    private void validateCompanyName(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name cannot be empty");
        }
        
        if (companyName.length() > 200) {
            throw new IllegalArgumentException("Company name cannot exceed 200 characters");
        }
    }
    
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Company email cannot be empty");
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
    
    private void ensureCanBeModified() {
        if (!canBeModified()) {
            throw new IllegalStateException("Company cannot be modified in current state: " + companyStatus);
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
    
    public CompanyStatus getCompanyStatus() {
        return companyStatus;
    }
    
    public Integer getMaxUsers() {
        return maxUsers;
    }
    
    public LocalDateTime getSubscriptionExpiresAt() {
        return subscriptionExpiresAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public Integer getCreatedBy() {
        return createdBy;
    }
    
    // ========== Object Methods ==========
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        CompanyAggregate company = (CompanyAggregate) obj;
        return Objects.equals(companyId, company.companyId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(companyId);
    }
    
    @Override
    public String toString() {
        return String.format("Company{id=%d, name=%s, email=%s, status=%s}", 
                           companyId, companyName, email, companyStatus);
    }
}