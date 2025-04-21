// src/main/java/org/example/backend/service/UserService.java
package org.example.backend.service;

import org.example.backend.model.Department;
import org.example.backend.model.Role;
import org.example.backend.model.User;
import org.example.backend.model.UserRole;
import org.example.backend.repository.UserRepository;
import org.example.backend.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Autowired
    private RoleService roleService;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User findById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }
    
    public List<User> findByDepartment(Department department) {
        return userRepository.findByDepartment(department);
    }

    public User createUser(User user) {
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    public User updateUser(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }
    
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