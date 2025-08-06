// backend/src/main/java/org/example/backend/exception/MissingCompanyIdException.java
package org.example.backend.exception;

/**
 * Exception thrown when company ID cannot be determined from JWT, context, or request parameters
 * 
 * This is a security-critical exception that prevents unauthorized access to company data
 */
public class MissingCompanyIdException extends RuntimeException {
    
    public MissingCompanyIdException() {
        super("Company ID is required but could not be determined from authentication context");
    }
    
    public MissingCompanyIdException(String message) {
        super(message);
    }
    
    public MissingCompanyIdException(String message, Throwable cause) {
        super(message, cause);
    }
}