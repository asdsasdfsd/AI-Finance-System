// backend/src/test/java/org/example/backend/domain/aggregate/company/CompanyAggregateTest.java - 修复版本
package org.example.backend.domain.aggregate.company;

import org.example.backend.domain.aggregate.AggregateTestBase;
import org.example.backend.domain.event.CompanyCreatedEvent;
import org.example.backend.domain.valueobject.CompanyStatus;
import org.example.backend.domain.valueobject.TenantId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CompanyAggregate - 修复版本
 */
public class CompanyAggregateTest extends AggregateTestBase {
    
    @Test
    @DisplayName("Should create company successfully with valid data")
    void shouldCreateCompanySuccessfully() {
        // Given
        String companyName = "Test Company Ltd";
        String email = "test@company.com";
        String address = "123 Business Street";
        String city = "Shanghai";
        String stateProvince = "Shanghai";
        String postalCode = "200000";
        Integer createdBy = TEST_USER_ID;
        
        // When
        CompanyAggregate company = CompanyAggregate.create(
            companyName, email, address, city, stateProvince, postalCode, createdBy
        );
        
        // Then
        assertNotNull(company);
        assertEquals(companyName, company.getCompanyName());
        assertEquals(email, company.getEmail());
        assertEquals(address, company.getAddress());
        assertEquals(city, company.getCity());
        assertEquals(stateProvince, company.getStateProvince());
        assertEquals(postalCode, company.getPostalCode());
        assertEquals(createdBy, company.getCreatedBy());
        assertEquals("CNY", company.getDefaultCurrency());
        assertEquals("01-01", company.getFiscalYearStart());
        assertEquals(100, company.getMaxUsers());
        
        // Verify status is active
        assertNotNull(company.getCompanyStatus());
        assertEquals(CompanyStatus.Status.ACTIVE, company.getCompanyStatus().getStatus());
        
        // Verify audit fields
        assertValidCreationTime(company.getCreatedAt());
        assertValidCreationTime(company.getUpdatedAt());
        
        // Verify domain event
        assertEventPublished(company.getDomainEvents(), CompanyCreatedEvent.class);
    }
    
    @Test
    @DisplayName("Should update basic info successfully")
    void shouldUpdateBasicInfoSuccessfully() {
        // Given
        CompanyAggregate company = createTestCompany();
        LocalDateTime originalUpdatedAt = company.getUpdatedAt();
        
        String newCompanyName = "Updated Company Name";
        String newAddress = "456 New Address";
        String newCity = "Beijing";
        String newWebsite = "https://updated-company.com";
        
        // Wait a moment to ensure different timestamp
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        // When
        company.updateBasicInfo(newCompanyName, newAddress, newCity, "Beijing", "100000", newWebsite);
        
        // Then
        assertEquals(newCompanyName, company.getCompanyName());
        assertEquals(newAddress, company.getAddress());
        assertEquals(newCity, company.getCity());
        assertEquals("Beijing", company.getStateProvince());
        assertEquals("100000", company.getPostalCode());
        assertEquals(newWebsite, company.getWebsite());
        assertTrue(company.getUpdatedAt().isAfter(originalUpdatedAt));
    }
    
    @Test
    @DisplayName("Should update financial settings successfully")
    void shouldUpdateFinancialSettingsSuccessfully() {
        // Given
        CompanyAggregate company = createTestCompany();
        
        // When
        company.updateFinancialSettings("04-01", "USD");
        
        // Then
        assertEquals("USD", company.getDefaultCurrency());
        assertEquals("04-01", company.getFiscalYearStart());
    }
    
    @Test
    @DisplayName("Should activate company successfully")
    void shouldActivateCompanySuccessfully() {
        // Given
        CompanyAggregate company = createTestCompany();
        company.deactivate(); // First deactivate
        
        // When
        company.activate();
        
        // Then
        assertEquals(CompanyStatus.Status.ACTIVE, company.getCompanyStatus().getStatus());
    }
    
    @Test
    @DisplayName("Should deactivate company successfully")
    void shouldDeactivateCompanySuccessfully() {
        // Given
        CompanyAggregate company = createTestCompany();
        
        // When
        company.deactivate();
        
        // Then
        assertEquals(CompanyStatus.Status.INACTIVE, company.getCompanyStatus().getStatus());
    }
    
    @Test
    @DisplayName("Should update user limit successfully")
    void shouldUpdateUserLimitSuccessfully() {
        // Given
        CompanyAggregate company = createTestCompany();
        
        // When
        company.updateUserLimit(200);
        
        // Then
        assertEquals(200, company.getMaxUsers());
    }
    
    // Helper method to create test company
    private CompanyAggregate createTestCompany() {
        return CompanyAggregate.create(
            "Test Company Ltd",
            "test@company.com", 
            "123 Business Street",
            "Shanghai",
            "Shanghai", 
            "200000",
            TEST_USER_ID
        );
    }
}

