// backend/src/main/java/org/example/backend/application/dto/CreateTransactionCommand.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Command for creating new transactions
 */
@Data
@Builder
public class CreateTransactionCommand {
    private BigDecimal amount;
    private String currency;
    private LocalDate transactionDate;
    private String description;
    private String paymentMethod;
    private String referenceNumber;
    private Boolean isRecurring;
    private Boolean isTaxable;
    
    // External references
    private Integer companyId;
    private Integer userId;
    private Integer departmentId;
    private Integer fundId;
    private Integer categoryId;
}

