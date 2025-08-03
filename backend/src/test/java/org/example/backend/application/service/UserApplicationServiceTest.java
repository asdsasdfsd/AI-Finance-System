// backend/src/test/java/org/example/backend/application/service/UserApplicationServiceTest.java
package org.example.backend.application.service;

import org.example.backend.application.dto.CreateUserCommand;
import org.example.backend.application.dto.UpdateUserCommand;
import org.example.backend.application.dto.UserDTO;
import org.example.backend.domain.aggregate.company.CompanyAggregate;
import org.example.backend.domain.aggregate.company.CompanyAggregateRepository;
import org.example.backend.domain.aggregate.user.UserAggregate;
import org.example.backend.domain.aggregate.user.UserAggregateRepository;
import org.example.backend.domain.event.DomainEventPublisher;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.model.Role;
import org.example.backend.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserApplicationService - Fixed Version
 */
@ExtendWith(MockitoExtension.class)
class UserApplicationServiceTest {

    @Mock
    private UserAggregateRepository userRepository;

    @Mock
    private CompanyAggregateRepository companyRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private UserApplicationService userApplicationService;

    // Test constants
    private static final Integer TEST_USER_ID = 1;
    private static final Integer TEST_COMPANY_ID = 100;
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_FULL_NAME = "Test User";
    private static final String TEST_PASSWORD = "testpassword";
    private static final String TEST_ENCODED_PASSWORD = "encodedpassword";

    private UserAggregate testUser;
    private CompanyAggregate testCompany;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testUser = createMockUser();
        testCompany = createMockCompany();
        testRole = createMockRole();
    }

    // ========== Create User Tests ==========

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() {
        // Given
        CreateUserCommand command = createValidCreateCommand();
        when(companyRepository.findById(TEST_COMPANY_ID)).thenReturn(Optional.of(testCompany));
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(TEST_ENCODED_PASSWORD);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(UserAggregate.class))).thenReturn(testUser);

        // When
        UserDTO result = userApplicationService.createUser(command);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getUserId(), result.getUserId());
        assertEquals(testUser.getUsername(), result.getUsername());
        
        verify(companyRepository).findById(TEST_COMPANY_ID);
        verify(userRepository).existsByUsername(TEST_USERNAME);
        verify(userRepository).existsByEmail(TEST_EMAIL);
        verify(passwordEncoder).encode(TEST_PASSWORD);
        verify(userRepository).save(any(UserAggregate.class));
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    @DisplayName("Should throw exception when company not found")
    void shouldThrowExceptionWhenCompanyNotFound() {
        // Given
        CreateUserCommand command = createValidCreateCommand();
        when(companyRepository.findById(TEST_COMPANY_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            userApplicationService.createUser(command);
        });
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        // Given
        CreateUserCommand command = createValidCreateCommand();
        when(companyRepository.findById(TEST_COMPANY_ID)).thenReturn(Optional.of(testCompany));
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userApplicationService.createUser(command);
        });
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        CreateUserCommand command = createValidCreateCommand();
        when(companyRepository.findById(TEST_COMPANY_ID)).thenReturn(Optional.of(testCompany));
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userApplicationService.createUser(command);
        });
    }

    // ========== Update User Tests ==========

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
        // Given
        UpdateUserCommand command = createValidUpdateCommand();
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn(TEST_ENCODED_PASSWORD);
        when(userRepository.save(any(UserAggregate.class))).thenReturn(testUser);

        // When
        UserDTO result = userApplicationService.updateUser(TEST_USER_ID, command);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should throw exception when user not found for update")
    void shouldThrowExceptionWhenUserNotFoundForUpdate() {
        // Given
        UpdateUserCommand command = createValidUpdateCommand();
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            userApplicationService.updateUser(TEST_USER_ID, command);
        });
    }

    @Test
    @DisplayName("Should enable user successfully")
    void shouldEnableUserSuccessfully() {
        // Given
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserAggregate.class))).thenReturn(testUser);

        // When
        UserDTO result = userApplicationService.enableUser(TEST_USER_ID);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(TEST_USER_ID);
        verify(testUser).enable();
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should disable user successfully")
    void shouldDisableUserSuccessfully() {
        // Given
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserAggregate.class))).thenReturn(testUser);

        // When
        UserDTO result = userApplicationService.disableUser(TEST_USER_ID);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(TEST_USER_ID);
        verify(testUser).disable();
        verify(userRepository).save(testUser);
    }

    // ========== Helper Methods ==========

    private CreateUserCommand createValidCreateCommand() {
        return CreateUserCommand.builder()
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .fullName(TEST_FULL_NAME)
                .companyId(TEST_COMPANY_ID)
                .enabled(true)
                .build();
    }

    private UpdateUserCommand createValidUpdateCommand() {
        return UpdateUserCommand.builder()
                .fullName("Updated Full Name")
                .email("updated@example.com")
                .password("newpassword")
                .build();
    }

    private UserAggregate createMockUser() {
        UserAggregate user = mock(UserAggregate.class);
        
        when(user.getUserId()).thenReturn(TEST_USER_ID);
        when(user.getUsername()).thenReturn(TEST_USERNAME);
        when(user.getEmail()).thenReturn(TEST_EMAIL);
        when(user.getFullName()).thenReturn(TEST_FULL_NAME);
        when(user.getEnabled()).thenReturn(true);
        when(user.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(user.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(user.getRoles()).thenReturn(Set.of(testRole));
        when(user.getDomainEvents()).thenReturn(new ArrayList<>());
        
        // Mock behavior methods
        doNothing().when(user).updateBasicInfo(anyString(), anyString());
        doNothing().when(user).changePassword(anyString());
        doNothing().when(user).enable();
        doNothing().when(user).disable();
        doNothing().when(user).clearDomainEvents();
        
        return user;
    }

    private CompanyAggregate createMockCompany() {
        CompanyAggregate company = mock(CompanyAggregate.class);
        
        when(company.getCompanyId()).thenReturn(TEST_COMPANY_ID);
        when(company.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        
        return company;
    }

    private Role createMockRole() {
        Role role = new Role();
        role.setRoleId(1);
        role.setName("USER");
        role.setDescription("Test User Role");
        return role;
    }
}