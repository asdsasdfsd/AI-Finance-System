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
 * CustomUserDetailsService - 修复版本
 * 
 * 使用DDD的UserApplicationService获取用户信息，并通过安全方法获取密码
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

            // 通过应用服务的安全方法获取密码
            String password = userApplicationService.getPasswordForAuthentication(username);

            Set<String> roleNames = user.getRoleNames();

            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    password,
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

    private Collection<? extends GrantedAuthority> getAuthorities(Set<String> roleNames) {
        return roleNames.stream()
                .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
                .collect(Collectors.toList());
    }
}