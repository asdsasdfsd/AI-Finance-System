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
    @Column(name = "transaction_id")
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
    
    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType;
    
    @Column(name = "currency", length = 3)
    private String currency;
    
    @Column(name = "transaction_date")
    private LocalDate transactionDate;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "payment_method", length = 100)
    private String paymentMethod;
    
    @Column(name = "reference_number", length = 100)
    private String referenceNumber;
    
    @Column(name = "is_recurring")
    private Boolean isRecurring;
    
    @Column(name = "is_taxable")
    private Boolean isTaxable;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
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