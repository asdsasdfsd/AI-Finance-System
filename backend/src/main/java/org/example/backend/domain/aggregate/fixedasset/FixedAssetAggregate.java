// backend/src/main/java/org/example/backend/domain/aggregate/fixedasset/FixedAssetAggregate.java
package org.example.backend.domain.aggregate.fixedasset;

import org.example.backend.domain.valueobject.Money;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.event.FixedAssetCreatedEvent;
import org.example.backend.domain.event.FixedAssetDepreciatedEvent;
import org.example.backend.domain.event.FixedAssetDisposedEvent;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Fixed Asset Aggregate Root - 完全修复版本
 * 
 * 使用JPA默认命名策略，避免列名冲突
 */
@Entity
@Table(name = "Fixed_Asset")
public class FixedAssetAggregate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer assetId;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    private LocalDate acquisitionDate;
    
    // 直接使用BigDecimal字段，避免Money值对象的映射冲突
    private java.math.BigDecimal acquisitionCost;
    
    private java.math.BigDecimal currentValue;
    
    private java.math.BigDecimal accumulatedDepreciation;
    
    // 货币代码，默认为CNY
    @Column(length = 3)
    private String currency = "CNY";
    
    private String location;
    
    private String serialNumber;
    
    @Enumerated(EnumType.STRING)
    private AssetStatus status;
    
    // External references - 使用简单的Integer字段
    @Column(name = "company_id")
    private Integer companyId;
    
    private Integer departmentId;
    
    // Audit fields
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime disposedAt;
    
    // Domain events
    @Transient
    private List<Object> domainEvents = new ArrayList<>();
    
    // ========== Constructors ==========
    
    protected FixedAssetAggregate() {}
    
    private FixedAssetAggregate(String name, String description, Money acquisitionCost,
                              LocalDate acquisitionDate, TenantId tenantId, Integer departmentId) {
        validateAssetCreation(name, acquisitionCost, acquisitionDate, tenantId);
        
        this.name = name;
        this.description = description;
        this.acquisitionCost = acquisitionCost.getAmount();
        this.currentValue = acquisitionCost.getAmount(); // 初始价值等于购置成本
        this.accumulatedDepreciation = java.math.BigDecimal.ZERO;
        this.currency = acquisitionCost.getCurrencyCode();
        this.acquisitionDate = acquisitionDate;
        this.companyId = tenantId.getValue();
        this.departmentId = departmentId;
        this.status = AssetStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // 发布创建事件
        addDomainEvent(new FixedAssetCreatedEvent(this.assetId, name, acquisitionCost, tenantId.getValue()));
    }
    
    // ========== Factory Methods ==========
    
    /**
     * Create new fixed asset
     */
    public static FixedAssetAggregate create(String name, String description, Money acquisitionCost,
                                           LocalDate acquisitionDate, TenantId tenantId, Integer departmentId) {
        return new FixedAssetAggregate(name, description, acquisitionCost, acquisitionDate, tenantId, departmentId);
    }
    
    // ========== Business Methods ==========
    
    /**
     * Update asset information
     */
    public void updateAssetInfo(String name, String description, String location) {
        ensureCanBeModified();
        
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
        this.description = description;
        this.location = location;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Set asset location
     */
    public void setLocation(String location) {
        ensureCanBeModified();
        this.location = location;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Set serial number
     */
    public void setSerialNumber(String serialNumber) {
        ensureCanBeModified();
        this.serialNumber = serialNumber;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Transfer asset to another department
     */
    public void transferToDepartment(Integer departmentId) {
        ensureCanBeModified();
        this.departmentId = departmentId;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Record depreciation
     */
    public void recordDepreciation(Money depreciationAmount) {
        ensureCanBeModified();
        validateDepreciation(depreciationAmount);
        
        this.accumulatedDepreciation = this.accumulatedDepreciation.add(depreciationAmount.getAmount());
        this.currentValue = this.acquisitionCost.subtract(this.accumulatedDepreciation);
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new FixedAssetDepreciatedEvent(this.assetId, depreciationAmount, 
                                                     getCurrentValue(), this.companyId));
    }
    
    /**
     * Dispose asset
     */
    public void dispose(Money disposalAmount, String reason) {
        ensureCanBeDisposed();
        
        this.status = AssetStatus.DISPOSED;
        this.disposedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new FixedAssetDisposedEvent(this.assetId, disposalAmount, reason, this.companyId));
    }
    
    /**
     * Write off asset
     */
    public void writeOff(String reason) {
        ensureCanBeDisposed();
        
        this.status = AssetStatus.WRITTEN_OFF;
        this.disposedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new FixedAssetDisposedEvent(this.assetId, Money.zero(this.currency), 
                                                  reason, this.companyId));
    }
    
    // ========== Money Value Object Getters ==========
    
    /**
     * Get acquisition cost as Money value object
     */
    public Money getAcquisitionCost() {
        return Money.of(this.acquisitionCost, this.currency);
    }
    
    /**
     * Get current value as Money value object
     */
    public Money getCurrentValue() {
        return Money.of(this.currentValue, this.currency);
    }
    
    /**
     * Get accumulated depreciation as Money value object
     */
    public Money getAccumulatedDepreciation() {
        return Money.of(this.accumulatedDepreciation, this.currency);
    }
    
    /**
     * Get tenant ID as value object
     */
    public TenantId getTenantId() {
        return TenantId.of(this.companyId);
    }
    
    // ========== Query Methods ==========
    
    /**
     * Calculate net book value
     */
    public Money getNetBookValue() {
        java.math.BigDecimal netValue = this.acquisitionCost.subtract(this.accumulatedDepreciation);
        return Money.of(netValue, this.currency);
    }
    
    /**
     * Check if asset is active
     */
    public boolean isActive() {
        return status == AssetStatus.ACTIVE;
    }
    
    /**
     * Check if asset can be modified
     */
    public boolean canBeModified() {
        return status == AssetStatus.ACTIVE;
    }
    
    /**
     * Check if asset can be disposed
     */
    public boolean canBeDisposed() {
        return status == AssetStatus.ACTIVE;
    }
    
    /**
     * Calculate depreciation rate (for informational purposes)
     */
    public double getDepreciationRate() {
        if (this.acquisitionCost.compareTo(java.math.BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return this.accumulatedDepreciation.doubleValue() / this.acquisitionCost.doubleValue();
    }
    
    // ========== Validation Methods ==========
    
    private void validateAssetCreation(String name, Money acquisitionCost, LocalDate acquisitionDate, TenantId tenantId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Asset name cannot be empty");
        }
        if (acquisitionCost == null || !acquisitionCost.isPositive()) {
            throw new IllegalArgumentException("Acquisition cost must be positive");
        }
        if (acquisitionDate == null) {
            throw new IllegalArgumentException("Acquisition date cannot be null");
        }
        if (acquisitionDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Acquisition date cannot be in the future");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }
    }
    
    private void validateDepreciation(Money depreciationAmount) {
        if (depreciationAmount == null || !depreciationAmount.isPositive()) {
            throw new IllegalArgumentException("Depreciation amount must be positive");
        }
        
        java.math.BigDecimal futureAccumulated = this.accumulatedDepreciation.add(depreciationAmount.getAmount());
        if (futureAccumulated.compareTo(this.acquisitionCost) > 0) {
            throw new IllegalArgumentException("Total depreciation cannot exceed acquisition cost");
        }
    }
    
    private void ensureCanBeModified() {
        if (!canBeModified()) {
            throw new IllegalStateException("Asset cannot be modified in current status: " + status);
        }
    }
    
    private void ensureCanBeDisposed() {
        if (!canBeDisposed()) {
            throw new IllegalStateException("Asset cannot be disposed in current status: " + status);
        }
    }
    
    // ========== Domain Events Management ==========
    
    private void addDomainEvent(Object event) {
        this.domainEvents.add(event);
    }
    
    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
    
    // ========== Getters ==========
    
    public Integer getAssetId() { return assetId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocalDate getAcquisitionDate() { return acquisitionDate; }
    public String getLocation() { return location; }
    public String getSerialNumber() { return serialNumber; }
    public AssetStatus getStatus() { return status; }
    public Integer getDepartmentId() { return departmentId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getDisposedAt() { return disposedAt; }
    public String getCurrency() { return currency; }
    public Integer getCompanyId() { return companyId; }
    
    // ========== Enums ==========
    
    public enum AssetStatus {
        ACTIVE("Active"),
        DISPOSED("Disposed"),
        WRITTEN_OFF("Written Off");
        
        private final String displayName;
        
        AssetStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // ========== Object Methods ==========
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        FixedAssetAggregate that = (FixedAssetAggregate) obj;
        return Objects.equals(assetId, that.assetId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(assetId);
    }
    
    @Override
    public String toString() {
        return String.format("FixedAsset{id=%d, name=%s, status=%s, value=%s %s}", 
                           assetId, name, status, currentValue, currency);
    }
}