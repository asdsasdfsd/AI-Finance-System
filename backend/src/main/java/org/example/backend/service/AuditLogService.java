// backend/src/main/java/org/example/backend/service/AuditLogService.java
package org.example.backend.service;

import org.example.backend.application.service.UserApplicationService;
import org.example.backend.application.service.CompanyApplicationService;
import org.example.backend.application.dto.UserDTO;
import org.example.backend.application.dto.CompanyDTO;
import org.example.backend.model.AuditLog;
import org.example.backend.model.User;
import org.example.backend.repository.AuditLogRepository;
import org.example.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AuditLog Service - DDD适配器版本
 * 
 * 改造策略：
 * 1. 保持原有Service接口不变（向后兼容）
 * 2. 使用DDD应用服务验证用户和公司状态
 * 3. 添加多租户隔离和权限验证
 * 4. 增强审计日志的业务功能
 */
@Service
@Transactional
public class AuditLogService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private UserApplicationService userApplicationService;
    
    @Autowired
    private CompanyApplicationService companyApplicationService;

    // ========== 保持原有接口不变 ==========
    
    public List<AuditLog> findAll() {
        return auditLogRepository.findAll();
    }

    public AuditLog findById(Integer id) {
        return auditLogRepository.findById(id).orElse(null);
    }
    
    /**
     * 增强版：验证用户权限
     */
    public List<AuditLog> findByUser(User user) {
        if (user != null) {
            validateUserAccess(user.getUserId());
        }
        return auditLogRepository.findByUser(user);
    }
    
    public List<AuditLog> findByEntity(String entityType, String entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }
    
    public List<AuditLog> findByDateRange(LocalDateTime start, LocalDateTime end) {
        validateDateRange(start, end);
        return auditLogRepository.findByTimestampBetween(start, end);
    }

    /**
     * 增强版：保存前进行业务验证
     */
    public AuditLog save(AuditLog auditLog) {
        validateAuditLogForSave(auditLog);
        
        if (auditLog.getTimestamp() == null) {
            auditLog.setTimestamp(LocalDateTime.now());
        }
        return auditLogRepository.save(auditLog);
    }
    
    /**
     * 增强版：添加更多验证
     */
    public AuditLog logAction(User user, String action, String entityType, String entityId, 
                            String details, String ipAddress) {
        if (user != null) {
            validateUserAccess(user.getUserId());
        }
        
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        log.setIpAddress(ipAddress);
        return save(log);
    }
    
    // ========== 新增的DDD业务方法 ==========
    
    /**
     * 根据用户ID获取审计日志（新方法）
     */
    public List<AuditLog> findByUserId(Integer userId) {
        UserDTO userDTO = validateAndGetUser(userId);
        User user = convertUserToEntity(userDTO);
        return auditLogRepository.findByUser(user);
    }
    
    /**
     * 根据公司获取审计日志（新方法）
     */
    public List<AuditLog> findByCompanyId(Integer companyId) {
        CompanyDTO company = validateAndGetCompany(companyId);
        
        // 获取公司下所有用户的审计日志
        List<UserDTO> companyUsers = userApplicationService.getUsersByCompany(companyId);
        
        return companyUsers.stream()
                .flatMap(userDTO -> {
                    User user = convertUserToEntity(userDTO);
                    return auditLogRepository.findByUser(user).stream();
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 根据用户ID和日期范围获取审计日志（新方法）
     */
    public List<AuditLog> findByUserIdAndDateRange(Integer userId, LocalDateTime start, LocalDateTime end) {
        UserDTO userDTO = validateAndGetUser(userId);
        validateDateRange(start, end);
        
        User user = convertUserToEntity(userDTO);
        return auditLogRepository.findByUser(user).stream()
                .filter(log -> log.getTimestamp().isAfter(start) && log.getTimestamp().isBefore(end))
                .collect(Collectors.toList());
    }
    
    /**
     * 根据公司和日期范围获取审计日志（新方法）
     */
    public List<AuditLog> findByCompanyIdAndDateRange(Integer companyId, LocalDateTime start, LocalDateTime end) {
        validateAndGetCompany(companyId);
        validateDateRange(start, end);
        
        List<AuditLog> companyLogs = findByCompanyId(companyId);
        return companyLogs.stream()
                .filter(log -> log.getTimestamp().isAfter(start) && log.getTimestamp().isBefore(end))
                .collect(Collectors.toList());
    }
    
    /**
     * 根据操作类型获取审计日志（新方法）
     */
    public List<AuditLog> findByAction(String action) {
        return auditLogRepository.findAll().stream()
                .filter(log -> action.equals(log.getAction()))
                .collect(Collectors.toList());
    }
    
    /**
     * 安全的日志记录方法 - 使用用户ID（新方法）
     */
    public AuditLog logActionByUserId(Integer userId, String action, String entityType, 
                                    String entityId, String details, String ipAddress) {
        UserDTO userDTO = null;
        User user = null;
        
        if (userId != null) {
            try {
                userDTO = validateAndGetUser(userId);
                user = convertUserToEntity(userDTO);
            } catch (Exception e) {
                // 如果用户不存在或无权限，记录系统日志
                user = null;
            }
        }
        
        return logAction(user, action, entityType, entityId, details, ipAddress);
    }
    
    /**
     * 批量记录审计日志（新方法）
     */
    public List<AuditLog> logBatchActions(List<BatchAuditRequest> requests) {
        return requests.stream()
                .map(request -> logActionByUserId(
                    request.getUserId(),
                    request.getAction(),
                    request.getEntityType(),
                    request.getEntityId(),
                    request.getDetails(),
                    request.getIpAddress()
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户的活动统计（新方法）
     */
    public UserActivityStats getUserActivityStats(Integer userId, LocalDateTime since) {
        UserDTO userDTO = validateAndGetUser(userId);
        List<AuditLog> userLogs = findByUserIdAndDateRange(userId, since, LocalDateTime.now());
        
        long totalActions = userLogs.size();
        long uniqueDays = userLogs.stream()
                .map(log -> log.getTimestamp().toLocalDate())
                .distinct()
                .count();
        
        String mostFrequentAction = userLogs.stream()
                .collect(Collectors.groupingBy(AuditLog::getAction, Collectors.counting()))
                .entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse("N/A");
        
        return new UserActivityStats(totalActions, uniqueDays, mostFrequentAction);
    }
    
    /**
     * 获取公司的审计统计（新方法）
     */
    public CompanyAuditStats getCompanyAuditStats(Integer companyId, LocalDateTime since) {
        validateAndGetCompany(companyId);
        List<AuditLog> companyLogs = findByCompanyIdAndDateRange(companyId, since, LocalDateTime.now());
        
        long totalLogs = companyLogs.size();
        long activeUsers = companyLogs.stream()
                .filter(log -> log.getUser() != null)
                .map(log -> log.getUser().getUserId())
                .distinct()
                .count();
        
        java.util.Map<String, Long> actionCounts = companyLogs.stream()
                .collect(Collectors.groupingBy(AuditLog::getAction, Collectors.counting()));
        
        return new CompanyAuditStats(totalLogs, activeUsers, actionCounts);
    }
    
    /**
     * 清理旧的审计日志（新方法）
     */
    @Transactional
    public int cleanupOldLogs(LocalDateTime cutoffDate) {
        List<AuditLog> oldLogs = auditLogRepository.findAll().stream()
                .filter(log -> log.getTimestamp().isBefore(cutoffDate))
                .collect(Collectors.toList());
        
        int deletedCount = oldLogs.size();
        oldLogs.forEach(log -> auditLogRepository.delete(log));
        
        return deletedCount;
    }
    
    // ========== 业务验证方法 ==========
    
    /**
     * 验证用户访问权限
     */
    private void validateUserAccess(Integer userId) {
        try {
            UserDTO user = userApplicationService.getUserById(userId);
            if (!user.isActiveAndUnlocked()) {
                throw new IllegalStateException("User is not active: " + userId);
            }
        } catch (Exception e) {
            throw new ResourceNotFoundException("User not found: " + userId, e);
        }
    }
    
    /**
     * 验证并获取用户信息
     */
    private UserDTO validateAndGetUser(Integer userId) {
        try {
            UserDTO user = userApplicationService.getUserById(userId);
            if (!user.isActiveAndUnlocked()) {
                throw new IllegalStateException("User is not active: " + userId);
            }
            return user;
        } catch (Exception e) {
            throw new ResourceNotFoundException("User not found: " + userId, e);
        }
    }
    
    /**
     * 验证并获取公司信息
     */
    private CompanyDTO validateAndGetCompany(Integer companyId) {
        try {
            CompanyDTO company = companyApplicationService.getCompanyById(companyId);
            if (!company.isActive()) {
                throw new IllegalStateException("Company is not active: " + companyId);
            }
            return company;
        } catch (Exception e) {
            throw new ResourceNotFoundException("Company not found: " + companyId, e);
        }
    }
    
    /**
     * 验证日期范围
     */
    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end dates cannot be null");
        }
        
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        // 限制查询范围不超过1年
        if (start.isBefore(end.minusYears(1))) {
            throw new IllegalArgumentException("Date range cannot exceed 1 year");
        }
    }
    
    /**
     * 验证审计日志保存前的业务规则
     */
    private void validateAuditLogForSave(AuditLog auditLog) {
        if (auditLog == null) {
            throw new IllegalArgumentException("Audit log cannot be null");
        }
        
        if (auditLog.getAction() == null || auditLog.getAction().trim().isEmpty()) {
            throw new IllegalArgumentException("Action cannot be empty");
        }
        
        if (auditLog.getEntityType() == null || auditLog.getEntityType().trim().isEmpty()) {
            throw new IllegalArgumentException("Entity type cannot be empty");
        }
        
        // 验证用户（如果有）
        if (auditLog.getUser() != null) {
            validateUserAccess(auditLog.getUser().getUserId());
        }
        
        // 验证IP地址格式（可选）
        if (auditLog.getIpAddress() != null && !isValidIpAddress(auditLog.getIpAddress())) {
            throw new IllegalArgumentException("Invalid IP address format: " + auditLog.getIpAddress());
        }
    }
    
    /**
     * 简单的IP地址格式验证
     */
    private boolean isValidIpAddress(String ip) {
        return ip.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$") || 
               ip.matches("^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");
    }
    
    // ========== 转换方法 ==========
    
    /**
     * 将DDD UserDTO转换为传统User实体
     */
    private User convertUserToEntity(UserDTO dto) {
        User user = new User();
        user.setUserId(dto.getUserId());
        user.setUsername(dto.getUsername());
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setEnabled(dto.getEnabled());
        return user;
    }
    
    // ========== 内部类 ==========
    
    /**
     * 批量审计请求
     */
    public static class BatchAuditRequest {
        private Integer userId;
        private String action;
        private String entityType;
        private String entityId;
        private String details;
        private String ipAddress;
        
        // 构造函数
        public BatchAuditRequest(Integer userId, String action, String entityType, String entityId, 
                               String details, String ipAddress) {
            this.userId = userId;
            this.action = action;
            this.entityType = entityType;
            this.entityId = entityId;
            this.details = details;
            this.ipAddress = ipAddress;
        }
        
        // Getters
        public Integer getUserId() { return userId; }
        public String getAction() { return action; }
        public String getEntityType() { return entityType; }
        public String getEntityId() { return entityId; }
        public String getDetails() { return details; }
        public String getIpAddress() { return ipAddress; }
    }
    
    /**
     * 用户活动统计
     */
    public static class UserActivityStats {
        private final long totalActions;
        private final long activeDays;
        private final String mostFrequentAction;
        
        public UserActivityStats(long totalActions, long activeDays, String mostFrequentAction) {
            this.totalActions = totalActions;
            this.activeDays = activeDays;
            this.mostFrequentAction = mostFrequentAction;
        }
        
        public long getTotalActions() { return totalActions; }
        public long getActiveDays() { return activeDays; }
        public String getMostFrequentAction() { return mostFrequentAction; }
    }
    
    /**
     * 公司审计统计
     */
    public static class CompanyAuditStats {
        private final long totalLogs;
        private final long activeUsers;
        private final java.util.Map<String, Long> actionCounts;
        
        public CompanyAuditStats(long totalLogs, long activeUsers, java.util.Map<String, Long> actionCounts) {
            this.totalLogs = totalLogs;
            this.activeUsers = activeUsers;
            this.actionCounts = actionCounts;
        }
        
        public long getTotalLogs() { return totalLogs; }
        public long getActiveUsers() { return activeUsers; }
        public java.util.Map<String, Long> getActionCounts() { return actionCounts; }
    }
}