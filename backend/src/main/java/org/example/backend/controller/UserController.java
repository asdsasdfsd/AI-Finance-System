// src/main/java/org/example/backend/controller/UserController.java
package org.example.backend.controller;

import org.example.backend.model.Department;
import org.example.backend.model.Role;
import org.example.backend.model.User;
import org.example.backend.model.Company;
import org.example.backend.service.DepartmentService;
import org.example.backend.service.RoleService;
import org.example.backend.service.UserService;
import org.example.backend.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private CompanyService companyService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Integer id) {
        User user = userService.findById(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/department/{departmentId}")
    public List<User> getByDepartment(@PathVariable Integer departmentId) {
        Department department = departmentService.findById(departmentId);
        return userService.findByDepartment(department);
    }
    
    @GetMapping("/{id}/roles")
    public ResponseEntity<Set<String>> getUserRoles(@PathVariable Integer id) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userService.getUserRoles(user));
    }

    @PostMapping
    public User createUser(@RequestBody Map<String, Object> userData) {
        User user = mapRequestDataToUser(userData, null);
        return userService.createUser(user);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody Map<String, Object> userData) {
        User existingUser = userService.findById(id);
        if (existingUser == null) {
            return ResponseEntity.notFound().build();
        }
        
        User user = mapRequestDataToUser(userData, existingUser);
        user.setUserId(id);
        user.setCreatedAt(existingUser.getCreatedAt()); // 保持创建时间
        user.setUpdatedAt(LocalDateTime.now());
        
        return ResponseEntity.ok(userService.updateUser(user));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        if (userService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/roles/{roleName}")
    public ResponseEntity<Void> assignRole(@PathVariable Integer id, @PathVariable String roleName) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        userService.assignRole(user, roleName);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}/roles/{roleName}")
    public ResponseEntity<Void> removeRole(@PathVariable Integer id, @PathVariable String roleName) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        userService.removeRole(user, roleName);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 将前端请求数据转换为User对象
     * 处理角色字符串数组和其他复杂数据类型
     */
    private User mapRequestDataToUser(Map<String, Object> userData, User existingUser) {
        User user = new User();
        
        // 基本字段
        user.setUsername((String) userData.get("username"));
        user.setFullName((String) userData.get("fullName"));
        user.setEmail((String) userData.get("email"));
        
        // 处理密码 - 只有在创建用户或明确提供密码时才设置
        if (userData.containsKey("password") && userData.get("password") != null) {
            String password = (String) userData.get("password");
            if (!password.trim().isEmpty()) {
                user.setPassword(password);
            } else if (existingUser != null) {
                // 如果密码为空且是更新操作，保持原密码
                user.setPassword(existingUser.getPassword());
            }
        } else if (existingUser != null) {
            // 更新时如果没有提供密码，保持原密码
            user.setPassword(existingUser.getPassword());
        }
        
        // 处理enabled状态
        if (userData.containsKey("enabled")) {
            user.setEnabled((Boolean) userData.get("enabled"));
        } else {
            user.setEnabled(true); // 默认启用
        }
        
        // 处理公司关联
        if (userData.containsKey("companyId") && userData.get("companyId") != null) {
            Integer companyId = (Integer) userData.get("companyId");
            Company company = companyService.findById(companyId);
            user.setCompany(company);
        }
        
        // 处理部门关联
        if (userData.containsKey("departmentId") && userData.get("departmentId") != null) {
            Integer departmentId = (Integer) userData.get("departmentId");
            Department department = departmentService.findById(departmentId);
            user.setDepartment(department);
        }
        
        // 处理角色 - 关键修复点：正确处理角色字符串数组
        if (userData.containsKey("roles") && userData.get("roles") != null) {
            Set<Role> roles = new HashSet<>();
            Object rolesData = userData.get("roles");
            
            if (rolesData instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> rolesList = (List<String>) rolesData;
                for (String roleName : rolesList) {
                    if (roleName != null && !roleName.trim().isEmpty()) {
                        Role role = roleService.findByName(roleName);
                        if (role != null) {
                            roles.add(role);
                        }
                    }
                }
            }
            user.setRoles(roles);
        } else if (existingUser != null) {
            // 如果没有提供角色信息且是更新操作，保持原有角色
            user.setRoles(existingUser.getRoles());
        }
        
        return user;
    }
}