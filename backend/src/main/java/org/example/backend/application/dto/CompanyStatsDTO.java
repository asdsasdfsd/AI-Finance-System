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
    private long inactiveCompanies;  // 改为 inactiveCompanies
    
    /**
     * Calculate active percentage
     */
    public double getActivePercentage() {
        return totalCompanies > 0 ? (double) activeCompanies / totalCompanies * 100 : 0;
    }
    
    /**
     * Calculate inactive percentage
     */
    public double getInactivePercentage() {
        return totalCompanies > 0 ? (double) inactiveCompanies / totalCompanies * 100 : 0;
    }
}