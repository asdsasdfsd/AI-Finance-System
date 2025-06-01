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
 * Fixed Asset Aggregate Root
 * 
 * 职责：
 * 1. 管理固定资产生命周期
 * 2. 计算折旧和净值
 * 3. 控制资产状态流转
 * 4. 处置和报废管理
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
    
    @Column(name = "acquisition_date")
    private LocalDate acquisitionDate;
    
    // Money value objects - embedded
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "acquisition_cost")),
        @AttributeOverride(name = "currencyCode", column = @Column(name = "acquisition_currency"))
    })
    private Money acquisitionCost;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "current_value")),
        @AttributeOverride(name = "currencyCode", column = @Column(name = "current_currency"))
    })
    private Money currentValue;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "accumulated_depreciation")),
        @AttributeOverride(name = "currencyCode", column = @Column(name = "depreciation_currency"))
    })
    private Money accumulatedDepreciation;
    
    private String location;
    
    @Column(name = "serial_number")
    private String serialNumber;
    
    @Enumerated(EnumType.STRING)
    private AssetStatus status;
    
    // External references
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "company_id"))
    private TenantId tenantId;
    
    @Column(name = "department_id")
    private Integer departmentId;
    
    // Audit fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "disposed_at")
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
        this.acquisitionCost = acquisitionCost;
        this.currentValue = acquisitionCost; // 初始价值等于购置成本
        this.accumulatedDepreciation = Money.zero(acquisitionCost.getCurrencyCode());
        this.acquisitionDate = acquisitionDate;
        this.tenantId = tenantId;
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
        
        this.accumulatedDepreciation = this.accumulatedDepreciation.add(depreciationAmount);
        this.currentValue = this.acquisitionCost.subtract(this.accumulatedDepreciation);
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new FixedAssetDepreciatedEvent(this.assetId, depreciationAmount, 
                                                     this.currentValue, this.tenantId.getValue()));
    }
    
    /**
     * Dispose asset
     */
    public void dispose(Money disposalAmount, String reason) {
        ensureCanBeDisposed();
        
        this.status = AssetStatus.DISPOSED;
        this.disposedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new FixedAssetDisposedEvent(this.assetId, disposalAmount, reason, 
                                                  this.tenantId.getValue()));
    }
    
    /**
     * Write off asset
     */
    public void writeOff(String reason) {
        ensureCanBeDisposed();
        
        this.status = AssetStatus.WRITTEN_OFF;
        this.disposedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new FixedAssetDisposedEvent(this.assetId, Money.zero(acquisitionCost.getCurrencyCode()), 
                                                  reason, this.tenantId.getValue()));
    }
    
    // ========== Query Methods ==========
    
    /**
     * Calculate net book value
     */
    public Money getNetBookValue() {
        return acquisitionCost.subtract(accumulatedDepreciation);
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
        if (acquisitionCost.isZero()) {
            return 0.0;
        }
        return accumulatedDepreciation.getAmount().doubleValue() / 
               acquisitionCost.getAmount().doubleValue();
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
        
        Money futureAccumulatedDepreciation = accumulatedDepreciation.add(depreciationAmount);
        if (futureAccumulatedDepreciation.isGreaterThan(acquisitionCost)) {
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
    public Money getAcquisitionCost() { return acquisitionCost; }
    public Money getCurrentValue() { return currentValue; }
    public Money getAccumulatedDepreciation() { return accumulatedDepreciation; }
    public String getLocation() { return location; }
    public String getSerialNumber() { return serialNumber; }
    public AssetStatus getStatus() { return status; }
    public TenantId getTenantId() { return tenantId; }
    public Integer getDepartmentId() { return departmentId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getDisposedAt() { return disposedAt; }
    
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
        return String.format("FixedAsset{id=%d, name=%s, status=%s, value=%s}", 
                           assetId, name, status, currentValue);
    }
}