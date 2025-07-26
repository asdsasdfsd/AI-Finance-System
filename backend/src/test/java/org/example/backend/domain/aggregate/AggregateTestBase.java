// backend/src/test/java/org/example/backend/domain/aggregate/AggregateTestBase.java
package org.example.backend.domain.aggregate;

import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.Money;
import org.example.backend.domain.valueobject.TransactionStatus;
import org.example.backend.domain.valueobject.CompanyStatus;
import org.example.backend.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Base class for aggregate unit tests
 * 
 * Provides common test utilities and factory methods for creating test data
 */
@ExtendWith(MockitoExtension.class)
public abstract class AggregateTestBase {
    
    // Common test data
    protected static final Integer TEST_COMPANY_ID = 1;
    protected static final Integer TEST_USER_ID = 100;
    protected static final String TEST_CURRENCY = "CNY";
    protected static final LocalDate TEST_DATE = LocalDate.of(2024, 1, 15);
    protected static final LocalDateTime TEST_DATETIME = LocalDateTime.of(2024, 1, 15, 10, 30);
    
    @BeforeEach
    void setUp() {
        // Common setup for all aggregate tests
    }
    
    // Factory methods for value objects
    protected TenantId createTestTenantId() {
        return TenantId.of(TEST_COMPANY_ID);
    }
    
    protected TenantId createTenantId(Integer companyId) {
        return TenantId.of(companyId);
    }
    
    protected Money createTestMoney() {
        return Money.of(new BigDecimal("1000.00"), TEST_CURRENCY);
    }
    
    protected Money createMoney(String amount) {
        return Money.of(new BigDecimal(amount), TEST_CURRENCY);
    }
    
    protected Money createMoney(String amount, String currency) {
        return Money.of(new BigDecimal(amount), currency);
    }
    
    protected Role createTestRole() {
        Role role = new Role();
        role.setRoleId(1);
        role.setName("USER");
        role.setDescription("Test User Role");
        return role;
    }
    
    protected Role createRole(String name) {
        Role role = new Role();
        role.setRoleId(1);
        role.setName(name);
        role.setDescription("Test " + name + " Role");
        return role;
    }
    
    // Helper methods for validation
    protected void assertValidCreationTime(LocalDateTime createdAt) {
        assertValidCreationTime(createdAt, LocalDateTime.now());
    }
    
    protected void assertValidCreationTime(LocalDateTime createdAt, LocalDateTime reference) {
        org.junit.jupiter.api.Assertions.assertNotNull(createdAt);
        org.junit.jupiter.api.Assertions.assertTrue(
            createdAt.isBefore(reference.plusSeconds(1)) && 
            createdAt.isAfter(reference.minusSeconds(5))
        );
    }
    
    protected void assertEventPublished(java.util.List<Object> events, Class<?> expectedEventType) {
        org.junit.jupiter.api.Assertions.assertTrue(
            events.stream().anyMatch(event -> expectedEventType.isInstance(event)),
            "Expected event of type " + expectedEventType.getSimpleName() + " not found"
        );
    }
}

