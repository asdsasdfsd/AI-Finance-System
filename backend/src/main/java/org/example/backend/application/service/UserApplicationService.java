// backend/src/main/java/org/example/backend/application/service/UserApplicationService.java
package org.example.backend.application.service;

import org.example.backend.application.dto.CreateUserCommand;
import org.example.backend.application.dto.UpdateUserCommand;
import org.example.backend.application.dto.UserDTO;
import org.example.backend.domain.aggregate.user.UserAggregate;
import org.example.backend.domain.aggregate.user.UserAggregateRepository;
import org.example.backend.domain.aggregate.company.CompanyAggregate;
import org.example.backend.domain.aggregate.company.CompanyAggregateRepository;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.event.DomainEventPublisher;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.exception.UnauthorizedException;
import org.example.backend.model.Role;
import org.example.backend.repository.RoleRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * User Application Service
 * 
 * Orchestrates user management use cases including authentication,
 * authorization, role management, and multi-tenant user operations
 */
@Service
@Transactional
public class UserApplicationService {
    
    private final UserAggregateRepository userRepository;
    private final CompanyAggregateRepository companyRepository;
    private final RoleRepository roleRepository;
    private final DomainEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    
    public UserApplicationService(UserAggregateRepository userRepository,
                                CompanyAggregateRepository companyRepository,
                                RoleRepository roleRepository,
                                DomainEventPublisher eventPublisher,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
        this.eventPublisher = eventPublisher;
        this.passwordEncoder = passwordEncoder;
    }
    
    // ========== Command Handlers ==========
    
    /**
     * Create new user
     */
    public UserDTO createUser(CreateUserCommand command) {
        validateCreateCommand(command);
        
        // Check for duplicate username or email
        if (userRepository.existsByUsername(command.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + command.getUsername());
        }
        
        if (userRepository.existsByEmail(command.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + command.getEmail());
        }
        
        // Validate company exists and can accept new users
        CompanyAggregate company = findCompanyById(command.getCompanyId());
        if (!company.isActive()) {
            throw new IllegalArgumentException("Cannot create user in inactive company");
        }
        
        // Check user limit
        long currentUserCount = userRepository.countByTenantId(company.getTenantId());
        if (!company.canAddUser((int) currentUserCount)) {
            throw new IllegalArgumentException("Company has reached maximum user limit");
        }
        
        // Get roles
        Set<Role> roles = getRolesByNames(command.getRoleNames());
        if (roles.isEmpty()) {
            // Default to USER role if no roles specified
            Role defaultRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Default USER role not found"));
            roles.add(defaultRole);
        }
        
        // Encode password
        String encodedPassword = passwordEncoder.encode(command.getPassword());
        
        UserAggregate user = UserAggregate.createUser(
            command.getUsername(),
            command.getEmail(),
            encodedPassword,
            command.getFullName(),
            company.getTenantId(),
            roles.iterator().next() // Primary role
        );
        
        // Add additional roles if any
        roles.stream().skip(1).forEach(user::assignRole);
        
        // Set optional fields
        setOptionalFields(user, command);
        
        UserAggregate savedUser = userRepository.save(user);
        
        // Publish domain events
        eventPublisher.publishAll(savedUser.getDomainEvents());
        savedUser.clearDomainEvents();
        
        return mapToDTO(savedUser);
    }
    
    /**
     * Create SSO user
     */
    public UserDTO createSsoUser(String username, String email, String fullName, 
                                String externalId, Integer companyId, String defaultRoleName) {
        CompanyAggregate company = findCompanyById(companyId);
        TenantId tenantId = company.getTenantId();
        
        Role defaultRole = roleRepository.findByName(defaultRoleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + defaultRoleName));
        
        UserAggregate user = UserAggregate.createFromSso(
            username, email, fullName, externalId, tenantId, defaultRole
        );
        
        UserAggregate savedUser = userRepository.save(user);
        
        // Publish domain events
        eventPublisher.publishAll(savedUser.getDomainEvents());
        savedUser.clearDomainEvents();
        
        return mapToDTO(savedUser);
    }
    
    /**
     * Update user information
     */
    public UserDTO updateUser(Integer userId, UpdateUserCommand command) {
        validateUpdateCommand(command);
        
        UserAggregate user = findUserById(userId);
        
        // Update basic information
        user.updateBasicInfo(command.getFullName(), command.getEmail());
        
        // Update password if provided
        if (command.getPassword() != null && !command.getPassword().trim().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(command.getPassword());
            user.changePassword(encodedPassword);
        }
        
        // Update roles if provided
        if (command.getRoleNames() != null && !command.getRoleNames().isEmpty()) {
            Set<Role> newRoles = getRolesByNames(command.getRoleNames());
            user.replaceRoles(newRoles);
        }
        
        // Update department if provided
        if (command.getDepartmentId() != null) {
            user.setDepartment(command.getDepartmentId());
        }
        
        // Update preferences if provided
        if (command.getPreferredLanguage() != null || command.getTimezone() != null) {
            user.updatePreferences(command.getPreferredLanguage(), command.getTimezone());
        }
        
        UserAggregate savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }
    
    /**
     * Enable user
     */
    public UserDTO enableUser(Integer userId) {
        UserAggregate user = findUserById(userId);
        user.enable();
        
        UserAggregate savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }
    
    /**
     * Disable user
     */
    public UserDTO disableUser(Integer userId) {
        UserAggregate user = findUserById(userId);
        user.disable();
        
        UserAggregate savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }
    
    /**
     * Assign role to user
     */
    public UserDTO assignRole(Integer userId, String roleName) {
        UserAggregate user = findUserById(userId);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        
        user.assignRole(role);
        
        UserAggregate savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }
    
    /**
     * Remove role from user
     */
    public UserDTO removeRole(Integer userId, String roleName) {
        UserAggregate user = findUserById(userId);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        
        user.removeRole(role);
        
        UserAggregate savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }
    
    /**
     * Change user password
     */
    public void changePassword(Integer userId, String currentPassword, String newPassword) {
        UserAggregate user = findUserById(userId);
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new UnauthorizedException("Current password is incorrect");
        }
        
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.changePassword(encodedNewPassword);
        
        userRepository.save(user);
    }
    
    /**
     * Record successful login
     */
    public void recordSuccessfulLogin(Integer userId) {
        UserAggregate user = findUserById(userId);
        user.recordSuccessfulLogin();
        userRepository.save(user);
    }
    
    /**
     * Record failed login
     */
    public void recordFailedLogin(Integer userId) {
        UserAggregate user = findUserById(userId);
        user.recordFailedLogin();
        userRepository.save(user);
    }
    
    /**
     * Unlock user account
     */
    public UserDTO unlockUser(Integer userId) {
        UserAggregate user = findUserById(userId);
        user.unlock();
        
        UserAggregate savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }
    
    // ========== Query Handlers ==========
    
    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Integer userId) {
        UserAggregate user = findUserById(userId);
        return mapToDTO(user);
    }
    
    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        UserAggregate user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return mapToDTO(user);
    }
    
    /**
     * Get user by external ID (SSO)
     */
    @Transactional(readOnly = true)
    public UserDTO getUserByExternalId(String externalId) {
        UserAggregate user = userRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with external ID: " + externalId));
        return mapToDTO(user);
    }
    
    /**
     * Get all users for a company
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByCompany(Integer companyId) {
        CompanyAggregate company = findCompanyById(companyId);
        List<UserAggregate> users = userRepository.findByTenantId(company.getTenantId());
        return users.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    
    /**
     * Get users by department
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByDepartment(Integer companyId, Integer departmentId) {
        CompanyAggregate company = findCompanyById(companyId);
        List<UserAggregate> users = userRepository.findByTenantIdAndDepartmentId(company.getTenantId(), departmentId);
        return users.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    
    /**
     * Get users by role
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByRole(Integer companyId, String roleName) {
        CompanyAggregate company = findCompanyById(companyId);
        List<UserAggregate> users = userRepository.findByTenantIdAndRole(company.getTenantId(), roleName);
        return users.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    
    /**
     * Search users by name or email
     */
    @Transactional(readOnly = true)
    public List<UserDTO> searchUsers(Integer companyId, String searchTerm) {
        CompanyAggregate company = findCompanyById(companyId);
        List<UserAggregate> users = userRepository.searchByNameOrEmail(company.getTenantId(), searchTerm);
        return users.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    
    /**
     * Check if user exists by username
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * Check if user exists by email
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    // ========== Helper Methods ==========
    
    private UserAggregate findUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }
    
    private CompanyAggregate findCompanyById(Integer companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + companyId));
    }
    
    private Set<Role> getRolesByNames(Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
            roles.add(role);
        }
        return roles;
    }
    
    private void validateCreateCommand(CreateUserCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Create user command cannot be null");
        }
        if (command.getUsername() == null || command.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (command.getEmail() == null || command.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (command.getPassword() == null || command.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (command.getFullName() == null || command.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (command.getCompanyId() == null) {
            throw new IllegalArgumentException("Company ID is required");
        }
    }
    
    private void validateUpdateCommand(UpdateUserCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Update user command cannot be null");
        }
    }
    
    private void setOptionalFields(UserAggregate user, CreateUserCommand command) {
        if (command.getDepartmentId() != null) {
            user.setDepartment(command.getDepartmentId());
        }
        
        if (command.getPreferredLanguage() != null || command.getTimezone() != null) {
            user.updatePreferences(command.getPreferredLanguage(), command.getTimezone());
        }
        
        // Fix: Use the method that returns primitive boolean to avoid null comparison issues
        if (!command.getEnabled()) {
            user.disable();
        }
    }
    
    private UserDTO mapToDTO(UserAggregate user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        
        return UserDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .enabled(user.getEnabled())
                .externalId(user.getExternalId())
                .tenantId(user.getTenantId().getValue())
                .departmentId(user.getDepartmentId())
                .preferredLanguage(user.getPreferredLanguage())
                .timezone(user.getTimezone())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .passwordChangedAt(user.getPasswordChangedAt())
                .roleNames(roleNames)
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .lockedUntil(user.getLockedUntil())
                .isLocked(user.isLocked())
                .isActiveAndUnlocked(user.isActiveAndUnlocked())
                .isSsoUser(user.isSsoUser())
                .isPasswordExpired(user.isPasswordExpired())
                .build();
    }
}