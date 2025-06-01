// backend/src/main/java/org/example/backend/application/dto/JournalEntryDTO.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.example.backend.domain.aggregate.journalentry.JournalEntryAggregate;

/**
 * Journal Entry DTO
 */
@Data
@Builder
public class JournalEntryDTO {
    private Integer entryId;
    private Integer companyId;
    private LocalDate entryDate;
    private String description;
    private JournalEntryAggregate.EntryStatus status;
    private LocalDateTime createdAt;
    private boolean isBalanced;
    private List<JournalLineDTO> journalLines;
    
    @Data
    @Builder
    public static class JournalLineDTO {
        private Integer lineId;
        private Integer accountId;
        private String description;
        private BigDecimal debitAmount;
        private BigDecimal creditAmount;
    }
}

