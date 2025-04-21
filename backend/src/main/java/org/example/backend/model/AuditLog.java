// src/main/java/org/example/backend/model/AuditLog.java
package org.example.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "Audit_Log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer logId;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private String action;
    private String entityType;
    private String entityId;
    private String details;
    private LocalDateTime timestamp;
    private String ipAddress;

    // Default values
    public AuditLog() {
        this.timestamp = LocalDateTime.now();
    }
}