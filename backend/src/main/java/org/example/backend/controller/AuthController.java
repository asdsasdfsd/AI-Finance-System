// backend/src/main/java/org/example/backend/controller/AuthController.java
package org.example.backend.controller;

import org.example.backend.dto.AuthRequest;
import org.example.backend.dto.AuthResponse;
import org.example.backend.dto.UserDTO;
import org.example.backend.dto.RegisterRequest;
import org.example.backend.model.User;
import org.example.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/company/register")
    public ResponseEntity<UserDTO> registerCompany(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registerCompanyAdmin(request));
    }
    
    @PostMapping("/sso/login")
    public ResponseEntity<AuthResponse> ssoLogin(@RequestParam String code, @RequestParam String state) {
        return ResponseEntity.ok(authService.authenticateWithSso(code, state));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token.substring(7));
        return ResponseEntity.ok().build();
    }
}