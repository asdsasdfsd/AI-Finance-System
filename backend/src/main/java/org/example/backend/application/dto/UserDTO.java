// backend/src/main/java/org/example/backend/application/dto/UserDTO.java (Enhanced)
package org.example.backend.application.dto;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Builder;
import lombok.Data;

/**
 * User Data Transfer Object for API responses
 */
@Data
@Builder
public class UserDTO {
    private Integer userId;
    private String username;
    private String email;
    private String fullName;
    private Boolean enabled;
    private String externalId;
    private Integer tenantId;
    private Integer departmentId;
    private String preferredLanguage;
    private String timezone;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime passwordChangedAt;
    private Set<String> roleNames;
    private Integer failedLoginAttempts;
    private LocalDateTime lockedUntil;
    
    // Computed fields for business logic
    private boolean isLocked;
    private boolean isActiveAndUnlocked;
    private boolean isSsoUser;
    private boolean isPasswordExpired;
}