// backend/src/main/java/org/example/backend/security/CustomUserDetailsService.java
package org.example.backend.security;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.example.backend.application.service.UserApplicationService;
import org.example.backend.application.dto.UserDTO;
import org.example.backend.exception.ResourceNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CustomUserDetailsService - DDD版本
 * 
 * 使用DDD的UserApplicationService获取用户信息
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserApplicationService userApplicationService;

    public CustomUserDetailsService(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            // 使用DDD应用服务获取用户信息
            UserDTO user = userApplicationService.getUserByUsername(username);
            
            if (user == null) {
                throw new UsernameNotFoundException("User not found with username: " + username);
            }

            Set<String> roleNames = user.getRoleNames();

            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    getPasswordForUser(user), // 获取密码
                    user.isActiveAndUnlocked(),
                    true, // accountNonExpired
                    !user.isPasswordExpired(), // credentialsNonExpired
                    !user.isLocked(), // accountNonLocked
                    getAuthorities(roleNames)
            );
        } catch (ResourceNotFoundException e) {
            throw new UsernameNotFoundException("User not found with username: " + username, e);
        } catch (Exception e) {
            System.err.println("Error loading user by username: " + e.getMessage());
            throw new UsernameNotFoundException("Error loading user: " + username, e);
        }
    }

    /**
     * 获取用户密码
     * 注意：在DDD模式下，密码不应该通过DTO暴露
     * 这里需要特殊处理或者修改UserApplicationService添加获取密码的方法
     */
    private String getPasswordForUser(UserDTO user) {
        // 临时解决方案：如果是SSO用户，返回一个占位符密码
        if (user.isSsoUser()) {
            return "{noop}SSO_MANAGED"; // SSO用户的占位符密码
        }
        
        // 对于非SSO用户，我们需要从UserApplicationService获取实际的加密密码
        // 这可能需要在UserApplicationService中添加一个getPasswordForAuthentication方法
        try {
            // 暂时的解决方案：通过UserApplicationService获取完整用户信息
            // 注意：这里需要UserApplicationService提供获取密码的安全方法
            return getUserPasswordSecurely(user.getUsername());
        } catch (Exception e) {
            System.err.println("Failed to get password for user: " + user.getUsername());
            throw new RuntimeException("Failed to authenticate user", e);
        }
    }
    
    /**
     * 安全地获取用户密码用于认证
     * 注意：这个方法可能需要在UserApplicationService中实现
     */
    private String getUserPasswordSecurely(String username) {
        // 这里需要调用UserApplicationService的一个特殊方法来获取密码
        // 或者直接访问UserAggregate的密码字段
        // 临时解决方案：返回一个默认值，后续需要改进
        
        // TODO: 在UserApplicationService中添加getPasswordForAuthentication方法
        // return userApplicationService.getPasswordForAuthentication(username);
        
        // 临时解决方案：抛出异常提醒需要实现
        throw new RuntimeException("Password retrieval not implemented in DDD mode. " +
                "Need to add getPasswordForAuthentication method to UserApplicationService");
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Set<String> roleNames) {
        return roleNames.stream()
                .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
                .collect(Collectors.toList());
    }
}