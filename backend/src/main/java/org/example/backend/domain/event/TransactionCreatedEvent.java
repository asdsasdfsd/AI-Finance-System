
// backend/src/main/java/org/example/backend/domain/event/TransactionCreatedEvent.java
package org.example.backend.domain.event;

import org.example.backend.domain.valueobject.Money;
import org.example.backend.domain.aggregate.transaction.TransactionAggregate.TransactionType;

/**
 * 交易创建事件
 */
public class TransactionCreatedEvent extends DomainEvent {
    private final Integer transactionId;
    private final TransactionType transactionType;
    private final Money amount;
    private final Integer companyId;
    
    public TransactionCreatedEvent(Integer transactionId, TransactionType transactionType, Money amount, Integer companyId) {
        super();
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.companyId = companyId;
    }
    
    public Integer getTransactionId() {
        return transactionId;
    }
    
    public TransactionType getTransactionType() {
        return transactionType;
    }
    
    public Money getAmount() {
        return amount;
    }
    
    public Integer getCompanyId() {
        return companyId;
    }
    
    @Override
    public String toString() {
        return String.format("TransactionCreatedEvent{transactionId=%d, type=%s, amount=%s, companyId=%d}", 
                           transactionId, transactionType, amount, companyId);
    }
}
