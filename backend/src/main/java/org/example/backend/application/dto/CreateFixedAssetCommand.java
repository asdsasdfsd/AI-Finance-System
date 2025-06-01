// backend/src/main/java/org/example/backend/application/dto/CreateFixedAssetCommand.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Command for creating fixed assets
 */
@Data
@Builder
public class CreateFixedAssetCommand {
    private String name;
    private String description;
    private LocalDate acquisitionDate;
    private BigDecimal acquisitionCost;
    private String location;
    private String serialNumber;
    private Integer companyId;
    private Integer departmentId;
}

