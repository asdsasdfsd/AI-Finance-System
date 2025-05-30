// src/main/java/org/example/backend/model/Account.java
package org.example.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "Account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer accountId;
    
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
    
    private String accountCode;
    private String name;
    
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    
    @ManyToOne
    @JoinColumn(name = "parent_account_id")
    private Account parentAccount;
    
    private Boolean isActive;
    
    @Enumerated(EnumType.STRING)
    private BalanceDirection balanceDirection;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default values
    public Account() {
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Account type enum
    public enum AccountType {
        ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE
    }
    
    // Balance direction enum
    public enum BalanceDirection {
        DEBIT, CREDIT
    }
}