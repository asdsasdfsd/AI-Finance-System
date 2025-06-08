// backend/src/main/java/org/example/backend/application/dto/ReportListQuery.java
package org.example.backend.application.dto;

import org.example.backend.domain.valueobject.ReportType;
import org.example.backend.domain.valueobject.ReportStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * Report List Query
 * 
 * Query object for filtering and paginating report lists
 */
@Data
@Builder
public class ReportListQuery {
    private Integer tenantId;
    private ReportType reportType;
    private ReportStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String searchTerm;
    private Integer pageNumber;
    private Integer pageSize;
    private String sortBy;
    private String sortDirection;
    
    public boolean hasFilters() {
        return reportType != null || 
               status != null || 
               startDate != null || 
               endDate != null || 
               (searchTerm != null && !searchTerm.trim().isEmpty());
    }
}

