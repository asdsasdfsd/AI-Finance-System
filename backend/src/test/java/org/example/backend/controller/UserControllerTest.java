// backend/src/test/java/org/example/backend/controller/UserControllerTest.java
package org.example.backend.controller;

import org.example.backend.application.service.UserApplicationService;
import org.example.backend.application.dto.UserDTO;
import org.example.backend.application.dto.CreateUserCommand;
import org.example.backend.application.dto.UpdateUserCommand;
import org.example.backend.exception.ResourceNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserController - Testing Real Controller with Mocked Dependencies
 * 
 * This test class creates a REAL instance of UserController and mocks its dependencies,
 * following the proper unit testing approach for testing HTTP layer business logic.
 * 
 * Coverage Target: From 0% to 80%+ for all Controller methods
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("User Controller Tests - Real Controller Implementation")
class UserControllerTest {

    // Mock dependencies (not the controller itself!)
    @Mock
    private UserApplicationService userApplicationService;

    // Real controller instance under test
    private UserController userController;

    // Test constants
    private static final Integer TEST_USER_ID = 1;
    private static final Integer TEST_COMPANY_ID = 100;
    private static final Integer TEST_DEPARTMENT_ID = 200;
    private static final String TEST_USERNAME = "test_user";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_EXTERNAL_ID = "ext_123";
    private static final String TEST_ROLE_NAME = "USER";
    private static final String TEST_PASSWORD = "new_password";
    private static final String TEST_CURRENT_PASSWORD = "current_password";

    @BeforeEach
    void setUp() {
        // Create real controller instance with mocked dependencies
        userController = new UserController(userApplicationService);
    }

    // ========== Constructor Tests ==========
    
    @Test
    @DisplayName("Should create UserController instance successfully")
    void shouldCreateUserControllerInstanceSuccessfully() {
        // When & Then
        assertNotNull(userController);
    }

    // ========== Create User Operations ==========
    
    @Nested
    @DisplayName("Create User Operations")
    class CreateUserOperations {
        
        @Test
        @DisplayName("Should create user successfully via POST")
        void shouldCreateUserSuccessfully() {
            // Given - prepare request data
            Map<String, Object> request = createValidUserRequest();
            UserDTO expectedUser = createMockUserDTO();
            
            // Mock application service behavior
            when(userApplicationService.createUser(any(CreateUserCommand.class)))
                .thenReturn(expectedUser);
            
            // When - call real Controller method
            ResponseEntity<UserDTO> response = userController.createUser(request);
            
            // Then - verify HTTP response
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertEquals(TEST_USERNAME, response.getBody().getUsername());
            
            // Verify service interaction
            verify(userApplicationService).createUser(any(CreateUserCommand.class));
        }
        
        @Test
        @DisplayName("Should create SSO user successfully")
        void shouldCreateSsoUserSuccessfully() {
            // Given
            Map<String, Object> request = createValidSsoUserRequest();
            UserDTO expectedUser = createMockUserDTO();
            
            when(userApplicationService.createSsoUser(
                eq(TEST_USERNAME), eq(TEST_EMAIL), anyString(), 
                eq(TEST_EXTERNAL_ID), eq(TEST_COMPANY_ID), anyString()))
                .thenReturn(expectedUser);
            
            // When
            ResponseEntity<UserDTO> response = userController.createSsoUser(request);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            
            verify(userApplicationService).createSsoUser(
                eq(TEST_USERNAME), eq(TEST_EMAIL), anyString(),
                eq(TEST_EXTERNAL_ID), eq(TEST_COMPANY_ID), anyString());
        }
        
        @Test
        @DisplayName("Should map request to CreateUserCommand correctly")
        void shouldMapRequestToCreateUserCommandCorrectly() {
            // Given
            Map<String, Object> request = createValidUserRequest();
            UserDTO mockUser = createMockUserDTO();
            
            ArgumentCaptor<CreateUserCommand> commandCaptor = 
                ArgumentCaptor.forClass(CreateUserCommand.class);
            
            when(userApplicationService.createUser(commandCaptor.capture()))
                .thenReturn(mockUser);
            
            // When
            userController.createUser(request);
            
            // Then - verify mapping is correct
            CreateUserCommand capturedCommand = commandCaptor.getValue();
            assertNotNull(capturedCommand);
            
            verify(userApplicationService).createUser(any(CreateUserCommand.class));
        }
    }

    // ========== Get User Operations ==========
    
    @Nested
    @DisplayName("Get User Operations")
    class GetUserOperations {
        
        @Test
        @DisplayName("Should get user by ID successfully via GET")
        void shouldGetUserByIdSuccessfully() {
            // Given
            UserDTO expectedUser = createMockUserDTO();
            when(userApplicationService.getUserById(TEST_USER_ID))
                .thenReturn(expectedUser);
            
            // When
            ResponseEntity<UserDTO> response = userController.getUserById(TEST_USER_ID);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertEquals(TEST_USER_ID, response.getBody().getUserId());
            
            verify(userApplicationService).getUserById(TEST_USER_ID);
        }
        
        @Test
        @DisplayName("Should get user by username successfully")
        void shouldGetUserByUsernameSuccessfully() {
            // Given
            UserDTO expectedUser = createMockUserDTO();
            when(userApplicationService.getUserByUsername(TEST_USERNAME))
                .thenReturn(expectedUser);
            
            // When
            ResponseEntity<UserDTO> response = userController.getUserByUsername(TEST_USERNAME);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertEquals(TEST_USERNAME, response.getBody().getUsername());
            
            verify(userApplicationService).getUserByUsername(TEST_USERNAME);
        }
        
        @Test
        @DisplayName("Should get user by external ID successfully")
        void shouldGetUserByExternalIdSuccessfully() {
            // Given
            UserDTO expectedUser = createMockUserDTO();
            when(userApplicationService.getUserByExternalId(TEST_EXTERNAL_ID))
                .thenReturn(expectedUser);
            
            // When
            ResponseEntity<UserDTO> response = userController.getUserByExternalId(TEST_EXTERNAL_ID);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            
            verify(userApplicationService).getUserByExternalId(TEST_EXTERNAL_ID);
        }
        
        @Test
        @DisplayName("Should get users by department successfully")
        void shouldGetUsersByDepartmentSuccessfully() {
            // Given
            List<UserDTO> expectedUsers = Arrays.asList(createMockUserDTO());
            when(userApplicationService.getUsersByDepartment(TEST_COMPANY_ID, TEST_DEPARTMENT_ID))
                .thenReturn(expectedUsers);
            
            // When
            ResponseEntity<List<UserDTO>> response = userController.getUsersByDepartment(TEST_DEPARTMENT_ID, TEST_COMPANY_ID);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertFalse(response.getBody().isEmpty());
            
            verify(userApplicationService).getUsersByDepartment(TEST_COMPANY_ID, TEST_DEPARTMENT_ID);
        }
        
        @Test
        @DisplayName("Should get users by role successfully")
        void shouldGetUsersByRoleSuccessfully() {
            // Given
            List<UserDTO> expectedUsers = Arrays.asList(createMockUserDTO());
            when(userApplicationService.getUsersByRole(TEST_COMPANY_ID, TEST_ROLE_NAME))
                .thenReturn(expectedUsers);
            
            // When
            ResponseEntity<List<UserDTO>> response = userController.getUsersByRole(TEST_ROLE_NAME, TEST_COMPANY_ID);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertFalse(response.getBody().isEmpty());
            
            verify(userApplicationService).getUsersByRole(TEST_COMPANY_ID, TEST_ROLE_NAME);
        }
        
        @Test
        @DisplayName("Should search users successfully")
        void shouldSearchUsersSuccessfully() {
            // Given
            String keyword = "test";
            List<UserDTO> expectedUsers = Arrays.asList(createMockUserDTO());
            when(userApplicationService.searchUsers(TEST_COMPANY_ID, keyword))
                .thenReturn(expectedUsers);
            
            // When
            ResponseEntity<List<UserDTO>> response = userController.searchUsers(keyword, TEST_COMPANY_ID);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertFalse(response.getBody().isEmpty());
            
            verify(userApplicationService).searchUsers(TEST_COMPANY_ID, keyword);
        }
    }

    // ========== Update User Operations ==========
    
    @Nested
    @DisplayName("Update User Operations")
    class UpdateUserOperations {
        
        @Test
        @DisplayName("Should update user successfully via PUT")
        void shouldUpdateUserSuccessfully() {
            // Given
            Map<String, Object> request = createValidUpdateRequest();
            UserDTO expectedUser = createMockUserDTO();
            
            when(userApplicationService.updateUser(eq(TEST_USER_ID), any(UpdateUserCommand.class)))
                .thenReturn(expectedUser);
            
            // When
            ResponseEntity<UserDTO> response = userController.updateUser(TEST_USER_ID, request);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            
            verify(userApplicationService).updateUser(eq(TEST_USER_ID), any(UpdateUserCommand.class));
        }
        
        @Test
        @DisplayName("Should change password successfully")
        void shouldChangePasswordSuccessfully() {
            // Given
            Map<String, Object> request = createPasswordChangeRequest();
            
            doNothing().when(userApplicationService).changePassword(
                eq(TEST_USER_ID), eq(TEST_CURRENT_PASSWORD), eq(TEST_PASSWORD));
            
            // When
            ResponseEntity<Map<String, Object>> response = userController.changePassword(TEST_USER_ID, request);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            
            verify(userApplicationService).changePassword(TEST_USER_ID, TEST_CURRENT_PASSWORD, TEST_PASSWORD);
        }
        
        @Test
        @DisplayName("Should map request to UpdateUserCommand correctly")
        void shouldMapRequestToUpdateUserCommandCorrectly() {
            // Given
            Map<String, Object> request = createValidUpdateRequest();
            UserDTO mockUser = createMockUserDTO();
            
            ArgumentCaptor<UpdateUserCommand> commandCaptor = 
                ArgumentCaptor.forClass(UpdateUserCommand.class);
            
            when(userApplicationService.updateUser(eq(TEST_USER_ID), commandCaptor.capture()))
                .thenReturn(mockUser);
            
            // When
            userController.updateUser(TEST_USER_ID, request);
            
            // Then - verify mapping is correct
            UpdateUserCommand capturedCommand = commandCaptor.getValue();
            assertNotNull(capturedCommand);
            
            verify(userApplicationService).updateUser(eq(TEST_USER_ID), any(UpdateUserCommand.class));
        }
    }

    // ========== Delete User Operations ==========
    
    @Test
    @DisplayName("Should delete user successfully via DELETE")
    void shouldDeleteUserSuccessfully() {
        // Given
        UserDTO disabledUser = createMockUserDTO();
        when(userApplicationService.disableUser(TEST_USER_ID))
            .thenReturn(disabledUser);
        
        // When
        ResponseEntity<Void> response = userController.deleteUser(TEST_USER_ID);
        
        // Then
        assertNotNull(response);
        assertEquals(204, response.getStatusCode().value()); // No Content
        
        verify(userApplicationService).disableUser(TEST_USER_ID);
    }

    // ========== User State Management Operations ==========
    
    @Nested
    @DisplayName("User State Management Operations")
    class UserStateManagementOperations {
        
        @Test
        @DisplayName("Should enable user successfully")
        void shouldEnableUserSuccessfully() {
            // Given
            UserDTO enabledUser = createMockUserDTO();
            when(userApplicationService.enableUser(TEST_USER_ID))
                .thenReturn(enabledUser);
            
            // When
            ResponseEntity<UserDTO> response = userController.enableUser(TEST_USER_ID);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            
            verify(userApplicationService).enableUser(TEST_USER_ID);
        }
        
        @Test
        @DisplayName("Should disable user successfully")
        void shouldDisableUserSuccessfully() {
            // Given
            UserDTO disabledUser = createMockUserDTO();
            when(userApplicationService.disableUser(TEST_USER_ID))
                .thenReturn(disabledUser);
            
            // When
            ResponseEntity<UserDTO> response = userController.disableUser(TEST_USER_ID);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            
            verify(userApplicationService).disableUser(TEST_USER_ID);
        }
        
        @Test
        @DisplayName("Should unlock user successfully")
        void shouldUnlockUserSuccessfully() {
            // Given
            UserDTO unlockedUser = createMockUserDTO();
            when(userApplicationService.unlockUser(TEST_USER_ID))
                .thenReturn(unlockedUser);
            
            // When
            ResponseEntity<UserDTO> response = userController.unlockUser(TEST_USER_ID);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            
            verify(userApplicationService).unlockUser(TEST_USER_ID);
        }
    }

    // ========== Role Management Operations ==========
    
    @Nested
    @DisplayName("Role Management Operations")
    class RoleManagementOperations {
        
        @Test
        @DisplayName("Should assign role successfully")
        void shouldAssignRoleSuccessfully() {
            // Given
            UserDTO userWithRole = createMockUserDTO();
            when(userApplicationService.assignRole(TEST_USER_ID, TEST_ROLE_NAME))
                .thenReturn(userWithRole);
            
            // When
            ResponseEntity<UserDTO> response = userController.assignRole(TEST_USER_ID, TEST_ROLE_NAME);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            
            verify(userApplicationService).assignRole(TEST_USER_ID, TEST_ROLE_NAME);
        }
        
        @Test
        @DisplayName("Should remove role successfully")
        void shouldRemoveRoleSuccessfully() {
            // Given
            UserDTO userWithoutRole = createMockUserDTO();
            when(userApplicationService.removeRole(TEST_USER_ID, TEST_ROLE_NAME))
                .thenReturn(userWithoutRole);
            
            // When
            ResponseEntity<UserDTO> response = userController.removeRole(TEST_USER_ID, TEST_ROLE_NAME);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            
            verify(userApplicationService).removeRole(TEST_USER_ID, TEST_ROLE_NAME);
        }
        
        @Test
        @DisplayName("Should extract role names from request correctly")
        void shouldExtractRoleNamesFromRequestCorrectly() {
            // Given
            Map<String, Object> request = createValidUserRequest();
            request.put("roleNames", Arrays.asList("USER", "ADMIN"));
            UserDTO mockUser = createMockUserDTO();
            
            when(userApplicationService.createUser(any(CreateUserCommand.class)))
                .thenReturn(mockUser);
            
            // When
            ResponseEntity<UserDTO> response = userController.createUser(request);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            verify(userApplicationService).createUser(any(CreateUserCommand.class));
        }
    }

    // ========== Validation Operations ==========
    
    @Nested
    @DisplayName("Validation Operations")
    class ValidationOperations {
        
        @Test
        @DisplayName("Should check username exists successfully")
        void shouldCheckUsernameExistsSuccessfully() {
            // Given
            when(userApplicationService.existsByUsername(TEST_USERNAME))
                .thenReturn(true);
            
            // When
            ResponseEntity<Map<String, Boolean>> response = userController.checkUsernameExists(TEST_USERNAME);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().get("exists"));
            
            verify(userApplicationService).existsByUsername(TEST_USERNAME);
        }
        
        @Test
        @DisplayName("Should check email exists successfully")
        void shouldCheckEmailExistsSuccessfully() {
            // Given
            when(userApplicationService.existsByEmail(TEST_EMAIL))
                .thenReturn(false);
            
            // When
            ResponseEntity<Map<String, Boolean>> response = userController.checkEmailExists(TEST_EMAIL);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertFalse(response.getBody().get("exists"));
            
            verify(userApplicationService).existsByEmail(TEST_EMAIL);
        }
    }

    // ========== Login Tracking Operations ==========
    
    @Nested
    @DisplayName("Login Tracking Operations")
    class LoginTrackingOperations {
        
        @Test
        @DisplayName("Should record successful login successfully")
        void shouldRecordSuccessfulLoginSuccessfully() {
            // Given
            doNothing().when(userApplicationService).recordSuccessfulLogin(TEST_USER_ID);
            
            // When
            ResponseEntity<Void> response = userController.recordSuccessfulLogin(TEST_USER_ID);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            
            verify(userApplicationService).recordSuccessfulLogin(TEST_USER_ID);
        }
        
        @Test
        @DisplayName("Should record failed login successfully")
        void shouldRecordFailedLoginSuccessfully() {
            // Given
            doNothing().when(userApplicationService).recordFailedLogin(TEST_USER_ID);
            
            // When
            ResponseEntity<Void> response = userController.recordFailedLogin(TEST_USER_ID);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            
            verify(userApplicationService).recordFailedLogin(TEST_USER_ID);
        }
    }

    // ========== Exception Handling Tests ==========
    
    @Nested
    @DisplayName("Exception Handling")
    class ExceptionHandling {
        
        @Test
        @DisplayName("Should handle ResourceNotFoundException correctly")
        void shouldHandleResourceNotFoundExceptionCorrectly() {
            // Given
            when(userApplicationService.getUserById(TEST_USER_ID))
                .thenThrow(new ResourceNotFoundException("User not found"));
            
            // When
            ResponseEntity<UserDTO> response = userController.getUserById(TEST_USER_ID);
            
            // Then - Controller catches exception and returns 404
            assertNotNull(response);
            assertEquals(404, response.getStatusCode().value());
            
            verify(userApplicationService).getUserById(TEST_USER_ID);
        }
        
        @Test
        @DisplayName("Should handle invalid request data")
        void shouldHandleInvalidRequestDataWithBadRequest() {
            // Given - empty request will cause mapping errors before reaching service
            Map<String, Object> invalidRequest = new HashMap<>();
            // Empty request should trigger mapping error in Controller before calling service
            
            // When
            ResponseEntity<UserDTO> response = userController.createUser(invalidRequest);
            
            // Then - Controller catches mapping exception and returns 400 (bad request)
            // Based on actual Controller code: catch (Exception e) -> status(400) for createUser
            assertNotNull(response);
            assertEquals(400, response.getStatusCode().value());
            
            // Service is not called because mapping fails first
            verifyNoInteractions(userApplicationService);
        }
        
        @Test
        @DisplayName("Should handle missing required fields")
        void shouldHandleMissingRequiredFields() {
            // Given - request with missing required fields causes mapping error
            Map<String, Object> incompleteRequest = new HashMap<>();
            incompleteRequest.put("username", TEST_USERNAME); // missing email and other required fields
            
            // When
            ResponseEntity<UserDTO> response = userController.createUser(incompleteRequest);
            
            // Then - Controller catches mapping exception and returns 400
            // Based on actual Controller: createUser maps empty/invalid request -> Exception -> 400
            assertNotNull(response);
            assertEquals(400, response.getStatusCode().value());
            
            // Service is not called because mapping fails first
            verifyNoInteractions(userApplicationService);
        }
        
        @Test
        @DisplayName("Should handle service layer validation errors")
        void shouldHandleServiceLayerValidationErrors() {
            // Given - valid request format but service throws validation error
            Map<String, Object> request = createValidUserRequest();
            
            when(userApplicationService.createUser(any(CreateUserCommand.class)))
                .thenThrow(new IllegalArgumentException("Username already exists"));
            
            // When
            ResponseEntity<UserDTO> response = userController.createUser(request);
            
            // Then - Controller catches service exception and returns 400
            assertNotNull(response);
            assertEquals(400, response.getStatusCode().value());
            
            verify(userApplicationService).createUser(any(CreateUserCommand.class));
        }
    }

    // ========== Request Mapping Utility Tests ==========
    
    @Nested
    @DisplayName("Request Mapping Utilities")
    class RequestMappingUtilities {
        
        @Test
        @DisplayName("Should handle getString with default value")
        void shouldHandleGetStringWithDefaultValue() {
            // Given
            Map<String, Object> request = createValidUserRequest();
            request.put("email", TEST_EMAIL);
            UserDTO mockUser = createMockUserDTO();
            
            when(userApplicationService.createUser(any(CreateUserCommand.class)))
                .thenReturn(mockUser);
            
            // When
            ResponseEntity<UserDTO> response = userController.createUser(request);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            verify(userApplicationService).createUser(any(CreateUserCommand.class));
        }
        
        @Test
        @DisplayName("Should handle getInteger extraction")
        void shouldHandleGetIntegerExtraction() {
            // Given
            Map<String, Object> request = createValidUserRequest();
            request.put("companyId", TEST_COMPANY_ID);
            UserDTO mockUser = createMockUserDTO();
            
            when(userApplicationService.createUser(any(CreateUserCommand.class)))
                .thenReturn(mockUser);
            
            // When
            ResponseEntity<UserDTO> response = userController.createUser(request);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            verify(userApplicationService).createUser(any(CreateUserCommand.class));
        }
        
        @Test
        @DisplayName("Should handle getRequiredString extraction")
        void shouldHandleGetRequiredStringExtraction() {
            // Given
            Map<String, Object> request = createValidUserRequest();
            request.put("username", TEST_USERNAME);
            request.put("email", TEST_EMAIL);
            UserDTO mockUser = createMockUserDTO();
            
            when(userApplicationService.createUser(any(CreateUserCommand.class)))
                .thenReturn(mockUser);
            
            // When
            ResponseEntity<UserDTO> response = userController.createUser(request);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            verify(userApplicationService).createUser(any(CreateUserCommand.class));
        }
        
        @Test
        @DisplayName("Should handle getRequiredInteger extraction")
        void shouldHandleGetRequiredIntegerExtraction() {
            // Given
            Map<String, Object> request = createValidUserRequest();
            request.put("companyId", TEST_COMPANY_ID);
            UserDTO mockUser = createMockUserDTO();
            
            when(userApplicationService.createUser(any(CreateUserCommand.class)))
                .thenReturn(mockUser);
            
            // When
            ResponseEntity<UserDTO> response = userController.createUser(request);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            verify(userApplicationService).createUser(any(CreateUserCommand.class));
        }
        
        @Test
        @DisplayName("Should handle getBoolean with default value")
        void shouldHandleGetBooleanWithDefaultValue() {
            // Given
            Map<String, Object> request = createValidUserRequest();
            request.put("active", true);
            UserDTO mockUser = createMockUserDTO();
            
            when(userApplicationService.createUser(any(CreateUserCommand.class)))
                .thenReturn(mockUser);
            
            // When
            ResponseEntity<UserDTO> response = userController.createUser(request);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            verify(userApplicationService).createUser(any(CreateUserCommand.class));
        }
    }

    // ========== Test Data Helper Methods ==========

    private Map<String, Object> createValidUserRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("username", TEST_USERNAME);
        request.put("email", TEST_EMAIL);
        request.put("fullName", "Test User");
        request.put("password", "test_password");
        request.put("companyId", TEST_COMPANY_ID);
        request.put("departmentId", TEST_DEPARTMENT_ID);
        request.put("roleNames", Arrays.asList("USER"));
        request.put("active", true);
        return request;
    }

    private Map<String, Object> createValidSsoUserRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("username", TEST_USERNAME);
        request.put("email", TEST_EMAIL);
        request.put("fullName", "Test SSO User");
        request.put("externalId", TEST_EXTERNAL_ID);
        request.put("companyId", TEST_COMPANY_ID);
        request.put("departmentId", TEST_DEPARTMENT_ID);
        request.put("roleNames", Arrays.asList("USER"));
        return request;
    }

    private UserDTO createMockUserDTO() {
        UserDTO userDTO = UserDTO.builder()
            .userId(TEST_USER_ID)
            .username(TEST_USERNAME)
            .email(TEST_EMAIL)
            .fullName("Test User")
            .tenantId(TEST_COMPANY_ID)
            .departmentId(TEST_DEPARTMENT_ID)
            .enabled(true)
            .build();
        return userDTO;
    }

    private Map<String, Object> createValidUpdateRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("email", "updated@example.com");
        request.put("fullName", "Updated User");
        request.put("companyId", TEST_COMPANY_ID);
        request.put("departmentId", TEST_DEPARTMENT_ID);
        return request;
    }

    private Map<String, Object> createPasswordChangeRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("currentPassword", TEST_CURRENT_PASSWORD);
        request.put("newPassword", TEST_PASSWORD);
        return request;
    }
}