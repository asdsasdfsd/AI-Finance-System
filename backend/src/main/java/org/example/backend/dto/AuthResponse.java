// backend/src/main/java/org/example/backend/dto/AuthResponse.java
package org.example.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String tokenType;
    private long expiresIn;
    private UserDTO user;
    
    // Flags for tracking auto-provisioning during SSO
    private boolean newUserCreated;
    private boolean newCompanyCreated;
}