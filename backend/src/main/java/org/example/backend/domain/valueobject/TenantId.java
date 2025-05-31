
// backend/src/main/java/org/example/backend/domain/valueobject/TenantId.java
package org.example.backend.domain.valueobject;

import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 * 租户ID值对象
 * 
 * 用于多租户数据隔离
 */
@Embeddable
public class TenantId {
    
    private Integer value;
    
    // JPA需要的默认构造函数
    protected TenantId() {
    }
    
    private TenantId(Integer value) {
        validateValue(value);
        this.value = value;
    }
    
    /**
     * 创建租户ID
     */
    public static TenantId of(Integer value) {
        return new TenantId(value);
    }
    
    /**
     * 创建租户ID - 便利方法
     */
    public static TenantId of(int value) {
        return new TenantId(value);
    }
    
    private void validateValue(Integer value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("租户ID必须是正整数");
        }
    }
    
    public Integer getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        TenantId tenantId = (TenantId) obj;
        return Objects.equals(value, tenantId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "TenantId{" + value + '}';
    }
}
