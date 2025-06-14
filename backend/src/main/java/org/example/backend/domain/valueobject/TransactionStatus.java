// backend/src/main/java/org/example/backend/domain/valueobject/TransactionStatus.java
package org.example.backend.domain.valueobject;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Transaction Status value object - FIXED with proper JPA mapping
 * 
 * Represents the current state of a financial transaction
 * with proper state transition validation and integer storage (0-5)
 */
@Embeddable
public class TransactionStatus {
    
    public enum Status {
        DRAFT(0, "Draft"),                    // 草稿
        PENDING_APPROVAL(1, "Pending Approval"), // 待审批
        APPROVED(2, "Approved"),              // 已批准
        REJECTED(3, "Rejected"),              // 已拒绝
        CANCELLED(4, "Cancelled"),            // 已取消
        VOIDED(5, "Voided");                 // 已作废
        
        private final int code;
        private final String displayName;
        
        Status(int code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }
        
        public int getCode() {
            return code;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        /**
         * 根据状态码获取枚举值
         */
        public static Status fromCode(int code) {
            for (Status status : values()) {
                if (status.getCode() == code) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid transaction status code: " + code);
        }
    }
    
    // FIXED: 明确指定存储为整数（枚举序号 0-5）
    @Enumerated(EnumType.ORDINAL)  // 关键修复：存储为整数序号
    @Column(name = "status")
    private Status status;
    
    // JPA requires default constructor
    protected TransactionStatus() {
        this.status = Status.APPROVED; // 默认为已批准，而不是草稿
    }
    
    private TransactionStatus(Status status) {
        validateStatus(status);
        this.status = status;
    }
    
    /**
     * Create transaction status
     */
    public static TransactionStatus of(Status status) {
        return new TransactionStatus(status);
    }
    
    /**
     * Create transaction status from integer code
     */
    public static TransactionStatus fromCode(int code) {
        Status status = Status.fromCode(code);
        return new TransactionStatus(status);
    }
    
    /**
     * Create draft status
     */
    public static TransactionStatus draft() {
        return new TransactionStatus(Status.DRAFT);
    }
    
    /**
     * Create approved status
     */
    public static TransactionStatus approved() {
        return new TransactionStatus(Status.APPROVED);
    }
    
    /**
     * Create pending approval status
     */
    public static TransactionStatus pendingApproval() {
        return new TransactionStatus(Status.PENDING_APPROVAL);
    }
    
    /**
     * Approve transaction - validate state transition
     */
    public TransactionStatus approve() {
        if (!canBeApproved()) {
            throw new IllegalStateException("Transaction cannot be approved in current state: " + status);
        }
        return new TransactionStatus(Status.APPROVED);
    }
    
    /**
     * Cancel transaction - validate state transition
     */
    public TransactionStatus cancel() {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Transaction cannot be cancelled in current state: " + status);
        }
        return new TransactionStatus(Status.CANCELLED);
    }
    
    /**
     * Reject transaction - validate state transition
     */
    public TransactionStatus reject() {
        if (!canBeRejected()) {
            throw new IllegalStateException("Transaction cannot be rejected in current state: " + status);
        }
        return new TransactionStatus(Status.REJECTED);
    }
    
    /**
     * Void transaction - validate state transition
     */
    public TransactionStatus voidTransaction() {
        if (!canBeVoided()) {
            throw new IllegalStateException("Transaction cannot be voided in current state: " + status);
        }
        return new TransactionStatus(Status.VOIDED);
    }
    
    /**
     * Submit for approval - validate state transition
     */
    public TransactionStatus submitForApproval() {
        if (status != Status.DRAFT) {
            throw new IllegalStateException("Only draft transactions can be submitted for approval");
        }
        return new TransactionStatus(Status.PENDING_APPROVAL);
    }
    
    /**
     * Check if transaction can be modified
     */
    public boolean canBeModified() {
        return status == Status.DRAFT;
    }
    
    /**
     * Check if transaction can be approved
     */
    public boolean canBeApproved() {
        return status == Status.DRAFT || status == Status.PENDING_APPROVAL;
    }
    
    /**
     * Check if transaction can be rejected
     */
    public boolean canBeRejected() {
        return status == Status.DRAFT || status == Status.PENDING_APPROVAL;
    }
    
    /**
     * Check if transaction can be cancelled
     */
    public boolean canBeCancelled() {
        return status == Status.DRAFT || status == Status.PENDING_APPROVAL;
    }
    
    /**
     * Check if transaction can be voided
     */
    public boolean canBeVoided() {
        return status == Status.APPROVED;
    }
    
    /**
     * Check if transaction is completed
     */
    public boolean isCompleted() {
        return status == Status.APPROVED;
    }
    
    /**
     * Check if transaction is final state
     */
    public boolean isFinalState() {
        return status == Status.APPROVED || 
               status == Status.CANCELLED || 
               status == Status.VOIDED ||
               status == Status.REJECTED;
    }
    
    /**
     * Check if transaction is pending
     */
    public boolean isPending() {
        return status == Status.PENDING_APPROVAL;
    }
    
    /**
     * Check if transaction is draft
     */
    public boolean isDraft() {
        return status == Status.DRAFT;
    }
    
    /**
     * Get status code (0-5)
     */
    public int getStatusCode() {
        return status.getCode();
    }
    
    /**
     * Get display name
     */
    public String getDisplayName() {
        return status.getDisplayName();
    }
    
    private void validateStatus(Status status) {
        if (status == null) {
            throw new IllegalArgumentException("Transaction status cannot be null");
        }
    }
    
    public Status getStatus() {
        return status;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        TransactionStatus that = (TransactionStatus) obj;
        return Objects.equals(status, that.status);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(status);
    }
    
    @Override
    public String toString() {
        return status.getDisplayName() + " (" + status.getCode() + ")";
    }
}