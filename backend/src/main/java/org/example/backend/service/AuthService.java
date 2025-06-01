// backend/src/main/java/org/example/backend/service/AuthService.java
package org.example.backend.service;

import org.example.backend.application.service.UserApplicationService;
import org.example.backend.application.service.CompanyApplicationService;
import org.example.backend.application.dto.CreateUserCommand;
import org.example.backend.application.dto.CreateCompanyCommand;
import org.example.backend.application.dto.UserDTO;
import org.example.backend.application.dto.CompanyDTO;
import org.example.backend.dto.AuthRequest;
import org.example.backend.dto.AuthResponse;
import org.example.backend.dto.RegisterRequest;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.security.CustomUserDetailsService;
import org.example.backend.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Auth Service - DDD版本
 * 
 * 使用DDD应用服务进行用户和公司管理
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserApplicationService userApplicationService;
    private final CompanyApplicationService companyApplicationService;
    private final PasswordEncoder passwordEncoder;
    private final SsoService ssoService;

    public AuthService(AuthenticationManager authenticationManager,
                       CustomUserDetailsService userDetailsService,
                       JwtUtil jwtUtil,
                       UserApplicationService userApplicationService,
                       CompanyApplicationService companyApplicationService,
                       PasswordEncoder passwordEncoder,
                       SsoService ssoService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.userApplicationService = userApplicationService;
        this.companyApplicationService = companyApplicationService;
        this.passwordEncoder = passwordEncoder;
        this.ssoService = ssoService;
    }

    public AuthResponse authenticate(AuthRequest request) {
        System.out.println("登录用户名: " + request.getUsername());
        System.out.println("登录明文密码: " + request.getPassword());

        // 手动获取用户信息
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

        // 输出加密密码和匹配结果（调试用）
        System.out.println("数据库加密密码: " + userDetails.getPassword());
        System.out.println("是否匹配: " + passwordEncoder.matches(request.getPassword(), userDetails.getPassword()));

        // 再走标准 Spring Security 验证（如失败将抛异常）
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 生成 JWT token
        final String token = jwtUtil.generateToken(userDetails);

        // 更新用户登录时间 - 使用DDD应用服务
        try {
            userApplicationService.recordSuccessfulLogin(
                userApplicationService.getUserByUsername(request.getUsername()).getUserId()
            );
        } catch (Exception e) {
            System.err.println("Failed to update last login time: " + e.getMessage());
        }

        // 获取用户信息构建响应 - 使用DDD应用服务
        UserDTO user = userApplicationService.getUserByUsername(request.getUsername());

        // 构造认证响应
        return buildAuthResponse(token, user, userDetails);
    }

    @Transactional
    public org.example.backend.dto.UserDTO register(RegisterRequest request) {
        // Check if username or email already exists - 使用DDD应用服务
        if (userApplicationService.existsByUsername(request.getUsername()) || 
            userApplicationService.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Username or email already in use");
        }
        
        // Get company - 使用DDD应用服务
        CompanyDTO company = companyApplicationService.getCompanyById(request.getCompanyId());
        if (company == null) {
            throw new ResourceNotFoundException("Company not found");
        }
        
        // Create new user - 使用DDD应用服务
        CreateUserCommand userCommand = CreateUserCommand.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .companyId(request.getCompanyId())
                .enabled(true)
                .roleNames(Set.of("USER")) // 默认角色
                .build();
        
        UserDTO savedUser = userApplicationService.createUser(userCommand);
        
        return mapUserToLegacyDTO(savedUser);
    }

    @Transactional
    public org.example.backend.dto.UserDTO registerCompanyAdmin(RegisterRequest request) {
        // Check if username or email already exists - 使用DDD应用服务
        if (userApplicationService.existsByUsername(request.getUsername()) || 
            userApplicationService.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Username or email already in use");
        }
        
        // Create new company - 使用DDD应用服务
        CreateCompanyCommand companyCommand = CreateCompanyCommand.builder()
                .companyName(request.getCompanyName())
                .address(request.getAddress())
                .city(request.getCity())
                .stateProvince(request.getStateProvince())
                .postalCode(request.getPostalCode())
                .email(request.getEmail())
                .registrationNumber(request.getRegistrationNumber())
                .taxId(request.getTaxId())
                .createdBy(1) // 系统创建
                .build();
        
        CompanyDTO savedCompany = companyApplicationService.createCompany(companyCommand);
        
        // Create admin user - 使用DDD应用服务
        CreateUserCommand userCommand = CreateUserCommand.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .companyId(savedCompany.getCompanyId())
                .enabled(true)
                .roleNames(Set.of("COMPANY_ADMIN")) // 管理员角色
                .build();
        
        UserDTO savedUser = userApplicationService.createUser(userCommand);
        
        return mapUserToLegacyDTO(savedUser);
    }

    /**
     * Authenticate user with Microsoft SSO
     * Supports auto-provisioning of users and companies
     * 
     * @param code Authorization code from Microsoft
     * @param state State parameter for security validation
     * @return Authentication response with token and user info
     */
    public AuthResponse authenticateWithSso(String code, String state) {
        // Process SSO authentication with flags for new user/company
        Map<String, Boolean> provisioningFlags = new HashMap<>();
        UserDTO user = ssoService.processSsoLogin(code, state, provisioningFlags);
        
        // Generate JWT token
        final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        final String token = jwtUtil.generateToken(userDetails);
        
        // Update last login timestamp - 使用DDD应用服务
        userApplicationService.recordSuccessfulLogin(user.getUserId());
        
        // Build response DTO
        AuthResponse response = buildAuthResponse(token, user, userDetails);
        
        // Add provisioning flags to response
        response.setNewUserCreated(provisioningFlags.getOrDefault("newUserCreated", false));
        response.setNewCompanyCreated(provisioningFlags.getOrDefault("newCompanyCreated", false));
        
        return response;
    }

    public void logout(String token) {
        // In a stateless JWT system, we typically don't need server-side logout
        // But we could implement a token blacklist if required
        // For now, this is a placeholder for future implementation
    }
    
    private AuthResponse buildAuthResponse(String token, UserDTO user, UserDetails userDetails) {
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        
        org.example.backend.dto.UserDTO userDTO = org.example.backend.dto.UserDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .companyId(user.getTenantId()) // DDD中使用tenantId
                .companyName(getCompanyName(user.getTenantId()))
                .roles(roles)
                .build();
        
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .user(userDTO)
                .build();
    }
    
    private org.example.backend.dto.UserDTO mapUserToLegacyDTO(UserDTO user) {
        return org.example.backend.dto.UserDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .companyId(user.getTenantId()) // DDD中使用tenantId
                .companyName(getCompanyName(user.getTenantId()))
                .roles(user.getRoleNames())
                .build();
    }
    
    private String getCompanyName(Integer companyId) {
        try {
            CompanyDTO company = companyApplicationService.getCompanyById(companyId);
            return company != null ? company.getCompanyName() : null;
        } catch (Exception e) {
            return null;
        }
    }
}