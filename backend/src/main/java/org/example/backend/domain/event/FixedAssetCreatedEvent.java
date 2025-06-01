// backend/src/main/java/org/example/backend/domain/event/FixedAssetCreatedEvent.java
package org.example.backend.domain.event;

import org.example.backend.domain.valueobject.Money;

/**
 * 固定资产创建事件
 */
public class FixedAssetCreatedEvent extends DomainEvent {
    private final Integer assetId;
    private final String assetName;
    private final Money acquisitionCost;
    private final Integer companyId;
    
    public FixedAssetCreatedEvent(Integer assetId, String assetName, Money acquisitionCost, Integer companyId) {
        super();
        this.assetId = assetId;
        this.assetName = assetName;
        this.acquisitionCost = acquisitionCost;
        this.companyId = companyId;
    }
    
    public Integer getAssetId() {
        return assetId;
    }
    
    public String getAssetName() {
        return assetName;
    }
    
    public Money getAcquisitionCost() {
        return acquisitionCost;
    }
    
    public Integer getCompanyId() {
        return companyId;
    }
    
    @Override
    public String toString() {
        return String.format("FixedAssetCreatedEvent{assetId=%d, assetName=%s, cost=%s, companyId=%d}", 
                           assetId, assetName, acquisitionCost, companyId);
    }
}

