// backend/src/main/java/org/example/backend/domain/event/JournalEntryPostedEvent.java
package org.example.backend.domain.event;

/**
 * 会计分录过账事件
 */
public class JournalEntryPostedEvent extends DomainEvent {
    private final Integer entryId;
    private final Integer companyId;
    
    public JournalEntryPostedEvent(Integer entryId, Integer companyId) {
        super();
        this.entryId = entryId;
        this.companyId = companyId;
    }
    
    public Integer getEntryId() {
        return entryId;
    }
    
    public Integer getCompanyId() {
        return companyId;
    }
    
    @Override
    public String toString() {
        return String.format("JournalEntryPostedEvent{entryId=%d, companyId=%d}", 
                           entryId, companyId);
    }
}

