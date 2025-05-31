// backend/src/main/java/org/example/backend/application/dto/CompanyStatsDTO.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Company statistics DTO
 */
@Data
@Builder
public class CompanyStatsDTO {
    private long totalCompanies;
    private long activeCompanies;
    private long suspendedCompanies;
    private long deletedCompanies;
    
    /**
     * Calculate active percentage
     */
    public double getActivePercentage() {
        return totalCompanies > 0 ? (double) activeCompanies / totalCompanies * 100 : 0;
    }
}