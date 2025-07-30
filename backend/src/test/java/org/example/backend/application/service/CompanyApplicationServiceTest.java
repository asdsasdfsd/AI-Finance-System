// backend/src/test/java/org/example/backend/application/service/CompanyApplicationServiceTest.java
package org.example.backend.application.service;

import org.example.backend.application.dto.CreateCompanyCommand;
import org.example.backend.application.dto.UpdateCompanyCommand;
import org.example.backend.application.dto.CompanyDTO;
import org.example.backend.domain.aggregate.company.CompanyAggregate;
import org.example.backend.domain.aggregate.company.CompanyAggregateRepository;
import org.example.backend.domain.valueobject.CompanyStatus;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.event.DomainEventPublisher;
import org.example.backend.exception.ResourceNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CompanyApplicationService
 * 
 * Tests the application service layer coordination of company management use cases
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CompanyApplicationService Tests")
class CompanyApplicationServiceTest {
    
    @Mock
    private CompanyAggregateRepository companyRepository;
    
    @Mock
    private DomainEventPublisher eventPublisher;
    
    @InjectMocks
    private CompanyApplicationService companyApplicationService;
    
    private CreateCompanyCommand createCommand;
    private UpdateCompanyCommand updateCommand;
    private CompanyAggregate testCompany;
    
    @BeforeEach
    void setUp() {
        createCommand = CreateCompanyCommand.builder()
            .companyName("Test Company")
            .email("test@example.com")
            .address("Test Address")
            .registrationNumber("BL123456")
            .createdBy(1)
            .build();
            
        updateCommand = UpdateCompanyCommand.builder()
            .companyName("Updated Company")
            .email("updated@example.com")
            .address("Updated Address")
            .registrationNumber("BL654321")
            .build();
            
        testCompany = CompanyAggregate.create(
            "Test Company",
            "test@example.com",
            "Test Address",
            "BL123456",
            1
        );
    }
    
    @Nested
    @DisplayName("Create Company Tests")
    class CreateCompanyTests {
        
        @Test
        @DisplayName("Should create company successfully with valid command")
        void shouldCreateCompanySuccessfully() {
            // Given
            when(companyRepository.existsByCompanyName(createCommand.getCompanyName())).thenReturn(false);
            when(companyRepository.existsByEmail(createCommand.getEmail())).thenReturn(false);
            when(companyRepository.save(any(CompanyAggregate.class))).thenReturn(testCompany);
            
            // When
            CompanyDTO result = companyApplicationService.createCompany(createCommand);
            
            // Then
            assertNotNull(result);
            assertEquals(createCommand.getCompanyName(), result.getCompanyName());
            assertEquals(createCommand.getEmail(), result.getEmail());
            assertTrue(result.isActive());
            
            verify(companyRepository).existsByCompanyName(createCommand.getCompanyName());
            verify(companyRepository).existsByEmail(createCommand.getEmail());
            verify(companyRepository).save(any(CompanyAggregate.class));
            verify(eventPublisher).publishAll(any());
        }
        
        @Test
        @DisplayName("Should throw exception when company name already exists")
        void shouldThrowExceptionWhenCompanyNameExists() {
            // Given
            when(companyRepository.existsByCompanyName(createCommand.getCompanyName())).thenReturn(true);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> companyApplicationService.createCompany(createCommand)
            );
            
            assertTrue(exception.getMessage().contains("Company name already exists"));
            verify(companyRepository).existsByCompanyName(createCommand.getCompanyName());
            verify(companyRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            // Given
            when(companyRepository.existsByCompanyName(createCommand.getCompanyName())).thenReturn(false);
            when(companyRepository.existsByEmail(createCommand.getEmail())).thenReturn(true);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> companyApplicationService.createCompany(createCommand)
            );
            
            assertTrue(exception.getMessage().contains("Email already exists"));
            verify(companyRepository).existsByEmail(createCommand.getEmail());
            verify(companyRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Should throw exception when command is null")
        void shouldThrowExceptionWhenCommandIsNull() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> companyApplicationService.createCompany(null)
            );
            
            assertEquals("CreateCompanyCommand cannot be null", exception.getMessage());
            verify(companyRepository, never()).save(any());
        }
    }
    
    @Nested
    @DisplayName("Update Company Tests")
    class UpdateCompanyTests {
        
        @Test
        @DisplayName("Should update company successfully")
        void shouldUpdateCompanySuccessfully() {
            // Given
            when(companyRepository.findById(updateCommand.getCompanyId())).thenReturn(Optional.of(testCompany));
            when(companyRepository.save(any(CompanyAggregate.class))).thenReturn(testCompany);
            
            // When
            CompanyDTO result = companyApplicationService.updateCompany(1, updateCommand);
            
            // Then
            assertNotNull(result);
            verify(companyRepository).findById(1);
            verify(companyRepository).save(testCompany);
        }
        
        @Test
        @DisplayName("Should throw exception when company not found")
        void shouldThrowExceptionWhenCompanyNotFound() {
            // Given
            when(companyRepository.findById(1)).thenReturn(Optional.empty());
            
            // When & Then
            ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> companyApplicationService.updateCompany(1, updateCommand)
            );
            
            assertTrue(exception.getMessage().contains("Company not found"));
            verify(companyRepository).findById(1);
            verify(companyRepository, never()).save(any());
        }
    }
    
    @Nested
    @DisplayName("Company Status Management Tests")
    class CompanyStatusTests {
        
        @Test
        @DisplayName("Should activate company successfully")
        void shouldActivateCompanySuccessfully() {
            // Given
            testCompany.deactivate();
            when(companyRepository.findById(1)).thenReturn(Optional.of(testCompany));
            when(companyRepository.save(any(CompanyAggregate.class))).thenReturn(testCompany);
            
            // When
            CompanyDTO result = companyApplicationService.activateCompany(1);
            
            // Then
            assertNotNull(result);
            verify(companyRepository).findById(1);
            verify(companyRepository).save(testCompany);
            verify(eventPublisher).publishAll(any());
        }
        
        @Test
        @DisplayName("Should deactivate company successfully")
        void shouldDeactivateCompanySuccessfully() {
            // Given
            when(companyRepository.findById(1)).thenReturn(Optional.of(testCompany));
            when(companyRepository.save(any(CompanyAggregate.class))).thenReturn(testCompany);
            
            // When
            CompanyDTO result = companyApplicationService.deactivateCompany(1, "Business closure");
            
            // Then
            assertNotNull(result);
            verify(companyRepository).findById(1);
            verify(companyRepository).save(testCompany);
            verify(eventPublisher).publishAll(any());
        }
    }
    
    @Nested
    @DisplayName("Query Tests")
    class QueryTests {
        
        @Test
        @DisplayName("Should get company by ID successfully")
        void shouldGetCompanyByIdSuccessfully() {
            // Given
            when(companyRepository.findById(1)).thenReturn(Optional.of(testCompany));
            
            // When
            CompanyDTO result = companyApplicationService.getCompanyById(1);
            
            // Then
            assertNotNull(result);
            assertEquals(testCompany.getCompanyName(), result.getCompanyName());
            verify(companyRepository).findById(1);
        }
        
        @Test
        @DisplayName("Should get all companies successfully")
        void shouldGetAllCompaniesSuccessfully() {
            // Given
            List<CompanyAggregate> companies = List.of(testCompany);
            when(companyRepository.findAll()).thenReturn(companies);
            
            // When
            List<CompanyDTO> result = companyApplicationService.getAllCompanies();
            
            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(companyRepository).findAll();
        }
    }
}