// src/main/java/org/example/backend/model/FixedAsset.java
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
    private Integer assetId;
    
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
    
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
    
    private String name;
    private String description;
    private LocalDate acquisitionDate;
    private BigDecimal acquisitionCost;
    private BigDecimal currentValue;
    private BigDecimal accumulatedDepreciation;
    private String location;
    private String serialNumber;
    
    @Enumerated(EnumType.STRING)
    private AssetStatus status;
    
    private LocalDateTime createdAt;
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