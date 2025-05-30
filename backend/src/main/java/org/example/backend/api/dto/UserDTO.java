// backend/src/main/java/org/example/backend/dto/UserDTO.java (updated)
package org.example.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Set;

@Data
@Builder
public class UserDTO {
    private Integer userId;
    private String username;
    private String fullName;
    private String email;
    private Integer companyId;
    private String companyName;
    private Set<String> roles;
}