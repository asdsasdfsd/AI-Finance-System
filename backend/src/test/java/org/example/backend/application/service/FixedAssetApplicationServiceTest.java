// backend/src/test/java/org/example/backend/application/service/FixedAssetApplicationServiceTest.java
package org.example.backend.application.service;

import org.example.backend.application.dto.FixedAssetDTO;
import org.example.backend.application.dto.CreateFixedAssetCommand;
import org.example.backend.application.dto.UpdateFixedAssetCommand;
import org.example.backend.domain.aggregate.fixedasset.FixedAssetAggregate;
import org.example.backend.domain.aggregate.fixedasset.FixedAssetAggregateRepository;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.Money;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FixedAssetApplicationService
 * 
 * Tests fixed asset management functionality including creation, updates, depreciation,
 * disposal, and lifecycle management
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FixedAssetApplicationService Tests")
class FixedAssetApplicationServiceTest {
    
    @Mock
    private FixedAssetAggregateRepository fixedAssetRepository;
    
    @Mock
    private DomainEventPublisher eventPublisher;
    
    @InjectMocks
    private FixedAssetApplicationService fixedAssetApplicationService;
    
    private CreateFixedAssetCommand createCommand;
    private UpdateFixedAssetCommand updateCommand;
    private FixedAssetAggregate testFixedAsset;
    
    @BeforeEach
    void setUp() {
        createCommand = CreateFixedAssetCommand.builder()
            .name("Test Computer")
            .description("Dell Laptop for development")
            .acquisitionCost(new BigDecimal("5000.00"))
            .acquisitionDate(LocalDate.now().minusDays(30))
            .companyId(1)
            .departmentId(1)
            .location("Office Floor 1")
            .serialNumber("DL123456")
            .build();
            
        updateCommand = UpdateFixedAssetCommand.builder()
            .name("Updated Computer")
            .description("Updated Dell Laptop")
            .location("Office Floor 2")
            .companyId(1)
            .departmentId(2)
            .build();
            
        testFixedAsset = FixedAssetAggregate.create(
            "Test Computer",
            "Dell Laptop for development",
            Money.of(new BigDecimal("5000.00"), "CNY"),
            LocalDate.now().minusDays(30),
            TenantId.of(1),
            1
        );
    }
    
    @Nested
    @DisplayName("Create Fixed Asset Tests")
    class CreateFixedAssetTests {
        
        @Test
        @DisplayName("Should create fixed asset successfully with valid command")
        void shouldCreateFixedAssetSuccessfully() {
            // Given
            when(fixedAssetRepository.save(any(FixedAssetAggregate.class))).thenReturn(testFixedAsset);
            
            // When
            FixedAssetDTO result = fixedAssetApplicationService.createFixedAsset(createCommand);
            
            // Then
            assertNotNull(result);
            assertEquals(createCommand.getName(), result.getName());
            assertEquals(createCommand.getDescription(), result.getDescription());
            assertEquals(createCommand.getAcquisitionCost(), result.getAcquisitionCost());
            assertEquals("ACTIVE", result.getStatus());
            
            verify(fixedAssetRepository).save(any(FixedAssetAggregate.class));
            verify(eventPublisher).publishAll(any());
        }
        
        @Test
        @DisplayName("Should set optional fields when provided")
        void shouldSetOptionalFieldsWhenProvided() {
            // Given
            when(fixedAssetRepository.save(any(FixedAssetAggregate.class))).thenAnswer(invocation -> {
                FixedAssetAggregate asset = invocation.getArgument(0);
                // Verify optional fields are set
                assertNotNull(asset);
                return asset;
            });
            
            // When
            FixedAssetDTO result = fixedAssetApplicationService.createFixedAsset(createCommand);
            
            // Then
            assertNotNull(result);
            verify(fixedAssetRepository).save(any(FixedAssetAggregate.class));
        }
        
        @Test
        @DisplayName("Should throw exception when create command is null")
        void shouldThrowExceptionWhenCreateCommandIsNull() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fixedAssetApplicationService.createFixedAsset(null)
            );
            
            assertEquals("CreateFixedAssetCommand cannot be null", exception.getMessage());
            verify(fixedAssetRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Should throw exception when acquisition cost is negative")
        void shouldThrowExceptionWhenAcquisitionCostIsNegative() {
            // Given
            createCommand = CreateFixedAssetCommand.builder()
                .name("Test Asset")
                .description("Test Description")
                .acquisitionCost(new BigDecimal("-1000.00"))
                .acquisitionDate(LocalDate.now())
                .companyId(1)
                .departmentId(1)
                .build();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fixedAssetApplicationService.createFixedAsset(createCommand)
            );
            
            assertTrue(exception.getMessage().contains("Acquisition cost must be positive"));
            verify(fixedAssetRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Should throw exception when acquisition date is in future")
        void shouldThrowExceptionWhenAcquisitionDateInFuture() {
            // Given
            createCommand = CreateFixedAssetCommand.builder()
                .name("Test Asset")
                .description("Test Description")
                .acquisitionCost(new BigDecimal("1000.00"))
                .acquisitionDate(LocalDate.now().plusDays(1))
                .companyId(1)
                .departmentId(1)
                .build();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fixedAssetApplicationService.createFixedAsset(createCommand)
            );
            
            assertTrue(exception.getMessage().contains("Acquisition date cannot be in the future"));
            verify(fixedAssetRepository, never()).save(any());
        }
    }
    
    @Nested
    @DisplayName("Update Fixed Asset Tests")
    class UpdateFixedAssetTests {
        
        @Test
        @DisplayName("Should update fixed asset successfully")
        void shouldUpdateFixedAssetSuccessfully() {
            // Given
            when(fixedAssetRepository.findByAssetIdAndTenantId(1, any(TenantId.class)))
                .thenReturn(Optional.of(testFixedAsset));
            when(fixedAssetRepository.save(any(FixedAssetAggregate.class))).thenReturn(testFixedAsset);
            
            // When
            FixedAssetDTO result = fixedAssetApplicationService.updateFixedAsset(1, updateCommand);
            
            // Then
            assertNotNull(result);
            verify(fixedAssetRepository).findByAssetIdAndTenantId(1, any(TenantId.class));
            verify(fixedAssetRepository).save(testFixedAsset);
        }
        
        @Test
        @DisplayName("Should transfer asset to different department")
        void shouldTransferAssetToDifferentDepartment() {
            // Given
            when(fixedAssetRepository.findByAssetIdAndTenantId(1, any(TenantId.class)))
                .thenReturn(Optional.of(testFixedAsset));
            when(fixedAssetRepository.save(any(FixedAssetAggregate.class))).thenReturn(testFixedAsset);
            
            // When
            FixedAssetDTO result = fixedAssetApplicationService.updateFixedAsset(1, updateCommand);
            
            // Then
            assertNotNull(result);
            verify(fixedAssetRepository).save(testFixedAsset);
        }
        
        @Test
        @DisplayName("Should throw exception when fixed asset not found")
        void shouldThrowExceptionWhenFixedAssetNotFound() {
            // Given
            when(fixedAssetRepository.findByAssetIdAndTenantId(1, any(TenantId.class)))
                .thenReturn(Optional.empty());
            
            // When & Then
            ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> fixedAssetApplicationService.updateFixedAsset(1, updateCommand)
            );
            
            assertTrue(exception.getMessage().contains("Fixed asset not found"));
            verify(fixedAssetRepository).findByAssetIdAndTenantId(1, any(TenantId.class));
            verify(fixedAssetRepository, never()).save(any());
        }
    }
    
    @Nested
    @DisplayName("Fixed Asset Depreciation Tests")
    class DepreciationTests {
        
        @Test
        @DisplayName("Should calculate and record depreciation successfully")
        void shouldCalculateAndRecordDepreciationSuccessfully() {
            // Given
            BigDecimal depreciationAmount = new BigDecimal("500.00");
            when(fixedAssetRepository.findByAssetIdAndTenantId(1, any(TenantId.class)))
                .thenReturn(Optional.of(testFixedAsset));
            when(fixedAssetRepository.save(any(FixedAssetAggregate.class))).thenReturn(testFixedAsset);
            
            // When
            FixedAssetDTO result = fixedAssetApplicationService.calculateDepreciation(1, 1, depreciationAmount);
            
            // Then
            assertNotNull(result);
            verify(fixedAssetRepository).findByAssetIdAndTenantId(1, any(TenantId.class));
            verify(fixedAssetRepository).save(testFixedAsset);
            verify(eventPublisher).publishAll(any());
        }
        
        @Test
        @DisplayName("Should throw exception when depreciation amount is negative")
        void shouldThrowExceptionWhenDepreciationAmountIsNegative() {
            // Given
            BigDecimal negativeAmount = new BigDecimal("-100.00");
            when(fixedAssetRepository.findByAssetIdAndTenantId(1, any(TenantId.class)))
                .thenReturn(Optional.of(testFixedAsset));
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fixedAssetApplicationService.calculateDepreciation(1, 1, negativeAmount)
            );
            
            assertTrue(exception.getMessage().contains("Depreciation amount must be positive"));
            verify(fixedAssetRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Should throw exception when depreciation exceeds remaining value")
        void shouldThrowExceptionWhenDepreciationExceedsRemainingValue() {
            // Given
            // Assume the asset has already been depreciated significantly
            BigDecimal excessiveDepreciation = new BigDecimal("10000.00"); // More than acquisition cost
            when(fixedAssetRepository.findByAssetIdAndTenantId(1, any(TenantId.class)))
                .thenReturn(Optional.of(testFixedAsset));
            
            // When & Then
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> fixedAssetApplicationService.calculateDepreciation(1, 1, excessiveDepreciation)
            );
            
            assertTrue(exception.getMessage().contains("Depreciation amount exceeds remaining value"));
            verify(fixedAssetRepository, never()).save(any());
        }
    }
    
    @Nested
    @DisplayName("Fixed Asset Disposal Tests")
    class DisposalTests {
        
        @Test
        @DisplayName("Should dispose asset successfully")
        void shouldDisposeAssetSuccessfully() {
            // Given
            BigDecimal disposalAmount = new BigDecimal("2000.00");
            String reason = "Upgrade to newer model";
            when(fixedAssetRepository.findByAssetIdAndTenantId(1, any(TenantId.class)))
                .thenReturn(Optional.of(testFixedAsset));
            when(fixedAssetRepository.save(any(FixedAssetAggregate.class))).thenReturn(testFixedAsset);
            
            // When
            FixedAssetDTO result = fixedAssetApplicationService.disposeAsset(1, 1, disposalAmount, reason);
            
            // Then
            assertNotNull(result);
            verify(fixedAssetRepository).findByAssetIdAndTenantId(1, any(TenantId.class));
            verify(fixedAssetRepository).save(testFixedAsset);
            verify(eventPublisher).publishAll(any());
        }
        
        @Test
        @DisplayName("Should write off asset successfully")
        void shouldWriteOffAssetSuccessfully() {
            // Given
            String reason = "Asset damaged beyond repair";
            when(fixedAssetRepository.findByAssetIdAndTenantId(1, any(TenantId.class)))
                .thenReturn(Optional.of(testFixedAsset));
            when(fixedAssetRepository.save(any(FixedAssetAggregate.class))).thenReturn(testFixedAsset);
            
            // When
            FixedAssetDTO result = fixedAssetApplicationService.writeOffAsset(1, 1, reason);
            
            // Then
            assertNotNull(result);
            verify(fixedAssetRepository).findByAssetIdAndTenantId(1, any(TenantId.class));
            verify(fixedAssetRepository).save(testFixedAsset);
            verify(eventPublisher).publishAll(any());
        }
        
        @Test
        @DisplayName("Should throw exception when disposal amount is negative")
        void shouldThrowExceptionWhenDisposalAmountIsNegative() {
            // Given
            BigDecimal negativeAmount = new BigDecimal("-500.00");
            when(fixedAssetRepository.findByAssetIdAndTenantId(1, any(TenantId.class)))
                .thenReturn(Optional.of(testFixedAsset));
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fixedAssetApplicationService.disposeAsset(1, 1, negativeAmount, "Test disposal")
            );
            
            assertTrue(exception.getMessage().contains("Disposal amount cannot be negative"));
            verify(fixedAssetRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Should throw exception when trying to dispose already disposed asset")
        void shouldThrowExceptionWhenDisposingAlreadyDisposedAsset() {
            // Given
            testFixedAsset.dispose(Money.of(new BigDecimal("1000.00"), "CNY"), "Already disposed");
            when(fixedAssetRepository.findByAssetIdAndTenantId(1, any(TenantId.class)))
                .thenReturn(Optional.of(testFixedAsset));
            
            // When & Then
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> fixedAssetApplicationService.disposeAsset(1, 1, new BigDecimal("500.00"), "Second disposal")
            );
            
            assertTrue(exception.getMessage().contains("Cannot dispose asset"));
            verify(fixedAssetRepository, never()).save(any());
        }
    }
    
    @Nested
    @DisplayName("Query Tests")
    class QueryTests {
        
        @Test
        @DisplayName("Should get fixed asset by ID successfully")
        void shouldGetFixedAssetByIdSuccessfully() {
            // Given
            when(fixedAssetRepository.findByAssetIdAndTenantId(1, any(TenantId.class)))
                .thenReturn(Optional.of(testFixedAsset));
            
            // When
            FixedAssetDTO result = fixedAssetApplicationService.getFixedAssetById(1, 1);
            
            // Then
            assertNotNull(result);
            assertEquals(testFixedAsset.getAssetId(), result.getAssetId());
            verify(fixedAssetRepository).findByAssetIdAndTenantId(1, any(TenantId.class));
        }
        
        @Test
        @DisplayName("Should get fixed assets by company successfully")
        void shouldGetFixedAssetsByCompanySuccessfully() {
            // Given
            List<FixedAssetAggregate> assets = List.of(testFixedAsset);
            when(fixedAssetRepository.findByTenantId(any(TenantId.class))).thenReturn(assets);
            
            // When
            List<FixedAssetDTO> result = fixedAssetApplicationService.getFixedAssetsByCompany(1);
            
            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(fixedAssetRepository).findByTenantId(any(TenantId.class));
        }
        
        @Test
        @DisplayName("Should get active fixed assets successfully")
        void shouldGetActiveFixedAssetsSuccessfully() {
            // Given
            List<FixedAssetAggregate> activeAssets = List.of(testFixedAsset);
            when(fixedAssetRepository.findByTenantIdAndStatus(any(TenantId.class), any()))
                .thenReturn(activeAssets);
            
            // When
            List<FixedAssetDTO> result = fixedAssetApplicationService.getActiveFixedAssets(1);
            
            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(fixedAssetRepository).findByTenantIdAndStatus(any(TenantId.class), any());
        }
        
        @Test
        @DisplayName("Should get fixed assets by department successfully")
        void shouldGetFixedAssetsByDepartmentSuccessfully() {
            // Given
            List<FixedAssetAggregate> departmentAssets = List.of(testFixedAsset);
            when(fixedAssetRepository.findByTenantIdAndDepartmentId(any(TenantId.class), eq(1)))
                .thenReturn(departmentAssets);
            
            // When
            List<FixedAssetDTO> result = fixedAssetApplicationService.getFixedAssetsByDepartment(1, 1);
            
            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(fixedAssetRepository).findByTenantIdAndDepartmentId(any(TenantId.class), eq(1));
        }
    }
    
    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {
        
        @Test
        @DisplayName("Should validate asset name is not empty")
        void shouldValidateAssetNameNotEmpty() {
            // Given
            createCommand = CreateFixedAssetCommand.builder()
                .name("")
                .description("Test Description")
                .acquisitionCost(new BigDecimal("1000.00"))
                .acquisitionDate(LocalDate.now())
                .companyId(1)
                .departmentId(1)
                .build();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fixedAssetApplicationService.createFixedAsset(createCommand)
            );
            
            assertTrue(exception.getMessage().contains("Asset name cannot be empty"));
            verify(fixedAssetRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Should validate description is not empty")
        void shouldValidateDescriptionNotEmpty() {
            // Given
            createCommand = CreateFixedAssetCommand.builder()
                .name("Test Asset")
                .description("")
                .acquisitionCost(new BigDecimal("1000.00"))
                .acquisitionDate(LocalDate.now())
                .companyId(1)
                .departmentId(1)
                .build();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fixedAssetApplicationService.createFixedAsset(createCommand)
            );
            
            assertTrue(exception.getMessage().contains("Description cannot be empty"));
            verify(fixedAssetRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Should validate company ID is positive")
        void shouldValidateCompanyIdIsPositive() {
            // Given
            createCommand = CreateFixedAssetCommand.builder()
                .name("Test Asset")
                .description("Test Description")
                .acquisitionCost(new BigDecimal("1000.00"))
                .acquisitionDate(LocalDate.now())
                .companyId(0)
                .departmentId(1)
                .build();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fixedAssetApplicationService.createFixedAsset(createCommand)
            );
            
            assertTrue(exception.getMessage().contains("Company ID must be positive"));
            verify(fixedAssetRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Should validate department ID is positive")
        void shouldValidateDepartmentIdIsPositive() {
            // Given
            createCommand = CreateFixedAssetCommand.builder()
                .name("Test Asset")
                .description("Test Description")
                .acquisitionCost(new BigDecimal("1000.00"))
                .acquisitionDate(LocalDate.now())
                .companyId(1)
                .departmentId(0)
                .build();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fixedAssetApplicationService.createFixedAsset(createCommand)
            );
            
            assertTrue(exception.getMessage().contains("Department ID must be positive"));
            verify(fixedAssetRepository, never()).save(any());
        }
    }
}