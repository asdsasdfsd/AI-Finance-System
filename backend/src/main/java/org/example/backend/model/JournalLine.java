// src/main/java/org/example/backend/model/JournalLine.java
package org.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

import org.example.backend.domain.aggregate.journalentry.JournalEntryAggregate;

@Data
@Entity
@Table(name = "Journal_Line")
public class JournalLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer lineId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id")
    private JournalEntryAggregate journalEntry;
    
    @Column(name = "account_id")
    private Integer accountId;
    
    private String description;
    
    @Column(name = "debit_amount")
    private BigDecimal debitAmount;
    
    @Column(name = "credit_amount") 
    private BigDecimal creditAmount;
    
    // 构造函数和getter/setter
    public JournalLine() {}
    
    public Integer getLineId() { return lineId; }
    public Integer getAccountId() { return accountId; }
    public String getDescription() { return description; }
    public BigDecimal getDebitAmount() { return debitAmount; }
    public BigDecimal getCreditAmount() { return creditAmount; }
    
    public void setJournalEntry(JournalEntryAggregate journalEntry) { this.journalEntry = journalEntry; }
    public void setAccountId(Integer accountId) { this.accountId = accountId; }
    public void setDescription(String description) { this.description = description; }
    public void setDebitAmount(BigDecimal debitAmount) { this.debitAmount = debitAmount; }
    public void setCreditAmount(BigDecimal creditAmount) { this.creditAmount = creditAmount; }
}