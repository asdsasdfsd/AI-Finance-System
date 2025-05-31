// backend/src/main/java/org/example/backend/domain/valueobject/CompanyStatus.java
package org.example.backend.domain.valueobject;

import java.util.Objects;

import jakarta.persistence.Embeddable;

/**
 * Company Status value object
 * 
 * Represents the current operational status of a company
 * with business validation rules
 */
@Embeddable
public class CompanyStatus {
    
    public enum Status {
        ACTIVE("Active"),
        SUSPENDED("Suspended"), 
        DELETED("Deleted"),
        PENDING("Pending Activation");
        
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
    protected CompanyStatus() {
        this.status = Status.ACTIVE;
    }
    
    private CompanyStatus(Status status) {
        validateStatus(status);
        this.status = status;
    }
    
    /**
     * Create company status with validation
     */
    public static CompanyStatus of(Status status) {
        return new CompanyStatus(status);
    }
    
    /**
     * Create active status
     */
    public static CompanyStatus active() {
        return new CompanyStatus(Status.ACTIVE);
    }
    
    /**
     * Create suspended status
     */
    public static CompanyStatus suspended() {
        return new CompanyStatus(Status.SUSPENDED);
    }
    
    /**
     * Create deleted status
     */
    public static CompanyStatus deleted() {
        return new CompanyStatus(Status.DELETED);
    }
    
    /**
     * Check if company is operational
     */
    public boolean isOperational() {
        return status == Status.ACTIVE;
    }
    
    /**
     * Check if company can be modified
     */
    public boolean canBeModified() {
        return status != Status.DELETED;
    }
    
    /**
     * Check if company can accept new users
     */
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