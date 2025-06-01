// backend/src/main/java/org/example/backend/application/dto/CompanyDTO.java
package org.example.backend.application.dto;

import java.time.LocalDateTime;

import org.example.backend.domain.valueobject.CompanyStatus;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@Getter
public class CompanyDTO {
    private Integer companyId;
    private String companyName;
    private String address;
    private String city;
    private String stateProvince;
    private String postalCode;
    private String email;
    private String website;
    private String registrationNumber;
    private String taxId;
    private String fiscalYearStart;
    private String defaultCurrency;
    private CompanyStatus.Status status;
    private Integer maxUsers;
    private LocalDateTime subscriptionExpiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer createdBy;
    
    // Computed fields for business logic
    private Integer tenantId;
    private boolean isActive;
    private boolean canBeModified;
    private boolean subscriptionValid;
}
