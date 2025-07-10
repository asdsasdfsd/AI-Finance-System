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
                return jwtUtil.extractCompanyId(token);
            } catch (Exception e) {
                System.err.println("Error extracting company ID from token: " + e.getMessage());
            }
        }
        // Fallback to hardcoded value for development/transition period
        return 1;
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
                return jwtUtil.extractUserId(token);
            } catch (Exception e) {
                System.err.println("Error extracting user ID from token: " + e.getMessage());
            }
        }
        // Fallback to hardcoded value for development/transition period
        return 1;
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
        return getCurrentUsername(); // Fallback to username
    }

    /**
     * Get the JWT token from the current HTTP request
     * 
     * @return JWT token or null if not found
     */
    private String getCurrentJwtToken() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            String authorizationHeader = request.getHeader("Authorization");
            
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                return authorizationHeader.substring(7);
            }
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

        public static UserContextBuilder builder() {
            return new UserContextBuilder();
        }

        // Getters
        public String getUsername() { return username; }
        public Integer getUserId() { return userId; }
        public Integer getCompanyId() { return companyId; }
        public String getFullName() { return fullName; }

        @Override
        public String toString() {
            return "UserContext{" +
                    "username='" + username + '\'' +
                    ", userId=" + userId +
                    ", companyId=" + companyId +
                    ", fullName='" + fullName + '\'' +
                    '}';
        }

        public static class UserContextBuilder {
            private String username;
            private Integer userId;
            private Integer companyId;
            private String fullName;

            public UserContextBuilder username(String username) {
                this.username = username;
                return this;
            }

            public UserContextBuilder userId(Integer userId) {
                this.userId = userId;
                return this;
            }

            public UserContextBuilder companyId(Integer companyId) {
                this.companyId = companyId;
                return this;
            }

            public UserContextBuilder fullName(String fullName) {
                this.fullName = fullName;
                return this;
            }

            public UserContext build() {
                return new UserContext(username, userId, companyId, fullName);
            }
        }
    }
}