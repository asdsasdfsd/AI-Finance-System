// backend/src/test/java/org/example/backend/domain/aggregate/user/UserAggregateTest.java - 修复版本
package org.example.backend.domain.aggregate.user;

import org.example.backend.domain.aggregate.AggregateTestBase;
import org.example.backend.domain.event.UserCreatedEvent;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserAggregate - 修复版本
 */
public class UserAggregateTest extends AggregateTestBase {
    
    @Test
    @DisplayName("Should create user successfully with valid data")
    void shouldCreateUserSuccessfully() {
        // Given
        String username = "testuser";
        String email = "testuser@company.com";
        String encodedPassword = "encoded_password_hash";
        String fullName = "Test User";
        TenantId tenantId = createTestTenantId();
        Role userRole = createTestRole();
        
        // When
        UserAggregate user = UserAggregate.createUser(
            username, email, encodedPassword, fullName, tenantId, userRole
        );
        
        // Then
        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(encodedPassword, user.getPassword());
        assertEquals(fullName, user.getFullName());
        assertEquals(tenantId, user.getTenantId());
        assertTrue(user.getEnabled());
        assertEquals("zh-CN", user.getPreferredLanguage());
        assertEquals("Asia/Shanghai", user.getTimezone());
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
        
        // Verify role assignment
        assertTrue(user.getRoles().contains(userRole));
        assertEquals(1, user.getRoles().size());
        
        // Verify audit fields
        assertValidCreationTime(user.getCreatedAt());
        assertValidCreationTime(user.getUpdatedAt());
        assertValidCreationTime(user.getPasswordChangedAt());
        
        // Verify domain event
        assertEventPublished(user.getDomainEvents(), UserCreatedEvent.class);
    }
    
    @Test
    @DisplayName("Should create admin user successfully")
    void shouldCreateAdminUserSuccessfully() {
        // Given
        String username = "admin";
        String email = "admin@company.com";
        String encodedPassword = "encoded_admin_password";
        String fullName = "Admin User";
        TenantId tenantId = createTestTenantId();
        Role adminRole = createRole("ADMIN");
        
        // When
        UserAggregate user = UserAggregate.createAdmin(
            username, email, encodedPassword, fullName, tenantId, adminRole
        );
        
        // Then
        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertTrue(user.getRoles().contains(adminRole));
        assertEventPublished(user.getDomainEvents(), UserCreatedEvent.class);
    }
    
    @Test
    @DisplayName("Should create SSO user successfully")
    void shouldCreateSsoUserSuccessfully() {
        // Given
        String username = "ssouser";
        String email = "ssouser@company.com";
        String fullName = "SSO User";
        String externalId = "sso_external_123";
        TenantId tenantId = createTestTenantId();
        Role defaultRole = createTestRole();
        
        // When
        UserAggregate user = UserAggregate.createFromSso(
            username, email, fullName, externalId, tenantId, defaultRole
        );
        
        // Then
        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(fullName, user.getFullName());
        assertEquals(externalId, user.getExternalId());
        assertEquals("SSO_MANAGED", user.getPassword());
        assertTrue(user.getRoles().contains(defaultRole));
    }
    
    @Test
    @DisplayName("Should update basic info successfully")
    void shouldUpdateBasicInfoSuccessfully() {
        // Given
        UserAggregate user = createTestUser();
        String newFullName = "Updated Full Name";
        String newEmail = "updated@company.com";
        
        // When
        user.updateBasicInfo(newFullName, newEmail);
        
        // Then
        assertEquals(newFullName, user.getFullName());
        assertEquals(newEmail, user.getEmail());
    }
    
    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePasswordSuccessfully() {
        // Given
        UserAggregate user = createTestUser();
        String newPassword = "new_encoded_password";
        LocalDateTime originalPasswordChangedAt = user.getPasswordChangedAt();
        
        // Wait a moment to ensure different timestamp
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        // When
        user.changePassword(newPassword);
        
        // Then
        assertEquals(newPassword, user.getPassword());
        assertTrue(user.getPasswordChangedAt().isAfter(originalPasswordChangedAt));
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
    }
    
    @Test
    @DisplayName("Should assign role successfully")
    void shouldAssignRoleSuccessfully() {
        // Given
        UserAggregate user = createTestUser();
        Role newRole = createRole("MANAGER");
        
        // When
        user.assignRole(newRole);
        
        // Then
        assertTrue(user.getRoles().contains(newRole));
        assertEquals(2, user.getRoles().size());
    }
    
    @Test
    @DisplayName("Should record successful login")
    void shouldRecordSuccessfulLogin() {
        // Given
        UserAggregate user = createTestUser();
        LocalDateTime beforeLogin = LocalDateTime.now();
        
        // When
        user.recordSuccessfulLogin();
        
        // Then
        assertNotNull(user.getLastLogin());
        assertTrue(user.getLastLogin().isAfter(beforeLogin.minusSeconds(1)));
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
    }
    
    // Helper method to create test user
    private UserAggregate createTestUser() {
        return UserAggregate.createUser(
            "testuser",
            "testuser@company.com",
            "encoded_password",
            "Test User",
            createTestTenantId(),
            createTestRole()
        );
    }
}

