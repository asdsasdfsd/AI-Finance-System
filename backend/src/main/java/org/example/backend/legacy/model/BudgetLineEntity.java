// src/main/java/org/example/backend/model/BudgetLine.java
package org.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "Budget_Line")
public class BudgetLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer lineId;
    
    @ManyToOne
    @JoinColumn(name = "budget_id")
    private Budget budget;
    
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
    
    private BigDecimal amount;
    private BigDecimal usedAmount;
    private String notes;

    // Default values
    public BudgetLine() {
        this.amount = BigDecimal.ZERO;
        this.usedAmount = BigDecimal.ZERO;
    }
}