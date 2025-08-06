// backend/src/test/java/org/example/backend/controller/CompanyControllerTest.java
package org.example.backend.controller;

import org.example.backend.application.service.CompanyApplicationService;
import org.example.backend.application.dto.CreateCompanyCommand;
import org.example.backend.application.dto.UpdateCompanyCommand;
import org.example.backend.application.dto.CompanyDTO;
import org.example.backend.application.dto.CompanyStatsDTO;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.domain.valueobject.CompanyStatus;

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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CompanyController - Testing Real Controller with Mocked Dependencies
 * 
 * This test class creates a REAL instance of CompanyController and mocks its dependencies,
 * following the proper unit testing approach for testing HTTP layer business logic.
 * 
 * Coverage Target: From 0% to 80%+ for all Controller methods
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Company Controller Tests - Real Controller Implementation")
class CompanyControllerTest {

    // Mock dependencies (not the controller itself!)
    @Mock
    private CompanyApplicationService companyApplicationService;

    // Real controller instance under test
    private CompanyController companyController;

    // Test constants
    private static final Integer TEST_COMPANY_ID = 1;
    private static final String TEST_COMPANY_NAME = "Test Company Inc.";
    private static final String TEST_EMAIL = "test@company.com";
    private static final String TEST_ADDRESS = "123 Test Street";
    private static final String TEST_CITY = "Test City";
    private static final String TEST_STATE = "Test State";
    private static final String TEST_POSTAL_CODE = "12345";
    private static final String TEST_WEBSITE = "https://test.com";
    private static final String TEST_REGISTRATION_NUMBER = "REG123456";
    private static final String TEST_TAX_ID = "TAX789";
    private static final Integer TEST_USER_ID = 100;
    private static final Integer TEST_MAX_USERS = 50;
    private static final String TEST_CURRENCY = "USD";
    private static final String TEST_FISCAL_YEAR_START = "01-01";

    @BeforeEach
    void setUp() {
        // Create real controller instance with mocked dependencies
        companyController = new CompanyController(companyApplicationService);
    }

    // ========== Create Company Tests ==========

    @Nested
    @DisplayName("Create Company Operations")
    class CreateCompanyOperations {

        @Test
        @DisplayName("Should create company successfully via POST")
        void shouldCreateCompanySuccessfully() {
            // Given
            Map<String, Object> request = createValidCompanyRequest();
            CompanyDTO expectedCompany = createMockCompanyDTO();
            
            when(companyApplicationService.createCompany(any(CreateCompanyCommand.class)))
                .thenReturn(expectedCompany);
            
            // When
            ResponseEntity<CompanyDTO> response = companyController.createCompany(request);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertEquals(TEST_COMPANY_NAME, response.getBody().getCompanyName());
            assertEquals(TEST_EMAIL, response.getBody().getEmail());
            
            // Verify service interaction
            verify(companyApplicationService).createCompany(any(CreateCompanyCommand.class));
        }

        @Test
        @DisplayName("Should map request to CreateCompanyCommand correctly")
        void shouldMapRequestToCreateCompanyCommandCorrectly() {
            // Given
            Map<String, Object> request = createValidCompanyRequest();
            CompanyDTO mockCompany = createMockCompanyDTO();
            
            ArgumentCaptor<CreateCompanyCommand> commandCaptor = 
                ArgumentCaptor.forClass(CreateCompanyCommand.class);
            
            when(companyApplicationService.createCompany(commandCaptor.capture()))
                .thenReturn(mockCompany);
            
            // When
            companyController.createCompany(request);
            
            // Then - verify mapping is correct
            CreateCompanyCommand capturedCommand = commandCaptor.getValue();
            assertEquals(TEST_COMPANY_NAME, capturedCommand.getCompanyName());
            assertEquals(TEST_EMAIL, capturedCommand.getEmail());
            assertEquals(TEST_ADDRESS, capturedCommand.getAddress());
            assertEquals(TEST_CITY, capturedCommand.getCity());
            assertEquals(TEST_STATE, capturedCommand.getStateProvince());
            assertEquals(TEST_POSTAL_CODE, capturedCommand.getPostalCode());
            assertEquals(TEST_WEBSITE, capturedCommand.getWebsite());
            assertEquals(TEST_REGISTRATION_NUMBER, capturedCommand.getRegistrationNumber());
            assertEquals(TEST_USER_ID, capturedCommand.getCreatedBy());
        }

        @Test
        @DisplayName("Should handle service exception when creating company")
        void shouldHandleServiceExceptionWhenCreatingCompany() {
            // Given
            Map<String, Object> request = createValidCompanyRequest();
            
            when(companyApplicationService.createCompany(any(CreateCompanyCommand.class)))
                .thenThrow(new IllegalArgumentException("Company name already exists"));
            
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                companyController.createCompany(request);
            });
            
            verify(companyApplicationService).createCompany(any(CreateCompanyCommand.class));
        }

        @Test
        @DisplayName("Should create company with admin successfully")
        void shouldCreateCompanyWithAdminSuccessfully() {
            // Given
            Map<String, Object> request = createValidCompanyRequest();
            CompanyDTO expectedCompany = createMockCompanyDTO();
            
            when(companyApplicationService.createCompany(any(CreateCompanyCommand.class)))
                .thenReturn(expectedCompany);
            
            // When
            ResponseEntity<Map<String, Object>> response = companyController.createCompanyWithAdmin(request);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().containsKey("company"));
            assertTrue(response.getBody().containsKey("message"));
            assertEquals("公司创建成功", response.getBody().get("message"));
            
            verify(companyApplicationService).createCompany(any(CreateCompanyCommand.class));
        }

        @Test
        @DisplayName("Should handle exception when creating company with admin")
        void shouldHandleExceptionWhenCreatingCompanyWithAdmin() {
            // Given
            Map<String, Object> request = createValidCompanyRequest();
            
            when(companyApplicationService.createCompany(any(CreateCompanyCommand.class)))
                .thenThrow(new IllegalArgumentException("Company creation failed"));
            
            // When
            ResponseEntity<Map<String, Object>> response = companyController.createCompanyWithAdmin(request);
            
            // Then
            assertNotNull(response);
            assertEquals(400, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertEquals("error", response.getBody().get("status"));
            assertTrue(response.getBody().get("message").toString().contains("创建公司失败"));
            
            verify(companyApplicationService).createCompany(any(CreateCompanyCommand.class));
        }
    }

    // ========== Update Company Tests ==========

    @Nested
    @DisplayName("Update Company Operations")
    class UpdateCompanyOperations {

        @Test
        @DisplayName("Should update company successfully via PUT")
        void shouldUpdateCompanySuccessfully() {
            // Given
            Map<String, Object> request = createValidUpdateRequest();
            CompanyDTO expectedCompany = createMockCompanyDTO();
            
            when(companyApplicationService.updateCompany(eq(TEST_COMPANY_ID), any(UpdateCompanyCommand.class)))
                .thenReturn(expectedCompany);
            
            // When
            ResponseEntity<CompanyDTO> response = companyController.updateCompany(TEST_COMPANY_ID, request);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            
            verify(companyApplicationService).updateCompany(eq(TEST_COMPANY_ID), any(UpdateCompanyCommand.class));
        }

        @Test
        @DisplayName("Should handle IllegalArgumentException when updating company")
        void shouldHandleIllegalArgumentExceptionWhenUpdatingCompany() {
            // Given
            Map<String, Object> request = createValidUpdateRequest();
            
            when(companyApplicationService.updateCompany(eq(TEST_COMPANY_ID), any(UpdateCompanyCommand.class)))
                .thenThrow(new IllegalArgumentException("Invalid update data"));
            
            // When
            ResponseEntity<CompanyDTO> response = companyController.updateCompany(TEST_COMPANY_ID, request);
            
            // Then
            assertNotNull(response);
            assertEquals(400, response.getStatusCode().value());
            
            verify(companyApplicationService).updateCompany(eq(TEST_COMPANY_ID), any(UpdateCompanyCommand.class));
        }

        @Test
        @DisplayName("Should handle general exception when updating company")
        void shouldHandleGeneralExceptionWhenUpdatingCompany() {
            // Given
            Map<String, Object> request = createValidUpdateRequest();
            
            when(companyApplicationService.updateCompany(eq(TEST_COMPANY_ID), any(UpdateCompanyCommand.class)))
                .thenThrow(new RuntimeException("Server error"));
            
            // When
            ResponseEntity<CompanyDTO> response = companyController.updateCompany(TEST_COMPANY_ID, request);
            
            // Then
            assertNotNull(response);
            assertEquals(500, response.getStatusCode().value());
            
            verify(companyApplicationService).updateCompany(eq(TEST_COMPANY_ID), any(UpdateCompanyCommand.class));
        }

        @Test
        @DisplayName("Should activate company successfully")
        void shouldActivateCompanySuccessfully() {
            // Given
            CompanyDTO expectedCompany = createMockCompanyDTO();
            
            when(companyApplicationService.activateCompany(TEST_COMPANY_ID))
                .thenReturn(expectedCompany);
            
            // When
            ResponseEntity<CompanyDTO> response = companyController.activateCompany(TEST_COMPANY_ID);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            
            verify(companyApplicationService).activateCompany(TEST_COMPANY_ID);
        }

        @Test
        @DisplayName("Should handle exception when activating company")
        void shouldHandleExceptionWhenActivatingCompany() {
            // Given
            when(companyApplicationService.activateCompany(TEST_COMPANY_ID))
                .thenThrow(new ResourceNotFoundException("Company not found"));
            
            // When
            ResponseEntity<CompanyDTO> response = companyController.activateCompany(TEST_COMPANY_ID);
            
            // Then
            assertNotNull(response);
            assertEquals(400, response.getStatusCode().value());
            
            verify(companyApplicationService).activateCompany(TEST_COMPANY_ID);
        }

        @Test
        @DisplayName("Should update subscription successfully")
        void shouldUpdateSubscriptionSuccessfully() {
            // Given
            Map<String, Object> request = createValidSubscriptionRequest();
            CompanyDTO expectedCompany = createMockCompanyDTO();
            
            when(companyApplicationService.updateSubscription(eq(TEST_COMPANY_ID), any(LocalDateTime.class)))
                .thenReturn(expectedCompany);
            
            // When
            ResponseEntity<CompanyDTO> response = companyController.updateSubscription(TEST_COMPANY_ID, request);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            
            verify(companyApplicationService).updateSubscription(eq(TEST_COMPANY_ID), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should handle exception when updating subscription")
        void shouldHandleExceptionWhenUpdatingSubscription() {
            // Given
            Map<String, Object> request = createValidSubscriptionRequest();
            
            when(companyApplicationService.updateSubscription(eq(TEST_COMPANY_ID), any(LocalDateTime.class)))
                .thenThrow(new IllegalArgumentException("Invalid subscription date"));
            
            // When
            ResponseEntity<CompanyDTO> response = companyController.updateSubscription(TEST_COMPANY_ID, request);
            
            // Then
            assertNotNull(response);
            assertEquals(400, response.getStatusCode().value());
            
            verify(companyApplicationService).updateSubscription(eq(TEST_COMPANY_ID), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should map request to UpdateCompanyCommand correctly")
        void shouldMapRequestToUpdateCompanyCommandCorrectly() {
            // Given
            Map<String, Object> request = createValidUpdateRequest();
            request.put("fiscalYearStart", "04-01");
            request.put("defaultCurrency", "EUR");
            request.put("maxUsers", 200);
            
            CompanyDTO mockCompany = createMockCompanyDTO();
            ArgumentCaptor<UpdateCompanyCommand> commandCaptor = 
                ArgumentCaptor.forClass(UpdateCompanyCommand.class);
            
            when(companyApplicationService.updateCompany(eq(TEST_COMPANY_ID), commandCaptor.capture()))
                .thenReturn(mockCompany);
            
            // When
            companyController.updateCompany(TEST_COMPANY_ID, request);
            
            // Then - verify mapping is correct
            UpdateCompanyCommand capturedCommand = commandCaptor.getValue();
            assertEquals("Updated " + TEST_COMPANY_NAME, capturedCommand.getCompanyName());
            assertEquals("Updated " + TEST_ADDRESS, capturedCommand.getAddress());
            assertEquals("Updated " + TEST_CITY, capturedCommand.getCity());
            assertEquals("Updated " + TEST_STATE, capturedCommand.getStateProvince());
            assertEquals("54321", capturedCommand.getPostalCode());
            assertEquals("https://updated.com", capturedCommand.getWebsite());
            assertEquals("04-01", capturedCommand.getFiscalYearStart());
            assertEquals("EUR", capturedCommand.getDefaultCurrency());
            assertEquals(Integer.valueOf(200), capturedCommand.getMaxUsers());
            
            verify(companyApplicationService).updateCompany(eq(TEST_COMPANY_ID), any(UpdateCompanyCommand.class));
        }
    }

    // ========== Query Company Tests ==========

    @Nested
    @DisplayName("Query Company Operations")
    class QueryCompanyOperations {

        @Test
        @DisplayName("Should get all companies successfully via GET")
        void shouldGetAllCompaniesSuccessfully() {
            // Given
            List<CompanyDTO> expectedCompanies = Arrays.asList(
                createMockCompanyDTO(),
                createAnotherMockCompanyDTO()
            );
            
            when(companyApplicationService.getAllCompanies())
                .thenReturn(expectedCompanies);
            
            // When
            ResponseEntity<List<CompanyDTO>> response = companyController.getAllCompanies();
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().size());
            
            verify(companyApplicationService).getAllCompanies();
        }

        @Test
        @DisplayName("Should get company by ID successfully via GET")
        void shouldGetCompanyByIdSuccessfully() {
            // Given
            CompanyDTO expectedCompany = createMockCompanyDTO();
            
            when(companyApplicationService.getCompanyById(TEST_COMPANY_ID))
                .thenReturn(expectedCompany);
            
            // When
            ResponseEntity<CompanyDTO> response = companyController.getCompanyById(TEST_COMPANY_ID);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertEquals(TEST_COMPANY_ID, response.getBody().getCompanyId());
            
            verify(companyApplicationService).getCompanyById(TEST_COMPANY_ID);
        }

        @Test
        @DisplayName("Should handle exception when getting company by ID")
        void shouldHandleExceptionWhenGettingCompanyById() {
            // Given
            when(companyApplicationService.getCompanyById(TEST_COMPANY_ID))
                .thenThrow(new ResourceNotFoundException("Company not found"));
            
            // When
            ResponseEntity<CompanyDTO> response = companyController.getCompanyById(TEST_COMPANY_ID);
            
            // Then
            assertNotNull(response);
            assertEquals(404, response.getStatusCode().value());
            
            verify(companyApplicationService).getCompanyById(TEST_COMPANY_ID);
        }

        @Test
        @DisplayName("Should get active companies successfully")
        void shouldGetActiveCompaniesSuccessfully() {
            // Given
            List<CompanyDTO> expectedCompanies = Arrays.asList(createMockCompanyDTO());
            
            when(companyApplicationService.getActiveCompanies())
                .thenReturn(expectedCompanies);
            
            // When
            ResponseEntity<List<CompanyDTO>> response = companyController.getActiveCompanies();
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertFalse(response.getBody().isEmpty());
            
            verify(companyApplicationService).getActiveCompanies();
        }

        @Test
        @DisplayName("Should search companies successfully")
        void shouldSearchCompaniesSuccessfully() {
            // Given
            String searchName = "Test";
            List<CompanyDTO> expectedCompanies = Arrays.asList(createMockCompanyDTO());
            
            when(companyApplicationService.searchCompaniesByName(searchName))
                .thenReturn(expectedCompanies);
            
            // When
            ResponseEntity<List<CompanyDTO>> response = companyController.searchCompanies(searchName);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertFalse(response.getBody().isEmpty());
            
            verify(companyApplicationService).searchCompaniesByName(searchName);
        }

        @Test
        @DisplayName("Should get company statistics successfully")
        void shouldGetCompanyStatisticsSuccessfully() {
            // Given
            CompanyStatsDTO expectedStats = createMockCompanyStatsDTO();
            
            when(companyApplicationService.getCompanyStatistics())
                .thenReturn(expectedStats);
            
            // When
            ResponseEntity<CompanyStatsDTO> response = companyController.getCompanyStatistics();
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertEquals(10L, response.getBody().getTotalCompanies());
            assertEquals(8L, response.getBody().getActiveCompanies());
            assertEquals(2L, response.getBody().getInactiveCompanies());
            
            verify(companyApplicationService).getCompanyStatistics();
        }

        @Test
        @DisplayName("Should get expiring subscriptions successfully")
        void shouldGetExpiringSubscriptionsSuccessfully() {
            // Given
            Integer warningDays = 30;
            List<CompanyDTO> expectedCompanies = Arrays.asList(createMockCompanyDTO());
            
            when(companyApplicationService.getCompaniesWithExpiringSubscriptions(warningDays))
                .thenReturn(expectedCompanies);
            
            // When
            ResponseEntity<List<CompanyDTO>> response = companyController.getExpiringSubscriptions(warningDays);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertFalse(response.getBody().isEmpty());
            
            verify(companyApplicationService).getCompaniesWithExpiringSubscriptions(warningDays);
        }
    }

    // ========== Helper Method Tests ==========

    @Nested
    @DisplayName("Helper Method Tests")
    class HelperMethodTests {

        @Test
        @DisplayName("Should test canAddUser method")
        void shouldTestCanAddUserMethod() {
            // Given
            Integer companyId = TEST_COMPANY_ID;
            Integer currentUserCount = 25;
            
            when(companyApplicationService.canAddUser(companyId, currentUserCount))
                .thenReturn(true);
            
            // When
            ResponseEntity<Map<String, Object>> response = companyController.canAddUser(companyId, currentUserCount);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertTrue((Boolean) response.getBody().get("canAdd"));
            assertEquals(companyId, response.getBody().get("companyId"));
            assertEquals(currentUserCount, response.getBody().get("currentUserCount"));
            
            verify(companyApplicationService).canAddUser(companyId, currentUserCount);
        }

        @Test
        @DisplayName("Should handle exception in canAddUser method")
        void shouldHandleExceptionInCanAddUserMethod() {
            // Given
            Integer companyId = TEST_COMPANY_ID;
            Integer currentUserCount = 25;
            
            when(companyApplicationService.canAddUser(companyId, currentUserCount))
                .thenThrow(new ResourceNotFoundException("Company not found"));
            
            // When
            ResponseEntity<Map<String, Object>> response = companyController.canAddUser(companyId, currentUserCount);
            
            // Then
            assertNotNull(response);
            assertEquals(400, response.getStatusCode().value());
            
            verify(companyApplicationService).canAddUser(companyId, currentUserCount);
        }

        @Test
        @DisplayName("Should handle getString helper with default value")
        void shouldHandleGetStringHelperWithDefaultValue() {
            // Test helper method indirectly through createCompany
            Map<String, Object> request = new HashMap<>();
            request.put("companyName", TEST_COMPANY_NAME);
            request.put("email", TEST_EMAIL);
            request.put("createdBy", TEST_USER_ID);
            // website not provided - should use default
            
            CompanyDTO mockCompany = createMockCompanyDTO();
            when(companyApplicationService.createCompany(any(CreateCompanyCommand.class)))
                .thenReturn(mockCompany);
            
            // When
            ResponseEntity<CompanyDTO> response = companyController.createCompany(request);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            
            verify(companyApplicationService).createCompany(any(CreateCompanyCommand.class));
        }

        @Test
        @DisplayName("Should handle getInteger helper with default value")
        void shouldHandleGetIntegerHelperWithDefaultValue() {
            // Test helper method indirectly through createCompany
            Map<String, Object> request = new HashMap<>();
            request.put("companyName", TEST_COMPANY_NAME);
            request.put("email", TEST_EMAIL);
            request.put("createdBy", TEST_USER_ID);
            // maxUsers not provided - should use default
            
            CompanyDTO mockCompany = createMockCompanyDTO();
            when(companyApplicationService.createCompany(any(CreateCompanyCommand.class)))
                .thenReturn(mockCompany);
            
            // When
            ResponseEntity<CompanyDTO> response = companyController.createCompany(request);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            
            verify(companyApplicationService).createCompany(any(CreateCompanyCommand.class));
        }

        @Test
        @DisplayName("Should handle getRequiredString helper method")
        void shouldHandleGetRequiredStringHelperMethod() {
            // Test helper method indirectly through createCompany with missing required field
            Map<String, Object> request = new HashMap<>();
            request.put("email", TEST_EMAIL);
            request.put("createdBy", TEST_USER_ID);
            // companyName missing - should trigger validation
            
            // When & Then - Should throw exception during mapping, not from service
            assertThrows(IllegalArgumentException.class, () -> {
                companyController.createCompany(request);
            });
        }

        @Test
        @DisplayName("Should handle fiscal year start string parsing")
        void shouldHandleFiscalYearStartStringParsing() {
            // Test helper method indirectly through createCompany with fiscal year as string
            Map<String, Object> request = createValidCompanyRequest();
            request.put("fiscalYearStart", "01-01"); // String format
            
            CompanyDTO mockCompany = createMockCompanyDTO();
            when(companyApplicationService.createCompany(any(CreateCompanyCommand.class)))
                .thenReturn(mockCompany);
            
            // When
            ResponseEntity<CompanyDTO> response = companyController.createCompany(request);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            
            verify(companyApplicationService).createCompany(any(CreateCompanyCommand.class));
        }

        @Test
        @DisplayName("Should handle number format exception in getInteger helper")
        void shouldHandleNumberFormatExceptionInGetIntegerHelper() {
            // Test helper method indirectly through createCompany with invalid number
            Map<String, Object> request = createValidCompanyRequest();
            request.put("maxUsers", "invalid-number"); // Invalid number format
            
            CompanyDTO mockCompany = createMockCompanyDTO();
            when(companyApplicationService.createCompany(any(CreateCompanyCommand.class)))
                .thenReturn(mockCompany);
            
            // When
            ResponseEntity<CompanyDTO> response = companyController.createCompany(request);
            
            // Then - should use default value when parsing fails
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            
            verify(companyApplicationService).createCompany(any(CreateCompanyCommand.class));
        }

        @Test
        @DisplayName("Should handle empty string in getRequiredString")
        void shouldHandleEmptyStringInGetRequiredString() {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("companyName", ""); // Empty string
            request.put("email", TEST_EMAIL);
            request.put("createdBy", TEST_USER_ID);
            
            // When & Then - Should throw exception during mapping
            assertThrows(IllegalArgumentException.class, () -> {
                companyController.createCompany(request);
            });
        }

        @Test
        @DisplayName("Should handle whitespace-only string in getRequiredString")
        void shouldHandleWhitespaceOnlyStringInGetRequiredString() {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("companyName", "   "); // Whitespace only
            request.put("email", TEST_EMAIL);
            request.put("createdBy", TEST_USER_ID);
            
            // When & Then - Should throw exception during mapping
            assertThrows(IllegalArgumentException.class, () -> {
                companyController.createCompany(request);
            });
        }

        @Test
        @DisplayName("Should handle Integer type in getInteger helper")
        void shouldHandleIntegerTypeInGetIntegerHelper() {
            // Test helper method with actual Integer type
            Map<String, Object> request = createValidCompanyRequest();
            request.put("maxUsers", Integer.valueOf(75)); // Actual Integer object
            
            CompanyDTO mockCompany = createMockCompanyDTO();
            ArgumentCaptor<CreateCompanyCommand> commandCaptor = 
                ArgumentCaptor.forClass(CreateCompanyCommand.class);
            
            when(companyApplicationService.createCompany(commandCaptor.capture()))
                .thenReturn(mockCompany);
            
            // When
            companyController.createCompany(request);
            
            // Then - verify Integer is properly handled
            CreateCompanyCommand capturedCommand = commandCaptor.getValue();
            assertEquals(Integer.valueOf(75), capturedCommand.getMaxUsers());
            
            verify(companyApplicationService).createCompany(any(CreateCompanyCommand.class));
        }

        @Test
        @DisplayName("Should trim string values in getString helper")
        void shouldTrimStringValuesInGetStringHelper() {
            // Test helper method with strings that need trimming
            Map<String, Object> request = createValidCompanyRequest();
            request.put("companyName", "  " + TEST_COMPANY_NAME + "  "); // With spaces
            request.put("email", "  " + TEST_EMAIL + "  "); // With spaces
            
            CompanyDTO mockCompany = createMockCompanyDTO();
            ArgumentCaptor<CreateCompanyCommand> commandCaptor = 
                ArgumentCaptor.forClass(CreateCompanyCommand.class);
            
            when(companyApplicationService.createCompany(commandCaptor.capture()))
                .thenReturn(mockCompany);
            
            // When
            companyController.createCompany(request);
            
            // Then - verify strings are trimmed
            CreateCompanyCommand capturedCommand = commandCaptor.getValue();
            assertEquals(TEST_COMPANY_NAME, capturedCommand.getCompanyName());
            assertEquals(TEST_EMAIL, capturedCommand.getEmail());
            
            verify(companyApplicationService).createCompany(any(CreateCompanyCommand.class));
        }
    }

    // ========== Edge Cases and Error Handling ==========

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {

        @Test
        @DisplayName("Should handle empty request map")
        void shouldHandleEmptyRequestMap() {
            // Given
            Map<String, Object> emptyRequest = new HashMap<>();
            
            // When & Then - Should throw exception during mapping
            assertThrows(IllegalArgumentException.class, () -> {
                companyController.createCompany(emptyRequest);
            });
        }

        @Test
        @DisplayName("Should handle null request values")
        void shouldHandleNullRequestValues() {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("companyName", null);
            request.put("email", null);
            request.put("createdBy", null);
            
            // When & Then - Should throw exception during mapping
            assertThrows(IllegalArgumentException.class, () -> {
                companyController.createCompany(request);
            });
        }

        @Test
        @DisplayName("Should handle invalid subscription date format")
        void shouldHandleInvalidSubscriptionDateFormat() {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("expiresAt", "invalid-date-format");
            
            // When
            ResponseEntity<CompanyDTO> response = companyController.updateSubscription(TEST_COMPANY_ID, request);
            
            // Then
            assertNotNull(response);
            assertEquals(400, response.getStatusCode().value());
        }

        @Test
        @DisplayName("Should handle service returning null")
        void shouldHandleServiceReturningNull() {
            // Given
            when(companyApplicationService.getCompanyById(TEST_COMPANY_ID))
                .thenReturn(null);
            
            // When
            ResponseEntity<CompanyDTO> response = companyController.getCompanyById(TEST_COMPANY_ID);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNull(response.getBody());
            
            verify(companyApplicationService).getCompanyById(TEST_COMPANY_ID);
        }
    }

    // ========== Test Data Helper Methods ==========

    private Map<String, Object> createValidCompanyRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("companyName", TEST_COMPANY_NAME);
        request.put("email", TEST_EMAIL);
        request.put("address", TEST_ADDRESS);
        request.put("city", TEST_CITY);
        request.put("stateProvince", TEST_STATE);
        request.put("postalCode", TEST_POSTAL_CODE);
        request.put("website", TEST_WEBSITE);
        request.put("registrationNumber", TEST_REGISTRATION_NUMBER);
        request.put("taxId", TEST_TAX_ID);
        request.put("fiscalYearStart", TEST_FISCAL_YEAR_START);
        request.put("defaultCurrency", TEST_CURRENCY);
        request.put("maxUsers", TEST_MAX_USERS);
        request.put("createdBy", TEST_USER_ID);
        return request;
    }

    private Map<String, Object> createValidUpdateRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("companyName", "Updated " + TEST_COMPANY_NAME);
        request.put("address", "Updated " + TEST_ADDRESS);
        request.put("city", "Updated " + TEST_CITY);
        request.put("stateProvince", "Updated " + TEST_STATE);
        request.put("postalCode", "54321");
        request.put("website", "https://updated.com");
        return request;
    }

    private Map<String, Object> createValidSubscriptionRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("expiresAt", "2025-12-31T23:59:59");
        return request;
    }

    private CompanyDTO createMockCompanyDTO() {
        return CompanyDTO.builder()
            .companyId(TEST_COMPANY_ID)
            .companyName(TEST_COMPANY_NAME)
            .email(TEST_EMAIL)
            .address(TEST_ADDRESS)
            .city(TEST_CITY)
            .stateProvince(TEST_STATE)
            .postalCode(TEST_POSTAL_CODE)
            .website(TEST_WEBSITE)
            .registrationNumber(TEST_REGISTRATION_NUMBER)
            .taxId(TEST_TAX_ID)
            .fiscalYearStart("01-01")
            .defaultCurrency(TEST_CURRENCY)
            .status(CompanyStatus.Status.ACTIVE)
            .maxUsers(TEST_MAX_USERS)
            .subscriptionExpiresAt(LocalDateTime.now().plusMonths(12))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .createdBy(TEST_USER_ID)
            .tenantId(TEST_COMPANY_ID)
            .isActive(true)
            .canBeModified(true)
            .subscriptionValid(true)
            .build();
    }

    private CompanyDTO createAnotherMockCompanyDTO() {
        return CompanyDTO.builder()
            .companyId(2)
            .companyName("Another Test Company")
            .email("another@test.com")
            .address("456 Another Street")
            .city("Another City")
            .stateProvince("Another State")
            .postalCode("67890")
            .website("https://another.com")
            .status(CompanyStatus.Status.ACTIVE)
            .maxUsers(25)
            .subscriptionExpiresAt(LocalDateTime.now().plusMonths(6))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .createdBy(TEST_USER_ID)
            .tenantId(2)
            .isActive(true)
            .canBeModified(true)
            .subscriptionValid(true)
            .build();
    }

    private CompanyStatsDTO createMockCompanyStatsDTO() {
        return CompanyStatsDTO.builder()
            .totalCompanies(10L)
            .activeCompanies(8L)
            .inactiveCompanies(2L)
            .build();
    }
}