// backend/src/main/java/org/example/backend/model/JournalLine.java
package org.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

/**
 * JournalLine - 修复版本，解决双重映射冲突
 * 
 * 关键策略：移除所有@ManyToOne映射，只使用外键字段entry_id
 * 这样避免了与两个不同的JournalEntry实体的映射冲突
 */
@Data
@Entity
@Table(name = "Journal_Line")
public class JournalLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "line_id")
    private Integer lineId;
    
    // 关键修复：只使用外键字段，不使用@ManyToOne映射
    @Column(name = "entry_id", nullable = false)
    private Integer entryId;
    
    @Column(name = "account_id")
    private Integer accountId;
    
    private String description;
    
    @Column(name = "debit_amount", precision = 19, scale = 2)
    private BigDecimal debitAmount;
    
    @Column(name = "credit_amount", precision = 19, scale = 2) 
    private BigDecimal creditAmount;
    
    // 构造函数
    public JournalLine() {
        this.debitAmount = BigDecimal.ZERO;
        this.creditAmount = BigDecimal.ZERO;
    }
    
    // 明确的Getter和Setter方法
    public Integer getLineId() { 
        return lineId; 
    }
    
    public void setLineId(Integer lineId) { 
        this.lineId = lineId; 
    }
    
    public Integer getEntryId() { 
        return entryId; 
    }
    
    public void setEntryId(Integer entryId) { 
        this.entryId = entryId; 
    }
    
    public Integer getAccountId() { 
        return accountId; 
    }
    
    public void setAccountId(Integer accountId) { 
        this.accountId = accountId; 
    }
    
    public String getDescription() { 
        return description; 
    }
    
    public void setDescription(String description) { 
        this.description = description; 
    }
    
    public BigDecimal getDebitAmount() { 
        return debitAmount != null ? debitAmount : BigDecimal.ZERO; 
    }
    
    public void setDebitAmount(BigDecimal debitAmount) { 
        this.debitAmount = debitAmount != null ? debitAmount : BigDecimal.ZERO; 
    }
    
    public BigDecimal getCreditAmount() { 
        return creditAmount != null ? creditAmount : BigDecimal.ZERO; 
    }
    
    public void setCreditAmount(BigDecimal creditAmount) { 
        this.creditAmount = creditAmount != null ? creditAmount : BigDecimal.ZERO; 
    }
    
    // 兼容性方法：为传统代码提供JournalEntry设置方法
    // 这些方法只设置entryId，不会引起映射冲突
    public void setJournalEntry(org.example.backend.model.JournalEntry journalEntry) {
        if (journalEntry != null) {
            this.entryId = journalEntry.getEntryId();
        }
    }
    
    public void setJournalEntry(org.example.backend.domain.aggregate.journalentry.JournalEntryAggregate journalEntryAggregate) {
        if (journalEntryAggregate != null) {
            this.entryId = journalEntryAggregate.getEntryId();
        }
    }
    
    @Override
    public String toString() {
        return String.format("JournalLine{lineId=%d, entryId=%d, accountId=%d, debit=%s, credit=%s}", 
                           lineId, entryId, accountId, debitAmount, creditAmount);
    }
}