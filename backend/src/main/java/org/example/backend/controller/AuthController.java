// backend/src/main/java/org/example/backend/controller/AuthController.java
package org.example.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.example.backend.dto.AuthRequest;
import org.example.backend.dto.AuthResponse;
import org.example.backend.dto.RegisterRequest;
import org.example.backend.dto.UserDTO;
import org.example.backend.service.AuthService;
import org.example.backend.service.SsoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SsoService ssoService;

    public AuthController(AuthService authService, SsoService ssoService) {
        this.authService = authService;
        this.ssoService = ssoService;
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
    
    /**
     * Get SSO login URL endpoint
     * Returns URL to redirect user for Microsoft login
     * 
     * @return Response with login URL
     */
    @GetMapping("/sso/login-url")
    public ResponseEntity<Map<String, String>> getSsoLoginUrl() {
        Map<String, String> response = new HashMap<>();
        try {
            String url = ssoService.getSsoLoginUrl();
            response.put("url", url);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // If SSO is not properly configured, return error message
            response.put("error", "Microsoft SSO is not properly configured");
            response.put("message", "Please contact your administrator to set up Microsoft SSO");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token.substring(7));
        return ResponseEntity.ok().build();
    }
}