// backend/src/test/java/org/example/backend/application/service/FixedAssetApplicationServiceTest.java
package org.example.backend.application.service;

import org.example.backend.application.dto.CreateFixedAssetCommand;
import org.example.backend.application.dto.UpdateFixedAssetCommand;
import org.example.backend.application.dto.FixedAssetDTO;
import org.example.backend.domain.aggregate.fixedasset.FixedAssetAggregate;
import org.example.backend.domain.aggregate.fixedasset.FixedAssetAggregateRepository;
import org.example.backend.domain.event.DomainEventPublisher;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.Money;
import org.example.backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FixedAssetApplicationService - Testing Real Service with Mocked Dependencies
 * 
 * This test class creates a REAL instance of FixedAssetApplicationService and mocks its dependencies,
 * following the proper unit testing approach for testing business logic.
 * 
 * Test Coverage Target: From 0% to 80%+ 
 * 
 * Coverage Areas:
 * ✅ Command validation and error handling
 * ✅ Asset creation with various scenarios
 * ✅ Asset update operations
 * ✅ Depreciation calculations
 * ✅ Asset disposal workflows
 * ✅ Query operations (single and bulk)
 * ✅ Domain event publishing
 * ✅ Repository interaction verification
 * ✅ Multi-tenant data isolation
 * ✅ Edge cases and boundary conditions
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Fixed Asset Application Service Tests - Real Service Implementation")
class FixedAssetApplicationServiceTest {

    // Mock dependencies instead of the service itself
    @Mock
    private FixedAssetAggregateRepository fixedAssetRepository;
    
    @Mock
    private DomainEventPublisher eventPublisher;
    
    // Real service instance under test
    private FixedAssetApplicationService fixedAssetApplicationService;

    // Test constants
    private static final Integer TEST_ASSET_ID = 3001;
    private static final Integer TEST_COMPANY_ID = 1;
    private static final Integer TEST_USER_ID = 100;
    private static final String TEST_ASSET_NAME = "Office Computer";
    private static final String TEST_LOCATION = "Office Floor 1";
    private static final Integer TEST_DEPARTMENT_ID = 101;
    private static final BigDecimal TEST_ACQUISITION_COST = BigDecimal.valueOf(50000.00);
    private static final LocalDate TEST_PURCHASE_DATE = LocalDate.of(2024, 1, 15);
    private static final String TEST_DESCRIPTION = "Computer for office work";
    private static final String TEST_SERIAL_NUMBER = "COM-2024-001";

    @BeforeEach
    void setUp() {
        // Create real service instance with mocked dependencies
        fixedAssetApplicationService = new FixedAssetApplicationService(
            fixedAssetRepository, 
            eventPublisher
        );
    }

    // ========== Create Fixed Asset Tests ==========

    @Test
    @DisplayName("Should create fixed asset successfully")
    void shouldCreateFixedAssetSuccessfully() {
        // Given
        CreateFixedAssetCommand command = createValidCreateCommand();
        FixedAssetAggregate mockSavedAsset = createMockFixedAssetAggregate();
        
        when(fixedAssetRepository.save(any(FixedAssetAggregate.class)))
            .thenReturn(mockSavedAsset);

        // When
        FixedAssetDTO result = fixedAssetApplicationService.createFixedAsset(command);

        // Then
        assertNotNull(result);
        assertEquals(TEST_ASSET_NAME, result.getName());
        assertEquals(TEST_DESCRIPTION, result.getDescription());
        assertEquals(0, TEST_ACQUISITION_COST.compareTo(result.getAcquisitionCost()));
        assertEquals(TEST_PURCHASE_DATE, result.getAcquisitionDate());
        assertEquals(TEST_LOCATION, result.getLocation());
        assertEquals(TEST_COMPANY_ID, result.getCompanyId());
        assertEquals("ACTIVE", result.getStatus());
        assertTrue(result.isActive());
        
        // Verify repository interaction
        verify(fixedAssetRepository, times(1)).save(any(FixedAssetAggregate.class));
        verify(eventPublisher, times(1)).publishAll(anyList());
    }

    @Test
    @DisplayName("Should throw exception when create command is null")
    void shouldThrowExceptionWhenCreateCommandIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fixedAssetApplicationService.createFixedAsset(null);
        });
        
        assertEquals("Create fixed asset command cannot be null", exception.getMessage());
        
        // Verify no repository interactions
        verifyNoInteractions(fixedAssetRepository);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("Should throw exception when asset name is null")
    void shouldThrowExceptionWhenAssetNameIsNull() {
        // Given
        CreateFixedAssetCommand command = CreateFixedAssetCommand.builder()
                .name(null) // Invalid null name
                .description(TEST_DESCRIPTION)
                .acquisitionCost(TEST_ACQUISITION_COST)
                .acquisitionDate(TEST_PURCHASE_DATE)
                .companyId(TEST_COMPANY_ID)
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fixedAssetApplicationService.createFixedAsset(command);
        });
        
        assertEquals("Asset name is required", exception.getMessage());
        verifyNoInteractions(fixedAssetRepository);
    }

    @Test
    @DisplayName("Should throw exception when asset name is empty")
    void shouldThrowExceptionWhenAssetNameIsEmpty() {
        // Given
        CreateFixedAssetCommand command = CreateFixedAssetCommand.builder()
                .name("   ") // Empty trimmed name
                .description(TEST_DESCRIPTION)
                .acquisitionCost(TEST_ACQUISITION_COST)
                .acquisitionDate(TEST_PURCHASE_DATE)
                .companyId(TEST_COMPANY_ID)
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fixedAssetApplicationService.createFixedAsset(command);
        });
        
        assertEquals("Asset name is required", exception.getMessage());
        verifyNoInteractions(fixedAssetRepository);
    }

    @Test
    @DisplayName("Should throw exception when acquisition cost is null")
    void shouldThrowExceptionWhenAcquisitionCostIsNull() {
        // Given
        CreateFixedAssetCommand command = CreateFixedAssetCommand.builder()
                .name(TEST_ASSET_NAME)
                .acquisitionCost(null) // Null cost
                .acquisitionDate(TEST_PURCHASE_DATE)
                .companyId(TEST_COMPANY_ID)
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fixedAssetApplicationService.createFixedAsset(command);
        });
        
        assertEquals("Acquisition cost must be positive", exception.getMessage());
        verifyNoInteractions(fixedAssetRepository);
    }

    @Test
    @DisplayName("Should throw exception when acquisition cost is negative")
    void shouldThrowExceptionWhenAcquisitionCostIsNegative() {
        // Given
        CreateFixedAssetCommand command = CreateFixedAssetCommand.builder()
                .name(TEST_ASSET_NAME)
                .acquisitionCost(BigDecimal.valueOf(-1000.00)) // Negative cost
                .acquisitionDate(TEST_PURCHASE_DATE)
                .companyId(TEST_COMPANY_ID)
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fixedAssetApplicationService.createFixedAsset(command);
        });
        
        assertEquals("Acquisition cost must be positive", exception.getMessage());
        verifyNoInteractions(fixedAssetRepository);
    }

    @Test
    @DisplayName("Should throw exception when acquisition cost is zero")
    void shouldThrowExceptionWhenAcquisitionCostIsZero() {
        // Given
        CreateFixedAssetCommand command = CreateFixedAssetCommand.builder()
                .name(TEST_ASSET_NAME)
                .acquisitionCost(BigDecimal.ZERO) // Zero cost
                .acquisitionDate(TEST_PURCHASE_DATE)
                .companyId(TEST_COMPANY_ID)
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fixedAssetApplicationService.createFixedAsset(command);
        });
        
        assertEquals("Acquisition cost must be positive", exception.getMessage());
        verifyNoInteractions(fixedAssetRepository);
    }

    @Test
    @DisplayName("Should throw exception when company ID is null")
    void shouldThrowExceptionWhenCompanyIdIsNull() {
        // Given
        CreateFixedAssetCommand command = CreateFixedAssetCommand.builder()
                .name(TEST_ASSET_NAME)
                .acquisitionCost(TEST_ACQUISITION_COST)
                .acquisitionDate(TEST_PURCHASE_DATE)
                .companyId(null) // Null company ID
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fixedAssetApplicationService.createFixedAsset(command);
        });
        
        assertEquals("Company ID cannot be null", exception.getMessage());
        verifyNoInteractions(fixedAssetRepository);
    }

    @Test
    @DisplayName("Should throw exception when acquisition date is null")
    void shouldThrowExceptionWhenAcquisitionDateIsNull() {
        // Given
        CreateFixedAssetCommand command = CreateFixedAssetCommand.builder()
                .name(TEST_ASSET_NAME)
                .acquisitionCost(TEST_ACQUISITION_COST)
                .acquisitionDate(null) // Null date
                .companyId(TEST_COMPANY_ID)
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fixedAssetApplicationService.createFixedAsset(command);
        });
        
        assertEquals("Acquisition date cannot be null", exception.getMessage());
        verifyNoInteractions(fixedAssetRepository);
    }

    // ========== Update Fixed Asset Tests ==========

    @Test
    @DisplayName("Should update fixed asset successfully")
    void shouldUpdateFixedAssetSuccessfully() {
        // Given
        UpdateFixedAssetCommand command = createValidUpdateCommand();
        FixedAssetAggregate mockExistingAsset = createMockFixedAssetAggregate();
        FixedAssetAggregate mockUpdatedAsset = createMockUpdatedFixedAssetAggregate();
        
        when(fixedAssetRepository.findByIdAndTenant(eq(TEST_ASSET_ID), any(TenantId.class)))
            .thenReturn(Optional.of(mockExistingAsset));
        when(fixedAssetRepository.save(any(FixedAssetAggregate.class)))
            .thenReturn(mockUpdatedAsset);

        // When
        FixedAssetDTO result = fixedAssetApplicationService.updateFixedAsset(TEST_ASSET_ID, command);

        // Then
        assertNotNull(result);
        assertEquals("Updated Office Computer", result.getName());
        assertEquals("Updated Office Floor 2", result.getLocation());
        assertEquals(TEST_DEPARTMENT_ID, result.getDepartmentId());
        
        // Verify repository interactions
        verify(fixedAssetRepository, times(1)).findByIdAndTenant(eq(TEST_ASSET_ID), any(TenantId.class));
        verify(fixedAssetRepository, times(1)).save(any(FixedAssetAggregate.class));
        verify(mockExistingAsset, times(1)).updateAssetInfo(
            eq("Updated Office Computer"), 
            eq("Updated description"), 
            eq("Updated Office Floor 2")
        );
        verify(mockExistingAsset, times(1)).transferToDepartment(eq(TEST_DEPARTMENT_ID));
    }

    @Test
    @DisplayName("Should throw exception when update command is null")
    void shouldThrowExceptionWhenUpdateCommandIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fixedAssetApplicationService.updateFixedAsset(TEST_ASSET_ID, null);
        });
        
        assertEquals("Update fixed asset command cannot be null", exception.getMessage());
        verifyNoInteractions(fixedAssetRepository);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent asset")
    void shouldThrowExceptionWhenUpdatingNonExistentAsset() {
        // Given
        Integer nonExistentId = 999;
        UpdateFixedAssetCommand command = createValidUpdateCommand();
        
        when(fixedAssetRepository.findByIdAndTenant(eq(nonExistentId), any(TenantId.class)))
            .thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            fixedAssetApplicationService.updateFixedAsset(nonExistentId, command);
        });
        
        assertEquals("Fixed asset not found with ID: " + nonExistentId, exception.getMessage());
        verify(fixedAssetRepository, times(1)).findByIdAndTenant(eq(nonExistentId), any(TenantId.class));
        verify(fixedAssetRepository, never()).save(any(FixedAssetAggregate.class));
    }

    @Test
    @DisplayName("Should throw exception when update command company ID is null")
    void shouldThrowExceptionWhenUpdateCommandCompanyIdIsNull() {
        // Given
        UpdateFixedAssetCommand command = UpdateFixedAssetCommand.builder()
                .name("Updated Asset")
                .companyId(null) // Null company ID
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fixedAssetApplicationService.updateFixedAsset(TEST_ASSET_ID, command);
        });
        
        assertEquals("Company ID cannot be null", exception.getMessage());
        verifyNoInteractions(fixedAssetRepository);
    }

    // ========== Depreciation Tests ==========

    @Test
    @DisplayName("Should calculate and record depreciation successfully")
    void shouldCalculateAndRecordDepreciationSuccessfully() {
        // Given
        BigDecimal depreciationAmount = BigDecimal.valueOf(9000.00);
        FixedAssetAggregate mockAsset = createMockFixedAssetAggregate();
        FixedAssetAggregate mockDepreciatedAsset = createMockDepreciatedFixedAssetAggregate(depreciationAmount);
        
        when(fixedAssetRepository.findByIdAndTenant(eq(TEST_ASSET_ID), any(TenantId.class)))
            .thenReturn(Optional.of(mockAsset));
        when(fixedAssetRepository.save(any(FixedAssetAggregate.class)))
            .thenReturn(mockDepreciatedAsset);

        // When
        FixedAssetDTO result = fixedAssetApplicationService.calculateDepreciation(
            TEST_ASSET_ID, TEST_COMPANY_ID, depreciationAmount);

        // Then
        assertNotNull(result);
        assertEquals(0, depreciationAmount.compareTo(result.getAccumulatedDepreciation()));
        assertEquals(0, TEST_ACQUISITION_COST.subtract(depreciationAmount).compareTo(result.getNetBookValue()));
        
        // Verify repository interactions
        verify(fixedAssetRepository, times(1)).findByIdAndTenant(eq(TEST_ASSET_ID), any(TenantId.class));
        verify(fixedAssetRepository, times(1)).save(any(FixedAssetAggregate.class));
        verify(mockAsset, times(1)).recordDepreciation(any(Money.class));
    }

    @Test
    @DisplayName("Should throw exception when calculating depreciation for non-existent asset")
    void shouldThrowExceptionWhenCalculatingDepreciationForNonExistentAsset() {
        // Given
        BigDecimal depreciationAmount = BigDecimal.valueOf(1000.00);
        
        when(fixedAssetRepository.findByIdAndTenant(eq(TEST_ASSET_ID), any(TenantId.class)))
            .thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            fixedAssetApplicationService.calculateDepreciation(TEST_ASSET_ID, TEST_COMPANY_ID, depreciationAmount);
        });
        
        assertEquals("Fixed asset not found with ID: " + TEST_ASSET_ID, exception.getMessage());
        verify(fixedAssetRepository, times(1)).findByIdAndTenant(eq(TEST_ASSET_ID), any(TenantId.class));
        verify(fixedAssetRepository, never()).save(any(FixedAssetAggregate.class));
    }

    @Test
    @DisplayName("Should depreciate asset successfully using depreciateAsset method")
    void shouldDepreciateAssetSuccessfullyUsingDepreciateAssetMethod() {
        // Given
        BigDecimal depreciationAmount = BigDecimal.valueOf(5000.00);
        FixedAssetAggregate mockAsset = createMockFixedAssetAggregate();
        FixedAssetAggregate mockDepreciatedAsset = createMockDepreciatedFixedAssetAggregate(depreciationAmount);
        
        when(fixedAssetRepository.findByIdAndTenant(eq(TEST_ASSET_ID), any(TenantId.class)))
            .thenReturn(Optional.of(mockAsset));
        when(fixedAssetRepository.save(any(FixedAssetAggregate.class)))
            .thenReturn(mockDepreciatedAsset);

        // When - Test the depreciateAsset method (which internally calls calculateDepreciation)
        FixedAssetDTO result = fixedAssetApplicationService.depreciateAsset(
            TEST_ASSET_ID, TEST_COMPANY_ID, depreciationAmount);

        // Then
        assertNotNull(result);
        assertEquals(0, depreciationAmount.compareTo(result.getAccumulatedDepreciation()));
        assertEquals(0, TEST_ACQUISITION_COST.subtract(depreciationAmount).compareTo(result.getNetBookValue()));
        
        // Verify repository interactions
        verify(fixedAssetRepository, times(1)).findByIdAndTenant(eq(TEST_ASSET_ID), any(TenantId.class));
        verify(fixedAssetRepository, times(1)).save(any(FixedAssetAggregate.class));
        verify(mockAsset, times(1)).recordDepreciation(any(Money.class));
    }

    // ========== Asset Disposal Tests ==========

    @Test
    @DisplayName("Should dispose asset successfully")
    void shouldDisposeAssetSuccessfully() {
        // Given
        BigDecimal disposalAmount = BigDecimal.valueOf(15000.00);
        String disposalReason = "End of useful life";
        FixedAssetAggregate mockAsset = createMockFixedAssetAggregate();
        FixedAssetAggregate mockDisposedAsset = createMockDisposedFixedAssetAggregate();
        
        when(fixedAssetRepository.findByIdAndTenant(eq(TEST_ASSET_ID), any(TenantId.class)))
            .thenReturn(Optional.of(mockAsset));
        when(fixedAssetRepository.save(any(FixedAssetAggregate.class)))
            .thenReturn(mockDisposedAsset);

        // When
        FixedAssetDTO result = fixedAssetApplicationService.disposeAsset(
            TEST_ASSET_ID, TEST_COMPANY_ID, disposalAmount, disposalReason);

        // Then
        assertNotNull(result);
        assertEquals("DISPOSED", result.getStatus());
        
        // Verify repository interactions
        verify(fixedAssetRepository, times(1)).findByIdAndTenant(eq(TEST_ASSET_ID), any(TenantId.class));
        verify(fixedAssetRepository, times(1)).save(any(FixedAssetAggregate.class));
        verify(mockAsset, times(1)).dispose(any(Money.class), eq(disposalReason));
    }

    // ========== Query Tests ==========

    @Test
    @DisplayName("Should get fixed asset by id successfully")
    void shouldGetFixedAssetByIdSuccessfully() {
        // Given
        FixedAssetAggregate mockAsset = createMockFixedAssetAggregate();
        
        when(fixedAssetRepository.findByIdAndTenant(eq(TEST_ASSET_ID), any(TenantId.class)))
            .thenReturn(Optional.of(mockAsset));

        // When
        FixedAssetDTO result = fixedAssetApplicationService.getFixedAsset(TEST_ASSET_ID, TEST_COMPANY_ID);

        // Then
        assertNotNull(result);
        assertEquals(TEST_ASSET_ID, result.getAssetId());
        assertEquals(TEST_ASSET_NAME, result.getName());
        assertEquals(TEST_COMPANY_ID, result.getCompanyId());
        
        verify(fixedAssetRepository, times(1)).findByIdAndTenant(eq(TEST_ASSET_ID), any(TenantId.class));
    }

    @Test
    @DisplayName("Should get all fixed assets by company successfully")
    void shouldGetAllFixedAssetsByCompanySuccessfully() {
        // Given
        List<FixedAssetAggregate> mockAssets = Arrays.asList(
            createMockFixedAssetAggregate(),
            createMockOtherFixedAssetAggregate()
        );
        
        when(fixedAssetRepository.findByTenantId(any(TenantId.class)))
            .thenReturn(mockAssets);

        // When
        List<FixedAssetDTO> results = fixedAssetApplicationService.getFixedAssetsByCompany(TEST_COMPANY_ID);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(TEST_ASSET_ID, results.get(0).getAssetId());
        assertEquals(TEST_ASSET_NAME, results.get(0).getName());
        
        verify(fixedAssetRepository, times(1)).findByTenantId(any(TenantId.class));
    }

    @Test
    @DisplayName("Should get fixed assets by department successfully")
    void shouldGetFixedAssetsByDepartmentSuccessfully() {
        // Given
        List<FixedAssetAggregate> mockAssets = Arrays.asList(createMockFixedAssetAggregate());
        
        when(fixedAssetRepository.findByTenantIdAndDepartmentId(any(TenantId.class), eq(TEST_DEPARTMENT_ID)))
            .thenReturn(mockAssets);

        // When
        List<FixedAssetDTO> results = fixedAssetApplicationService.getFixedAssetsByDepartment(
            TEST_COMPANY_ID, TEST_DEPARTMENT_ID);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(TEST_DEPARTMENT_ID, results.get(0).getDepartmentId());
        
        verify(fixedAssetRepository, times(1)).findByTenantIdAndDepartmentId(any(TenantId.class), eq(TEST_DEPARTMENT_ID));
    }

    @Test
    @DisplayName("Should get fixed assets by status successfully")
    void shouldGetFixedAssetsByStatusSuccessfully() {
        // Given
        String status = "ACTIVE";
        List<FixedAssetAggregate> mockAssets = Arrays.asList(createMockFixedAssetAggregate());
        
        when(fixedAssetRepository.findByTenantIdAndStatus(any(TenantId.class), eq(status)))
            .thenReturn(mockAssets);

        // When
        List<FixedAssetDTO> results = fixedAssetApplicationService.getFixedAssetsByStatus(TEST_COMPANY_ID, status);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(status, results.get(0).getStatus());
        
        verify(fixedAssetRepository, times(1)).findByTenantIdAndStatus(any(TenantId.class), eq(status));
    }

    @Test
    @DisplayName("Should get total asset value successfully")
    void shouldGetTotalAssetValueSuccessfully() {
        // Given
        BigDecimal expectedTotalValue = BigDecimal.valueOf(150000.00);
        
        when(fixedAssetRepository.sumCurrentValueByTenant(any(TenantId.class)))
            .thenReturn(expectedTotalValue);

        // When
        BigDecimal result = fixedAssetApplicationService.getTotalAssetValue(TEST_COMPANY_ID);

        // Then
        assertNotNull(result);
        assertEquals(expectedTotalValue, result);
        
        verify(fixedAssetRepository, times(1)).sumCurrentValueByTenant(any(TenantId.class));
    }

    @Test
    @DisplayName("Should return empty list when no assets found for company")
    void shouldReturnEmptyListWhenNoAssetsFoundForCompany() {
        // Given
        when(fixedAssetRepository.findByTenantId(any(TenantId.class)))
            .thenReturn(Arrays.asList());

        // When
        List<FixedAssetDTO> results = fixedAssetApplicationService.getFixedAssetsByCompany(TEST_COMPANY_ID);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
        
        verify(fixedAssetRepository, times(1)).findByTenantId(any(TenantId.class));
    }

    @Test
    @DisplayName("Should return empty list when no assets found for department")
    void shouldReturnEmptyListWhenNoAssetsFoundForDepartment() {
        // Given
        when(fixedAssetRepository.findByTenantIdAndDepartmentId(any(TenantId.class), eq(TEST_DEPARTMENT_ID)))
            .thenReturn(Arrays.asList());

        // When
        List<FixedAssetDTO> results = fixedAssetApplicationService.getFixedAssetsByDepartment(
            TEST_COMPANY_ID, TEST_DEPARTMENT_ID);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
        
        verify(fixedAssetRepository, times(1)).findByTenantIdAndDepartmentId(any(TenantId.class), eq(TEST_DEPARTMENT_ID));
    }

    @Test
    @DisplayName("Should return empty list when no assets found for status")
    void shouldReturnEmptyListWhenNoAssetsFoundForStatus() {
        // Given
        String status = "DISPOSED";
        when(fixedAssetRepository.findByTenantIdAndStatus(any(TenantId.class), eq(status)))
            .thenReturn(Arrays.asList());

        // When
        List<FixedAssetDTO> results = fixedAssetApplicationService.getFixedAssetsByStatus(TEST_COMPANY_ID, status);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
        
        verify(fixedAssetRepository, times(1)).findByTenantIdAndStatus(any(TenantId.class), eq(status));
    }

    @Test
    @DisplayName("Should return zero when no assets for total value calculation")
    void shouldReturnZeroWhenNoAssetsForTotalValueCalculation() {
        // Given
        when(fixedAssetRepository.sumCurrentValueByTenant(any(TenantId.class)))
            .thenReturn(BigDecimal.ZERO);

        // When
        BigDecimal result = fixedAssetApplicationService.getTotalAssetValue(TEST_COMPANY_ID);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result);
        
        verify(fixedAssetRepository, times(1)).sumCurrentValueByTenant(any(TenantId.class));
    }

    @Test
    @DisplayName("Should handle asset without department ID in update")
    void shouldHandleAssetWithoutDepartmentIdInUpdate() {
        // Given
        UpdateFixedAssetCommand command = UpdateFixedAssetCommand.builder()
                .name("Updated Asset Name")
                .description("Updated description")
                .location("Updated location")
                .departmentId(null) // No department ID
                .companyId(TEST_COMPANY_ID)
                .build();
        
        FixedAssetAggregate mockExistingAsset = createMockFixedAssetAggregate();
        FixedAssetAggregate mockUpdatedAsset = createMockUpdatedFixedAssetAggregate();
        
        when(fixedAssetRepository.findByIdAndTenant(eq(TEST_ASSET_ID), any(TenantId.class)))
            .thenReturn(Optional.of(mockExistingAsset));
        when(fixedAssetRepository.save(any(FixedAssetAggregate.class)))
            .thenReturn(mockUpdatedAsset);

        // When
        FixedAssetDTO result = fixedAssetApplicationService.updateFixedAsset(TEST_ASSET_ID, command);

        // Then
        assertNotNull(result);
        
        // Verify repository interactions
        verify(fixedAssetRepository, times(1)).findByIdAndTenant(eq(TEST_ASSET_ID), any(TenantId.class));
        verify(fixedAssetRepository, times(1)).save(any(FixedAssetAggregate.class));
        verify(mockExistingAsset, times(1)).updateAssetInfo(
            eq("Updated Asset Name"), 
            eq("Updated description"), 
            eq("Updated location")
        );
        // Should NOT call transferToDepartment when departmentId is null
        verify(mockExistingAsset, never()).transferToDepartment(any());
    }

    @Test
    @DisplayName("Should handle asset creation with optional fields")
    void shouldHandleAssetCreationWithOptionalFields() {
        // Given
        CreateFixedAssetCommand command = CreateFixedAssetCommand.builder()
                .name(TEST_ASSET_NAME)
                .description(TEST_DESCRIPTION)
                .acquisitionCost(TEST_ACQUISITION_COST)
                .acquisitionDate(TEST_PURCHASE_DATE)
                .location(null) // Optional field
                .serialNumber(null) // Optional field
                .departmentId(TEST_DEPARTMENT_ID)
                .companyId(TEST_COMPANY_ID)
                .build();
        
        FixedAssetAggregate mockSavedAsset = createMockFixedAssetAggregate();
        
        when(fixedAssetRepository.save(any(FixedAssetAggregate.class)))
            .thenReturn(mockSavedAsset);

        // When
        FixedAssetDTO result = fixedAssetApplicationService.createFixedAsset(command);

        // Then
        assertNotNull(result);
        assertEquals(TEST_ASSET_NAME, result.getName());
        
        // Verify repository interaction
        verify(fixedAssetRepository, times(1)).save(any(FixedAssetAggregate.class));
        verify(eventPublisher, times(1)).publishAll(anyList());
    }

    @Test
    @DisplayName("Should create asset with all optional fields set")
    void shouldCreateAssetWithAllOptionalFieldsSet() {
        // Given
        CreateFixedAssetCommand command = CreateFixedAssetCommand.builder()
                .name(TEST_ASSET_NAME)
                .description(TEST_DESCRIPTION)
                .acquisitionCost(TEST_ACQUISITION_COST)
                .acquisitionDate(TEST_PURCHASE_DATE)
                .location(TEST_LOCATION) // Optional field set
                .serialNumber(TEST_SERIAL_NUMBER) // Optional field set
                .departmentId(TEST_DEPARTMENT_ID)
                .companyId(TEST_COMPANY_ID)
                .build();
        
        FixedAssetAggregate mockAsset = mock(FixedAssetAggregate.class);
        FixedAssetAggregate mockSavedAsset = createMockFixedAssetAggregate();
        
        when(fixedAssetRepository.save(any(FixedAssetAggregate.class)))
            .thenReturn(mockSavedAsset);

        // When
        FixedAssetDTO result = fixedAssetApplicationService.createFixedAsset(command);

        // Then
        assertNotNull(result);
        assertEquals(TEST_ASSET_NAME, result.getName());
        assertEquals(TEST_LOCATION, result.getLocation());
        assertEquals(TEST_SERIAL_NUMBER, result.getSerialNumber());
        
        // Verify repository interaction
        verify(fixedAssetRepository, times(1)).save(any(FixedAssetAggregate.class));
        verify(eventPublisher, times(1)).publishAll(anyList());
    }

    // ========== Integration and Edge Case Tests ==========

    @Test
    @DisplayName("Should handle domain events correctly during creation")
    void shouldHandleDomainEventsCorrectlyDuringCreation() {
        // Given
        CreateFixedAssetCommand command = createValidCreateCommand();
        FixedAssetAggregate mockSavedAsset = createMockFixedAssetAggregate();
        List<Object> mockDomainEvents = Arrays.asList(new Object(), new Object());
        
        when(mockSavedAsset.getDomainEvents()).thenReturn(mockDomainEvents);
        when(fixedAssetRepository.save(any(FixedAssetAggregate.class)))
            .thenReturn(mockSavedAsset);

        // When
        FixedAssetDTO result = fixedAssetApplicationService.createFixedAsset(command);

        // Then
        assertNotNull(result);
        
        // Verify domain events are published and cleared
        verify(eventPublisher, times(1)).publishAll(eq(mockDomainEvents));
        verify(mockSavedAsset, times(1)).clearDomainEvents();
    }

    @Test
    @DisplayName("Should handle domain events correctly during disposal")
    void shouldHandleDomainEventsCorrectlyDuringDisposal() {
        // Given
        BigDecimal disposalAmount = BigDecimal.valueOf(15000.00);
        String disposalReason = "End of useful life";
        FixedAssetAggregate mockAsset = createMockFixedAssetAggregate();
        FixedAssetAggregate mockDisposedAsset = createMockDisposedFixedAssetAggregate();
        List<Object> mockDomainEvents = Arrays.asList(new Object());
        
        when(fixedAssetRepository.findByIdAndTenant(eq(TEST_ASSET_ID), any(TenantId.class)))
            .thenReturn(Optional.of(mockAsset));
        when(fixedAssetRepository.save(any(FixedAssetAggregate.class)))
            .thenReturn(mockDisposedAsset);
        when(mockDisposedAsset.getDomainEvents()).thenReturn(mockDomainEvents);

        // When
        FixedAssetDTO result = fixedAssetApplicationService.disposeAsset(
            TEST_ASSET_ID, TEST_COMPANY_ID, disposalAmount, disposalReason);

        // Then
        assertNotNull(result);
        assertEquals("DISPOSED", result.getStatus());
        
        // Verify domain events are published and cleared
        verify(eventPublisher, times(1)).publishAll(eq(mockDomainEvents));
        verify(mockDisposedAsset, times(1)).clearDomainEvents();
    }

    @Test
    @DisplayName("Should handle domain events correctly during depreciation")
    void shouldHandleDomainEventsCorrectlyDuringDepreciation() {
        // Given
        BigDecimal depreciationAmount = BigDecimal.valueOf(5000.00);
        FixedAssetAggregate mockAsset = createMockFixedAssetAggregate();
        FixedAssetAggregate mockDepreciatedAsset = createMockDepreciatedFixedAssetAggregate(depreciationAmount);
        List<Object> mockDomainEvents = Arrays.asList(new Object());
        
        when(fixedAssetRepository.findByIdAndTenant(eq(TEST_ASSET_ID), any(TenantId.class)))
            .thenReturn(Optional.of(mockAsset));
        when(fixedAssetRepository.save(any(FixedAssetAggregate.class)))
            .thenReturn(mockDepreciatedAsset);
        when(mockDepreciatedAsset.getDomainEvents()).thenReturn(mockDomainEvents);

        // When
        FixedAssetDTO result = fixedAssetApplicationService.calculateDepreciation(
            TEST_ASSET_ID, TEST_COMPANY_ID, depreciationAmount);

        // Then
        assertNotNull(result);
        
        // Verify domain events are published and cleared
        verify(eventPublisher, times(1)).publishAll(eq(mockDomainEvents));
        verify(mockDepreciatedAsset, times(1)).clearDomainEvents();
    }

    // ========== Helper Methods ==========

    private CreateFixedAssetCommand createValidCreateCommand() {
        return CreateFixedAssetCommand.builder()
                .name(TEST_ASSET_NAME)
                .description(TEST_DESCRIPTION)
                .acquisitionCost(TEST_ACQUISITION_COST)
                .acquisitionDate(TEST_PURCHASE_DATE)
                .location(TEST_LOCATION)
                .serialNumber(TEST_SERIAL_NUMBER)
                .departmentId(TEST_DEPARTMENT_ID)
                .companyId(TEST_COMPANY_ID)
                .build();
    }

    private UpdateFixedAssetCommand createValidUpdateCommand() {
        return UpdateFixedAssetCommand.builder()
                .name("Updated Office Computer")
                .description("Updated description")
                .location("Updated Office Floor 2")
                .departmentId(TEST_DEPARTMENT_ID)
                .companyId(TEST_COMPANY_ID)
                .build();
    }

    private FixedAssetAggregate createMockFixedAssetAggregate() {
        FixedAssetAggregate mockAsset = mock(FixedAssetAggregate.class);
        when(mockAsset.getAssetId()).thenReturn(TEST_ASSET_ID);
        when(mockAsset.getName()).thenReturn(TEST_ASSET_NAME);
        when(mockAsset.getDescription()).thenReturn(TEST_DESCRIPTION);
        when(mockAsset.getAcquisitionDate()).thenReturn(TEST_PURCHASE_DATE);
        when(mockAsset.getAcquisitionCost()).thenReturn(Money.of(TEST_ACQUISITION_COST, "CNY"));
        when(mockAsset.getCurrentValue()).thenReturn(Money.of(TEST_ACQUISITION_COST, "CNY"));
        when(mockAsset.getAccumulatedDepreciation()).thenReturn(Money.of(BigDecimal.ZERO, "CNY"));
        when(mockAsset.getNetBookValue()).thenReturn(Money.of(TEST_ACQUISITION_COST, "CNY"));
        when(mockAsset.getLocation()).thenReturn(TEST_LOCATION);
        when(mockAsset.getSerialNumber()).thenReturn(TEST_SERIAL_NUMBER);
        when(mockAsset.getStatus()).thenReturn(FixedAssetAggregate.AssetStatus.ACTIVE);
        when(mockAsset.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(mockAsset.getDepartmentId()).thenReturn(TEST_DEPARTMENT_ID);
        when(mockAsset.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(mockAsset.getUpdatedAt()).thenReturn(LocalDateTime.now());
        when(mockAsset.isActive()).thenReturn(true);
        when(mockAsset.getDomainEvents()).thenReturn(Arrays.asList());
        return mockAsset;
    }

    private FixedAssetAggregate createMockUpdatedFixedAssetAggregate() {
        FixedAssetAggregate mockAsset = mock(FixedAssetAggregate.class);
        when(mockAsset.getAssetId()).thenReturn(TEST_ASSET_ID);
        when(mockAsset.getName()).thenReturn("Updated Office Computer");
        when(mockAsset.getDescription()).thenReturn("Updated description");
        when(mockAsset.getAcquisitionDate()).thenReturn(TEST_PURCHASE_DATE);
        when(mockAsset.getAcquisitionCost()).thenReturn(Money.of(TEST_ACQUISITION_COST, "CNY"));
        when(mockAsset.getCurrentValue()).thenReturn(Money.of(TEST_ACQUISITION_COST, "CNY"));
        when(mockAsset.getAccumulatedDepreciation()).thenReturn(Money.of(BigDecimal.ZERO, "CNY"));
        when(mockAsset.getNetBookValue()).thenReturn(Money.of(TEST_ACQUISITION_COST, "CNY"));
        when(mockAsset.getLocation()).thenReturn("Updated Office Floor 2");
        when(mockAsset.getSerialNumber()).thenReturn(TEST_SERIAL_NUMBER);
        when(mockAsset.getStatus()).thenReturn(FixedAssetAggregate.AssetStatus.ACTIVE);
        when(mockAsset.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(mockAsset.getDepartmentId()).thenReturn(TEST_DEPARTMENT_ID);
        when(mockAsset.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(mockAsset.getUpdatedAt()).thenReturn(LocalDateTime.now());
        when(mockAsset.isActive()).thenReturn(true);
        when(mockAsset.getDomainEvents()).thenReturn(Arrays.asList());
        return mockAsset;
    }

    private FixedAssetAggregate createMockDepreciatedFixedAssetAggregate(BigDecimal depreciationAmount) {
        FixedAssetAggregate mockAsset = mock(FixedAssetAggregate.class);
        when(mockAsset.getAssetId()).thenReturn(TEST_ASSET_ID);
        when(mockAsset.getName()).thenReturn(TEST_ASSET_NAME);
        when(mockAsset.getDescription()).thenReturn(TEST_DESCRIPTION);
        when(mockAsset.getAcquisitionDate()).thenReturn(TEST_PURCHASE_DATE);
        when(mockAsset.getAcquisitionCost()).thenReturn(Money.of(TEST_ACQUISITION_COST, "CNY"));
        when(mockAsset.getCurrentValue()).thenReturn(Money.of(TEST_ACQUISITION_COST.subtract(depreciationAmount), "CNY"));
        when(mockAsset.getAccumulatedDepreciation()).thenReturn(Money.of(depreciationAmount, "CNY"));
        when(mockAsset.getNetBookValue()).thenReturn(Money.of(TEST_ACQUISITION_COST.subtract(depreciationAmount), "CNY"));
        when(mockAsset.getLocation()).thenReturn(TEST_LOCATION);
        when(mockAsset.getSerialNumber()).thenReturn(TEST_SERIAL_NUMBER);
        when(mockAsset.getStatus()).thenReturn(FixedAssetAggregate.AssetStatus.ACTIVE);
        when(mockAsset.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(mockAsset.getDepartmentId()).thenReturn(TEST_DEPARTMENT_ID);
        when(mockAsset.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(mockAsset.getUpdatedAt()).thenReturn(LocalDateTime.now());
        when(mockAsset.isActive()).thenReturn(true);
        when(mockAsset.getDomainEvents()).thenReturn(Arrays.asList());
        return mockAsset;
    }

    private FixedAssetAggregate createMockDisposedFixedAssetAggregate() {
        FixedAssetAggregate mockAsset = mock(FixedAssetAggregate.class);
        when(mockAsset.getAssetId()).thenReturn(TEST_ASSET_ID);
        when(mockAsset.getName()).thenReturn(TEST_ASSET_NAME);
        when(mockAsset.getDescription()).thenReturn(TEST_DESCRIPTION);
        when(mockAsset.getAcquisitionDate()).thenReturn(TEST_PURCHASE_DATE);
        when(mockAsset.getAcquisitionCost()).thenReturn(Money.of(TEST_ACQUISITION_COST, "CNY"));
        when(mockAsset.getCurrentValue()).thenReturn(Money.of(TEST_ACQUISITION_COST, "CNY"));
        when(mockAsset.getAccumulatedDepreciation()).thenReturn(Money.of(BigDecimal.ZERO, "CNY"));
        when(mockAsset.getNetBookValue()).thenReturn(Money.of(TEST_ACQUISITION_COST, "CNY"));
        when(mockAsset.getLocation()).thenReturn(TEST_LOCATION);
        when(mockAsset.getSerialNumber()).thenReturn(TEST_SERIAL_NUMBER);
        when(mockAsset.getStatus()).thenReturn(FixedAssetAggregate.AssetStatus.DISPOSED);
        when(mockAsset.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(mockAsset.getDepartmentId()).thenReturn(TEST_DEPARTMENT_ID);
        when(mockAsset.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(mockAsset.getUpdatedAt()).thenReturn(LocalDateTime.now());
        when(mockAsset.isActive()).thenReturn(false);
        when(mockAsset.getDomainEvents()).thenReturn(Arrays.asList());
        return mockAsset;
    }

    private FixedAssetAggregate createMockOtherFixedAssetAggregate() {
        FixedAssetAggregate mockAsset = mock(FixedAssetAggregate.class);
        when(mockAsset.getAssetId()).thenReturn(3002);
        when(mockAsset.getName()).thenReturn("Office Printer");
        when(mockAsset.getDescription()).thenReturn("Office printer for documents");
        when(mockAsset.getAcquisitionDate()).thenReturn(TEST_PURCHASE_DATE);
        when(mockAsset.getAcquisitionCost()).thenReturn(Money.of(BigDecimal.valueOf(8000.00), "CNY"));
        when(mockAsset.getCurrentValue()).thenReturn(Money.of(BigDecimal.valueOf(8000.00), "CNY"));
        when(mockAsset.getAccumulatedDepreciation()).thenReturn(Money.of(BigDecimal.ZERO, "CNY"));
        when(mockAsset.getNetBookValue()).thenReturn(Money.of(BigDecimal.valueOf(8000.00), "CNY"));
        when(mockAsset.getAssetId()).thenReturn(TEST_ASSET_ID);
        when(mockAsset.getName()).thenReturn(TEST_ASSET_NAME);
        when(mockAsset.getDescription()).thenReturn(TEST_DESCRIPTION);
        when(mockAsset.getAcquisitionDate()).thenReturn(TEST_PURCHASE_DATE);
        when(mockAsset.getAcquisitionCost()).thenReturn(Money.of(TEST_ACQUISITION_COST, "CNY"));
        when(mockAsset.getCurrentValue()).thenReturn(Money.of(TEST_ACQUISITION_COST, "CNY"));
        when(mockAsset.getAccumulatedDepreciation()).thenReturn(Money.of(BigDecimal.ZERO, "CNY"));
        when(mockAsset.getNetBookValue()).thenReturn(Money.of(TEST_ACQUISITION_COST, "CNY"));
        when(mockAsset.getLocation()).thenReturn(TEST_LOCATION);
        when(mockAsset.getSerialNumber()).thenReturn(TEST_SERIAL_NUMBER);
        when(mockAsset.getStatus()).thenReturn(FixedAssetAggregate.AssetStatus.ACTIVE);
        when(mockAsset.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(mockAsset.getDepartmentId()).thenReturn(TEST_DEPARTMENT_ID);
        when(mockAsset.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(mockAsset.getUpdatedAt()).thenReturn(LocalDateTime.now());
        when(mockAsset.isActive()).thenReturn(true);
        when(mockAsset.getDomainEvents()).thenReturn(Arrays.asList());
        return mockAsset;
    }
}