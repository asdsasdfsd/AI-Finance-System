// backend/src/main/java/org/example/backend/application/dto/UpdateTransactionCommand.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

/**
 * Command for updating existing transactions
 */
@Data
@Builder
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
