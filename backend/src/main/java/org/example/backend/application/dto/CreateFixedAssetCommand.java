// backend/src/main/java/org/example/backend/application/dto/CreateFixedAssetCommand.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
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
