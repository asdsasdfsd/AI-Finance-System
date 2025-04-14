// src/main/java/org/example/backend/controller/AuditLogController.java
package org.example.backend.controller;

import org.example.backend.model.AuditLog;
import org.example.backend.model.User;
import org.example.backend.service.AuditLogService;
import org.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {
    @Autowired
    private AuditLogService auditLogService;
    
    @Autowired
    private UserService userService;

    @GetMapping
    public List<AuditLog> getAll() {
        return auditLogService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLog> getById(@PathVariable Integer id) {
        AuditLog auditLog = auditLogService.findById(id);
        return auditLog != null ? ResponseEntity.ok(auditLog) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/user/{userId}")
    public List<AuditLog> getByUser(@PathVariable Integer userId) {
        User user = userService.findById(userId);
        return auditLogService.findByUser(user);
    }
    
    @GetMapping("/entity-type/{entityType}/entity-id/{entityId}")
    public List<AuditLog> getByEntity(
            @PathVariable String entityType, 
            @PathVariable String entityId) {
        return auditLogService.findByEntity(entityType, entityId);
    }
    
    @GetMapping("/date-range")
    public List<AuditLog> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return auditLogService.findByDateRange(start, end);
    }

    @PostMapping
    public AuditLog create(@RequestBody AuditLog auditLog) {
        return auditLogService.save(auditLog);
    }
}