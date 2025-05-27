// backend/src/main/java/org/example/backend/util/TenantUtils.java
package org.example.backend.util;

import org.example.backend.tenant.TenantContext;

/**
 * 租户工具类
 */
public class TenantUtils {
    
    /**
     * 验证对象是否属于当前租户
     */
    public static void validateTenantAccess(Integer objectTenantId) {
        Integer currentTenant = TenantContext.getCurrentTenant();
        
        if (currentTenant == null) {
            throw new IllegalStateException("未设置租户上下文");
        }
        
        if (objectTenantId == null || !currentTenant.equals(objectTenantId)) {
            throw new SecurityException("无权访问其他租户的数据");
        }
    }
    
    /**
     * 安全地获取当前租户ID
     */
    public static Integer getCurrentTenantSafely() {
        Integer tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("未设置租户上下文");
        }
        return tenantId;
    }
    
    /**
     * 检查是否为多租户环境
     */
    public static boolean isMultiTenantEnabled() {
        return true; // 在实际项目中，这个值可能来自配置
    }
    
    /**
     * 格式化租户相关的日志信息
     */
    public static String formatTenantLog(String message) {
        Integer tenantId = TenantContext.getCurrentTenant();
        return String.format("[Tenant:%s] %s", tenantId != null ? tenantId : "UNKNOWN", message);
    }
}