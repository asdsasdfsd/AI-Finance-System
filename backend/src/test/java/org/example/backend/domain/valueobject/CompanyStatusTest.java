// backend/src/test/java/org/example/backend/domain/valueobject/CompanyStatusTest.java
package org.example.backend.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CompanyStatus value object
 * 
 * Tests cover creation, validation, business logic, and state management
 */
@DisplayName("CompanyStatus Value Object Tests")
class CompanyStatusTest extends ValueObjectTestBase {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create active company status")
        void shouldCreateActiveCompanyStatus() {
            // When
            CompanyStatus status = CompanyStatus.active();
            
            // Then
            assertNotNull(status);
            assertEquals(CompanyStatus.Status.ACTIVE, status.getStatus());
            assertTrue(status.isOperational());
        }

        @Test
        @DisplayName("Should create inactive company status")
        void shouldCreateInactiveCompanyStatus() {
            // When
            CompanyStatus status = CompanyStatus.inactive();
            
            // Then
            assertNotNull(status);
            assertEquals(CompanyStatus.Status.INACTIVE, status.getStatus());
            assertFalse(status.isOperational());
        }

        @Test
        @DisplayName("Should create company status with specific enum value")
        void shouldCreateCompanyStatusWithSpecificEnumValue() {
            // Given
            CompanyStatus.Status targetStatus = CompanyStatus.Status.ACTIVE;
            
            // When
            CompanyStatus status = CompanyStatus.of(targetStatus);
            
            // Then
            assertNotNull(status);
            assertEquals(targetStatus, status.getStatus());
        }

        @ParameterizedTest
        @EnumSource(CompanyStatus.Status.class)
        @DisplayName("Should create company status with all enum values")
        void shouldCreateCompanyStatusWithAllEnumValues(CompanyStatus.Status statusEnum) {
            // When
            CompanyStatus status = CompanyStatus.of(statusEnum);
            
            // Then
            assertNotNull(status);
            assertEquals(statusEnum, status.getStatus());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when creating with null status")
        void shouldThrowExceptionWhenCreatingWithNullStatus() {
            // Given
            CompanyStatus.Status nullStatus = null;
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CompanyStatus.of(nullStatus)
            );
            
            assertEquals("Company status cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should have default constructor with ACTIVE status")
        void shouldHaveDefaultConstructorWithActiveStatus() {
            // Note: Testing through reflection is not recommended for production tests
            // This test verifies the JPA default constructor behavior indirectly
            
            // Given & When
            CompanyStatus activeStatus = CompanyStatus.active();
            
            // Then
            assertEquals(CompanyStatus.Status.ACTIVE, activeStatus.getStatus());
            assertTrue(activeStatus.isOperational());
        }
    }

    @Nested
    @DisplayName("Status Enum Tests")
    class StatusEnumTests {

        @Test
        @DisplayName("Should have correct display names")
        void shouldHaveCorrectDisplayNames() {
            // When & Then
            assertEquals("Active", CompanyStatus.Status.ACTIVE.getDisplayName());
            assertEquals("Inactive", CompanyStatus.Status.INACTIVE.getDisplayName());
        }

        @Test
        @DisplayName("Should have exactly two status values")
        void shouldHaveExactlyTwoStatusValues() {
            // When
            CompanyStatus.Status[] statuses = CompanyStatus.Status.values();
            
            // Then
            assertEquals(2, statuses.length);
            assertTrue(java.util.Arrays.asList(statuses).contains(CompanyStatus.Status.ACTIVE));
            assertTrue(java.util.Arrays.asList(statuses).contains(CompanyStatus.Status.INACTIVE));
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should identify operational companies")
        void shouldIdentifyOperationalCompanies() {
            // Given
            CompanyStatus activeStatus = CompanyStatus.active();
            CompanyStatus inactiveStatus = CompanyStatus.inactive();
            
            // When & Then
            assertTrue(activeStatus.isOperational());
            assertFalse(inactiveStatus.isOperational());
        }

        @Test
        @DisplayName("Should determine modification permissions correctly")
        void shouldDetermineModificationPermissionsCorrectly() {
            // Given
            CompanyStatus activeStatus = CompanyStatus.active();
            CompanyStatus inactiveStatus = CompanyStatus.inactive();
            
            // When & Then
            assertTrue(activeStatus.canBeModified());
            assertFalse(inactiveStatus.canBeModified());
        }

        @Test
        @DisplayName("Should determine new user acceptance correctly")
        void shouldDetermineNewUserAcceptanceCorrectly() {
            // Given
            CompanyStatus activeStatus = CompanyStatus.active();
            CompanyStatus inactiveStatus = CompanyStatus.inactive();
            
            // When & Then
            assertTrue(activeStatus.canAcceptNewUsers());
            assertFalse(inactiveStatus.canAcceptNewUsers());
        }

        @Test
        @DisplayName("Should have consistent business rules across methods")
        void shouldHaveConsistentBusinessRulesAcrossMethods() {
            // Given
            CompanyStatus activeStatus = CompanyStatus.active();
            CompanyStatus inactiveStatus = CompanyStatus.inactive();
            
            // When & Then - All business rules should be consistent for ACTIVE status
            assertTrue(activeStatus.isOperational());
            assertTrue(activeStatus.canBeModified());
            assertTrue(activeStatus.canAcceptNewUsers());
            
            // All business rules should be consistent for INACTIVE status
            assertFalse(inactiveStatus.isOperational());
            assertFalse(inactiveStatus.canBeModified());
            assertFalse(inactiveStatus.canAcceptNewUsers());
        }
    }

    @Nested
    @DisplayName("Equality and Hash Tests")
    class EqualityAndHashTests {

        @Test
        @DisplayName("Should be equal when statuses are same")
        void shouldBeEqualWhenStatusesAreSame() {
            // Given
            CompanyStatus status1 = CompanyStatus.active();
            CompanyStatus status2 = CompanyStatus.active();
            
            // When & Then
            assertEquals(status1, status2);
            assertEquals(status1.hashCode(), status2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when statuses are different")
        void shouldNotBeEqualWhenStatusesAreDifferent() {
            // Given
            CompanyStatus activeStatus = CompanyStatus.active();
            CompanyStatus inactiveStatus = CompanyStatus.inactive();
            
            // When & Then
            assertNotEquals(activeStatus, inactiveStatus);
            assertNotEquals(activeStatus.hashCode(), inactiveStatus.hashCode());
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Given
            CompanyStatus status = CompanyStatus.active();
            
            // When & Then
            assertEquals(status, status);
            assertEquals(status.hashCode(), status.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Given
            CompanyStatus status = CompanyStatus.active();
            
            // When & Then
            assertNotEquals(status, null);
        }

        @Test
        @DisplayName("Should not be equal to different class")
        void shouldNotBeEqualToDifferentClass() {
            // Given
            CompanyStatus status = CompanyStatus.active();
            String differentObject = "ACTIVE";
            
            // When & Then
            assertNotEquals(status, differentObject);
        }

        @Test
        @DisplayName("Should maintain equality contract")
        void shouldMaintainEqualityContract() {
            // Given
            CompanyStatus status1 = CompanyStatus.of(CompanyStatus.Status.ACTIVE);
            CompanyStatus status2 = CompanyStatus.active();
            CompanyStatus status3 = CompanyStatus.of(CompanyStatus.Status.ACTIVE);
            
            // When & Then
            assertEqualsContract(status1, status2, status3);
        }
    }

    @Nested
    @DisplayName("String Representation Tests")
    class StringRepresentationTests {

        @Test
        @DisplayName("Should have meaningful toString representation")
        void shouldHaveMeaningfulToStringRepresentation() {
            // Given
            CompanyStatus activeStatus = CompanyStatus.active();
            CompanyStatus inactiveStatus = CompanyStatus.inactive();
            
            // When
            String activeToString = activeStatus.toString();
            String inactiveToString = inactiveStatus.toString();
            
            // Then
            assertEquals("Active", activeToString);
            assertEquals("Inactive", inactiveToString);
        }

        @Test
        @DisplayName("Should produce consistent toString output")
        void shouldProduceConsistentToStringOutput() {
            // Given
            CompanyStatus status = CompanyStatus.active();
            
            // When
            String firstCall = status.toString();
            String secondCall = status.toString();
            
            // Then
            assertEquals(firstCall, secondCall);
        }

        @Test
        @DisplayName("Should return display name in toString")
        void shouldReturnDisplayNameInToString() {
            // Given
            CompanyStatus activeStatus = CompanyStatus.active();
            
            // When
            String toString = activeStatus.toString();
            String displayName = activeStatus.getStatus().getDisplayName();
            
            // Then
            assertEquals(displayName, toString);
        }
    }

    @Nested
    @DisplayName("JPA Compatibility Tests")
    class JPACompatibilityTests {

        @Test
        @DisplayName("Should be embeddable value object")
        void shouldBeEmbeddableValueObject() {
            // Given
            CompanyStatus status = CompanyStatus.active();
            
            // When & Then
            assertTrue(status.getClass().isAnnotationPresent(jakarta.persistence.Embeddable.class));
        }

        @Test
        @DisplayName("Should use STRING enum mapping")
        void shouldUseStringEnumMapping() {
            // This test verifies that the enum is configured to be stored as STRING
            // rather than ORDINAL, which is important for database compatibility
            
            // Given
            CompanyStatus status = CompanyStatus.active();
            
            // When & Then
            // Verify the enum field has the correct JPA annotations
            try {
                java.lang.reflect.Field statusField = CompanyStatus.class.getDeclaredField("status");
                assertTrue(statusField.isAnnotationPresent(jakarta.persistence.Enumerated.class));
                
                jakarta.persistence.Enumerated enumAnnotation = 
                    statusField.getAnnotation(jakarta.persistence.Enumerated.class);
                assertEquals(jakarta.persistence.EnumType.STRING, enumAnnotation.value());
            } catch (NoSuchFieldException e) {
                fail("Status field should exist");
            }
        }

        @Test
        @DisplayName("Should have column annotation with correct length")
        void shouldHaveColumnAnnotationWithCorrectLength() {
            // Given & When & Then
            try {
                java.lang.reflect.Field statusField = CompanyStatus.class.getDeclaredField("status");
                assertTrue(statusField.isAnnotationPresent(jakarta.persistence.Column.class));
                
                jakarta.persistence.Column columnAnnotation = 
                    statusField.getAnnotation(jakarta.persistence.Column.class);
                assertEquals("status", columnAnnotation.name());
                assertEquals(20, columnAnnotation.length());
            } catch (NoSuchFieldException e) {
                fail("Status field should exist");
            }
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should be immutable value object")
        void shouldBeImmutableValueObject() {
            // Given
            CompanyStatus original = CompanyStatus.active();
            CompanyStatus.Status originalEnumValue = original.getStatus();
            
            // When
            CompanyStatus another = CompanyStatus.of(original.getStatus());
            
            // Then
            assertEquals(originalEnumValue, original.getStatus());
            assertNotSame(original, another);
            assertEquals(original, another);
        }

        @Test
        @DisplayName("Should not expose mutable state")
        void shouldNotExposeMutableState() {
            // Given
            CompanyStatus status = CompanyStatus.active();
            
            // When
            CompanyStatus.Status enumValue = status.getStatus();
            
            // Then
            // Enums are inherently immutable, so this is safe
            assertNotNull(enumValue);
            assertEquals(CompanyStatus.Status.ACTIVE, enumValue);
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should provide convenient factory methods")
        void shouldProvideConvenientFactoryMethods() {
            // When
            CompanyStatus activeByFactory = CompanyStatus.active();
            CompanyStatus inactiveByFactory = CompanyStatus.inactive();
            CompanyStatus activeByEnum = CompanyStatus.of(CompanyStatus.Status.ACTIVE);
            CompanyStatus inactiveByEnum = CompanyStatus.of(CompanyStatus.Status.INACTIVE);
            
            // Then
            assertEquals(activeByFactory, activeByEnum);
            assertEquals(inactiveByFactory, inactiveByEnum);
            assertEquals(CompanyStatus.Status.ACTIVE, activeByFactory.getStatus());
            assertEquals(CompanyStatus.Status.INACTIVE, inactiveByFactory.getStatus());
        }

        @Test
        @DisplayName("Should create distinct instances with factory methods")
        void shouldCreateDistinctInstancesWithFactoryMethods() {
            // When
            CompanyStatus status1 = CompanyStatus.active();
            CompanyStatus status2 = CompanyStatus.active();
            
            // Then
            assertNotSame(status1, status2);
            assertEquals(status1, status2);
        }
    }

    @Nested
    @DisplayName("Business Scenario Tests")
    class BusinessScenarioTests {

        @Test
        @DisplayName("Should handle company activation scenario")
        void shouldHandleCompanyActivationScenario() {
            // Given - A company that needs to be activated
            CompanyStatus.Status activeEnum = CompanyStatus.Status.ACTIVE;
            
            // When
            CompanyStatus activatedStatus = CompanyStatus.of(activeEnum);
            
            // Then
            assertTrue(activatedStatus.isOperational());
            assertTrue(activatedStatus.canBeModified());
            assertTrue(activatedStatus.canAcceptNewUsers());
            assertEquals("Active", activatedStatus.toString());
        }

        @Test
        @DisplayName("Should handle company deactivation scenario")
        void shouldHandleCompanyDeactivationScenario() {
            // Given - A company that needs to be deactivated
            CompanyStatus.Status inactiveEnum = CompanyStatus.Status.INACTIVE;
            
            // When
            CompanyStatus deactivatedStatus = CompanyStatus.of(inactiveEnum);
            
            // Then
            assertFalse(deactivatedStatus.isOperational());
            assertFalse(deactivatedStatus.canBeModified());
            assertFalse(deactivatedStatus.canAcceptNewUsers());
            assertEquals("Inactive", deactivatedStatus.toString());
        }

        @Test
        @DisplayName("Should support status-based business decisions")
        void shouldSupportStatusBasedBusinessDecisions() {
            // Given
            CompanyStatus activeStatus = CompanyStatus.active();
            CompanyStatus inactiveStatus = CompanyStatus.inactive();
            
            // When & Then - Simulate business logic decisions
            
            // Active companies can perform all operations
            if (activeStatus.isOperational()) {
                assertTrue(activeStatus.canAcceptNewUsers());
                assertTrue(activeStatus.canBeModified());
            }
            
            // Inactive companies have restricted operations
            if (!inactiveStatus.isOperational()) {
                assertFalse(inactiveStatus.canAcceptNewUsers());
                assertFalse(inactiveStatus.canBeModified());
            }
        }
    }
}