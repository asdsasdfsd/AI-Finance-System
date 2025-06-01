// backend/src/main/java/org/example/backend/domain/aggregate/journalentry/JournalEntryAggregate.java
package org.example.backend.domain.aggregate.journalentry;

import org.example.backend.domain.valueobject.Money;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.model.JournalLine;
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
 * 会计分录聚合根
 * 
 * 职责：
 * 1. 管理会计分录生命周期
 * 2. 确保借贷平衡
 * 3. 控制分录状态流转
 */
@Entity
@Table(name = "Journal_Entry")
public class JournalEntryAggregate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer entryId;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "company_id"))
    private TenantId tenantId;
    
    @Column(name = "entry_date")
    private LocalDate entryDate;
    
    private String reference;
    private String description;
    
    @Enumerated(EnumType.STRING)
    private EntryStatus status;
    
    @Column(name = "fund_id")
    private Integer fundId;
    
    @Column(name = "created_by")
    private Integer createdBy;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "posted_at")
    private LocalDateTime postedAt;
    
    // 分录行 - 内部管理
    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<JournalLine> journalLines = new ArrayList<>();
    
    @Transient
    private List<Object> domainEvents = new ArrayList<>();
    
    // 构造函数
    protected JournalEntryAggregate() {}
    
    private JournalEntryAggregate(TenantId tenantId, LocalDate entryDate, String description, Integer createdBy) {
        this.tenantId = tenantId;
        this.entryDate = entryDate;
        this.description = description;
        this.createdBy = createdBy;
        this.status = EntryStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
    }
    
    // 工厂方法
    public static JournalEntryAggregate create(TenantId tenantId, LocalDate entryDate, 
                                             String description, Integer createdBy) {
        return new JournalEntryAggregate(tenantId, entryDate, description, createdBy);
    }
    
    // 业务方法
    public void addJournalLine(Integer accountId, Money debitAmount, Money creditAmount, String description) {
        if (status != EntryStatus.DRAFT) {
            throw new IllegalStateException("Cannot modify posted journal entry");
        }
        
        JournalLine line = new JournalLine();
        line.setJournalEntry(this);
        line.setAccountId(accountId);
        line.setDebitAmount(debitAmount != null ? debitAmount.getAmount() : BigDecimal.ZERO);
        line.setCreditAmount(creditAmount != null ? creditAmount.getAmount() : BigDecimal.ZERO);
        line.setDescription(description);
        
        journalLines.add(line);
    }
    
    public void post() {
        if (status != EntryStatus.DRAFT) {
            throw new IllegalStateException("Journal entry is not in draft status");
        }
        
        validateBalance();
        
        this.status = EntryStatus.POSTED;
        this.postedAt = LocalDateTime.now();
        
        // 发布领域事件
        domainEvents.add(new JournalEntryPostedEvent(this.entryId, this.tenantId.getValue()));
    }
    
    public void validateBalance() {
        BigDecimal totalDebit = journalLines.stream()
                .map(line -> line.getDebitAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCredit = journalLines.stream()
                .map(line -> line.getCreditAmount())
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
    
    // Getters
    public Integer getEntryId() { return entryId; }
    public TenantId getTenantId() { return tenantId; }
    public LocalDate getEntryDate() { return entryDate; }
    public String getDescription() { return description; }
    public EntryStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<JournalLine> getJournalLines() { return new ArrayList<>(journalLines); }
    
    public enum EntryStatus {
        DRAFT, POSTED, VOIDED
    }
}

// 分录行实体 - 改为public类
