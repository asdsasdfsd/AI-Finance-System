// backend/src/main/java/org/example/backend/domain/event/FixedAssetDepreciatedEvent.java
package org.example.backend.domain.event;

import org.example.backend.domain.valueobject.Money;

/**
 * 固定资产折旧事件
 */
public class FixedAssetDepreciatedEvent extends DomainEvent {
    private final Integer assetId;
    private final Money depreciationAmount;
    private final Money newCurrentValue;
    private final Integer companyId;
    
    public FixedAssetDepreciatedEvent(Integer assetId, Money depreciationAmount, 
                                    Money newCurrentValue, Integer companyId) {
        super();
        this.assetId = assetId;
        this.depreciationAmount = depreciationAmount;
        this.newCurrentValue = newCurrentValue;
        this.companyId = companyId;
    }
    
    public Integer getAssetId() {
        return assetId;
    }
    
    public Money getDepreciationAmount() {
        return depreciationAmount;
    }
    
    public Money getNewCurrentValue() {
        return newCurrentValue;
    }
    
    public Integer getCompanyId() {
        return companyId;
    }
    
    @Override
    public String toString() {
        return String.format("FixedAssetDepreciatedEvent{assetId=%d, depreciation=%s, newValue=%s, companyId=%d}", 
                           assetId, depreciationAmount, newCurrentValue, companyId);
    }
}

