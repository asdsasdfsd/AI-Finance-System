// backend/src/main/java/org/example/backend/application/dto/CreateJournalEntryCommand.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Command for creating journal entries
 */
@Data
@Builder
public class CreateJournalEntryCommand {
    private Integer companyId;
    private LocalDate entryDate;
    private String description;
    private Integer createdBy;
    private List<JournalLineCommand> journalLines;
    
    @Data
    @Builder
    public static class JournalLineCommand {
        private Integer accountId;
        private String description;
        private BigDecimal debitAmount;
        private BigDecimal creditAmount;
    }
}