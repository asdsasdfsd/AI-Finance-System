// backend/src/main/java/org/example/backend/infrastructure/ai/dto/AITransactionData.java
package org.example.backend.infrastructure.ai.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.backend.application.dto.CreateTransactionCommand;
import java.time.LocalDate;
import java.util.Map;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AITransactionData {
    private String description;                // Transaction description
    private Double amount;                     // Amount
    private String currency;                   // Currency
    private String category;                   // Current category
    private LocalDate transactionDate;         // Transaction date
    private String transactionType;            // INCOME / EXPENSE
    private Integer companyId;                 // Company ID
    private Integer userId;                    // User ID
    private String paymentMethod;              // Payment method
    private String referenceNumber;            // Reference number
    private Boolean isRecurring;               // Is recurring transaction
    private Boolean isTaxable;                 // Is taxable transaction
    private Map<String, Object> metadata;      // Other metadata

    /**
     * Convert AI transaction data to CreateTransactionCommand
     * This method ensures proper type conversion and null safety
     */
    public CreateTransactionCommand toCreateTransactionCommand() {
        return CreateTransactionCommand.builder()
                .description(this.description)
                .amount(this.amount != null ? new BigDecimal(this.amount.toString()) : BigDecimal.ZERO)
                .currency(this.currency != null ? this.currency : "CNY")
                .transactionDate(this.transactionDate != null ? this.transactionDate : LocalDate.now())
                .companyId(this.companyId)
                .userId(this.userId)
                .paymentMethod(this.paymentMethod != null ? this.paymentMethod : "AI_PROCESSED")
                .referenceNumber(this.referenceNumber)
                .isRecurring(this.isRecurring != null ? this.isRecurring : false)
                .isTaxable(this.isTaxable != null ? this.isTaxable : false)
                .build();
    }
}