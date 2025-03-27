// backend/src/main/java/org/example/backend/controller/SsoController.java
package org.example.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.example.backend.dto.AuthResponse;
import org.example.backend.service.AuthService;
import org.example.backend.service.SsoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing Microsoft SSO operations
 * Provides endpoints for initiating SSO login and handling callbacks
 */
@RestController
@RequestMapping("/api/sso")
public class SsoController {

    private final SsoService ssoService;
    private final AuthService authService;

    public SsoController(SsoService ssoService, AuthService authService) {
        this.ssoService = ssoService;
        this.authService = authService;
    }

    /**
     * Get Microsoft SSO login URL
     * 
     * @return Login URL for Microsoft SSO
     */
    @GetMapping("/login-url")
    public ResponseEntity<Map<String, String>> getLoginUrl() {
        String url = ssoService.getSsoLoginUrl();
        Map<String, String> response = new HashMap<>();
        response.put("url", url);
        return ResponseEntity.ok(response);
    }

    /**
     * Handle SSO callback
     * This endpoint receives the authorization code from Microsoft
     * 
     * @param code Authorization code
     * @param state State parameter for security validation
     * @return Auth response with JWT token and user details
     */
    @PostMapping("/callback")
    public ResponseEntity<AuthResponse> handleCallback(
            @RequestParam String code,
            @RequestParam String state) {
        return ResponseEntity.ok(authService.authenticateWithSso(code, state));
    }
    
    /**
     * Alternative callback endpoint that supports GET requests
     * Useful for direct browser redirects from Microsoft
     * 
     * @param code Authorization code
     * @param state State parameter for security validation
     * @return Auth response with JWT token and user details
     */
    @GetMapping("/callback")
    public ResponseEntity<Map<String, String>> handleCallbackGet(
            @RequestParam String code,
            @RequestParam String state) {
        // This endpoint is meant to be called directly by the browser redirect
        // It returns a simple page that posts the code to the frontend
        Map<String, String> response = new HashMap<>();
        response.put("code", code);
        response.put("state", state);
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }
}