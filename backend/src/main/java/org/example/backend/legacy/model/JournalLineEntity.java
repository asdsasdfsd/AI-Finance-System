// src/main/java/org/example/backend/model/JournalLine.java
package org.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "Journal_Line")
public class JournalLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer lineId;
    
    @ManyToOne
    @JoinColumn(name = "entry_id")
    private JournalEntry journalEntry;
    
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
    
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
    
    private String description;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    
    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    // Default values
    public JournalLine() {
        this.debitAmount = BigDecimal.ZERO;
        this.creditAmount = BigDecimal.ZERO;
    }
}