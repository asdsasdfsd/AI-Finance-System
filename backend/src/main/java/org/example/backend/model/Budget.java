// src/main/java/org/example/backend/model/Budget.java
package org.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "Budget")
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer budgetId;
    
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
    
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
    
    @ManyToOne
    @JoinColumn(name = "fiscal_period_id")
    private FiscalPeriod fiscalPeriod;
    
    private String name;
    private String description;
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    private BudgetStatus status;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BudgetLine> budgetLines = new ArrayList<>();

    // Default values
    public Budget() {
        this.status = BudgetStatus.DRAFT;
        this.totalAmount = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Budget status enum
    public enum BudgetStatus {
        DRAFT, APPROVED, CLOSED
    }
}