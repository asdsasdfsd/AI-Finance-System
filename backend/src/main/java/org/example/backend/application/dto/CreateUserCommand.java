// backend/src/main/java/org/example/backend/application/dto/CreateUserCommand.java
package org.example.backend.application.dto;

import java.util.Set;

import lombok.Builder;
import lombok.Data;

/**
 * Command for creating new users
 */
@Data
@Builder
public class CreateUserCommand {
    private String username;
    private String email;
    private String password;
    private String fullName;
    @Builder.Default
    private Boolean enabled = true;
    private Integer companyId;
    private Integer departmentId;
    private String preferredLanguage;
    private String timezone;
    private Set<String> roleNames;
    
    /**
     * Get enabled status with null safety - returns primitive boolean
     */
    public boolean getEnabled() {
        return enabled != null ? enabled.booleanValue() : true;
    }
    
    /**
     * Get enabled status as Boolean object for potential null checking
     */
    public Boolean getEnabledAsBoolean() {
        return enabled;
    }
}