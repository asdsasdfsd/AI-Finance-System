// backend/src/test/java/org/example/backend/application/service/UserApplicationServiceTest.java
package org.example.backend.application.service;

import org.example.backend.application.dto.CreateUserCommand;
import org.example.backend.application.dto.UpdateUserCommand;
import org.example.backend.application.dto.ChangePasswordCommand;
import org.example.backend.application.dto.UserDTO;
import org.example.backend.domain.aggregate.user.UserAggregate;
import org.example.backend.domain.aggregate.user.UserAggregateRepository;
import org.example.backend.domain.aggregate.company.CompanyAggregateRepository;
import org.example.backend.domain.aggregate.company.CompanyAggregate;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.event.DomainEventPublisher;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.model.Role;
import org.example.backend.repository.RoleRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserApplicationService
 * 
 * Tests user management functionality including creation, updates, role assignment, and status changes
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserApplicationService Tests")
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
    
    private CreateUserCommand createCommand;
    private UpdateUserCommand updateCommand;
    private ChangePasswordCommand changePasswordCommand;
    private UserAggregate testUser;
    private CompanyAggregate testCompany;
    private Role testRole;
    
    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setRoleId(1);
        testRole.setName("USER");
        testRole.setDescription("Basic User Role");
        
        testCompany = CompanyAggregate.create(
            "Test Company",
            "test@company.com",
            "Test Address",
            "BL123456",
            1
        );
        
        createCommand = CreateUserCommand.builder()
            .username("testuser")
            .email("test@example.com")
            .password("password123")
            .fullName("Test User")
            .companyId(1)
            .roleNames(Set.of("USER"))
            .build();
            
        updateCommand = UpdateUserCommand.builder()
            .username("updateduser")
            .email("updated@example.com")
            .fullName("Updated User")
            .companyId(1)
            .build();
            .companyId(1)
            .build();
            
        changePasswordCommand = ChangePasswordCommand.builder()
            .currentPassword("oldpassword")
            .newPassword("newpassword123")
            .build();
            
        testUser = UserAggregate.create(
            "testuser",
            "test@example.com",
            "encodedpassword",
            "Test User",
            TenantId.of(1)
        );
    }
    
    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {
        
        @Test
        @DisplayName("Should create user successfully with valid command")
        void shouldCreateUserSuccessfully() {
            // Given
            when(companyRepository.findById(createCommand.getCompanyId())).thenReturn(Optional.of(testCompany));
            when(userRepository.existsByUsername(createCommand.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(createCommand.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(createCommand.getPassword())).thenReturn("encodedpassword");
            when(roleRepository.findByName("USER")).thenReturn(Optional.of(testRole));
            when(userRepository.save(any(UserAggregate.class))).thenReturn(testUser);
            
            // When
            UserDTO result = userApplicationService.createUser(createCommand);
            
            // Then
            assertNotNull(result);
            assertEquals(createCommand.getUsername(), result.getUsername());
            assertEquals(createCommand.getEmail(), result.getEmail());
            assertEquals(createCommand.getFullName(), result.getFullName());
            assertTrue(result.isActiveAndUnlocked());
            
            verify(companyRepository).findById(createCommand.getCompanyId());
            verify(userRepository).existsByUsername(createCommand.getUsername());
            verify(userRepository).existsByEmail(createCommand.getEmail());
            verify(passwordEncoder).encode(createCommand.getPassword());
            verify(roleRepository).findByName("USER");
            verify(userRepository).save(any(UserAggregate.class));
            verify(eventPublisher).publishAll(any());
        }
        
        @Test
        @DisplayName("Should throw exception when company not found")
        void shouldThrowExceptionWhenCompanyNotFound() {
            // Given
            when(companyRepository.findById(createCommand.getCompanyId())).thenReturn(Optional.empty());
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userApplicationService.createUser(createCommand)
            );
            
            assertTrue(exception.getMessage().contains("Company not found"));
            verify(companyRepository).findById(createCommand.getCompanyId());
            verify(userRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Should throw exception when username already exists")
        void shouldThrowExceptionWhenUsernameExists() {
            // Given
            when(companyRepository.findById(createCommand.getCompanyId())).thenReturn(Optional.of(testCompany));
            when(userRepository.existsByUsername(createCommand.getUsername())).thenReturn(true);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userApplicationService.createUser(createCommand)
            );
            
            assertTrue(exception.getMessage().contains("Username already exists"));
            verify(userRepository).existsByUsername(createCommand.getUsername());
            verify(userRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            // Given
            when(companyRepository.findById(createCommand.getCompanyId())).thenReturn(Optional.of(testCompany));
            when(userRepository.existsByUsername(createCommand.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(createCommand.getEmail())).thenReturn(true);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userApplicationService.createUser(createCommand)
            );
            
            assertTrue(exception.getMessage().contains("Email already exists"));
            verify(userRepository).existsByEmail(createCommand.getEmail());
            verify(userRepository, never()).save(any());
        }
    }
    
    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {
        
        @Test
        @DisplayName("Should update user successfully")
        void shouldUpdateUserSuccessfully() {
            // Given
            when(userRepository.findById(updateCommand.getUserId())).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(UserAggregate.class))).thenReturn(testUser);
            
            // When
            UserDTO result = userApplicationService.updateUser(1, updateCommand);
            
            // Then
            assertNotNull(result);
            verify(userRepository).findById(1);
            verify(userRepository).save(testUser);
        }
        
        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findById(1)).thenReturn(Optional.empty());
            
            // When & Then
            ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userApplicationService.updateUser(1, updateCommand)
            );
            
            assertTrue(exception.getMessage().contains("User not found"));
            verify(userRepository).findById(1);
            verify(userRepository, never()).save(any());
        }
    }
    
    @Nested
    @DisplayName("Password Management Tests")
    class PasswordManagementTests {
        
        @Test
        @DisplayName("Should change password successfully")
        void shouldChangePasswordSuccessfully() {
            // Given
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(changePasswordCommand.getCurrentPassword(), testUser.getPassword())).thenReturn(true);
            when(passwordEncoder.encode(changePasswordCommand.getNewPassword())).thenReturn("newencodedpassword");
            when(userRepository.save(any(UserAggregate.class))).thenReturn(testUser);
            
            // When
            UserDTO result = userApplicationService.changePassword(1, changePasswordCommand.getCurrentPassword(), changePasswordCommand.getNewPassword());
            
            // Then
            assertNotNull(result);
            verify(userRepository).findById(1);
            verify(passwordEncoder).matches(changePasswordCommand.getCurrentPassword(), testUser.getPassword());
            verify(passwordEncoder).encode(changePasswordCommand.getNewPassword());
            verify(userRepository).save(testUser);
        }
        
        @Test
        @DisplayName("Should throw exception when current password is incorrect")
        void shouldThrowExceptionWhenCurrentPasswordIncorrect() {
            // Given
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(changePasswordCommand.getCurrentPassword(), testUser.getPassword())).thenReturn(false);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userApplicationService.changePassword(1, changePasswordCommand.getCurrentPassword(), changePasswordCommand.getNewPassword())
            );
            
            assertTrue(exception.getMessage().contains("Current password is incorrect"));
            verify(passwordEncoder).matches(changePasswordCommand.getCurrentPassword(), testUser.getPassword());
            verify(userRepository, never()).save(any());
        }
    }
    
    @Nested
    @DisplayName("User Status Management Tests")
    class UserStatusTests {
        
        @Test
        @DisplayName("Should enable user successfully")
        void shouldEnableUserSuccessfully() {
            // Given
            testUser.disable();
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(UserAggregate.class))).thenReturn(testUser);
            
            // When
            UserDTO result = userApplicationService.enableUser(1);
            
            // Then
            assertNotNull(result);
            verify(userRepository).findById(1);
            verify(userRepository).save(testUser);
            verify(eventPublisher).publishAll(any());
        }
        
        @Test
        @DisplayName("Should disable user successfully")
        void shouldDisableUserSuccessfully() {
            // Given
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(UserAggregate.class))).thenReturn(testUser);
            
            // When
            UserDTO result = userApplicationService.disableUser(1);
            
            // Then
            assertNotNull(result);
            verify(userRepository).findById(1);
            verify(userRepository).save(testUser);
            verify(eventPublisher).publishAll(any());
        }
    }
    
    @Nested
    @DisplayName("Role Assignment Tests")
    class RoleAssignmentTests {
        
        @Test
        @DisplayName("Should assign role successfully")
        void shouldAssignRoleSuccessfully() {
            // Given
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(testRole));
            when(userRepository.save(any(UserAggregate.class))).thenReturn(testUser);
            
            // When
            UserDTO result = userApplicationService.assignRole(1, "ADMIN");
            
            // Then
            assertNotNull(result);
            verify(userRepository).findById(1);
            verify(roleRepository).findByName("ADMIN");
            verify(userRepository).save(testUser);
        }
        
        @Test
        @DisplayName("Should throw exception when role not found")
        void shouldThrowExceptionWhenRoleNotFound() {
            // Given
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(roleRepository.findByName("INVALID_ROLE")).thenReturn(Optional.empty());
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userApplicationService.assignRole(1, "INVALID_ROLE")
            );
            
            assertTrue(exception.getMessage().contains("Role not found"));
            verify(roleRepository).findByName("INVALID_ROLE");
            verify(userRepository, never()).save(any());
        }
    }
    
    @Nested
    @DisplayName("Query Tests")
    class QueryTests {
        
        @Test
        @DisplayName("Should get user by ID successfully")
        void shouldGetUserByIdSuccessfully() {
            // Given
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            
            // When
            UserDTO result = userApplicationService.getUserById(1);
            
            // Then
            assertNotNull(result);
            assertEquals(testUser.getUsername(), result.getUsername());
            verify(userRepository).findById(1);
        }
        
        @Test
        @DisplayName("Should get users by company successfully")
        void shouldGetUsersByCompanySuccessfully() {
            // Given
            List<UserAggregate> users = List.of(testUser);
            when(userRepository.findByTenantId(any(TenantId.class))).thenReturn(users);
            
            // When
            List<UserDTO> result = userApplicationService.getUsersByCompany(1);
            
            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(userRepository).findByTenantId(any(TenantId.class));
        }
    }
}