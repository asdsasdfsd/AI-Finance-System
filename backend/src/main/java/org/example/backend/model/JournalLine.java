// backend/src/main/java/org/example/backend/model/JournalLine.java
// 修复 JournalLine 类，使其同时支持 DDD 和传统模式

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
    
    // DDD模式 - 关联到 JournalEntryAggregate
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", insertable = false, updatable = false)
    private JournalEntryAggregate journalEntryAggregate;
    
    // 传统模式 - 关联到 JournalEntry 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", insertable = false, updatable = false)
    private JournalEntry journalEntry;
    
    // 直接使用外键，避免ORM冲突
    @Column(name = "entry_id")
    private Integer entryId;
    
    @Column(name = "account_id")
    private Integer accountId;
    
    private String description;
    
    @Column(name = "debit_amount")
    private BigDecimal debitAmount;
    
    @Column(name = "credit_amount") 
    private BigDecimal creditAmount;
    
    // 构造函数
    public JournalLine() {}
    
    // Getter 方法
    public Integer getLineId() { return lineId; }
    public Integer getAccountId() { return accountId; }
    public String getDescription() { return description; }
    public BigDecimal getDebitAmount() { return debitAmount; }
    public BigDecimal getCreditAmount() { return creditAmount; }
    public Integer getEntryId() { return entryId; }
    
    // Setter 方法 - DDD模式
    public void setJournalEntry(JournalEntryAggregate journalEntryAggregate) { 
        this.journalEntryAggregate = journalEntryAggregate;
        if (journalEntryAggregate != null) {
            this.entryId = journalEntryAggregate.getEntryId();
        }
    }
    
    // Setter 方法 - 传统模式  
    public void setJournalEntry(JournalEntry journalEntry) { 
        this.journalEntry = journalEntry;
        if (journalEntry != null) {
            this.entryId = journalEntry.getEntryId();
        }
    }
    
    // 其他 Setter 方法
    public void setAccountId(Integer accountId) { this.accountId = accountId; }
    public void setDescription(String description) { this.description = description; }
    public void setDebitAmount(BigDecimal debitAmount) { this.debitAmount = debitAmount; }
    public void setCreditAmount(BigDecimal creditAmount) { this.creditAmount = creditAmount; }
    public void setEntryId(Integer entryId) { this.entryId = entryId; }
}