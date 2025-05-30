// backend/src/main/java/org/example/backend/service/AuthService.java
package org.example.backend.service;

import org.example.backend.dto.AuthRequest;
import org.example.backend.dto.AuthResponse;
import org.example.backend.dto.RegisterRequest;
import org.example.backend.dto.UserDTO;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.model.Company;
import org.example.backend.model.Role;
import org.example.backend.model.User;
import org.example.backend.repository.CompanyRepository;
import org.example.backend.repository.RoleRepository;
import org.example.backend.repository.UserRepository;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final SsoService ssoService;

    public AuthService(AuthenticationManager authenticationManager,
                       CustomUserDetailsService userDetailsService,
                       JwtUtil jwtUtil,
                       UserRepository userRepository,
                       RoleRepository roleRepository,
                       CompanyRepository companyRepository,
                       PasswordEncoder passwordEncoder,
                       SsoService ssoService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.companyRepository = companyRepository;
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

        // 更新用户登录时间
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // 构造认证响应
        return buildAuthResponse(token, user, userDetails);
    }


    @Transactional
    public UserDTO register(RegisterRequest request) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(request.getUsername()) || 
            userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Username or email already in use");
        }
        
        // Get company
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        
        // Find default user role
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));
        
        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setCompany(company);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);
        
        User savedUser = userRepository.save(user);
        
        return mapUserToDTO(savedUser);
    }

    @Transactional
    public UserDTO registerCompanyAdmin(RegisterRequest request) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(request.getUsername()) || 
            userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Username or email already in use");
        }
        
        // Create new company
        Company company = new Company();
        company.setCompanyName(request.getCompanyName());
        company.setAddress(request.getAddress());
        company.setCity(request.getCity());
        company.setStateProvince(request.getStateProvince());
        company.setPostalCode(request.getPostalCode());
        company.setEmail(request.getEmail());
        company.setRegistrationNumber(request.getRegistrationNumber());
        company.setTaxId(request.getTaxId());
        company.setCreatedAt(LocalDateTime.now());
        company.setUpdatedAt(LocalDateTime.now());
        company.setStatus("ACTIVE");
        
        Company savedCompany = companyRepository.save(company);
        
        // Find admin role
        Role adminRole = roleRepository.findByName("COMPANY_ADMIN")
                .orElseThrow(() -> new ResourceNotFoundException("Admin role not found"));
        
        // Create admin user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setCompany(savedCompany);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        user.setRoles(roles);
        
        User savedUser = userRepository.save(user);
        
        return mapUserToDTO(savedUser);
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
        User user = ssoService.processSsoLogin(code, state, provisioningFlags);
        
        // Generate JWT token
        final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        final String token = jwtUtil.generateToken(userDetails);
        
        // Update last login timestamp
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
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
    
    private AuthResponse buildAuthResponse(String token, User user, UserDetails userDetails) {
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        
        UserDTO userDTO = UserDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .companyId(user.getCompany() != null ? user.getCompany().getCompanyId() : null)
                .companyName(user.getCompany() != null ? user.getCompany().getCompanyName() : null)
                .roles(roles)
                .build();
        
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .user(userDTO)
                .build();
    }
    
    private UserDTO mapUserToDTO(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        
        return UserDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .companyId(user.getCompany() != null ? user.getCompany().getCompanyId() : null)
                .companyName(user.getCompany() != null ? user.getCompany().getCompanyName() : null)
                .roles(roles)
                .build();
    }
}