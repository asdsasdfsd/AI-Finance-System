// backend/src/main/java/org/example/backend/util/JwtContextUtil.java
package org.example.backend.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * JWT Context Utility for extracting user information from current security context
 */
@Component
public class JwtContextUtil {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Get current authenticated username
     * 
     * @return Username or null if not authenticated
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * Get current user's company ID from JWT token
     * 
     * @return Company ID or null if not found
     */
    public Integer getCurrentCompanyId() {
        String token = getCurrentJwtToken();
        if (token != null) {
            try {
                Integer companyId = jwtUtil.extractCompanyId(token);
                if (companyId != null) {
                    return companyId;
                }
            } catch (Exception e) {
                System.err.println("Error extracting company ID from token: " + e.getMessage());
            }
        }
        
        // Log warning if no company ID found
        System.err.println("Warning: Could not extract company ID from JWT token. Token: " + (token != null ? "present" : "null"));
        return null;
    }

    /**
     * Get current user's user ID from JWT token
     * 
     * @return User ID or null if not found
     */
    public Integer getCurrentUserId() {
        String token = getCurrentJwtToken();
        if (token != null) {
            try {
                Integer userId = jwtUtil.extractUserId(token);
                if (userId != null) {
                    return userId;
                }
            } catch (Exception e) {
                System.err.println("Error extracting user ID from token: " + e.getMessage());
            }
        }
        
        // Log warning if no user ID found
        System.err.println("Warning: Could not extract user ID from JWT token. Token: " + (token != null ? "present" : "null"));
        return null;
    }

    /**
     * Get current user's full name from JWT token
     * 
     * @return Full name or null if not found
     */
    public String getCurrentUserFullName() {
        String token = getCurrentJwtToken();
        if (token != null) {
            try {
                return jwtUtil.extractFullName(token);
            } catch (Exception e) {
                System.err.println("Error extracting full name from token: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Get current JWT token from various sources
     * 
     * @return JWT token string or null if not found
     */
    public String getCurrentJwtToken() {
        // Method 1: Get token from Security Context (preferred)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof String) {
            String token = (String) authentication.getCredentials();
            if (token != null && !token.isEmpty()) {
                return token;
            }
        }

        // Method 2: Get token from HTTP request header (fallback)
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpServletRequest request = requestAttributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    return authHeader.substring(7);
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting token from request: " + e.getMessage());
        }

        return null;
    }

    /**
     * Check if current user belongs to specific company
     * 
     * @param companyId Company ID to check
     * @return true if user belongs to company, false otherwise
     */
    public boolean belongsToCompany(Integer companyId) {
        Integer currentCompanyId = getCurrentCompanyId();
        return currentCompanyId != null && currentCompanyId.equals(companyId);
    }

    /**
     * Validate that current user has access to the specified company
     * 
     * @param companyId Company ID to validate
     * @throws SecurityException if user doesn't belong to company
     */
    public void validateCompanyAccess(Integer companyId) {
        if (!belongsToCompany(companyId)) {
            throw new SecurityException("User does not have access to company ID: " + companyId);
        }
    }

    /**
     * Get current user context information
     * 
     * @return UserContext object with current user information
     */
    public UserContext getCurrentUserContext() {
        return UserContext.builder()
                .username(getCurrentUsername())
                .userId(getCurrentUserId())
                .companyId(getCurrentCompanyId())
                .fullName(getCurrentUserFullName())
                .build();
    }

    /**
     * Inner class to hold user context information
     */
    public static class UserContext {
        private String username;
        private Integer userId;
        private Integer companyId;
        private String fullName;

        private UserContext(String username, Integer userId, Integer companyId, String fullName) {
            this.username = username;
            this.userId = userId;
            this.companyId = companyId;
            this.fullName = fullName;
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public String getUsername() { return username; }
        public Integer getUserId() { return userId; }
        public Integer getCompanyId() { return companyId; }
        public String getFullName() { return fullName; }

        // Builder pattern
        public static class Builder {
            private String username;
            private Integer userId;
            private Integer companyId;
            private String fullName;

            public Builder username(String username) {
                this.username = username;
                return this;
            }

            public Builder userId(Integer userId) {
                this.userId = userId;
                return this;
            }

            public Builder companyId(Integer companyId) {
                this.companyId = companyId;
                return this;
            }

            public Builder fullName(String fullName) {
                this.fullName = fullName;
                return this;
            }

            public UserContext build() {
                return new UserContext(username, userId, companyId, fullName);
            }
        }
    }
}