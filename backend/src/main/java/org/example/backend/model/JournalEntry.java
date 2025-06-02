// backend/src/main/java/org/example/backend/model/JournalEntry.java
package org.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JournalEntry - 传统实体，修复版本
 * 
 * 修复了与JournalLine的关联映射冲突问题
 */
@Data
@Entity
@Table(name = "Journal_Entry")
public class JournalEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entry_id") // 明确指定列名
    private Integer entryId;
    
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
    
    @Column(name = "entry_date")
    private LocalDate entryDate;
    
    private String reference;
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "fund_id")
    private Fund fund;
    
    @Enumerated(EnumType.STRING)
    private EntryStatus status;
    
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @Column(name = "created_at") // 明确指定列名
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 修复关联：使用JoinColumn而不是mappedBy，避免冲突
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id") // 通过外键关联
    private List<JournalLine> journalLines = new ArrayList<>();

    // Default values
    public JournalEntry() {
        this.status = EntryStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // 明确添加 getter 方法以确保可访问性
    public Integer getEntryId() {
        return entryId;
    }
    
    public void setEntryId(Integer entryId) {
        this.entryId = entryId;
    }
    
    public Company getCompany() {
        return company;
    }
    
    public void setCompany(Company company) {
        this.company = company;
    }
    
    public LocalDate getEntryDate() {
        return entryDate;
    }
    
    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }
    
    public String getReference() {
        return reference;
    }
    
    public void setReference(String reference) {
        this.reference = reference;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Fund getFund() {
        return fund;
    }
    
    public void setFund(Fund fund) {
        this.fund = fund;
    }
    
    public EntryStatus getStatus() {
        return status;
    }
    
    public void setStatus(EntryStatus status) {
        this.status = status;
    }
    
    public User getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<JournalLine> getJournalLines() {
        return journalLines;
    }
    
    public void setJournalLines(List<JournalLine> journalLines) {
        this.journalLines = journalLines;
    }
    
    // Journal entry status enum
    public enum EntryStatus {
        DRAFT, POSTED, VOIDED
    }
}