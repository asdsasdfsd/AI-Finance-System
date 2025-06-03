// backend/src/main/java/org/example/backend/model/FixedAsset.java
package org.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Fixed_Asset")
public class FixedAsset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asset_id")  // 与Aggregate完全一致
    private Integer assetId;
    
    @ManyToOne
    @JoinColumn(name = "company_id")  // 明确指定外键列名
    private Company company;
    
    @ManyToOne
    @JoinColumn(name = "department_id")  // 明确指定外键列名
    private Department department;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "acquisition_date")
    private LocalDate acquisitionDate;
    
    @Column(name = "acquisition_cost", precision = 19, scale = 2)
    private BigDecimal acquisitionCost;
    
    @Column(name = "current_value", precision = 19, scale = 2)
    private BigDecimal currentValue;
    
    @Column(name = "accumulated_depreciation", precision = 19, scale = 2)
    private BigDecimal accumulatedDepreciation;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "serial_number")
    private String serialNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AssetStatus status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Default values
    public FixedAsset() {
        this.acquisitionCost = BigDecimal.ZERO;
        this.currentValue = BigDecimal.ZERO;
        this.accumulatedDepreciation = BigDecimal.ZERO;
        this.status = AssetStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Asset status enum
    public enum AssetStatus {
        ACTIVE, DISPOSED, WRITTEN_OFF
    }
}