// backend/src/main/java/org/example/backend/domain/aggregate/journalentry/JournalEntryAggregate.java
package org.example.backend.domain.aggregate.journalentry;

import org.example.backend.domain.valueobject.Money;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.event.JournalEntryPostedEvent;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.math.BigDecimal;

/**
 * 会计分录聚合根 - 最终修复版本
 * 
 * 彻底解决所有列名冲突问题：
 * 1. 完全避免驼峰字段名
 * 2. 移除所有可能的双重映射
 * 3. 简化与JournalLine的关联
 */
@Entity
@Table(name = "Journal_Entry")
public class JournalEntryAggregate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entry_id")
    private Integer journalId;  // 完全不同的字段名，避免任何可能的冲突
    
    @Column(name = "company_id")
    private Integer companyId;  // 直接使用基本类型避免嵌入式对象的复杂映射
    
    @Column(name = "entry_date")
    private LocalDate entryDate;
    
    @Column(name = "reference")
    private String reference;
    
    @Column(name = "description")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EntryStatus status;
    
    @Column(name = "fund_id")
    private Integer fundId;
    
    @Column(name = "created_by")
    private Integer creatorId;
    
    @Column(name = "created_at")
    private LocalDateTime creationTimestamp;
    
    @Column(name = "posted_at")
    private LocalDateTime postingTimestamp;
    
    // 完全不使用JPA关联，通过Repository查询获取
    @Transient
    private List<JournalLineData> journalLines = new ArrayList<>();
    
    @Transient
    private List<Object> domainEvents = new ArrayList<>();
    
    // ========== 构造函数 ==========
    
    protected JournalEntryAggregate() {}
    
    private JournalEntryAggregate(Integer companyId, LocalDate entryDate, String description, Integer createdBy) {
        this.companyId = companyId;
        this.entryDate = entryDate;
        this.description = description;
        this.creatorId = createdBy;
        this.status = EntryStatus.DRAFT;
        this.creationTimestamp = LocalDateTime.now();
    }
    
    // ========== 工厂方法 ==========
    
    public static JournalEntryAggregate create(TenantId tenantId, LocalDate entryDate, 
                                             String description, Integer createdBy) {
        return new JournalEntryAggregate(tenantId.getValue(), entryDate, description, createdBy);
    }
    
    // ========== 业务方法 ==========
    
    public void addJournalLine(Integer accountId, Money debitAmount, Money creditAmount, String description) {
        if (status != EntryStatus.DRAFT) {
            throw new IllegalStateException("Cannot modify posted journal entry");
        }
        
        JournalLineData line = new JournalLineData();
        line.accountId = accountId;
        line.debitAmount = debitAmount != null ? debitAmount.getAmount() : BigDecimal.ZERO;
        line.creditAmount = creditAmount != null ? creditAmount.getAmount() : BigDecimal.ZERO;
        line.description = description;
        
        journalLines.add(line);
    }
    
    public void post() {
        if (status != EntryStatus.DRAFT) {
            throw new IllegalStateException("Journal entry is not in draft status");
        }
        
        validateBalance();
        
        this.status = EntryStatus.POSTED;
        this.postingTimestamp = LocalDateTime.now();
        
        // 发布领域事件
        domainEvents.add(new JournalEntryPostedEvent(this.journalId, this.companyId));
    }
    
    public void validateBalance() {
        BigDecimal totalDebit = journalLines.stream()
                .map(line -> line.debitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCredit = journalLines.stream()
                .map(line -> line.creditAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new IllegalStateException("Journal entry is not balanced: Debit=" + totalDebit + ", Credit=" + totalCredit);
        }
    }
    
    public boolean isBalanced() {
        try {
            validateBalance();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }
    
    // ========== Domain Events Management ==========
    
    @DomainEvents
    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    @AfterDomainEventPublication
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
    
    // ========== 向后兼容的接口方法 ==========
    
    public Integer getEntryId() { return journalId; }
    public TenantId getTenantId() { return TenantId.of(companyId); }
    public LocalDate getEntryDate() { return entryDate; }
    public String getReference() { return reference; }
    public String getDescription() { return description; }
    public EntryStatus getStatus() { return status; }
    public Integer getFundId() { return fundId; }
    public Integer getCreatedBy() { return creatorId; }
    public LocalDateTime getCreatedAt() { return creationTimestamp; }
    public LocalDateTime getPostedAt() { return postingTimestamp; }
    
    public List<JournalLineData> getJournalLines() { 
        return new ArrayList<>(journalLines); 
    }
    
    // ========== Setters ==========
    
    public void setEntryId(Integer entryId) { this.journalId = entryId; }
    public void setTenantId(TenantId tenantId) { this.companyId = tenantId.getValue(); }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }
    public void setReference(String reference) { this.reference = reference; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(EntryStatus status) { this.status = status; }
    public void setFundId(Integer fundId) { this.fundId = fundId; }
    public void setCreatedBy(Integer createdBy) { this.creatorId = createdBy; }
    public void setCreatedAt(LocalDateTime createdAt) { this.creationTimestamp = createdAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postingTimestamp = postedAt; }
    public void setJournalLines(List<JournalLineData> journalLines) { 
        this.journalLines = journalLines != null ? journalLines : new ArrayList<>(); 
    }
    
    // ========== 内部数据类 ==========
    
    public static class JournalLineData {
        public Integer accountId;
        public BigDecimal debitAmount;
        public BigDecimal creditAmount;
        public String description;
        
        public Integer getAccountId() { return accountId; }
        public BigDecimal getDebitAmount() { return debitAmount; }
        public BigDecimal getCreditAmount() { return creditAmount; }
        public String getDescription() { return description; }
    }
    
    // ========== Enums ==========
    
    public enum EntryStatus {
        DRAFT, POSTED, VOIDED
    }
    
    // ========== Object Methods ==========
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        JournalEntryAggregate that = (JournalEntryAggregate) obj;
        return journalId != null && journalId.equals(that.journalId);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("JournalEntry{id=%d, date=%s, status=%s}", 
                           journalId, entryDate, status);
    }
}