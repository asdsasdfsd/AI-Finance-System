// backend/src/main/java/org/example/backend/application/dto/UpdateUserCommand.java
package org.example.backend.application.dto;

import java.util.Set;
import lombok.Builder;
import lombok.Data;

/**
 * Command for updating existing users
 */
@Data
@Builder
public class UpdateUserCommand {
    private String fullName;
    private String email;
    private String password;
    private Integer departmentId;
    private String preferredLanguage;
    private String timezone;
    private Set<String> roleNames;
}