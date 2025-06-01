// backend/src/main/java/org/example/backend/model/JournalLine.java
// 简化版本 - 移除所有可能的映射冲突

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
    @Column(name = "line_id")
    private Integer lineId;
    
    // 只使用外键，不使用JPA关联
    @Column(name = "entry_id")
    private Integer entryId;
    
    @Column(name = "account_id")
    private Integer accountId;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "debit_amount")
    private BigDecimal debitAmount;
    
    @Column(name = "credit_amount") 
    private BigDecimal creditAmount;
    
    // 完全移除JPA关联，避免映射冲突
    // 如果需要获取JournalEntry，通过Repository查询
    
    // ========== 构造函数 ==========
    
    public JournalLine() {}
    
    public JournalLine(Integer entryId, Integer accountId, String description, 
                      BigDecimal debitAmount, BigDecimal creditAmount) {
        this.entryId = entryId;
        this.accountId = accountId;
        this.description = description;
        this.debitAmount = debitAmount;
        this.creditAmount = creditAmount;
    }
    
    // ========== Getter 方法 ==========
    
    public Integer getLineId() { return lineId; }
    public Integer getEntryId() { return entryId; }
    public Integer getAccountId() { return accountId; }
    public String getDescription() { return description; }
    public BigDecimal getDebitAmount() { return debitAmount; }
    public BigDecimal getCreditAmount() { return creditAmount; }
    
    // ========== Setter 方法 ==========
    
    public void setLineId(Integer lineId) { this.lineId = lineId; }
    public void setEntryId(Integer entryId) { this.entryId = entryId; }
    public void setAccountId(Integer accountId) { this.accountId = accountId; }
    public void setDescription(String description) { this.description = description; }
    public void setDebitAmount(BigDecimal debitAmount) { this.debitAmount = debitAmount; }
    public void setCreditAmount(BigDecimal creditAmount) { this.creditAmount = creditAmount; }
    
    // ========== 兼容方法 - 支持不同的聚合根设置 ==========
    
    /**
     * 从JournalEntryAggregate设置entry_id
     */
    public void setJournalEntry(org.example.backend.domain.aggregate.journalentry.JournalEntryAggregate journalEntry) { 
        if (journalEntry != null) {
            this.entryId = journalEntry.getEntryId();
        }
    }
    
    /**
     * 从传统JournalEntry设置entry_id
     */
    public void setJournalEntry(JournalEntry journalEntry) { 
        if (journalEntry != null) {
            this.entryId = journalEntry.getEntryId();
        }
    }
    
    // ========== Object Methods ==========
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        JournalLine that = (JournalLine) obj;
        return lineId != null && lineId.equals(that.lineId);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("JournalLine{id=%d, entryId=%d, account=%d, debit=%s, credit=%s}", 
                           lineId, entryId, accountId, debitAmount, creditAmount);
    }
}