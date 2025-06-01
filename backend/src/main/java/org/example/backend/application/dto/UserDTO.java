// backend/src/main/java/org/example/backend/application/dto/UserDTO.java (Enhanced)
package org.example.backend.application.dto;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@Getter
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

    // 明确添加 builder 方法和所有 getter 方法
    public static UserDTOBuilder builder() {
        return new UserDTOBuilder();
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

    public String getFullName() {
        return fullName;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public String getExternalId() {
        return externalId;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public String getTimezone() {
        return timezone;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public Set<String> getRoleNames() {
        return roleNames;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public boolean isActiveAndUnlocked() {
        return isActiveAndUnlocked;
    }

    public boolean isSsoUser() {
        return isSsoUser;
    }

    public boolean isPasswordExpired() {
        return isPasswordExpired;
    }
}