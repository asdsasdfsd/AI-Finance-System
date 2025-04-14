// src/main/java/org/example/backend/model/JournalEntry.java
package org.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "Journal_Entry")
public class JournalEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer entryId;
    
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
    
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
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JournalLine> journalLines = new ArrayList<>();

    // Default values
    public JournalEntry() {
        this.status = EntryStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Journal entry status enum
    public enum EntryStatus {
        DRAFT, POSTED, VOIDED
    }
}