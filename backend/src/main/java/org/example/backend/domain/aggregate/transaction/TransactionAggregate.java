// backend/src/main/java/org/example/backend/domain/aggregate/transaction/TransactionAggregate.java
package org.example.backend.domain.aggregate.transaction;

import org.example.backend.domain.valueobject.Money;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.TransactionStatus;
import org.example.backend.domain.event.TransactionCreatedEvent;
import org.example.backend.domain.event.TransactionApprovedEvent;
import org.example.backend.domain.event.TransactionCancelledEvent;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Transaction Aggregate Root - Fixed version with proper status initialization
 * 
 * Responsibilities:
 * 1. Manage financial transaction lifecycle
 * 2. Enforce business rules and invariants
 * 3. Publish domain events for state changes
 * 4. Maintain transaction integrity
 */
@Entity
@Table(name = "Transaction", indexes = {
    @Index(name = "idx_transaction_company_date", columnList = "company_id, transaction_date"),
    @Index(name = "idx_transaction_user", columnList = "user_id"),
    @Index(name = "idx_transaction_status", columnList = "status"),
    @Index(name = "idx_transaction_type", columnList = "transaction_type")
})
public class TransactionAggregate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Integer transactionId;
    
    // Money value object - embedded
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "amount")),
        @AttributeOverride(name = "currencyCode", column = @Column(name = "currency"))
    })
    private Money money;
    
    // Transaction status value object - embedded
    @Embedded
    @AttributeOverride(name = "status", column = @Column(name = "status"))
    private TransactionStatus transactionStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "payment_method", length = 100)
    private String paymentMethod;
    
    @Column(name = "reference_number", length = 100)
    private String referenceNumber;
    
    @Column(name = "is_recurring")
    private Boolean isRecurring;
    
    @Column(name = "is_taxable")
    private Boolean isTaxable;
    
    // External aggregate references - only store IDs to maintain aggregate boundaries
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "company_id", nullable = false))
    private TenantId tenantId;
    
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    @Column(name = "department_id")
    private Integer departmentId;
    
    @Column(name = "fund_id")
    private Integer fundId;
    
    @Column(name = "category_id")
    private Integer categoryId;
    
    // Audit fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "approved_by")
    private Integer approvedBy;
    
    // Domain events
    @Transient
    private List<Object> domainEvents = new ArrayList<>();
    
    // ========== Constructors ==========
    
    protected TransactionAggregate() {
        // JPA requires default constructor
        // Initialize critical fields to prevent null pointer exceptions
        initializeDefaults();
    }
    
    private TransactionAggregate(TransactionType type, Money money, LocalDate transactionDate, 
                       String description, TenantId tenantId, Integer userId) {
        validateTransactionCreation(type, money, transactionDate, tenantId, userId);
        
        this.transactionType = type;
        this.money = money;
        this.transactionDate = transactionDate;
        this.description = description;
        this.tenantId = tenantId;
        this.userId = userId;
        this.transactionStatus = TransactionStatus.draft();
        this.isRecurring = false;
        this.isTaxable = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // Publish domain event
        addDomainEvent(new TransactionCreatedEvent(this.transactionId, type, money, tenantId.getValue()));
    }
    
    /**
     * Initialize default values to prevent null pointer exceptions
     * This is called by JPA after loading from database
     */
    @PostLoad
    @PostPersist
    @PostUpdate
    private void initializeDefaults() {
        if (this.transactionStatus == null) {
            this.transactionStatus = TransactionStatus.draft();
        }
        if (this.isRecurring == null) {
            this.isRecurring = false;
        }
        if (this.isTaxable == null) {
            this.isTaxable = false;
        }
        if (this.domainEvents == null) {
            this.domainEvents = new ArrayList<>();
        }
    }
    
    // ========== Factory Methods ==========
    
    /**
     * Create income transaction
     */
    public static TransactionAggregate createIncome(Money amount, LocalDate date, String description,
                                          TenantId tenantId, Integer userId) {
        return new TransactionAggregate(TransactionType.INCOME, amount, date, description, tenantId, userId);
    }
    
    /**
     * Create expense transaction
     */
    public static TransactionAggregate createExpense(Money amount, LocalDate date, String description,
                                           TenantId tenantId, Integer userId) {
        return new TransactionAggregate(TransactionType.EXPENSE, amount, date, description, tenantId, userId);
    }
    
    // ========== Business Methods ==========
    
    /**
     * Update transaction details - only allowed in draft status
     */
    public void updateTransaction(Money newMoney, String newDescription, 
                                String newPaymentMethod, String newReferenceNumber) {
        ensureCanBeModified();
        validateMoney(newMoney);
        
        this.money = newMoney;
        this.description = newDescription;
        this.paymentMethod = newPaymentMethod;
        this.referenceNumber = newReferenceNumber;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Set transaction category
     */
    public void setCategory(Integer categoryId) {
        ensureNotInFinalState();
        this.categoryId = categoryId;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Set transaction department
     */
    public void setDepartment(Integer departmentId) {
        ensureNotInFinalState();
        this.departmentId = departmentId;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Set transaction fund
     */
    public void setFund(Integer fundId) {
        ensureNotInFinalState();
        this.fundId = fundId;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Mark transaction as recurring
     */
    public void markAsRecurring() {
        ensureNotInFinalState();
        this.isRecurring = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Mark transaction as taxable
     */
    public void markAsTaxable() {
        ensureNotInFinalState();
        this.isTaxable = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Approve transaction
     */
    public void approve(Integer approverUserId) {
        validateApprover(approverUserId);
        
        this.transactionStatus = this.transactionStatus.approve();
        this.approvedAt = LocalDateTime.now();
        this.approvedBy = approverUserId;
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new TransactionApprovedEvent(this.transactionId, this.money, 
                                                   this.tenantId.getValue(), approverUserId));
    }
    
    /**
     * Cancel transaction
     */
    public void cancel() {
        this.transactionStatus = this.transactionStatus.cancel();
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new TransactionCancelledEvent(this.transactionId, this.tenantId.getValue()));
    }
    
    /**
     * Void transaction (for approved transactions)
     */
    public void voidTransaction(Integer voidedBy, String reason) {
        this.transactionStatus = this.transactionStatus.voidTransaction();
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new TransactionCancelledEvent(this.transactionId, this.tenantId.getValue(), reason));
    }
    
    // ========== Query Methods ==========
    
    /**
     * Check if transaction can be modified
     */
    public boolean canModify() {
        return transactionStatus != null && transactionStatus.canBeModified();
    }
    
    /**
     * Check if transaction can be approved
     */
    public boolean canBeApproved() {
        return transactionStatus != null && transactionStatus.canBeApproved() && 
               money != null && money.isPositive();
    }
    
    /**
     * Check if transaction is completed
     */
    public boolean isCompleted() {
        return transactionStatus != null && transactionStatus.isCompleted();
    }
    
    /**
     * Check if transaction is in final state
     */
    public boolean isFinalState() {
        return transactionStatus != null && transactionStatus.isFinalState();
    }
    
    /**
     * Get display amount
     */
    public String getDisplayAmount() {
        return money != null ? money.toDisplayString() : "0.00";
    }
    
    /**
     * Calculate tax amount
     */
    public Money calculateTax(double taxRate) {
        if (!Boolean.TRUE.equals(isTaxable) || money == null) {
            return Money.zero(money != null ? money.getCurrencyCode() : "CNY");
        }
        return money.multiply(taxRate);
    }
    
    // ========== Validation Methods ==========
    
    private void ensureCanBeModified() {
        if (!canModify()) {
            throw new IllegalStateException("Transaction cannot be modified in current state: " + 
                (transactionStatus != null ? transactionStatus.getStatus() : "NULL"));
        }
    }
    
    private void ensureNotInFinalState() {
        if (isFinalState()) {
            throw new IllegalStateException("Transaction is in final state and cannot be modified: " + 
                (transactionStatus != null ? transactionStatus.getStatus() : "NULL"));
        }
    }
    
    private void validateTransactionCreation(TransactionType type, Money money, LocalDate date, 
                                           TenantId tenantId, Integer userId) {
        if (type == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }
        if (money == null) {
            throw new IllegalArgumentException("Money amount cannot be null");
        }
        if (date == null) {
            throw new IllegalArgumentException("Transaction date cannot be null");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        validateMoney(money);
    }
    
    private void validateMoney(Money money) {
        if (money == null || !money.isPositive()) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
    }
    
    private void validateApprover(Integer approverUserId) {
        if (approverUserId == null) {
            throw new IllegalArgumentException("Approver user ID cannot be null");
        }
        if (approverUserId.equals(this.userId)) {
            throw new IllegalArgumentException("User cannot approve their own transaction");
        }
    }
    
    // ========== Domain Event Methods ==========
    
    protected void addDomainEvent(Object event) {
        if (this.domainEvents == null) {
            this.domainEvents = new ArrayList<>();
        }
        this.domainEvents.add(event);
    }
    
    public List<Object> getDomainEvents() {
        return this.domainEvents != null ? new ArrayList<>(this.domainEvents) : new ArrayList<>();
    }
    
    public void clearDomainEvents() {
        if (this.domainEvents != null) {
            this.domainEvents.clear();
        }
    }
    
    // ========== Getters ==========
    
    public Integer getTransactionId() {
        return transactionId;
    }
    
    public Money getMoney() {
        return money;
    }
    
    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }
    
    public TransactionType getTransactionType() {
        return transactionType;
    }
    
    public LocalDate getTransactionDate() {
        return transactionDate;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public String getReferenceNumber() {
        return referenceNumber;
    }
    
    public Boolean getIsRecurring() {
        return isRecurring;
    }
    
    public Boolean getIsTaxable() {
        return isTaxable;
    }
    
    public TenantId getTenantId() {
        return tenantId;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public Integer getDepartmentId() {
        return departmentId;
    }
    
    public Integer getFundId() {
        return fundId;
    }
    
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }
    
    public Integer getApprovedBy() {
        return approvedBy;
    }
    
    // ========== Transaction Types ==========
    
    public enum TransactionType {
        INCOME("Income", "收入"),
        EXPENSE("Expense", "支出");
        
        private final String englishName;
        private final String chineseName;
        
        TransactionType(String englishName, String chineseName) {
            this.englishName = englishName;
            this.chineseName = chineseName;
        }
        
        public String getEnglishName() {
            return englishName;
        }
        
        public String getChineseName() {
            return chineseName;
        }
        
        public String getDisplayName() {
            return englishName;
        }
    }
}