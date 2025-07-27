// backend/src/test/java/org/example/backend/domain/event/CompanyEventTest.java
package org.example.backend.domain.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for company-related domain events
 * 
 * Tests the creation and properties of:
 * 1. CompanyCreatedEvent
 * 2. CompanyActivatedEvent (future)
 * 3. CompanyDeactivatedEvent (future)
 */
public class CompanyEventTest extends DomainEventTestBase {
    
    @Test
    @DisplayName("Should create CompanyCreatedEvent with valid properties")
    void shouldCreateCompanyCreatedEventWithValidProperties() {
        // Given
        Integer companyId = TEST_COMPANY_ID;
        String companyName = "Test Company Ltd";
        String email = "test@company.com";
        Integer createdBy = TEST_USER_ID;
        
        // When
        CompanyCreatedEvent event = new CompanyCreatedEvent(
            companyId, companyName, email, createdBy
        );
        
        // Then
        assertDomainEventBasics(event);
        assertEquals("CompanyCreatedEvent", event.getEventType());
        assertEquals(companyId, event.getCompanyId());
        assertEquals(companyName, event.getCompanyName());
        assertEquals(email, event.getEmail());
        assertEquals(createdBy, event.getCreatedBy());
        
        // Validate individual components
        assertValidCompanyId(event.getCompanyId());
        assertValidUserId(event.getCreatedBy());
        assertValidCompanyName(event.getCompanyName());
        assertValidEmail(event.getEmail());
    }
    
    @Test
    @DisplayName("Should create CompanyCreatedEvent with Chinese company name")
    void shouldCreateCompanyCreatedEventWithChineseCompanyName() {
        // Given
        Integer companyId = TEST_COMPANY_ID;
        String companyName = "测试科技有限公司";
        String email = "test@company.cn";
        Integer createdBy = TEST_USER_ID;
        
        // When
        CompanyCreatedEvent event = new CompanyCreatedEvent(
            companyId, companyName, email, createdBy
        );
        
        // Then
        assertEquals(companyName, event.getCompanyName());
        assertEquals(email, event.getEmail());
        assertValidCompanyName(event.getCompanyName());
    }
    
    @Test
    @DisplayName("Should create CompanyCreatedEvent with long company name")
    void shouldCreateCompanyCreatedEventWithLongCompanyName() {
        // Given
        Integer companyId = TEST_COMPANY_ID;
        String companyName = "Very Long Company Name International Technology Solutions Limited Corporation";
        String email = "contact@longcompanyname.com";
        Integer createdBy = TEST_USER_ID;
        
        // When
        CompanyCreatedEvent event = new CompanyCreatedEvent(
            companyId, companyName, email, createdBy
        );
        
        // Then
        assertEquals(companyName, event.getCompanyName());
        assertValidCompanyName(event.getCompanyName());
    }
    
    @Test
    @DisplayName("Should have proper toString representation")
    void shouldHaveProperToStringRepresentation() {
        // Given
        Integer companyId = TEST_COMPANY_ID;
        String companyName = "Test Company Ltd";
        String email = "test@company.com";
        Integer createdBy = TEST_USER_ID;
        
        // When
        CompanyCreatedEvent event = new CompanyCreatedEvent(
            companyId, companyName, email, createdBy
        );
        String toString = event.toString();
        
        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("CompanyCreatedEvent"));
        assertTrue(toString.contains(companyId.toString()));
        assertTrue(toString.contains(companyName));
        assertTrue(toString.contains(email));
        assertTrue(toString.contains(createdBy.toString()));
    }
    
    @Test
    @DisplayName("Should create events with different company IDs")
    void shouldCreateEventsWithDifferentCompanyIds() {
        // Given
        Integer companyId1 = 1;
        Integer companyId2 = 2;
        String companyName1 = "Company One";
        String companyName2 = "Company Two";
        String email1 = "one@company.com";
        String email2 = "two@company.com";
        Integer createdBy = TEST_USER_ID;
        
        // When
        CompanyCreatedEvent event1 = new CompanyCreatedEvent(
            companyId1, companyName1, email1, createdBy
        );
        CompanyCreatedEvent event2 = new CompanyCreatedEvent(
            companyId2, companyName2, email2, createdBy
        );
        
        // Then
        assertNotEquals(event1.getCompanyId(), event2.getCompanyId());
        assertNotEquals(event1.getCompanyName(), event2.getCompanyName());
        assertNotEquals(event1.getEmail(), event2.getEmail());
        assertNotEquals(event1.getEventId(), event2.getEventId());
    }
    
    // Custom validation methods for company events
    private void assertValidCompanyName(String companyName) {
        assertNotNull(companyName, "Company name should not be null");
        assertFalse(companyName.trim().isEmpty(), "Company name should not be empty");
        assertTrue(companyName.length() <= 200, "Company name should not exceed 200 characters");
    }
    
    private void assertValidEmail(String email) {
        assertNotNull(email, "Email should not be null");
        assertFalse(email.trim().isEmpty(), "Email should not be empty");
        assertTrue(email.contains("@"), "Email should contain @ symbol");
        assertTrue(email.contains("."), "Email should contain domain extension");
        assertFalse(email.startsWith("@"), "Email should not start with @");
        assertFalse(email.endsWith("@"), "Email should not end with @");
    }
}