
// backend/src/main/java/org/example/backend/domain/event/UserCreatedEvent.java
package org.example.backend.domain.event;

/**
 * 用户创建事件
 */
public class UserCreatedEvent extends DomainEvent {
    private final Integer userId;
    private final String username;
    private final String email;
    private final Integer companyId;
    
    public UserCreatedEvent(Integer userId, String username, String email, Integer companyId) {
        super();
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.companyId = companyId;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public Integer getCompanyId() {
        return companyId;
    }
    
    @Override
    public String toString() {
        return String.format("UserCreatedEvent{userId=%d, username=%s, email=%s, companyId=%d}", 
                           userId, username, email, companyId);
    }
}
