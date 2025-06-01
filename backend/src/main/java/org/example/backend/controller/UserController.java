// backend/src/main/java/org/example/backend/infrastructure/web/UserControllerAdapter.java
package org.example.backend.controller;

import org.example.backend.application.service.UserApplicationService;
import org.example.backend.application.dto.CreateUserCommand;
import org.example.backend.application.dto.UpdateUserCommand;
import org.example.backend.application.dto.ChangePasswordCommand;
import org.example.backend.application.dto.UserDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * User Controller Adapter
 * 
 * 适配REST API调用到DDD用户应用服务
 * 保持与现有前端API的100%向后兼容性
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")

public class UserController {
    
    private final UserApplicationService userApplicationService;
    
    public UserController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }
    
    // ========== Create Operations ==========
    
    /**
     * 创建新用户 (向后兼容)
     */
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody Map<String, Object> request) {
        try {
            CreateUserCommand command = mapToCreateCommand(request);
            UserDTO result = userApplicationService.createUser(command);
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * 创建SSO用户
     */
    @PostMapping("/sso")
    public ResponseEntity<UserDTO> createSsoUser(@RequestBody Map<String, Object> request) {
        try {
            String username = getRequiredString(request, "username");
            String email = getRequiredString(request, "email");
            String fullName = getRequiredString(request, "fullName");
            String externalId = getRequiredString(request, "externalId");
            Integer companyId = getRequiredInteger(request, "companyId");
            String defaultRoleName = getString(request, "defaultRoleName", "USER");
            
            UserDTO result = userApplicationService.createSsoUser(
                username, email, fullName, externalId, companyId, defaultRoleName);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }
    
    // ========== Update Operations ==========
    
    /**
     * 更新用户信息 (向后兼容)
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Integer id, 
                                            @RequestBody Map<String, Object> request) {
        try {
            UpdateUserCommand command = mapToUpdateCommand(request);
            UserDTO result = userApplicationService.updateUser(id, command);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }
    
    /**
     * 启用用户
     */
    @PostMapping("/{id}/enable")
    public ResponseEntity<UserDTO> enableUser(@PathVariable Integer id) {
        try {
            UserDTO result = userApplicationService.enableUser(id);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }
    
    /**
     * 禁用用户
     */
    @PostMapping("/{id}/disable")
    public ResponseEntity<UserDTO> disableUser(@PathVariable Integer id) {
        try {
            UserDTO result = userApplicationService.disableUser(id);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/{id}/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@PathVariable Integer id,
                                                            @RequestBody Map<String, Object> request) {
        try {
            String currentPassword = getRequiredString(request, "currentPassword");
            String newPassword = getRequiredString(request, "newPassword");
            
            userApplicationService.changePassword(id, currentPassword, newPassword);
            
            Map<String, Object> response = Map.of(
                "status", "success",
                "message", "密码修改成功",
                "timestamp", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "status", "error",
                "message", "密码修改失败: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            );
            return ResponseEntity.status(400).body(errorResponse);
        }
    }
    
    /**
     * 解锁用户账户
     */
    @PostMapping("/{id}/unlock")
    public ResponseEntity<UserDTO> unlockUser(@PathVariable Integer id) {
        try {
            UserDTO result = userApplicationService.unlockUser(id);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }
    
    // ========== Role Management ==========
    
    /**
     * 分配角色给用户 (向后兼容)
     */
    @PostMapping("/{id}/roles/{roleName}")
    public ResponseEntity<UserDTO> assignRole(@PathVariable Integer id, 
                                            @PathVariable String roleName) {
        try {
            UserDTO result = userApplicationService.assignRole(id, roleName);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }
    
    /**
     * 移除用户角色 (向后兼容)
     */
    @DeleteMapping("/{id}/roles/{roleName}")
    public ResponseEntity<UserDTO> removeRole(@PathVariable Integer id, 
                                            @PathVariable String roleName) {
        try {
            UserDTO result = userApplicationService.removeRole(id, roleName);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }
    
    /**
     * 获取用户角色 (向后兼容)
     */
    @GetMapping("/{id}/roles")
    public ResponseEntity<Set<String>> getUserRoles(@PathVariable Integer id) {
        try {
            UserDTO user = userApplicationService.getUserById(id);
            return ResponseEntity.ok(user.getRoleNames());
            
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // ========== Query Operations (向后兼容) ==========
    
    /**
     * 获取所有用户 (向后兼容)
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(@RequestParam(required = false) Integer companyId) {
        try {
            List<UserDTO> users;
            
            if (companyId != null) {
                users = userApplicationService.getUsersByCompany(companyId);
            } else {
                // 如果没有指定公司ID，返回空列表（安全考虑）
                users = List.of();
            }
            
            return ResponseEntity.ok(users);
            
        } catch (Exception e) {
            return ResponseEntity.status(400).body(List.of());
        }
    }
    
    /**
     * 根据ID获取用户 (向后兼容)
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Integer id) {
        try {
            UserDTO user = userApplicationService.getUserById(id);
            return ResponseEntity.ok(user);
            
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 根据用户名获取用户
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        try {
            UserDTO user = userApplicationService.getUserByUsername(username);
            return ResponseEntity.ok(user);
            
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 根据外部ID获取用户（SSO）
     */
    @GetMapping("/external-id/{externalId}")
    public ResponseEntity<UserDTO> getUserByExternalId(@PathVariable String externalId) {
        try {
            UserDTO user = userApplicationService.getUserByExternalId(externalId);
            return ResponseEntity.ok(user);
            
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 根据部门获取用户 (向后兼容)
     */
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<UserDTO>> getUsersByDepartment(@PathVariable Integer departmentId,
                                                            @RequestParam Integer companyId) {
        try {
            List<UserDTO> users = userApplicationService.getUsersByDepartment(companyId, departmentId);
            return ResponseEntity.ok(users);
            
        } catch (Exception e) {
            return ResponseEntity.status(400).body(List.of());
        }
    }
    
    /**
     * 根据角色获取用户
     */
    @GetMapping("/role/{roleName}")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable String roleName,
                                                      @RequestParam Integer companyId) {
        try {
            List<UserDTO> users = userApplicationService.getUsersByRole(companyId, roleName);
            return ResponseEntity.ok(users);
            
        } catch (Exception e) {
            return ResponseEntity.status(400).body(List.of());
        }
    }
    
    /**
     * 搜索用户
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String searchTerm,
                                                    @RequestParam Integer companyId) {
        try {
            List<UserDTO> users = userApplicationService.searchUsers(companyId, searchTerm);
            return ResponseEntity.ok(users);
            
        } catch (Exception e) {
            return ResponseEntity.status(400).body(List.of());
        }
    }
    
    /**
     * 检查用户名是否存在
     */
    @GetMapping("/exists/username/{username}")
    public ResponseEntity<Map<String, Boolean>> checkUsernameExists(@PathVariable String username) {
        boolean exists = userApplicationService.existsByUsername(username);
        Map<String, Boolean> response = Map.of("exists", exists);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Map<String, Boolean>> checkEmailExists(@PathVariable String email) {
        boolean exists = userApplicationService.existsByEmail(email);
        Map<String, Boolean> response = Map.of("exists", exists);
        return ResponseEntity.ok(response);
    }
    
    // ========== Delete Operations ==========
    
    /**
     * 删除用户 (向后兼容)
     * 实际上是禁用用户，不是真正删除
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        try {
            userApplicationService.disableUser(id);
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // ========== Authentication Support ==========
    
    /**
     * 记录成功登录
     */
    @PostMapping("/{id}/login-success")
    public ResponseEntity<Void> recordSuccessfulLogin(@PathVariable Integer id) {
        try {
            userApplicationService.recordSuccessfulLogin(id);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }
    
    /**
     * 记录登录失败
     */
    @PostMapping("/{id}/login-failure")
    public ResponseEntity<Void> recordFailedLogin(@PathVariable Integer id) {
        try {
            userApplicationService.recordFailedLogin(id);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }
    
    // ========== Helper Methods ==========
    
    private CreateUserCommand mapToCreateCommand(Map<String, Object> request) {
        return CreateUserCommand.builder()
                .username(getRequiredString(request, "username"))
                .email(getRequiredString(request, "email"))
                .password(getRequiredString(request, "password"))
                .fullName(getRequiredString(request, "fullName"))
                .enabled(getBoolean(request, "enabled", true))
                .companyId(getRequiredInteger(request, "companyId"))
                .departmentId(getInteger(request, "departmentId"))
                .preferredLanguage(getString(request, "preferredLanguage", "zh-CN"))
                .timezone(getString(request, "timezone", "Asia/Shanghai"))
                .roleNames(getRoleNames(request))
                .build();
    }
    
    private UpdateUserCommand mapToUpdateCommand(Map<String, Object> request) {
        return UpdateUserCommand.builder()
                .fullName(getString(request, "fullName"))
                .email(getString(request, "email"))
                .password(getString(request, "password"))
                .departmentId(getInteger(request, "departmentId"))
                .preferredLanguage(getString(request, "preferredLanguage"))
                .timezone(getString(request, "timezone"))
                .roleNames(getRoleNames(request))
                .build();
    }
    
    @SuppressWarnings("unchecked")
    private Set<String> getRoleNames(Map<String, Object> request) {
        Object rolesObj = request.get("roles");
        Set<String> roleNames = new HashSet<>();
        
        if (rolesObj instanceof List) {
            List<String> rolesList = (List<String>) rolesObj;
            roleNames.addAll(rolesList);
        } else if (rolesObj instanceof Set) {
            Set<String> rolesSet = (Set<String>) rolesObj;
            roleNames.addAll(rolesSet);
        } else if (rolesObj instanceof String) {
            roleNames.add((String) rolesObj);
        }
        
        // 如果没有指定角色，默认为USER
        if (roleNames.isEmpty()) {
            roleNames.add("USER");
        }
        
        return roleNames;
    }
    
    // 工具方法：安全地从Map中获取值
    private String getRequiredString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null || value.toString().trim().isEmpty()) {
            throw new IllegalArgumentException("Required field '" + key + "' is missing or empty");
        }
        return value.toString().trim();
    }
    
    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString().trim() : null;
    }
    
    private String getString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString().trim() : defaultValue;
    }
    
    private Integer getRequiredInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Required field '" + key + "' is missing");
        }
        
        if (value instanceof Integer) {
            return (Integer) value;
        }
        
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Field '" + key + "' must be a valid integer");
        }
    }
    
    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        
        if (value instanceof Integer) {
            return (Integer) value;
        }
        
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private Boolean getBoolean(Map<String, Object> map, String key, Boolean defaultValue) {
        Object value = map.get(key);
        if (value == null) return defaultValue;
        
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        
        try {
            return Boolean.parseBoolean(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}