// backend/src/main/java/org/example/backend/exception/GlobalExceptionHandler.java
package org.example.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // Unauthorised Exceptions
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedException(UnauthorizedException e) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> error = new HashMap<>();
        
        error.put("code", "AUTH-SEC-004");
        error.put("message", e.getMessage());
        
        response.put("status", "error");
        response.put("error", error);
        
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
    
    // Resource Not Found Exception
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException e) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> error = new HashMap<>();
        
        error.put("code", "RESOURCE-NOT-FOUND");
        error.put("message", e.getMessage());
        
        response.put("status", "error");
        response.put("error", error);
        
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    
    // Common Exceptions
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> error = new HashMap<>();
        
        error.put("code", "SERVER-ERROR");
        error.put("message", "Internal Server Error: " + e.getMessage());
        
        response.put("status", "error");
        response.put("error", error);
        
        // Print the exception stack to the log instead of returning it to the client
        e.printStackTrace();
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}