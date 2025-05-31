// backend/src/main/java/org/example/backend/application/dto/ChangePasswordCommand.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Command for changing user password
 */
@Data
@Builder
public class ChangePasswordCommand {
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}