// src/main/java/org/example/backend/service/AuditLogService.java
package org.example.backend.service;

import org.example.backend.model.AuditLog;
import org.example.backend.model.User;
import org.example.backend.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {
    @Autowired
    private AuditLogRepository auditLogRepository;

    public List<AuditLog> findAll() {
        return auditLogRepository.findAll();
    }

    public AuditLog findById(Integer id) {
        return auditLogRepository.findById(id).orElse(null);
    }
    
    public List<AuditLog> findByUser(User user) {
        return auditLogRepository.findByUser(user);
    }
    
    public List<AuditLog> findByEntity(String entityType, String entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }
    
    public List<AuditLog> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByTimestampBetween(start, end);
    }

    public AuditLog save(AuditLog auditLog) {
        if (auditLog.getTimestamp() == null) {
            auditLog.setTimestamp(LocalDateTime.now());
        }
        return auditLogRepository.save(auditLog);
    }
    
    // Convenience method to log an action
    public AuditLog logAction(User user, String action, String entityType, String entityId, String details, String ipAddress) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        log.setIpAddress(ipAddress);
        return save(log);
    }
}