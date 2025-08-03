// backend/src/test/java/org/example/backend/application/service/FixedAssetApplicationServiceTest.java
package org.example.backend.application.service;

import org.example.backend.application.dto.CreateFixedAssetCommand;
import org.example.backend.application.dto.UpdateFixedAssetCommand;
import org.example.backend.application.dto.FixedAssetDTO;
import org.example.backend.domain.aggregate.fixedasset.FixedAssetAggregate;
import org.example.backend.domain.aggregate.fixedasset.FixedAssetAggregateRepository;
import org.example.backend.domain.event.DomainEventPublisher;
import org.example.backend.domain.valueobject.Money;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FixedAssetApplicationService - Fixed Version
 */
@ExtendWith(MockitoExtension.class)
class FixedAssetApplicationServiceTest {

    @Mock
    private FixedAssetAggregateRepository fixedAssetRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private FixedAssetApplicationService fixedAssetApplicationService;

    // Test constants
    private static final Integer TEST_ASSET_ID = 1001;
    private static final Integer TEST_COMPANY_ID = 1;
    private static final String TEST_ASSET_NAME = "Test Asset";
    private static final String TEST_ASSET_TYPE = "Equipment";
    private static final String TEST_CURRENCY = "CNY";
    private static final BigDecimal TEST_COST = BigDecimal.valueOf(50000.00);
    private static final LocalDate TEST_PURCHASE_DATE = LocalDate.of(2024, 1, 15);

    private FixedAssetAggregate testFixedAsset;

    @BeforeEach
    void setUp() {
        testFixedAsset = createMockFixedAsset();
    }

    // ========== Create Fixed Asset Tests ==========

    @Test
    @DisplayName("Should create fixed asset successfully")
    void shouldCreateFixedAssetSuccessfully() {
        // Given
        CreateFixedAssetCommand command = createValidCreateCommand();
        when(fixedAssetRepository.save(any(FixedAssetAggregate.class))).thenReturn(testFixedAsset);

        // When
        FixedAssetDTO result = fixedAssetApplicationService.createFixedAsset(command);

        // Then
        assertNotNull(result);
        assertEquals(testFixedAsset.getAssetId(), result.getAssetId());
        assertEquals(testFixedAsset.getName(), result.getName());
        
        verify(fixedAssetRepository).save(any(FixedAssetAggregate.class));
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    @DisplayName("Should throw exception when create command is null")
    void shouldThrowExceptionWhenCreateCommandIsNull() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            fixedAssetApplicationService.createFixedAsset(null);
        });
    }

    @Test
    @DisplayName("Should throw exception when asset name is null")
    void shouldThrowExceptionWhenAssetNameIsNull() {
        // Given
        CreateFixedAssetCommand command = CreateFixedAssetCommand.builder()
                .name(null)
                .description("Test description")
                .purchaseCost(TEST_COST)
                .currency(TEST_CURRENCY)
                .purchaseDate(TEST_PURCHASE_DATE)
                .companyId(TEST_COMPANY_ID)
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            fixedAssetApplicationService.createFixedAsset(command);
        });
    }

    // ========== Update Fixed Asset Tests ==========

    @Test
    @DisplayName("Should update fixed asset successfully")
    void shouldUpdateFixedAssetSuccessfully() {
        // Given
        UpdateFixedAssetCommand command = createValidUpdateCommand();
        when(fixedAssetRepository.findByIdAndTenantId(TEST_ASSET_ID, TEST_COMPANY_ID))
                .thenReturn(Optional.of(testFixedAsset));
        when(fixedAssetRepository.save(any(FixedAssetAggregate.class))).thenReturn(testFixedAsset);

        // When
        FixedAssetDTO result = fixedAssetApplicationService.updateFixedAsset(TEST_ASSET_ID, command);

        // Then
        assertNotNull(result);
        verify(fixedAssetRepository).findByIdAndTenantId(TEST_ASSET_ID, TEST_COMPANY_ID);(TEST_COMPANY_ID));
        verify(fixedAssetRepository).save(testFixedAsset);
    }

    @Test
    @DisplayName("Should throw exception when fixed asset not found for update")
    void shouldThrowExceptionWhenFixedAssetNotFoundForUpdate() {
        // Given
        UpdateFixedAssetCommand command = createValidUpdateCommand();
        when(fixedAssetRepository.findByAssetIdAndTenantId(TEST_ASSET_ID, TenantId.of(TEST_COMPANY_ID)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            fixedAssetApplicationService.updateFixedAsset(TEST_ASSET_ID, command);
        });
    }

    // ========== Depreciate Fixed Asset Tests ==========

    @Test
    @DisplayName("Should depreciate fixed asset successfully")
    void shouldDepreciateFixedAssetSuccessfully() {
        // Given
        BigDecimal depreciationAmount = BigDecimal.valueOf(5000.00);
        when(fixedAssetRepository.findByAssetIdAndTenantId(TEST_ASSET_ID, TenantId.of(TEST_COMPANY_ID)))
                .thenReturn(Optional.of(testFixedAsset));
        when(fixedAssetRepository.save(any(FixedAssetAggregate.class))).thenReturn(testFixedAsset);

        // When
        FixedAssetDTO result = fixedAssetApplicationService.depreciateAsset(TEST_ASSET_ID, TEST_COMPANY_ID, depreciationAmount);

        // Then
        assertNotNull(result);
        verify(fixedAssetRepository).findByAssetIdAndTenantId(TEST_ASSET_ID, TenantId.of(TEST_COMPANY_ID));
        verify(testFixedAsset).recordDepreciation(any(Money.class));
        verify(fixedAssetRepository).save(testFixedAsset);
    }

    @Test
    @DisplayName("Should throw exception when fixed asset not found for depreciation")
    void shouldThrowExceptionWhenFixedAssetNotFoundForDepreciation() {
        // Given
        BigDecimal depreciationAmount = BigDecimal.valueOf(5000.00);
        when(fixedAssetRepository.findByAssetIdAndTenantId(TEST_ASSET_ID, TenantId.of(TEST_COMPANY_ID)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            fixedAssetApplicationService.depreciateAsset(TEST_ASSET_ID, TEST_COMPANY_ID, depreciationAmount);
        });
    }

    // ========== Dispose Fixed Asset Tests ==========

    @Test
    @DisplayName("Should dispose fixed asset successfully")
    void shouldDisposeFixedAssetSuccessfully() {
        // Given
        Money disposalAmount = Money.of(BigDecimal.valueOf(10000.00), TEST_CURRENCY);
        String reason = "Asset sold";
        when(fixedAssetRepository.findByAssetIdAndTenantId(TEST_ASSET_ID, TenantId.of(TEST_COMPANY_ID)))
                .thenReturn(Optional.of(testFixedAsset));
        when(fixedAssetRepository.save(any(FixedAssetAggregate.class))).thenReturn(testFixedAsset);

        // When
        FixedAssetDTO result = fixedAssetApplicationService.disposeAsset(TEST_ASSET_ID, TEST_COMPANY_ID, disposalAmount, reason);

        // Then
        assertNotNull(result);
        verify(fixedAssetRepository).findByAssetIdAndTenantId(TEST_ASSET_ID, TenantId.of(TEST_COMPANY_ID));
        verify(testFixedAsset).dispose(disposalAmount, reason);
        verify(fixedAssetRepository).save(testFixedAsset);
    }

    @Test
    @DisplayName("Should throw exception when fixed asset not found for disposal")
    void shouldThrowExceptionWhenFixedAssetNotFoundForDisposal() {
        // Given
        BigDecimal disposalAmount = BigDecimal.valueOf(10000.00);
        String reason = "Asset sold";
        when(fixedAssetRepository.findByIdAndTenantId(TEST_ASSET_ID, TEST_COMPANY_ID))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            fixedAssetApplicationService.disposeAsset(TEST_ASSET_ID, TEST_COMPANY_ID, disposalAmount, reason);
        });
    }

    // ========== Query Tests ==========

    @Test
    @DisplayName("Should get fixed asset by ID successfully")
    void shouldGetFixedAssetByIdSuccessfully() {
        // Given
        when(fixedAssetRepository.findByIdAndTenantId(TEST_ASSET_ID, TEST_COMPANY_ID))
                .thenReturn(Optional.of(testFixedAsset));

        // When
        FixedAssetDTO result = fixedAssetApplicationService.getFixedAssetById(TEST_ASSET_ID, TEST_COMPANY_ID);

        // Then
        assertNotNull(result);
        assertEquals(testFixedAsset.getAssetId(), result.getAssetId());
        verify(fixedAssetRepository).findByIdAndTenantId(TEST_ASSET_ID, TEST_COMPANY_ID);
    }

    @Test
    @DisplayName("Should throw exception when fixed asset not found by ID")
    void shouldThrowExceptionWhenFixedAssetNotFoundById() {
        // Given
        when(fixedAssetRepository.findByIdAndTenantId(TEST_ASSET_ID, TEST_COMPANY_ID))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            fixedAssetApplicationService.getFixedAssetById(TEST_ASSET_ID, TEST_COMPANY_ID);
        });
    }

    @Test
    @DisplayName("Should get fixed assets by company successfully")
    void shouldGetFixedAssetsByCompanySuccessfully() {
        // Given
        List<FixedAssetAggregate> assets = List.of(testFixedAsset);
        when(fixedAssetRepository.findByTenantId(TenantId.of(TEST_COMPANY_ID)))
                .thenReturn(assets);

        // When
        List<FixedAssetDTO> result = fixedAssetApplicationService.getFixedAssetsByCompany(TEST_COMPANY_ID);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(fixedAssetRepository).findByTenantId(TenantId.of(TEST_COMPANY_ID));
    }

    // ========== Helper Methods ==========

    private CreateFixedAssetCommand createValidCreateCommand() {
        return CreateFixedAssetCommand.builder()
                .name(TEST_ASSET_NAME)
                .description("Test asset description")
                .acquisitionCost(TEST_COST)
                .acquisitionDate(TEST_PURCHASE_DATE)
                .companyId(TEST_COMPANY_ID)
                .location("Test location")
                .serialNumber("SN123456")
                .build();
    }

    private UpdateFixedAssetCommand createValidUpdateCommand() {
        return UpdateFixedAssetCommand.builder()
                .name("Updated Asset Name")
                .description("Updated description")
                .location("Updated location")
                .companyId(TEST_COMPANY_ID)
                .build();
    }

    private FixedAssetAggregate createMockFixedAsset() {
        FixedAssetAggregate asset = mock(FixedAssetAggregate.class);
        
        when(asset.getAssetId()).thenReturn(TEST_ASSET_ID);
        when(asset.getName()).thenReturn(TEST_ASSET_NAME);
        when(asset.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(asset.getDomainEvents()).thenReturn(new ArrayList<>());
        
        // Mock behavior methods
        doNothing().when(asset).recordDepreciation(any(Money.class));
        doNothing().when(asset).dispose(any(Money.class), anyString());
        doNothing().when(asset).clearDomainEvents();
        
        return asset;
    }
}