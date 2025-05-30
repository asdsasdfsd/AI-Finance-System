// src/main/java/org/example/backend/model/Transaction.java
package org.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transactionId;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
    
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
    
    @ManyToOne
    @JoinColumn(name = "fund_id")
    private Fund fund;
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    
    private String currency;
    private LocalDate transactionDate;
    private String description;
    private String paymentMethod;
    private String referenceNumber;
    private Boolean isRecurring;
    private Boolean isTaxable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default values
    public Transaction() {
        this.isRecurring = false;
        this.isTaxable = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Transaction type enum
    public enum TransactionType {
        INCOME, EXPENSE
    }
}