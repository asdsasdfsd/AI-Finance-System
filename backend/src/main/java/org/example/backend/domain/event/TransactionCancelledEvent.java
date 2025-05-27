
// backend/src/main/java/org/example/backend/domain/event/TransactionCancelledEvent.java
package org.example.backend.domain.event;

/**
 * 交易取消事件
 */
public class TransactionCancelledEvent extends DomainEvent {
    private final Integer transactionId;
    private final Integer companyId;
    private final String reason;
    
    public TransactionCancelledEvent(Integer transactionId, Integer companyId) {
        this(transactionId, companyId, null);
    }
    
    public TransactionCancelledEvent(Integer transactionId, Integer companyId, String reason) {
        super();
        this.transactionId = transactionId;
        this.companyId = companyId;
        this.reason = reason;
    }
    
    public Integer getTransactionId() {
        return transactionId;
    }
    
    public Integer getCompanyId() {
        return companyId;
    }
    
    public String getReason() {
        return reason;
    }
    
    @Override
    public String toString() {
        return String.format("TransactionCancelledEvent{transactionId=%d, companyId=%d, reason=%s}", 
                           transactionId, companyId, reason);
    }
}
