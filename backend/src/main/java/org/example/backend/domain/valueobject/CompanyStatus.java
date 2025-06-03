// backend/src/main/java/org/example/backend/domain/valueobject/CompanyStatus.java
package org.example.backend.domain.valueobject;

import java.util.Objects;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Column;

@Embeddable
public class CompanyStatus {
    
    public enum Status {
        ACTIVE("Active"),
        INACTIVE("Inactive");  // 改为 INACTIVE
        
        private final String displayName;
        
        Status(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @Enumerated(EnumType.STRING)  // 关键：指定存储为字符串
    @Column(name = "status", length = 20)
    private Status status;
    
    // JPA requires default constructor
    protected CompanyStatus() {
        this.status = Status.ACTIVE;
    }
    
    private CompanyStatus(Status status) {
        validateStatus(status);
        this.status = status;
    }
    
    public static CompanyStatus of(Status status) {
        return new CompanyStatus(status);
    }
    
    public static CompanyStatus active() {
        return new CompanyStatus(Status.ACTIVE);
    }
    
    public static CompanyStatus inactive() {  // 改为 inactive
        return new CompanyStatus(Status.INACTIVE);
    }
    
    // 移除 suspended() 和 deleted() 方法，因为只有两种状态
    
    public boolean isOperational() {
        return status == Status.ACTIVE;
    }
    
    public boolean canBeModified() {
        return status == Status.ACTIVE;  // 简化逻辑
    }
    
    public boolean canAcceptNewUsers() {
        return status == Status.ACTIVE;
    }
    
    private void validateStatus(Status status) {
        if (status == null) {
            throw new IllegalArgumentException("Company status cannot be null");
        }
    }
    
    public Status getStatus() {
        return status;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        CompanyStatus that = (CompanyStatus) obj;
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