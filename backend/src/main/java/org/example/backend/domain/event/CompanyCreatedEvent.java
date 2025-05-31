
// backend/src/main/java/org/example/backend/domain/event/CompanyCreatedEvent.java
package org.example.backend.domain.event;

/**
 * 公司创建事件
 */
public class CompanyCreatedEvent extends DomainEvent {
    private final Integer companyId;
    private final String companyName;
    private final String email;
    private final Integer createdBy;
    
    public CompanyCreatedEvent(Integer companyId, String companyName, String email, Integer createdBy) {
        super();
        this.companyId = companyId;
        this.companyName = companyName;
        this.email = email;
        this.createdBy = createdBy;
    }
    
    public Integer getCompanyId() {
        return companyId;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public Integer getCreatedBy() {
        return createdBy;
    }
    
    @Override
    public String toString() {
        return String.format("CompanyCreatedEvent{companyId=%d, companyName=%s, email=%s, createdBy=%d}", 
                           companyId, companyName, email, createdBy);
    }
}