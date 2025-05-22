// src/main/java/org/example/backend/service/UserService.java
package org.example.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.example.backend.model.Department;
import org.example.backend.model.Role;
import org.example.backend.model.User;
import org.example.backend.model.UserRole;
import org.example.backend.repository.UserRepository;
import org.example.backend.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User findById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }
    
    public List<User> findByDepartment(Department department) {
        return userRepository.findByDepartment(department);
    }

    @Transactional
    public User createUser(User user) {
        // 加密密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        // 设置创建时间
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    @Transactional
    public User updateUser(User user) {
        // 检查是否需要加密新密码
        // 如果密码不是已加密的格式（BCrypt密码通常以$2a$, $2b$, $2y$开头）
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            // 检查密码是否已经是加密格式
            if (!user.getPassword().startsWith("$2a$") && 
                !user.getPassword().startsWith("$2b$") && 
                !user.getPassword().startsWith("$2y$")) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }
    
    @Transactional
    public void assignRole(User user, String roleName) {
        Role role = roleService.findByName(roleName);
        if (role != null) {
            UserRole userRole = new UserRole(user, role);
            userRoleRepository.save(userRole);
        }
    }
    
    public void removeRole(User user, String roleName) {
        Role role = roleService.findByName(roleName);
        if (role != null) {
            List<UserRole> userRoles = userRoleRepository.findByUser(user);
            userRoles.stream()
                    .filter(ur -> ur.getRole().getName().equals(roleName))
                    .forEach(ur -> userRoleRepository.delete(ur));
        }
    }
    
    public Set<String> getUserRoles(User user) {
        List<UserRole> userRoles = userRoleRepository.findByUser(user);
        return userRoles.stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toSet());
    }
}