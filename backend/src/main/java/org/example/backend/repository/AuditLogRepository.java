// src/main/java/org/example/backend/repository/AuditLogRepository.java
package org.example.backend.repository;

import org.example.backend.model.AuditLog;
import org.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {
    List<AuditLog> findByUser(User user);
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId);
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}