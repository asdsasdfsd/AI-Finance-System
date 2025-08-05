// backend/src/test/java/org/example/backend/application/service/UserApplicationServiceTest.java
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
import org.example.backend.model.Role;
import org.example.backend.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Real Unit tests for UserApplicationService
 * 
 * Tests the actual service implementation by mocking its dependencies
 * This approach will provide real code coverage
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)  // Add lenient mode to avoid UnnecessaryStubbing
@DisplayName("User Application Service Tests - Real Implementation Coverage")
class UserApplicationServiceTest {

    // Mock dependencies (not the service itself!)
    @Mock
    private UserAggregateRepository userRepository;
    
    @Mock
    private CompanyAggregateRepository companyRepository;
    
    @Mock
    private RoleRepository roleRepository;
    
    @Mock
    private DomainEventPublisher eventPublisher;
    
    @Mock
    private PasswordEncoder passwordEncoder;

    // Real service instance
    private UserApplicationService userApplicationService;

    // Test constants
    private static final Integer TEST_USER_ID = 1;
    private static final Integer TEST_COMPANY_ID = 100;
    private static final Integer TEST_DEPARTMENT_ID = 200;
    private static final String TEST_USERNAME = "test_user";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_FULL_NAME = "Test User";
    private static final String TEST_PASSWORD = "test_password";
    private static final String TEST_NEW_PASSWORD = "new_password";
    private static final String TEST_ROLE_NAME = "USER";
    private static final String TEST_EXTERNAL_ID = "ext123";
    private static final String TEST_SEARCH_KEYWORD = "test";

    @BeforeEach
    void setUp() {
        // Create real service instance with mocked dependencies
        userApplicationService = new UserApplicationService(
                userRepository, 
                companyRepository, 
                roleRepository, 
                eventPublisher, 
                passwordEncoder
        );
    }

    // ========== Create User Tests ==========

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() {
        // Given
        CreateUserCommand command = createValidCreateCommand();
        CompanyAggregate mockCompany = createMockCompany();
        Role mockRole = createMockRole();
        UserAggregate mockUser = createMockUserAggregate();

        // Mock dependencies
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(companyRepository.findById(TEST_COMPANY_ID)).thenReturn(Optional.of(mockCompany));
        when(userRepository.countByTenantId(any(TenantId.class))).thenReturn(5L); // Mock user count < limit
        when(roleRepository.findByName(TEST_ROLE_NAME)).thenReturn(Optional.of(mockRole));
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn("encoded_password");
        when(userRepository.save(any(UserAggregate.class))).thenReturn(mockUser);
        doNothing().when(eventPublisher).publishAll(anyList());

        // When
        UserDTO result = userApplicationService.createUser(command);

        // Then
        assertNotNull(result);
        assertEquals(TEST_USER_ID, result.getUserId());
        assertEquals(TEST_USERNAME, result.getUsername());
        assertEquals(TEST_EMAIL, result.getEmail());
        assertEquals(TEST_FULL_NAME, result.getFullName());
        assertTrue(result.getEnabled());
        
        // Verify interactions
        verify(userRepository).existsByUsername(TEST_USERNAME);
        verify(userRepository).existsByEmail(TEST_EMAIL);
        verify(companyRepository).findById(TEST_COMPANY_ID);
        verify(userRepository).countByTenantId(any(TenantId.class));
        verify(roleRepository).findByName(TEST_ROLE_NAME);
        verify(passwordEncoder).encode(TEST_PASSWORD);
        verify(userRepository).save(any(UserAggregate.class));
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    @DisplayName("Should throw exception when create command is null")
    void shouldThrowExceptionWhenCreateCommandIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userApplicationService.createUser(null);
        });
        
        assertEquals("Create user command cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        // Given
        CreateUserCommand command = createValidCreateCommand();
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userApplicationService.createUser(command);
        });
        
        assertTrue(exception.getMessage().contains("Username already exists"));
        verify(userRepository).existsByUsername(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        CreateUserCommand command = createValidCreateCommand();
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userApplicationService.createUser(command);
        });
        
        assertTrue(exception.getMessage().contains("Email already exists"));
        verify(userRepository).existsByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should throw exception when company not found")
    void shouldThrowExceptionWhenCompanyNotFound() {
        // Given
        CreateUserCommand command = createValidCreateCommand();
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(companyRepository.findById(TEST_COMPANY_ID)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userApplicationService.createUser(command);
        });
        
        assertTrue(exception.getMessage().contains("Company not found"));
        verify(companyRepository).findById(TEST_COMPANY_ID);
    }

    @Test
    @DisplayName("Should throw exception when company user limit reached")
    void shouldThrowExceptionWhenCompanyUserLimitReached() {
        // Given
        CreateUserCommand command = createValidCreateCommand();
        CompanyAggregate mockCompany = mock(CompanyAggregate.class);
        when(mockCompany.getCompanyId()).thenReturn(TEST_COMPANY_ID);
        when(mockCompany.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(mockCompany.isActive()).thenReturn(true);
        when(mockCompany.canAddUser(anyInt())).thenReturn(false); // User limit reached
        
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(companyRepository.findById(TEST_COMPANY_ID)).thenReturn(Optional.of(mockCompany));
        when(userRepository.countByTenantId(any(TenantId.class))).thenReturn(100L); // High user count

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userApplicationService.createUser(command);
        });
        
        assertEquals("Company has reached maximum user limit", exception.getMessage());
        verify(userRepository).countByTenantId(any(TenantId.class));
        verify(mockCompany).canAddUser(anyInt());
    }

    // ========== Update User Tests ==========

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
        // Given
        UpdateUserCommand command = createValidUpdateCommand();
        UserAggregate mockUser = createMockUserAggregate();
        UserAggregate updatedUser = createUpdatedUserAggregate();

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(UserAggregate.class))).thenReturn(updatedUser);

        // When
        UserDTO result = userApplicationService.updateUser(TEST_USER_ID, command);

        // Then
        assertNotNull(result);
        assertEquals(TEST_USER_ID, result.getUserId());
        
        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository).save(any(UserAggregate.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent user")
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        // Given
        UpdateUserCommand command = createValidUpdateCommand();
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userApplicationService.updateUser(999, command);
        });
        
        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).findById(999);
    }

    // ========== Password Management Tests ==========

    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePasswordSuccessfully() {
        // Given
        UserAggregate mockUser = createMockUserAggregate();
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(TEST_PASSWORD, "old_encoded_password")).thenReturn(true);
        when(passwordEncoder.encode(TEST_NEW_PASSWORD)).thenReturn("new_encoded_password");
        when(userRepository.save(any(UserAggregate.class))).thenReturn(mockUser);

        // When
        userApplicationService.changePassword(TEST_USER_ID, TEST_PASSWORD, TEST_NEW_PASSWORD);

        // Then
        verify(userRepository).findById(TEST_USER_ID);
        verify(passwordEncoder).matches(TEST_PASSWORD, "old_encoded_password");
        verify(passwordEncoder).encode(TEST_NEW_PASSWORD);
        verify(userRepository).save(any(UserAggregate.class));
    }

    @Test
    @DisplayName("Should throw exception when current password is incorrect")
    void shouldThrowExceptionWhenCurrentPasswordIsIncorrect() {
        // Given
        UserAggregate mockUser = createMockUserAggregate();
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrong_password", "old_encoded_password")).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userApplicationService.changePassword(TEST_USER_ID, "wrong_password", TEST_NEW_PASSWORD);
        });
        
        verify(passwordEncoder).matches("wrong_password", "old_encoded_password");
    }

    // ========== User Status Management Tests ==========

    @Test
    @DisplayName("Should enable user successfully")
    void shouldEnableUserSuccessfully() {
        // Given
        UserAggregate mockUser = createMockUserAggregate();
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(UserAggregate.class))).thenReturn(mockUser);

        // When
        UserDTO result = userApplicationService.enableUser(TEST_USER_ID);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository).save(any(UserAggregate.class));
    }

    @Test
    @DisplayName("Should disable user successfully")
    void shouldDisableUserSuccessfully() {
        // Given
        UserAggregate mockUser = createMockUserAggregate();
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(UserAggregate.class))).thenReturn(mockUser);

        // When
        UserDTO result = userApplicationService.disableUser(TEST_USER_ID);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository).save(any(UserAggregate.class));
    }

    // ========== Role Management Tests ==========

    @Test
    @DisplayName("Should assign role successfully")
    void shouldAssignRoleSuccessfully() {
        // Given
        UserAggregate mockUser = createMockUserAggregate();
        Role mockRole = createMockRole();
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));
        when(roleRepository.findByName(TEST_ROLE_NAME)).thenReturn(Optional.of(mockRole));
        when(userRepository.save(any(UserAggregate.class))).thenReturn(mockUser);

        // When
        UserDTO result = userApplicationService.assignRole(TEST_USER_ID, TEST_ROLE_NAME);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(TEST_USER_ID);
        verify(roleRepository).findByName(TEST_ROLE_NAME);
        verify(userRepository).save(any(UserAggregate.class));
    }

    @Test
    @DisplayName("Should throw exception when assigning non-existent role")
    void shouldThrowExceptionWhenAssigningNonExistentRole() {
        // Given
        UserAggregate mockUser = createMockUserAggregate();
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));
        when(roleRepository.findByName("INVALID_ROLE")).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userApplicationService.assignRole(TEST_USER_ID, "INVALID_ROLE");
        });
        
        assertTrue(exception.getMessage().contains("Role not found"));
    }

    // ========== Query Tests ==========

    @Test
    @DisplayName("Should get user by id successfully")
    void shouldGetUserByIdSuccessfully() {
        // Given
        UserAggregate mockUser = createMockUserAggregate();
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));

        // When
        UserDTO result = userApplicationService.getUserById(TEST_USER_ID);

        // Then
        assertNotNull(result);
        assertEquals(TEST_USER_ID, result.getUserId());
        verify(userRepository).findById(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should get user by username successfully")
    void shouldGetUserByUsernameSuccessfully() {
        // Given
        UserAggregate mockUser = createMockUserAggregate();
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(mockUser));

        // When
        UserDTO result = userApplicationService.getUserByUsername(TEST_USERNAME);

        // Then
        assertNotNull(result);
        assertEquals(TEST_USERNAME, result.getUsername());
        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should get users by company successfully")
    void shouldGetUsersByCompanySuccessfully() {
        // Given
        List<UserAggregate> mockUsers = Arrays.asList(createMockUserAggregate(), createAnotherUserAggregate());
        // Mock the company first to avoid "Company not found" error
        CompanyAggregate mockCompany = createMockCompany();
        when(companyRepository.findById(TEST_COMPANY_ID)).thenReturn(Optional.of(mockCompany));
        when(userRepository.findByTenantId(any(TenantId.class))).thenReturn(mockUsers);

        // When
        List<UserDTO> results = userApplicationService.getUsersByCompany(TEST_COMPANY_ID);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(companyRepository).findById(TEST_COMPANY_ID);
        verify(userRepository).findByTenantId(any(TenantId.class));
    }

    // ========== Validation Tests ==========

    @Test
    @DisplayName("Should check if username exists")
    void shouldCheckIfUsernameExists() {
        // Given
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(true);

        // When
        boolean exists = userApplicationService.existsByUsername(TEST_USERNAME);

        // Then
        assertTrue(exists);
        verify(userRepository).existsByUsername(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should check if email exists")
    void shouldCheckIfEmailExists() {
        // Given
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        // When
        boolean exists = userApplicationService.existsByEmail(TEST_EMAIL);

        // Then
        assertTrue(exists);
        verify(userRepository).existsByEmail(TEST_EMAIL);
    }

    // ========== Authentication Support Tests ==========

    @Test
    @DisplayName("Should get password for authentication")
    void shouldGetPasswordForAuthentication() {
        // Given
        UserAggregate mockUser = createMockUserAggregate();
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(mockUser));

        // When
        String result = userApplicationService.getPasswordForAuthentication(TEST_USERNAME);

        // Then
        assertNotNull(result);
        assertEquals("old_encoded_password", result);
        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should record successful login")
    void shouldRecordSuccessfulLogin() {
        // Given
        UserAggregate mockUser = createMockUserAggregate();
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(UserAggregate.class))).thenReturn(mockUser);

        // When
        userApplicationService.recordSuccessfulLogin(TEST_USER_ID);

        // Then
        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository).save(any(UserAggregate.class));
    }

    @Test
    @DisplayName("Should record failed login")
    void shouldRecordFailedLogin() {
        // Given
        UserAggregate mockUser = createMockUserAggregate();
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(UserAggregate.class))).thenReturn(mockUser);

        // When
        userApplicationService.recordFailedLogin(TEST_USER_ID);

        // Then
        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository).save(any(UserAggregate.class));
    }

    // ========== Error Handling Tests ==========

    @Test
    @DisplayName("Should throw exception when user not found by id")
    void shouldThrowExceptionWhenUserNotFoundById() {
        // Given
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userApplicationService.getUserById(999);
        });
        
        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    @DisplayName("Should throw exception when user not found by username")
    void shouldThrowExceptionWhenUserNotFoundByUsername() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userApplicationService.getUserByUsername("nonexistent");
        });
        
        assertTrue(exception.getMessage().contains("User not found"));
    }

    // ========== Helper Methods ==========

    private CreateUserCommand createValidCreateCommand() {
        return CreateUserCommand.builder()
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .fullName(TEST_FULL_NAME)
                .companyId(TEST_COMPANY_ID)
                .roleNames(Set.of(TEST_ROLE_NAME))
                .enabled(true)
                .departmentId(TEST_DEPARTMENT_ID)
                .preferredLanguage("en")
                .timezone("UTC")
                .build();
    }

    private UpdateUserCommand createValidUpdateCommand() {
        return UpdateUserCommand.builder()
                .fullName("Updated Full Name")
                .email("updated@example.com")
                .departmentId(TEST_DEPARTMENT_ID)
                .preferredLanguage("zh")
                .timezone("Asia/Shanghai")
                .build();
    }

    private CompanyAggregate createMockCompany() {
        CompanyAggregate company = mock(CompanyAggregate.class);
        when(company.getCompanyId()).thenReturn(TEST_COMPANY_ID);
        when(company.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(company.getCompanyName()).thenReturn("Test Company");
        when(company.isActive()).thenReturn(true);  // Ensure company is active
        when(company.canAddUser(anyInt())).thenReturn(true);  // Allow adding users (no limit reached)
        return company;
    }

    private Role createMockRole() {
        Role role = mock(Role.class);
        when(role.getName()).thenReturn(TEST_ROLE_NAME);
        when(role.getRoleId()).thenReturn(1);
        return role;
    }

    private UserAggregate createMockUserAggregate() {
        UserAggregate user = mock(UserAggregate.class);
        Role mockRole = createMockRole(); // 先创建Role，避免嵌套stubbing
        
        // Only stub what's actually needed by the mapToDTO method
        when(user.getUserId()).thenReturn(TEST_USER_ID);
        when(user.getUsername()).thenReturn(TEST_USERNAME);
        when(user.getEmail()).thenReturn(TEST_EMAIL);
        when(user.getFullName()).thenReturn(TEST_FULL_NAME);
        when(user.getEnabled()).thenReturn(true);
        when(user.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(user.getRoles()).thenReturn(Set.of(mockRole)); // 使用预先创建的Role
        when(user.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(user.getDomainEvents()).thenReturn(Arrays.asList());
        
        // Password-related for changePassword tests
        when(user.getPassword()).thenReturn("old_encoded_password");
        
        return user;
    }

    private UserAggregate createUpdatedUserAggregate() {
        UserAggregate user = mock(UserAggregate.class);
        Role mockRole = createMockRole(); // 先创建Role
        
        // Only essential stubbing for update tests
        when(user.getUserId()).thenReturn(TEST_USER_ID);
        when(user.getUsername()).thenReturn(TEST_USERNAME);
        when(user.getEmail()).thenReturn("updated@example.com");
        when(user.getFullName()).thenReturn("Updated Full Name");
        when(user.getEnabled()).thenReturn(true);
        when(user.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(user.getRoles()).thenReturn(Set.of(mockRole));
        when(user.getCreatedAt()).thenReturn(LocalDateTime.now());
        return user;
    }

    private UserAggregate createAnotherUserAggregate() {
        UserAggregate user = mock(UserAggregate.class);
        Role mockRole = createMockRole(); // 先创建Role
        
        // Only essential stubbing
        when(user.getUserId()).thenReturn(2);
        when(user.getUsername()).thenReturn("another_user");
        when(user.getEmail()).thenReturn("another@example.com");
        when(user.getFullName()).thenReturn("Another User");
        when(user.getEnabled()).thenReturn(true);
        when(user.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(user.getRoles()).thenReturn(Set.of(mockRole));
        when(user.getCreatedAt()).thenReturn(LocalDateTime.now());
        return user;
    }
}