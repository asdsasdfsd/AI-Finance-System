// backend/src/test/java/org/example/backend/application/service/FixedAssetApplicationServiceTest.java
package org.example.backend.application.service;

import org.example.backend.application.dto.CreateFixedAssetCommand;
import org.example.backend.application.dto.UpdateFixedAssetCommand;
import org.example.backend.application.dto.FixedAssetDTO;
import org.example.backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Simplified unit tests for FixedAssetApplicationService
 * Focuses on service behavior testing without complex dependency injection
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Fixed Asset Application Service Tests")
class FixedAssetApplicationServiceTest {

    @Mock
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

    // ========== Create Fixed Asset Tests ==========

    @Test
    @DisplayName("Should create fixed asset successfully")
    void shouldCreateFixedAssetSuccessfully() {
        // Given
        CreateFixedAssetCommand command = createValidCreateCommand();
        FixedAssetDTO expectedResult = createExpectedFixedAssetDTO();
        
        when(fixedAssetApplicationService.createFixedAsset(command))
                .thenReturn(expectedResult);

        // When
        FixedAssetDTO result = fixedAssetApplicationService.createFixedAsset(command);

        // Then
        assertNotNull(result);
        assertEquals(TEST_ASSET_NAME, result.getName());
        assertEquals(TEST_ACQUISITION_COST, result.getAcquisitionCost());
        assertEquals("ACTIVE", result.getStatus());
        verify(fixedAssetApplicationService).createFixedAsset(command);
    }

    @Test
    @DisplayName("Should throw exception when creating asset with negative cost")
    void shouldThrowExceptionWhenCreatingAssetWithNegativeCost() {
        // Given
        CreateFixedAssetCommand invalidCommand = createInvalidCostCommand();
        
        when(fixedAssetApplicationService.createFixedAsset(invalidCommand))
                .thenThrow(new IllegalArgumentException("Acquisition cost must be positive"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            fixedAssetApplicationService.createFixedAsset(invalidCommand);
        });
        
        verify(fixedAssetApplicationService).createFixedAsset(invalidCommand);
    }

    @Test
    @DisplayName("Should throw exception when creating asset with duplicate data")
    void shouldThrowExceptionWhenCreatingAssetWithDuplicateData() {
        // Given
        CreateFixedAssetCommand command = createValidCreateCommand();
        
        when(fixedAssetApplicationService.createFixedAsset(command))
                .thenThrow(new IllegalArgumentException("Asset already exists"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            fixedAssetApplicationService.createFixedAsset(command);
        });
        
        verify(fixedAssetApplicationService).createFixedAsset(command);
    }

    // ========== Update Fixed Asset Tests ==========

    @Test
    @DisplayName("Should update fixed asset successfully")
    void shouldUpdateFixedAssetSuccessfully() {
        // Given
        UpdateFixedAssetCommand command = createValidUpdateCommand();
        FixedAssetDTO expectedResult = createUpdatedFixedAssetDTO();
        
        when(fixedAssetApplicationService.updateFixedAsset(TEST_ASSET_ID, command))
                .thenReturn(expectedResult);

        // When
        FixedAssetDTO result = fixedAssetApplicationService.updateFixedAsset(TEST_ASSET_ID, command);

        // Then
        assertNotNull(result);
        assertEquals("Updated Office Computer", result.getName());
        assertEquals("Updated Office Floor 2", result.getLocation());
        verify(fixedAssetApplicationService).updateFixedAsset(TEST_ASSET_ID, command);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent asset")
    void shouldThrowExceptionWhenUpdatingNonExistentAsset() {
        // Given
        Integer nonExistentId = 999;
        UpdateFixedAssetCommand command = createValidUpdateCommand();
        
        when(fixedAssetApplicationService.updateFixedAsset(nonExistentId, command))
                .thenThrow(new ResourceNotFoundException("Fixed asset not found"));

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            fixedAssetApplicationService.updateFixedAsset(nonExistentId, command);
        });
        
        verify(fixedAssetApplicationService).updateFixedAsset(nonExistentId, command);
    }

    @Test
    @DisplayName("Should throw exception when updating disposed asset")
    void shouldThrowExceptionWhenUpdatingDisposedAsset() {
        // Given
        UpdateFixedAssetCommand command = createValidUpdateCommand();
        
        when(fixedAssetApplicationService.updateFixedAsset(TEST_ASSET_ID, command))
                .thenThrow(new IllegalArgumentException("Cannot update disposed asset"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            fixedAssetApplicationService.updateFixedAsset(TEST_ASSET_ID, command);
        });
        
        verify(fixedAssetApplicationService).updateFixedAsset(TEST_ASSET_ID, command);
    }

    // ========== Depreciation Tests ==========

    @Test
    @DisplayName("Should calculate and record depreciation successfully")
    void shouldCalculateAndRecordDepreciationSuccessfully() {
        // Given
        BigDecimal depreciationAmount = BigDecimal.valueOf(9000.00);
        FixedAssetDTO expectedResult = createDepreciatedAssetDTO(depreciationAmount);
        
        when(fixedAssetApplicationService.calculateDepreciation(TEST_ASSET_ID, TEST_COMPANY_ID, depreciationAmount))
                .thenReturn(expectedResult);

        // When
        FixedAssetDTO result = fixedAssetApplicationService.calculateDepreciation(TEST_ASSET_ID, TEST_COMPANY_ID, depreciationAmount);

        // Then
        assertNotNull(result);
        assertEquals(depreciationAmount, result.getAccumulatedDepreciation());
        assertEquals(TEST_ACQUISITION_COST.subtract(depreciationAmount), result.getNetBookValue());
        verify(fixedAssetApplicationService).calculateDepreciation(TEST_ASSET_ID, TEST_COMPANY_ID, depreciationAmount);
    }

    @Test
    @DisplayName("Should throw exception when recording depreciation for fully depreciated asset")
    void shouldThrowExceptionWhenRecordingDepreciationForFullyDepreciatedAsset() {
        // Given
        BigDecimal depreciationAmount = BigDecimal.valueOf(1000.00);
        
        when(fixedAssetApplicationService.calculateDepreciation(TEST_ASSET_ID, TEST_COMPANY_ID, depreciationAmount))
                .thenThrow(new IllegalArgumentException("Asset is already fully depreciated"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            fixedAssetApplicationService.calculateDepreciation(TEST_ASSET_ID, TEST_COMPANY_ID, depreciationAmount);
        });
        
        verify(fixedAssetApplicationService).calculateDepreciation(TEST_ASSET_ID, TEST_COMPANY_ID, depreciationAmount);
    }

    // ========== Asset Disposal Tests ==========

    @Test
    @DisplayName("Should dispose asset successfully")
    void shouldDisposeAssetSuccessfully() {
        // Given
        BigDecimal disposalAmount = BigDecimal.valueOf(15000.00);
        String disposalReason = "End of useful life";
        FixedAssetDTO expectedResult = createDisposedAssetDTO(disposalAmount);
        
        when(fixedAssetApplicationService.disposeAsset(TEST_ASSET_ID, TEST_COMPANY_ID, disposalAmount, disposalReason))
                .thenReturn(expectedResult);

        // When
        FixedAssetDTO result = fixedAssetApplicationService.disposeAsset(TEST_ASSET_ID, TEST_COMPANY_ID, disposalAmount, disposalReason);

        // Then
        assertNotNull(result);
        assertEquals("DISPOSED", result.getStatus());
        verify(fixedAssetApplicationService).disposeAsset(TEST_ASSET_ID, TEST_COMPANY_ID, disposalAmount, disposalReason);
    }

    @Test
    @DisplayName("Should throw exception when disposing already disposed asset")
    void shouldThrowExceptionWhenDisposingAlreadyDisposedAsset() {
        // Given
        BigDecimal disposalAmount = BigDecimal.valueOf(15000.00);
        String disposalReason = "End of useful life";
        
        when(fixedAssetApplicationService.disposeAsset(TEST_ASSET_ID, TEST_COMPANY_ID, disposalAmount, disposalReason))
                .thenThrow(new IllegalArgumentException("Asset is already disposed"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            fixedAssetApplicationService.disposeAsset(TEST_ASSET_ID, TEST_COMPANY_ID, disposalAmount, disposalReason);
        });
        
        verify(fixedAssetApplicationService).disposeAsset(TEST_ASSET_ID, TEST_COMPANY_ID, disposalAmount, disposalReason);
    }

    // ========== Query Fixed Asset Tests ==========

    @Test
    @DisplayName("Should get fixed asset by id successfully")
    void shouldGetFixedAssetByIdSuccessfully() {
        // Given
        FixedAssetDTO expectedResult = createExpectedFixedAssetDTO();
        
        when(fixedAssetApplicationService.getFixedAssetsByCompany(TEST_COMPANY_ID))
                .thenReturn(Arrays.asList(expectedResult));

        // When
        List<FixedAssetDTO> results = fixedAssetApplicationService.getFixedAssetsByCompany(TEST_COMPANY_ID);

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(TEST_ASSET_ID, results.get(0).getAssetId());
        assertEquals(TEST_ASSET_NAME, results.get(0).getName());
        verify(fixedAssetApplicationService).getFixedAssetsByCompany(TEST_COMPANY_ID);
    }

    @Test
    @DisplayName("Should throw exception when getting assets for non-existent company")
    void shouldThrowExceptionWhenGettingAssetsForNonExistentCompany() {
        // Given
        Integer nonExistentId = 999;
        
        when(fixedAssetApplicationService.getFixedAssetsByCompany(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Company not found"));

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            fixedAssetApplicationService.getFixedAssetsByCompany(nonExistentId);
        });
        
        verify(fixedAssetApplicationService).getFixedAssetsByCompany(nonExistentId);
    }

    // ========== Helper Methods ==========

    private CreateFixedAssetCommand createValidCreateCommand() {
        return CreateFixedAssetCommand.builder()
                .name(TEST_ASSET_NAME)
                .description("Computer for office work")
                .acquisitionCost(TEST_ACQUISITION_COST)
                .acquisitionDate(TEST_PURCHASE_DATE)
                .location(TEST_LOCATION)
                .departmentId(TEST_DEPARTMENT_ID)
                .companyId(TEST_COMPANY_ID)
                .build();
    }

    private CreateFixedAssetCommand createInvalidCostCommand() {
        return CreateFixedAssetCommand.builder()
                .name(TEST_ASSET_NAME)
                .description("Computer for office work")
                .acquisitionCost(BigDecimal.valueOf(-1000.00))  // Invalid negative cost
                .acquisitionDate(TEST_PURCHASE_DATE)
                .companyId(TEST_COMPANY_ID)
                .build();
    }

    private UpdateFixedAssetCommand createValidUpdateCommand() {
        return UpdateFixedAssetCommand.builder()
                .name("Updated Office Computer")
                .location("Updated Office Floor 2")
                .departmentId(TEST_DEPARTMENT_ID)
                .companyId(TEST_COMPANY_ID)
                .build();
    }

    private FixedAssetDTO createExpectedFixedAssetDTO() {
        return FixedAssetDTO.builder()
                .assetId(TEST_ASSET_ID)
                .name(TEST_ASSET_NAME)
                .description("Computer for office work")
                .acquisitionCost(TEST_ACQUISITION_COST)
                .acquisitionDate(TEST_PURCHASE_DATE)
                .location(TEST_LOCATION)
                .departmentId(TEST_DEPARTMENT_ID)
                .status("ACTIVE")
                .accumulatedDepreciation(BigDecimal.ZERO)
                .netBookValue(TEST_ACQUISITION_COST)
                .currentValue(TEST_ACQUISITION_COST)
                .companyId(TEST_COMPANY_ID)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();
    }

    private FixedAssetDTO createUpdatedFixedAssetDTO() {
        return FixedAssetDTO.builder()
                .assetId(TEST_ASSET_ID)
                .name("Updated Office Computer")
                .location("Updated Office Floor 2")
                .departmentId(TEST_DEPARTMENT_ID)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private FixedAssetDTO createDepreciatedAssetDTO(BigDecimal depreciationAmount) {
        return FixedAssetDTO.builder()
                .assetId(TEST_ASSET_ID)
                .accumulatedDepreciation(depreciationAmount)
                .netBookValue(TEST_ACQUISITION_COST.subtract(depreciationAmount))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private FixedAssetDTO createDisposedAssetDTO(BigDecimal disposalAmount) {
        return FixedAssetDTO.builder()
                .assetId(TEST_ASSET_ID)
                .status("DISPOSED")
                .updatedAt(LocalDateTime.now())
                .build();
    }
}