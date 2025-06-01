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

    // 明确添加所有 getter 方法以确保兼容性
    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
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

    public Set<String> getRoleNames() {
        return roleNames;
    }
}