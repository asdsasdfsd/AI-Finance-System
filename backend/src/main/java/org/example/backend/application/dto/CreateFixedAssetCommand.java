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
    
    // Legacy fields for backward compatibility with tests
    private LocalDate purchaseDate;
    private BigDecimal purchaseCost;
    private String currency;
    
    // Getter methods for backward compatibility
    public LocalDate getPurchaseDate() {
        return purchaseDate != null ? purchaseDate : acquisitionDate;
    }
    
    public BigDecimal getPurchaseCost() {
        return purchaseCost != null ? purchaseCost : acquisitionCost;
    }
    
    public String getCurrency() {
        return currency != null ? currency : "CNY";
    }
    
    // Builder class enhancement for backward compatibility
    public static class CreateFixedAssetCommandBuilder {
        
        public CreateFixedAssetCommandBuilder purchaseDate(LocalDate purchaseDate) {
            this.purchaseDate = purchaseDate;
            // Also set acquisitionDate for consistency
            if (this.acquisitionDate == null) {
                this.acquisitionDate = purchaseDate;
            }
            return this;
        }
        
        public CreateFixedAssetCommandBuilder purchaseCost(BigDecimal purchaseCost) {
            this.purchaseCost = purchaseCost;
            // Also set acquisitionCost for consistency
            if (this.acquisitionCost == null) {
                this.acquisitionCost = purchaseCost;
            }
            return this;
        }
        
        public CreateFixedAssetCommandBuilder currency(String currency) {
            this.currency = currency;
            return this;
        }
    }
}
