// backend/src/test/java/org/example/backend/application/service/CompanyApplicationServiceTest.java
package org.example.backend.application.service;

import org.example.backend.application.dto.CreateCompanyCommand;
import org.example.backend.application.dto.UpdateCompanyCommand;
import org.example.backend.application.dto.CompanyDTO;
import org.example.backend.domain.aggregate.company.CompanyAggregate;
import org.example.backend.domain.aggregate.company.CompanyAggregateRepository;
import org.example.backend.domain.event.DomainEventPublisher;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CompanyApplicationService - Fixed Version
 */
@ExtendWith(MockitoExtension.class)
class CompanyApplicationServiceTest {

    @Mock
    private CompanyAggregateRepository companyRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private CompanyApplicationService companyApplicationService;

    // Test constants
    private static final Integer TEST_COMPANY_ID = 1;
    private static final Integer TEST_USER_ID = 100;
    private static final String TEST_COMPANY_NAME = "Test Company";
    private static final String TEST_EMAIL = "test@company.com";
    private static final String TEST_ADDRESS = "123 Test St";
    private static final String TEST_CITY = "Test City";
    private static final String TEST_STATE = "Test State";
    private static final String TEST_POSTAL_CODE = "12345";

    private CompanyAggregate testCompany;

    @BeforeEach
    void setUp() {
        testCompany = createMockCompany();
    }

    // ========== Create Company Tests ==========

    @Test
    @DisplayName("Should create company successfully")
    void shouldCreateCompanySuccessfully() {
        // Given
        CreateCompanyCommand command = createValidCreateCommand();
        when(companyRepository.existsByCompanyName(TEST_COMPANY_NAME)).thenReturn(false);
        when(companyRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(companyRepository.save(any(CompanyAggregate.class))).thenReturn(testCompany);

        // When
        CompanyDTO result = companyApplicationService.createCompany(command);

        // Then
        assertNotNull(result);
        assertEquals(testCompany.getCompanyId(), result.getCompanyId());
        assertEquals(testCompany.getCompanyName(), result.getCompanyName());
        
        verify(companyRepository).existsByCompanyName(TEST_COMPANY_NAME);
        verify(companyRepository).existsByEmail(TEST_EMAIL);
        verify(companyRepository).save(any(CompanyAggregate.class));
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    @DisplayName("Should throw exception when company name already exists")
    void shouldThrowExceptionWhenCompanyNameAlreadyExists() {
        // Given
        CreateCompanyCommand command = createValidCreateCommand();
        when(companyRepository.existsByCompanyName(TEST_COMPANY_NAME)).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            companyApplicationService.createCompany(command);
        });
    }

    @Test
    @DisplayName("Should throw exception when company email already exists")
    void shouldThrowExceptionWhenCompanyEmailAlreadyExists() {
        // Given
        CreateCompanyCommand command = createValidCreateCommand();
        when(companyRepository.existsByCompanyName(TEST_COMPANY_NAME)).thenReturn(false);
        when(companyRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            companyApplicationService.createCompany(command);
        });
    }

    // ========== Update Company Tests ==========

    @Test
    @DisplayName("Should update company successfully")
    void shouldUpdateCompanySuccessfully() {
        // Given
        UpdateCompanyCommand command = createValidUpdateCommand();
        when(companyRepository.findById(TEST_COMPANY_ID)).thenReturn(Optional.of(testCompany));
        when(companyRepository.save(any(CompanyAggregate.class))).thenReturn(testCompany);

        // When
        CompanyDTO result = companyApplicationService.updateCompany(TEST_COMPANY_ID, command);

        // Then
        assertNotNull(result);
        verify(companyRepository).findById(TEST_COMPANY_ID);
        verify(companyRepository).save(testCompany);
    }

    @Test
    @DisplayName("Should throw exception when company not found for update")
    void shouldThrowExceptionWhenCompanyNotFoundForUpdate() {
        // Given
        UpdateCompanyCommand command = createValidUpdateCommand();
        when(companyRepository.findById(TEST_COMPANY_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            companyApplicationService.updateCompany(TEST_COMPANY_ID, command);
        });
    }

    // ========== Helper Methods ==========

    private CreateCompanyCommand createValidCreateCommand() {
        return CreateCompanyCommand.builder()
                .companyName(TEST_COMPANY_NAME)
                .email(TEST_EMAIL)
                .address(TEST_ADDRESS)
                .city(TEST_CITY)
                .stateProvince(TEST_STATE)
                .postalCode(TEST_POSTAL_CODE)
                .createdBy(TEST_USER_ID)
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

    private CompanyAggregate createMockCompany() {
        CompanyAggregate company = mock(CompanyAggregate.class);
        
        when(company.getCompanyId()).thenReturn(TEST_COMPANY_ID);
        when(company.getCompanyName()).thenReturn(TEST_COMPANY_NAME);
        when(company.getEmail()).thenReturn(TEST_EMAIL);
        when(company.getAddress()).thenReturn(TEST_ADDRESS);
        when(company.getCity()).thenReturn(TEST_CITY);
        when(company.getStateProvince()).thenReturn(TEST_STATE);
        when(company.getPostalCode()).thenReturn(TEST_POSTAL_CODE);
        when(company.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(company.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(company.getDomainEvents()).thenReturn(new ArrayList<>());
        
        // Mock update methods
        doNothing().when(company).updateBasicInfo(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
        doNothing().when(company).updateRegistrationInfo(anyString(), anyString());
        doNothing().when(company).updateFinancialSettings(anyString(), anyString());
        doNothing().when(company).clearDomainEvents();
        
        return company;
    }
}