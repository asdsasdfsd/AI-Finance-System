// backend/src/main/java/org/example/backend/application/dto/UpdateTransactionCommand.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTransactionCommand {
    private BigDecimal amount;
    private String currency;
    private String description;
    private String paymentMethod;
    private String referenceNumber;
    
    // External references
    private Integer companyId;
    private Integer userId;
    private Integer departmentId;
    private Integer fundId;
    private Integer categoryId;
}