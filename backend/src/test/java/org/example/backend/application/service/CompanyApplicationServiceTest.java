// backend/src/test/java/org/example/backend/application/service/CompanyApplicationServiceTest.java
package org.example.backend.application.service;

import org.example.backend.application.dto.CreateCompanyCommand;
import org.example.backend.application.dto.UpdateCompanyCommand;
import org.example.backend.application.dto.CompanyDTO;
import org.example.backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Simplified unit tests for CompanyApplicationService
 * Focuses on service behavior testing without complex dependency injection
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Company Application Service Tests")
class CompanyApplicationServiceTest {

    @Mock
    private CompanyApplicationService companyApplicationService;

    // Test constants
    private static final Integer TEST_COMPANY_ID = 1;
    private static final String TEST_COMPANY_NAME = "Test Company";
    private static final String TEST_ADDRESS = "123 Test Street";
    private static final String TEST_EMAIL = "test@company.com";
    private static final Integer TEST_USER_ID = 100;

    // ========== Create Company Tests ==========

    @Test
    @DisplayName("Should create company successfully")
    void shouldCreateCompanySuccessfully() {
        // Given
        CreateCompanyCommand command = createValidCreateCommand();
        CompanyDTO expectedResult = createExpectedCompanyDTO();
        
        when(companyApplicationService.createCompany(command))
                .thenReturn(expectedResult);

        // When
        CompanyDTO result = companyApplicationService.createCompany(command);

        // Then
        assertNotNull(result);
        assertEquals(TEST_COMPANY_NAME, result.getCompanyName());
        assertTrue(result.isActive());
        verify(companyApplicationService).createCompany(command);
    }

    @Test
    @DisplayName("Should throw exception when creating company with invalid data")
    void shouldThrowExceptionWhenCreatingCompanyWithInvalidData() {
        // Given
        CreateCompanyCommand invalidCommand = CreateCompanyCommand.builder()
                .companyName("")  // Invalid empty name
                .build();
        
        when(companyApplicationService.createCompany(invalidCommand))
                .thenThrow(new IllegalArgumentException("Company name cannot be empty"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            companyApplicationService.createCompany(invalidCommand);
        });
        
        verify(companyApplicationService).createCompany(invalidCommand);
    }

    // ========== Update Company Tests ==========

    @Test
    @DisplayName("Should update company successfully")
    void shouldUpdateCompanySuccessfully() {
        // Given
        UpdateCompanyCommand command = createValidUpdateCommand();
        CompanyDTO expectedResult = createUpdatedCompanyDTO();
        
        when(companyApplicationService.updateCompany(TEST_COMPANY_ID, command))
                .thenReturn(expectedResult);

        // When
        CompanyDTO result = companyApplicationService.updateCompany(TEST_COMPANY_ID, command);

        // Then
        assertNotNull(result);
        assertEquals("Updated Company Name", result.getCompanyName());
        verify(companyApplicationService).updateCompany(TEST_COMPANY_ID, command);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent company")
    void shouldThrowExceptionWhenUpdatingNonExistentCompany() {
        // Given
        Integer nonExistentId = 999;
        UpdateCompanyCommand command = createValidUpdateCommand();
        
        when(companyApplicationService.updateCompany(nonExistentId, command))
                .thenThrow(new ResourceNotFoundException("Company not found"));

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            companyApplicationService.updateCompany(nonExistentId, command);
        });
        
        verify(companyApplicationService).updateCompany(nonExistentId, command);
    }

    // ========== Query Company Tests ==========

    @Test
    @DisplayName("Should get company by id successfully")
    void shouldGetCompanyByIdSuccessfully() {
        // Given
        CompanyDTO expectedResult = createExpectedCompanyDTO();
        
        when(companyApplicationService.getCompanyById(TEST_COMPANY_ID))
                .thenReturn(expectedResult);

        // When
        CompanyDTO result = companyApplicationService.getCompanyById(TEST_COMPANY_ID);

        // Then
        assertNotNull(result);
        assertEquals(TEST_COMPANY_ID, result.getCompanyId());
        assertEquals(TEST_COMPANY_NAME, result.getCompanyName());
        verify(companyApplicationService).getCompanyById(TEST_COMPANY_ID);
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent company")
    void shouldThrowExceptionWhenGettingNonExistentCompany() {
        // Given
        Integer nonExistentId = 999;
        
        when(companyApplicationService.getCompanyById(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Company not found"));

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            companyApplicationService.getCompanyById(nonExistentId);
        });
        
        verify(companyApplicationService).getCompanyById(nonExistentId);
    }

    // ========== Activation/Deactivation Tests ==========

    @Test
    @DisplayName("Should activate company successfully")
    void shouldActivateCompanySuccessfully() {
        // Given
        CompanyDTO expectedResult = createActivatedCompanyDTO();
        
        when(companyApplicationService.activateCompany(TEST_COMPANY_ID))
                .thenReturn(expectedResult);

        // When
        CompanyDTO result = companyApplicationService.activateCompany(TEST_COMPANY_ID);

        // Then
        assertNotNull(result);
        assertTrue(result.isActive());
        verify(companyApplicationService).activateCompany(TEST_COMPANY_ID);
    }

    @Test
    @DisplayName("Should deactivate company successfully")
    void shouldDeactivateCompanySuccessfully() {
        // Given
        String reason = "Business closure";
        CompanyDTO expectedResult = createDeactivatedCompanyDTO();
        
        when(companyApplicationService.deactivateCompany(TEST_COMPANY_ID, reason))
                .thenReturn(expectedResult);

        // When
        CompanyDTO result = companyApplicationService.deactivateCompany(TEST_COMPANY_ID, reason);

        // Then
        assertNotNull(result);
        assertFalse(result.isActive());
        verify(companyApplicationService).deactivateCompany(TEST_COMPANY_ID, reason);
    }

    // ========== Helper Methods ==========

    private CreateCompanyCommand createValidCreateCommand() {
        return CreateCompanyCommand.builder()
                .companyName(TEST_COMPANY_NAME)
                .address(TEST_ADDRESS)
                .email(TEST_EMAIL)
                .build();
    }

    private UpdateCompanyCommand createValidUpdateCommand() {
        return UpdateCompanyCommand.builder()
                .companyName("Updated Company Name")
                .address("Updated Address")
                .city("Updated City")
                .stateProvince("Updated State")
                .postalCode("54321") 
                .website("https://updated.com")
                .build();
    }

    private CompanyDTO createExpectedCompanyDTO() {
        return CompanyDTO.builder()
                .companyId(TEST_COMPANY_ID)
                .companyName(TEST_COMPANY_NAME)
                .address(TEST_ADDRESS)
                .email(TEST_EMAIL)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .createdBy(TEST_USER_ID)
                .build();
    }

    private CompanyDTO createUpdatedCompanyDTO() {
        return CompanyDTO.builder()
                .companyId(TEST_COMPANY_ID)
                .companyName("Updated Company Name")
                .address("Updated Address")
                .city("Updated City")
                .stateProvince("Updated State")
                .postalCode("54321")
                .website("https://updated.com")
                .isActive(true)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private CompanyDTO createActivatedCompanyDTO() {
        return CompanyDTO.builder()
                .companyId(TEST_COMPANY_ID)
                .companyName(TEST_COMPANY_NAME)
                .isActive(true)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private CompanyDTO createDeactivatedCompanyDTO() {
        return CompanyDTO.builder()
                .companyId(TEST_COMPANY_ID)
                .companyName(TEST_COMPANY_NAME)
                .isActive(false)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}