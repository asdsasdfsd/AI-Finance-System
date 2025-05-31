
// backend/src/main/java/org/example/backend/domain/event/TransactionApprovedEvent.java
package org.example.backend.domain.event;

import org.example.backend.domain.valueobject.Money;

/**
 * 交易批准事件
 */
public class TransactionApprovedEvent extends DomainEvent {
    private final Integer transactionId;
    private final Money amount;
    private final Integer companyId;
    private final Integer approvedBy;
    
    public TransactionApprovedEvent(Integer transactionId, Money amount, 
                                  Integer companyId, Integer approvedBy) {
        super();
        this.transactionId = transactionId;
        this.amount = amount;
        this.companyId = companyId;
        this.approvedBy = approvedBy;
    }
    
    public Integer getTransactionId() {
        return transactionId;
    }
    
    public Money getAmount() {
        return amount;
    }
    
    public Integer getCompanyId() {
        return companyId;
    }
    
    public Integer getApprovedBy() {
        return approvedBy;
    }
    
    @Override
    public String toString() {
        return String.format("TransactionApprovedEvent{transactionId=%d, amount=%s, companyId=%d, approvedBy=%d}", 
                           transactionId, amount, companyId, approvedBy);
    }
}
