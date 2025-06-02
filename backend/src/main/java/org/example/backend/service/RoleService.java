// backend/src/main/java/org/example/backend/service/RoleService.java
package org.example.backend.service;

import org.example.backend.application.service.UserApplicationService;
import org.example.backend.application.dto.UserDTO;
import org.example.backend.model.Role;
import org.example.backend.repository.RoleRepository;
import org.example.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Role Service - DDD适配器版本
 * 
 * 改造策略：
 * 1. 保持原有Service接口不变（向后兼容）
 * 2. 利用UserApplicationService进行角色使用情况验证
 * 3. 添加角色权限管理和使用统计功能
 * 4. 增强角色分配的业务验证
 */
@Service
@Transactional
public class RoleService {
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserApplicationService userApplicationService;

    // ========== 保持原有接口不变 ==========
    
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Role findById(Integer id) {
        return roleRepository.findById(id).orElse(null);
    }
    
    /**
     * 根据名称查找角色 - 修复返回类型兼容性
     */
    public Role findByName(String name) {
        Optional<Role> roleOptional = roleRepository.findByName(name);
        return roleOptional.orElse(null);
    }
    
    /**
     * 根据名称查找角色 - 返回Optional版本
     */
    public Optional<Role> findOptionalByName(String name) {
        return roleRepository.findByName(name);
    }
    
    /**
     * 检查角色名是否存在
     */
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }

    /**
     * 增强版：保存前进行业务验证
     */
    public Role save(Role role) {
        validateRoleForSave(role);
        return roleRepository.save(role);
    }

    /**
     * 增强版：删除前检查角色使用情况
     */
    public void deleteById(Integer id) {
        Role role = findById(id);
        if (role != null) {
            validateRoleCanBeDeleted(role);
        }
        roleRepository.deleteById(id);
    }
    
    // ========== 新增的DDD业务方法 ==========
    
    /**
     * 创建新角色（新方法，增强业务验证）
     */
    public Role createRole(String name, String description) {
        validateRoleName(name);
        
        if (existsByName(name)) {
            throw new IllegalArgumentException("Role name already exists: " + name);
        }
        
        Role role = new Role();
        role.setName(name.toUpperCase()); // 统一大写
        role.setDescription(description);
        
        return save(role);
    }
    
    /**
     * 更新角色信息（新方法）
     */
    public Role updateRole(Integer roleId, String description) {
        Role role = findById(roleId);
        if (role == null) {
            throw new ResourceNotFoundException("Role not found: " + roleId);
        }
        
        // 系统内置角色不允许修改描述之外的信息
        if (isSystemRole(role.getName())) {
            // 只允许修改描述
            role.setDescription(description);
        } else {
            role.setDescription(description);
        }
        
        return save(role);
    }
    
    /**
     * 获取角色使用统计（新方法）
     */
    public RoleUsageStats getRoleUsageStats(String roleName) {
        Role role = findByName(roleName);
        if (role == null) {
            throw new ResourceNotFoundException("Role not found: " + roleName);
        }
        
        // 统计所有公司中使用此角色的用户
        int totalUsers = 0;
        int activeUsers = 0;
        
        // 这里简化处理，实际可能需要分页查询大量数据
        try {
            // 由于UserApplicationService是按公司查询的，这里做简化处理
            // 实际使用中可能需要添加全局用户查询方法
            totalUsers = role.getUsers().size();
            activeUsers = (int) role.getUsers().stream()
                    .filter(user -> user.getEnabled())
                    .count();
        } catch (Exception e) {
            // 如果无法获取用户信息，返回基本统计
            totalUsers = role.getUsers().size();
            activeUsers = totalUsers; // 假设都是活跃的
        }
        
        return new RoleUsageStats(
            role.getName(),
            role.getDescription(),
            totalUsers,
            activeUsers,
            isSystemRole(role.getName())
        );
    }
    
    /**
     * 获取系统预定义角色列表（新方法）
     */
    public List<Role> getSystemRoles() {
        return findAll().stream()
                .filter(role -> isSystemRole(role.getName()))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取自定义角色列表（新方法）
     */
    public List<Role> getCustomRoles() {
        return findAll().stream()
                .filter(role -> !isSystemRole(role.getName()))
                .collect(Collectors.toList());
    }
    
    /**
     * 批量分配角色给用户（新方法）
     */
    public void assignRolesToUser(Integer userId, Set<String> roleNames) {
        // 验证所有角色都存在
        for (String roleName : roleNames) {
            if (!existsByName(roleName)) {
                throw new ResourceNotFoundException("Role not found: " + roleName);
            }
        }
        
        // 使用UserApplicationService进行角色分配
        try {
            UserDTO user = userApplicationService.getUserById(userId);
            
            // 清除现有角色并分配新角色
            for (String roleName : roleNames) {
                userApplicationService.assignRole(userId, roleName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to assign roles to user: " + userId, e);
        }
    }
    
    /**
     * 获取角色层次结构（新方法）
     */
    public RoleHierarchy getRoleHierarchy() {
        List<Role> allRoles = findAll();
        
        // 定义角色等级（从高到低）
        String[] roleOrder = {
            "SYSTEM_ADMIN",
            "COMPANY_ADMIN", 
            "FINANCE_MANAGER",
            "FINANCE_OPERATOR",
            "AUDITOR",
            "REPORT_VIEWER",
            "USER"
        };
        
        List<Role> orderedRoles = allRoles.stream()
                .sorted((r1, r2) -> {
                    int idx1 = getOrderIndex(r1.getName(), roleOrder);
                    int idx2 = getOrderIndex(r2.getName(), roleOrder);
                    return Integer.compare(idx1, idx2);
                })
                .collect(Collectors.toList());
        
        return new RoleHierarchy(orderedRoles, roleOrder);
    }
    
    /**
     * 验证角色权限等级（新方法）
     */
    public boolean canAssignRole(String assignerRole, String targetRole) {
        String[] roleOrder = {
            "SYSTEM_ADMIN",
            "COMPANY_ADMIN", 
            "FINANCE_MANAGER",
            "FINANCE_OPERATOR",
            "AUDITOR",
            "REPORT_VIEWER",
            "USER"
        };
        
        int assignerLevel = getOrderIndex(assignerRole, roleOrder);
        int targetLevel = getOrderIndex(targetRole, roleOrder);
        
        // 只能分配等级不高于自己的角色
        return assignerLevel <= targetLevel;
    }
    
    // ========== 业务验证方法 ==========
    
    /**
     * 验证角色保存前的业务规则
     */
    private void validateRoleForSave(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        
        if (role.getName() == null || role.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be empty");
        }
        
        validateRoleName(role.getName());
        
        // 验证角色名称唯一性（对新建角色）
        if (role.getRoleId() == null) {
            if (existsByName(role.getName())) {
                throw new IllegalArgumentException("Role name already exists: " + role.getName());
            }
        }
        
        // 系统角色不允许修改名称
        if (role.getRoleId() != null) {
            Role existingRole = findById(role.getRoleId());
            if (existingRole != null && isSystemRole(existingRole.getName())) {
                if (!existingRole.getName().equals(role.getName())) {
                    throw new IllegalArgumentException("Cannot modify system role name: " + existingRole.getName());
                }
            }
        }
    }
    
    /**
     * 验证角色是否可以删除
     */
    private void validateRoleCanBeDeleted(Role role) {
        // 系统角色不允许删除
        if (isSystemRole(role.getName())) {
            throw new IllegalStateException("Cannot delete system role: " + role.getName());
        }
        
        // 检查是否有用户正在使用此角色
        if (!role.getUsers().isEmpty()) {
            throw new IllegalStateException("Cannot delete role that is assigned to users: " + role.getName());
        }
    }
    
    /**
     * 验证角色名称格式
     */
    private void validateRoleName(String name) {
        if (name.length() < 2 || name.length() > 50) {
            throw new IllegalArgumentException("Role name must be between 2-50 characters");
        }
        
        if (!name.matches("^[A-Z][A-Z0-9_]*$")) {
            throw new IllegalArgumentException("Role name must start with uppercase letter and contain only uppercase letters, numbers, and underscores");
        }
    }
    
    /**
     * 检查是否为系统预定义角色
     */
    private boolean isSystemRole(String roleName) {
        Set<String> systemRoles = Set.of(
            "SYSTEM_ADMIN",
            "COMPANY_ADMIN",
            "FINANCE_MANAGER", 
            "FINANCE_OPERATOR",
            "REPORT_VIEWER",
            "USER",
            "AUDITOR"
        );
        return systemRoles.contains(roleName);
    }
    
    /**
     * 获取角色在等级序列中的索引
     */
    private int getOrderIndex(String roleName, String[] roleOrder) {
        for (int i = 0; i < roleOrder.length; i++) {
            if (roleOrder[i].equals(roleName)) {
                return i;
            }
        }
        return roleOrder.length; // 未定义的角色放在最后
    }
    
    // ========== 内部类 ==========
    
    /**
     * 角色使用统计
     */
    public static class RoleUsageStats {
        private final String roleName;
        private final String description;
        private final int totalUsers;
        private final int activeUsers;
        private final boolean isSystemRole;
        
        public RoleUsageStats(String roleName, String description, int totalUsers, 
                            int activeUsers, boolean isSystemRole) {
            this.roleName = roleName;
            this.description = description;
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.isSystemRole = isSystemRole;
        }
        
        public String getRoleName() { return roleName; }
        public String getDescription() { return description; }
        public int getTotalUsers() { return totalUsers; }
        public int getActiveUsers() { return activeUsers; }
        public boolean isSystemRole() { return isSystemRole; }
    }
    
    /**
     * 角色层次结构
     */
    public static class RoleHierarchy {
        private final List<Role> orderedRoles;
        private final String[] roleOrder;
        
        public RoleHierarchy(List<Role> orderedRoles, String[] roleOrder) {
            this.orderedRoles = orderedRoles;
            this.roleOrder = roleOrder;
        }
        
        public List<Role> getOrderedRoles() { return orderedRoles; }
        public String[] getRoleOrder() { return roleOrder; }
    }
}