// backend/src/main/java/org/example/backend/application/dto/CreateTransactionCommand.java
// 3. 修复 CreateTransactionCommand.java
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
