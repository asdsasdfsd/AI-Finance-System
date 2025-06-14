// backend/src/main/java/org/example/backend/controller/HealthController.java
package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.aggregate.company.CompanyAggregateRepository;
import org.example.backend.domain.aggregate.transaction.TransactionAggregateRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller
 * Provides system health status endpoints for frontend connectivity testing
 */
@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class HealthController {

    private final CompanyAggregateRepository companyRepository;
    private final TransactionAggregateRepository transactionRepository;

    /**
     * Basic health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "Financial Management System");
        health.put("version", "1.0.0");
        
        log.info("Health check performed successfully");
        return ResponseEntity.ok(health);
    }

    /**
     * Detailed health check with database connectivity
     */
    @GetMapping("/health/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealthCheck() {
        Map<String, Object> health = new HashMap<>();
        Map<String, Object> components = new HashMap<>();
        
        try {
            // Test database connectivity
            long companyCount = companyRepository.count();
            long transactionCount = transactionRepository.count();
            
            components.put("database", Map.of(
                "status", "UP",
                "companyCount", companyCount,
                "transactionCount", transactionCount
            ));
            
            health.put("status", "UP");
            health.put("components", components);
            health.put("timestamp", LocalDateTime.now());
            health.put("service", "Financial Management System");
            
            log.info("Detailed health check performed successfully");
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            log.error("Health check failed", e);
            
            components.put("database", Map.of(
                "status", "DOWN",
                "error", e.getMessage()
            ));
            
            health.put("status", "DOWN");
            health.put("components", components);
            health.put("timestamp", LocalDateTime.now());
            health.put("error", "System unhealthy");
            
            return ResponseEntity.status(503).body(health);
        }
    }

    /**
     * API connectivity test endpoint
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "pong");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    /**
     * CORS preflight test endpoint
     */
    @RequestMapping(value = "/cors-test", method = {RequestMethod.GET, RequestMethod.OPTIONS})
    public ResponseEntity<Map<String, Object>> corsTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "CORS is working");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}