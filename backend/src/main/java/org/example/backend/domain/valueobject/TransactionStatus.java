// backend/src/main/java/org/example/backend/domain/valueobject/TransactionStatus.java
package org.example.backend.domain.valueobject;

import java.util.Objects;

import jakarta.persistence.Embeddable;

/**
 * Transaction Status value object
 * 
 * Represents the current state of a financial transaction
 * with proper state transition validation
 */
@Embeddable
public class TransactionStatus {
    
    public enum Status {
        DRAFT("Draft"),
        PENDING_APPROVAL("Pending Approval"),
        APPROVED("Approved"),
        REJECTED("Rejected"),
        CANCELLED("Cancelled"),
        VOIDED("Voided");
        
        private final String displayName;
        
        Status(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private Status status;
    
    // JPA requires default constructor
    protected TransactionStatus() {
        this.status = Status.DRAFT;
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
     * Void transaction - validate state transition
     */
    public TransactionStatus voidTransaction() {
        if (!canBeVoided()) {
            throw new IllegalStateException("Transaction cannot be voided in current state: " + status);
        }
        return new TransactionStatus(Status.VOIDED);
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
        return status.getDisplayName();
    }
}