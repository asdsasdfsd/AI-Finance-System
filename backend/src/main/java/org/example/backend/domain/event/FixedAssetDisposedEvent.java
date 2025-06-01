// backend/src/main/java/org/example/backend/domain/event/FixedAssetDisposedEvent.java
package org.example.backend.domain.event;

import org.example.backend.domain.valueobject.Money;

/**
 * 固定资产处置事件
 */
public class FixedAssetDisposedEvent extends DomainEvent {
    private final Integer assetId;
    private final Money disposalAmount;
    private final String reason;
    private final Integer companyId;
    
    public FixedAssetDisposedEvent(Integer assetId, Money disposalAmount, String reason, Integer companyId) {
        super();
        this.assetId = assetId;
        this.disposalAmount = disposalAmount;
        this.reason = reason;
        this.companyId = companyId;
    }
    
    public Integer getAssetId() {
        return assetId;
    }
    
    public Money getDisposalAmount() {
        return disposalAmount;
    }
    
    public String getReason() {
        return reason;
    }
    
    public Integer getCompanyId() {
        return companyId;
    }
    
    @Override
    public String toString() {
        return String.format("FixedAssetDisposedEvent{assetId=%d, disposalAmount=%s, reason=%s, companyId=%d}", 
                           assetId, disposalAmount, reason, companyId);
    }
}