// backend/src/main/java/org/example/backend/model/Transaction.java
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

    // FIXED: 添加status字段
    @Enumerated(EnumType.ORDINAL) // 存储为整数 0-5
    @Column(name = "status")
    private TransactionStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Default values
    public Transaction() {
        this.isRecurring = false;
        this.isTaxable = false;
        this.status = TransactionStatus.APPROVED; // 默认为已批准
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Transaction type enum
    public enum TransactionType {
        INCOME, EXPENSE
    }

    // FIXED: 添加TransactionStatus枚举
    /**
     * Transaction Status Enum - 对应整数值 0-5
     */
    public enum TransactionStatus {
        DRAFT(0, "Draft"),                    // 草稿
        PENDING_APPROVAL(1, "Pending Approval"), // 待审批
        APPROVED(2, "Approved"),              // 已批准
        REJECTED(3, "Rejected"),              // 已拒绝
        CANCELLED(4, "Cancelled"),            // 已取消
        VOIDED(5, "Voided");                 // 已作废
        
        private final int code;
        private final String displayName;
        
        TransactionStatus(int code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }
        
        /**
         * 获取状态码 (0-5)
         */
        public int getCode() {
            return code;
        }
        
        /**
         * 获取显示名称
         */
        public String getDisplayName() {
            return displayName;
        }
        
        /**
         * 根据状态码获取枚举值
         */
        public static TransactionStatus fromCode(int code) {
            for (TransactionStatus status : values()) {
                if (status.getCode() == code) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid transaction status code: " + code);
        }
        
        /**
         * 检查是否可以修改
         */
        public boolean canBeModified() {
            return this == DRAFT;
        }
        
        /**
         * 检查是否可以批准
         */
        public boolean canBeApproved() {
            return this == DRAFT || this == PENDING_APPROVAL;
        }
        
        /**
         * 检查是否可以取消
         */
        public boolean canBeCancelled() {
            return this == DRAFT || this == PENDING_APPROVAL;
        }
        
        /**
         * 检查是否可以作废
         */
        public boolean canBeVoided() {
            return this == APPROVED;
        }
        
        /**
         * 检查是否已完成
         */
        public boolean isCompleted() {
            return this == APPROVED;
        }
        
        /**
         * 检查是否为最终状态
         */
        public boolean isFinalState() {
            return this == APPROVED || this == CANCELLED || this == VOIDED || this == REJECTED;
        }
    }

    // FIXED: 添加便捷方法
    /**
     * 检查交易是否已批准
     */
    public boolean isApproved() {
        return status == TransactionStatus.APPROVED;
    }
    
    /**
     * 检查交易是否可以修改
     */
    public boolean canBeModified() {
        return status != null && status.canBeModified();
    }
    
    /**
     * 批准交易
     */
    public void approve() {
        if (status == null || !status.canBeApproved()) {
            throw new IllegalStateException("Transaction cannot be approved in current state: " + status);
        }
        this.status = TransactionStatus.APPROVED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 取消交易
     */
    public void cancel() {
        if (status == null || !status.canBeCancelled()) {
            throw new IllegalStateException("Transaction cannot be cancelled in current state: " + status);
        }
        this.status = TransactionStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 作废交易
     */
    public void voidTransaction() {
        if (status == null || !status.canBeVoided()) {
            throw new IllegalStateException("Transaction cannot be voided in current state: " + status);
        }
        this.status = TransactionStatus.VOIDED;
        this.updatedAt = LocalDateTime.now();
    }
}