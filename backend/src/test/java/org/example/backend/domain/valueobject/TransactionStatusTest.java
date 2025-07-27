// backend/src/test/java/org/example/backend/domain/valueobject/TransactionStatusTest.java
package org.example.backend.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransactionStatus value object
 * 
 * Tests cover status transitions, validation rules, and state management
 */
@DisplayName("TransactionStatus Value Object Tests")
class TransactionStatusTest {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create TransactionStatus with DRAFT status")
        void shouldCreateTransactionStatusWithDraftStatus() {
            // When
            TransactionStatus status = TransactionStatus.draft();
            
            // Then
            assertNotNull(status);
            assertEquals(TransactionStatus.Status.DRAFT, status.getStatus());
            assertTrue(status.isDraft());
        }

        @Test
        @DisplayName("Should create TransactionStatus with specific status")
        void shouldCreateTransactionStatusWithSpecificStatus() {
            // Given
            TransactionStatus.Status targetStatus = TransactionStatus.Status.APPROVED;
            
            // When
            TransactionStatus status = TransactionStatus.of(targetStatus);
            
            // Then
            assertNotNull(status);
            assertEquals(targetStatus, status.getStatus());
            assertEquals(TransactionStatus.Status.APPROVED, status.getStatus());
        }

        @ParameterizedTest
        @EnumSource(TransactionStatus.Status.class)
        @DisplayName("Should create TransactionStatus with all enum values")
        void shouldCreateTransactionStatusWithAllEnumValues(TransactionStatus.Status statusEnum) {
            // When
            TransactionStatus status = TransactionStatus.of(statusEnum);
            
            // Then
            assertNotNull(status);
            assertEquals(statusEnum, status.getStatus());
        }
    }

    @Nested
    @DisplayName("Status Enum Tests")
    class StatusEnumTests {

        @Test
        @DisplayName("Should have correct status codes")
        void shouldHaveCorrectStatusCodes() {
            // When & Then
            assertEquals(0, TransactionStatus.Status.DRAFT.getCode());
            assertEquals(1, TransactionStatus.Status.PENDING_APPROVAL.getCode());
            assertEquals(2, TransactionStatus.Status.APPROVED.getCode());
            assertEquals(3, TransactionStatus.Status.REJECTED.getCode());
            assertEquals(4, TransactionStatus.Status.CANCELLED.getCode());
            assertEquals(5, TransactionStatus.Status.VOIDED.getCode());
        }

        @Test
        @DisplayName("Should have correct display names")
        void shouldHaveCorrectDisplayNames() {
            // When & Then
            assertEquals("Draft", TransactionStatus.Status.DRAFT.getDisplayName());
            assertEquals("Pending Approval", TransactionStatus.Status.PENDING_APPROVAL.getDisplayName());
            assertEquals("Approved", TransactionStatus.Status.APPROVED.getDisplayName());
            assertEquals("Rejected", TransactionStatus.Status.REJECTED.getDisplayName());
            assertEquals("Cancelled", TransactionStatus.Status.CANCELLED.getDisplayName());
            assertEquals("Voided", TransactionStatus.Status.VOIDED.getDisplayName());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4, 5})
        @DisplayName("Should get status from valid codes")
        void shouldGetStatusFromValidCodes(int code) {
            // When
            TransactionStatus.Status status = TransactionStatus.Status.fromCode(code);
            
            // Then
            assertNotNull(status);
            assertEquals(code, status.getCode());
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 6, 10, 100})
        @DisplayName("Should throw exception for invalid status codes")
        void shouldThrowExceptionForInvalidStatusCodes(int invalidCode) {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TransactionStatus.Status.fromCode(invalidCode)
            );
            
            assertTrue(exception.getMessage().contains("Invalid transaction status code"));
            assertTrue(exception.getMessage().contains(String.valueOf(invalidCode)));
        }
    }

    @Nested
    @DisplayName("State Transition Tests")
    class StateTransitionTests {

        @Test
        @DisplayName("Should transition from DRAFT to PENDING_APPROVAL")
        void shouldTransitionFromDraftToPendingApproval() {
            // Given
            TransactionStatus draftStatus = TransactionStatus.draft();
            
            // When
            TransactionStatus pendingStatus = draftStatus.submitForApproval();
            
            // Then
            assertEquals(TransactionStatus.Status.PENDING_APPROVAL, pendingStatus.getStatus());
            assertEquals(TransactionStatus.Status.PENDING_APPROVAL, pendingStatus.getStatus());
        }

        @Test
        @DisplayName("Should transition from PENDING_APPROVAL to APPROVED")
        void shouldTransitionFromPendingApprovalToApproved() {
            // Given
            TransactionStatus pendingStatus = TransactionStatus.of(TransactionStatus.Status.PENDING_APPROVAL);
            
            // When
            TransactionStatus approvedStatus = pendingStatus.approve();
            
            // Then
            assertEquals(TransactionStatus.Status.APPROVED, approvedStatus.getStatus());
            assertEquals(TransactionStatus.Status.APPROVED, approvedStatus.getStatus());
        }

        @Test
        @DisplayName("Should transition from PENDING_APPROVAL to REJECTED")
        void shouldTransitionFromPendingApprovalToRejected() {
            // Given
            TransactionStatus pendingStatus = TransactionStatus.of(TransactionStatus.Status.PENDING_APPROVAL);
            
            // When
            TransactionStatus rejectedStatus = pendingStatus.reject();
            
            // Then
            assertEquals(TransactionStatus.Status.REJECTED, rejectedStatus.getStatus());
            assertEquals(TransactionStatus.Status.REJECTED, rejectedStatus.getStatus());
        }

        @Test
        @DisplayName("Should transition from DRAFT to CANCELLED")
        void shouldTransitionFromDraftToCancelled() {
            // Given
            TransactionStatus draftStatus = TransactionStatus.draft();
            
            // When
            TransactionStatus cancelledStatus = draftStatus.cancel();
            
            // Then
            assertEquals(TransactionStatus.Status.CANCELLED, cancelledStatus.getStatus());
            assertEquals(TransactionStatus.Status.CANCELLED, cancelledStatus.getStatus());
        }

        @Test
        @DisplayName("Should transition from APPROVED to VOIDED")
        void shouldTransitionFromApprovedToVoided() {
            // Given
            TransactionStatus approvedStatus = TransactionStatus.of(TransactionStatus.Status.APPROVED);
            
            // When
            TransactionStatus voidedStatus = approvedStatus.voidTransaction();
            
            // Then
            assertEquals(TransactionStatus.Status.VOIDED, voidedStatus.getStatus());
            assertEquals(TransactionStatus.Status.VOIDED, voidedStatus.getStatus());
        }

        @Test
        @DisplayName("Should throw exception for invalid transition from APPROVED to PENDING")
        void shouldThrowExceptionForInvalidTransitionFromApprovedToPending() {
            // Given
            TransactionStatus approvedStatus = TransactionStatus.of(TransactionStatus.Status.APPROVED);
            
            // When & Then
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> approvedStatus.submitForApproval()
            );
            
            assertTrue(exception.getMessage().contains("Only draft transactions can be submitted for approval"));
        }

        @Test
        @DisplayName("Should throw exception for invalid transition from REJECTED to APPROVED")
        void shouldThrowExceptionForInvalidTransitionFromRejectedToApproved() {
            // Given
            TransactionStatus rejectedStatus = TransactionStatus.of(TransactionStatus.Status.REJECTED);
            
            // When & Then
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> rejectedStatus.approve()
            );
            
            assertTrue(exception.getMessage().contains("Transaction cannot be approved in current state"));
        }
    }

    @Nested
    @DisplayName("State Query Tests")
    class StateQueryTests {

        @Test
        @DisplayName("Should identify modifiable states")
        void shouldIdentifyModifiableStates() {
            // Given
            TransactionStatus draft = TransactionStatus.draft();
            TransactionStatus pending = TransactionStatus.of(TransactionStatus.Status.PENDING_APPROVAL);
            TransactionStatus approved = TransactionStatus.of(TransactionStatus.Status.APPROVED);
            TransactionStatus rejected = TransactionStatus.of(TransactionStatus.Status.REJECTED);
            
            // When & Then
            assertTrue(draft.canBeModified());
            assertFalse(pending.canBeModified());
            assertFalse(approved.canBeModified());
            assertFalse(rejected.canBeModified());
        }

        @Test
        @DisplayName("Should identify approvable states")
        void shouldIdentifyApprovableStates() {
            // Given
            TransactionStatus draft = TransactionStatus.draft();
            TransactionStatus pending = TransactionStatus.of(TransactionStatus.Status.PENDING_APPROVAL);
            TransactionStatus approved = TransactionStatus.of(TransactionStatus.Status.APPROVED);
            
            // When & Then - According to actual implementation: DRAFT and PENDING_APPROVAL can be approved
            assertTrue(draft.canBeApproved());
            assertTrue(pending.canBeApproved());
            assertFalse(approved.canBeApproved());
        }

        @Test
        @DisplayName("Should identify completed states")
        void shouldIdentifyCompletedStates() {
            // Given
            TransactionStatus draft = TransactionStatus.draft();
            TransactionStatus pending = TransactionStatus.of(TransactionStatus.Status.PENDING_APPROVAL);
            TransactionStatus approved = TransactionStatus.of(TransactionStatus.Status.APPROVED);
            TransactionStatus rejected = TransactionStatus.of(TransactionStatus.Status.REJECTED);
            
            // When & Then
            assertFalse(draft.isCompleted());
            assertFalse(pending.isCompleted());
            assertTrue(approved.isCompleted());
            assertFalse(rejected.isCompleted());
        }

        @Test
        @DisplayName("Should identify final states")
        void shouldIdentifyFinalStates() {
            // Given
            TransactionStatus draft = TransactionStatus.draft();
            TransactionStatus pending = TransactionStatus.of(TransactionStatus.Status.PENDING_APPROVAL);
            TransactionStatus approved = TransactionStatus.of(TransactionStatus.Status.APPROVED);
            TransactionStatus rejected = TransactionStatus.of(TransactionStatus.Status.REJECTED);
            TransactionStatus cancelled = TransactionStatus.of(TransactionStatus.Status.CANCELLED);
            TransactionStatus voided = TransactionStatus.of(TransactionStatus.Status.VOIDED);
            
            // When & Then
            assertFalse(draft.isFinalState());
            assertFalse(pending.isFinalState());
            assertTrue(approved.isFinalState());
            assertTrue(rejected.isFinalState());
            assertTrue(cancelled.isFinalState());
            assertTrue(voided.isFinalState());
        }
    }

    @Nested
    @DisplayName("Equality and Hash Tests")
    class EqualityAndHashTests {

        @Test
        @DisplayName("Should be equal when statuses are same")
        void shouldBeEqualWhenStatusesAreSame() {
            // Given
            TransactionStatus status1 = TransactionStatus.of(TransactionStatus.Status.APPROVED);
            TransactionStatus status2 = TransactionStatus.of(TransactionStatus.Status.APPROVED);
            
            // When & Then
            assertEquals(status1, status2);
            assertEquals(status1.hashCode(), status2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when statuses are different")
        void shouldNotBeEqualWhenStatusesAreDifferent() {
            // Given
            TransactionStatus status1 = TransactionStatus.of(TransactionStatus.Status.DRAFT);
            TransactionStatus status2 = TransactionStatus.of(TransactionStatus.Status.APPROVED);
            
            // When & Then
            assertNotEquals(status1, status2);
            assertNotEquals(status1.hashCode(), status2.hashCode());
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Given
            TransactionStatus status = TransactionStatus.draft();
            
            // When & Then
            assertEquals(status, status);
            assertEquals(status.hashCode(), status.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Given
            TransactionStatus status = TransactionStatus.draft();
            
            // When & Then
            assertNotEquals(status, null);
        }

        @Test
        @DisplayName("Should not be equal to different class")
        void shouldNotBeEqualToDifferentClass() {
            // Given
            TransactionStatus status = TransactionStatus.draft();
            String differentObject = "DRAFT";
            
            // When & Then
            assertNotEquals(status, differentObject);
        }
    }

    @Nested
    @DisplayName("String Representation Tests")
    class StringRepresentationTests {

        @Test
        @DisplayName("Should have meaningful toString representation")
        void shouldHaveMeaningfulToStringRepresentation() {
            // Given
            TransactionStatus status = TransactionStatus.of(TransactionStatus.Status.PENDING_APPROVAL);
            
            // When
            String stringRepresentation = status.toString();
            
            // Then
            assertNotNull(stringRepresentation);
            assertTrue(stringRepresentation.contains("Pending Approval"));
            assertTrue(stringRepresentation.contains("1")); // Should contain status code
        }

        @Test
        @DisplayName("Should provide display name")
        void shouldProvideDisplayName() {
            // Given
            TransactionStatus status = TransactionStatus.of(TransactionStatus.Status.APPROVED);
            
            // When
            String displayName = status.getDisplayName();
            
            // Then
            assertEquals("Approved", displayName);
        }
    }

    @Nested
    @DisplayName("JPA Compatibility Tests")
    class JPACompatibilityTests {

        @Test
        @DisplayName("Should be embeddable value object")
        void shouldBeEmbeddableValueObject() {
            // Given
            TransactionStatus status = TransactionStatus.draft();
            
            // When & Then
            assertTrue(status.getClass().isAnnotationPresent(jakarta.persistence.Embeddable.class));
        }

        @Test
        @DisplayName("Should support ordinal enum mapping")
        void shouldSupportOrdinalEnumMapping() {
            // Given
            TransactionStatus.Status[] allStatuses = TransactionStatus.Status.values();
            
            // When & Then
            for (int i = 0; i < allStatuses.length; i++) {
                assertEquals(i, allStatuses[i].ordinal());
                assertEquals(i, allStatuses[i].getCode());
            }
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should maintain immutability during transitions")
        void shouldMaintainImmutabilityDuringTransitions() {
            // Given
            TransactionStatus originalStatus = TransactionStatus.draft();
            TransactionStatus.Status originalEnumValue = originalStatus.getStatus();
            
            // When
            TransactionStatus newStatus = originalStatus.submitForApproval();
            
            // Then
            assertEquals(TransactionStatus.Status.DRAFT, originalStatus.getStatus());
            assertEquals(originalEnumValue, originalStatus.getStatus());
            assertEquals(TransactionStatus.Status.PENDING_APPROVAL, newStatus.getStatus());
            assertNotSame(originalStatus, newStatus);
        }

        @Test
        @DisplayName("Should handle workflow progression correctly")
        void shouldHandleWorkflowProgressionCorrectly() {
            // Given
            TransactionStatus status = TransactionStatus.draft();
            
            // When - Progress through normal workflow
            TransactionStatus pending = status.submitForApproval();
            TransactionStatus approved = pending.approve();
            
            // Then
            assertTrue(status.isDraft());
            assertEquals(TransactionStatus.Status.PENDING_APPROVAL, pending.getStatus());
            assertEquals(TransactionStatus.Status.APPROVED, approved.getStatus());
            assertTrue(approved.isCompleted());
            assertTrue(approved.isFinalState());
        }

        @Test
        @DisplayName("Should handle rejection workflow correctly")
        void shouldHandleRejectionWorkflowCorrectly() {
            // Given
            TransactionStatus pending = TransactionStatus.of(TransactionStatus.Status.PENDING_APPROVAL);
            
            // When
            TransactionStatus rejected = pending.reject();
            
            // Then
            assertEquals(TransactionStatus.Status.REJECTED, rejected.getStatus());
            assertTrue(rejected.isFinalState());
            assertFalse(rejected.isCompleted());
            assertFalse(rejected.canBeModified());
        }

        @Test
        @DisplayName("Should validate state transitions comprehensively")
        void shouldValidateStateTransitionsComprehensively() {
            // Test all valid transitions
            assertDoesNotThrow(() -> {
                TransactionStatus.draft().submitForApproval();
                TransactionStatus.draft().cancel();
                TransactionStatus.of(TransactionStatus.Status.PENDING_APPROVAL).approve();
                TransactionStatus.of(TransactionStatus.Status.PENDING_APPROVAL).reject();
                TransactionStatus.of(TransactionStatus.Status.PENDING_APPROVAL).cancel();
                TransactionStatus.of(TransactionStatus.Status.APPROVED).voidTransaction();
            });
            
            // Test some invalid transitions
            assertThrows(IllegalStateException.class, 
                () -> TransactionStatus.of(TransactionStatus.Status.CANCELLED).approve());
            assertThrows(IllegalStateException.class, 
                () -> TransactionStatus.of(TransactionStatus.Status.VOIDED).submitForApproval());
        }
    }
}