// backend/src/main/java/org/example/backend/application/dto/CreateJournalEntryCommand.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateJournalEntryCommand {
    private Integer companyId;
    private LocalDate entryDate;
    private String description;
    private Integer createdBy;
    private List<JournalLineCommand> journalLines;
    
    @Data
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JournalLineCommand {
        private Integer accountId;
        private String description;
        private BigDecimal debitAmount;
        private BigDecimal creditAmount;
    }
}