// backend/src/main/java/org/example/backend/dto/AuthRequest.java
package org.example.backend.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
    private boolean rememberMe;
}