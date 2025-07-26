// backend/src/test/java/org/example/backend/domain/aggregate/fixedasset/FixedAssetAggregateTest.java - 修复版本
package org.example.backend.domain.aggregate.fixedasset;

import org.example.backend.domain.aggregate.AggregateTestBase;
import org.example.backend.domain.event.FixedAssetCreatedEvent;
import org.example.backend.domain.valueobject.Money;
import org.example.backend.domain.valueobject.TenantId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FixedAssetAggregate - 修复版本
 */
public class FixedAssetAggregateTest extends AggregateTestBase {
    
    @Test
    @DisplayName("Should create fixed asset successfully")
    void shouldCreateFixedAssetSuccessfully() {
        // Given
        String assetName = "Office Computer";
        String description = "Dell Desktop Computer";
        Money acquisitionCost = createMoney("5000.00");
        LocalDate acquisitionDate = TEST_DATE;
        TenantId tenantId = createTestTenantId();
        Integer departmentId = 100;
        
        // When
        FixedAssetAggregate asset = FixedAssetAggregate.create(
            assetName, description, acquisitionCost, acquisitionDate, tenantId, departmentId
        );
        
        // Then
        assertNotNull(asset);
        assertEquals(assetName, asset.getName());
        assertEquals(description, asset.getDescription());
        assertEquals(acquisitionCost, asset.getAcquisitionCost());
        assertEquals(acquisitionDate, asset.getAcquisitionDate());
        assertEquals(tenantId, asset.getTenantId());
        assertEquals(departmentId, asset.getDepartmentId());
        assertEquals(FixedAssetAggregate.AssetStatus.ACTIVE, asset.getStatus());
        assertEquals(acquisitionCost, asset.getCurrentValue());
        assertEquals(Money.zero(TEST_CURRENCY), asset.getAccumulatedDepreciation());
        
        // Verify audit fields
        assertValidCreationTime(asset.getCreatedAt());
        
        // Verify domain event
        assertEventPublished(asset.getDomainEvents(), FixedAssetCreatedEvent.class);
    }
    
    @Test
    @DisplayName("Should update asset info successfully")
    void shouldUpdateAssetInfoSuccessfully() {
        // Given
        FixedAssetAggregate asset = createTestAsset();
        String newName = "Updated Computer";
        String newDescription = "Updated computer with SSD upgrade";
        String newLocation = "Office Floor 2";
        
        // When
        asset.updateAssetInfo(newName, newDescription, newLocation);
        
        // Then
        assertEquals(newName, asset.getName());
        assertEquals(newDescription, asset.getDescription());
        assertEquals(newLocation, asset.getLocation());
    }
    
    @Test
    @DisplayName("Should record depreciation successfully")
    void shouldRecordDepreciationSuccessfully() {
        // Given
        FixedAssetAggregate asset = createTestAsset();
        Money depreciationAmount = createMoney("1000.00");
        
        // When
        asset.recordDepreciation(depreciationAmount);
        
        // Then
        assertEquals(depreciationAmount, asset.getAccumulatedDepreciation());
        assertEquals(createMoney("4000.00"), asset.getCurrentValue());
    }
    
    @Test
    @DisplayName("Should calculate net book value correctly")
    void shouldCalculateNetBookValueCorrectly() {
        // Given
        FixedAssetAggregate asset = createTestAsset();
        asset.recordDepreciation(createMoney("2000.00"));
        
        // When
        Money netBookValue = asset.getNetBookValue();
        
        // Then
        assertEquals(createMoney("3000.00"), netBookValue);
    }
    
    @Test
    @DisplayName("Should dispose asset successfully")
    void shouldDisposeAssetSuccessfully() {
        // Given
        FixedAssetAggregate asset = createTestAsset();
        Money disposalAmount = createMoney("2000.00");
        String reason = "Sold to another company";
        
        // When
        asset.dispose(disposalAmount, reason);
        
        // Then
        assertEquals(FixedAssetAggregate.AssetStatus.DISPOSED, asset.getStatus());
        assertNotNull(asset.getDisposedAt());
    }
    
    @Test
    @DisplayName("Should check if asset is active correctly")
    void shouldCheckIfAssetIsActiveCorrectly() {
        // Given
        FixedAssetAggregate asset = createTestAsset();
        
        // When & Then
        assertTrue(asset.isActive());
        
        asset.dispose(createMoney("1000.00"), "Test disposal");
        assertFalse(asset.isActive());
    }
    
    // Helper method to create test fixed asset
    private FixedAssetAggregate createTestAsset() {
        return FixedAssetAggregate.create(
            "Test Computer",
            "Dell Desktop Computer",
            createMoney("5000.00"),
            TEST_DATE,
            createTestTenantId(),
            100
        );
    }
}