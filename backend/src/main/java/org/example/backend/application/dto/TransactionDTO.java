// backend/src/main/java/org/example/backend/application/dto/TransactionDTO.java
package org.example.backend.application.dto;

import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.valueobject.TransactionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Transaction Data Transfer Object for API responses
 */
@Data
@Builder
public class TransactionDTO {
    private Integer transactionId;
    private BigDecimal amount;
    private String currency;
    private TransactionAggregate.TransactionType transactionType;
    private TransactionStatus.Status status;
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
    
    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime approvedAt;
    private Integer approvedBy;
    
    // Computed fields
    private String displayAmount;
    private boolean canModify;
    private boolean canApprove;
}