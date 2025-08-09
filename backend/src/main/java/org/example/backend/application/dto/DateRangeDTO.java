// backend/src/main/java/org/example/backend/application/dto/DateRangeDTO.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for date range
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DateRangeDTO {
    private LocalDate startDate;
    private LocalDate endDate;
}

