// backend/src/main/java/org/example/backend/application/dto/UpdateCompanyCommand.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Command for updating existing companies
 */
@Data
@Builder
public class UpdateCompanyCommand {
    private String companyName;
    private String address;
    private String city;
    private String stateProvince;
    private String postalCode;
    private String website;
    private String registrationNumber;
    private String taxId;
    private String fiscalYearStart;
    private String defaultCurrency;
    private Integer maxUsers;
}

