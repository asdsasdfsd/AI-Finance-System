// backend/src/main/java/org/example/backend/service/UserService.java
package org.example.backend.service;

import org.example.backend.model.User;
import org.example.backend.repository.UserRepository;
import org.example.backend.application.service.UserApplicationService;
import org.example.backend.application.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 简单User Service - 为传统Controller提供兼容性
 * 
 * 注意：这是为了兼容现有传统Controller而保留的简单Service
 * 新业务逻辑应该使用UserApplicationService
 */
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserApplicationService userApplicationService;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    public User save(User user) {
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public void deleteById(Integer id) {
        userRepository.deleteById(id);
    }
    
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * 将DDD的UserDTO转换为传统User实体
     * 用于兼容传统Controller
     */
    public User convertFromDTO(UserDTO dto) {
        if (dto == null) return null;
        
        User user = new User();
        user.setUserId(dto.getUserId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setEnabled(dto.getEnabled());
        user.setExternalId(dto.getExternalId());
        user.setPreferredLanguage(dto.getPreferredLanguage());
        user.setTimezone(dto.getTimezone());
        user.setLastLogin(dto.getLastLogin());
        user.setCreatedAt(dto.getCreatedAt());
        user.setUpdatedAt(dto.getUpdatedAt());
        
        return user;
    }
}