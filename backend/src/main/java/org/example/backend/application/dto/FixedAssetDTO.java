// backend/src/main/java/org/example/backend/application/dto/FixedAssetDTO.java
package org.example.backend.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@Getter
public class FixedAssetDTO {
    private Integer assetId;
    private String name;
    private String description;
    private LocalDate acquisitionDate;
    private BigDecimal acquisitionCost;
    private BigDecimal currentValue;
    private BigDecimal accumulatedDepreciation;
    private String location;
    private String serialNumber;
    private String status;
    private Integer companyId;
    private Integer departmentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields
    private boolean isActive;
    private BigDecimal netBookValue;
}

