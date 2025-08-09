// backend/src/main/java/org/example/backend/application/dto/AIDataFilterCommand.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Command for filtering data for AI analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIDataFilterCommand {
    private Integer companyId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer departmentId;
    private List<String> categoryFilter;
    private Double minAmount;
    private Double maxAmount;
}

