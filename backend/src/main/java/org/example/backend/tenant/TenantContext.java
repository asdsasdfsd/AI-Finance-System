// backend/src/main/java/org/example/backend/tenant/TenantContext.java
package org.example.backend.tenant;

/**
 * 租户上下文
 * 
 * 通过ThreadLocal存储当前请求的租户信息
 */
public class TenantContext {
    
    private static final ThreadLocal<Integer> CURRENT_TENANT = new ThreadLocal<>();
    
    /**
     * 设置当前租户ID
     */
    public static void setCurrentTenant(Integer tenantId) {
        if (tenantId == null || tenantId <= 0) {
            throw new IllegalArgumentException("租户ID必须是正整数");
        }
        CURRENT_TENANT.set(tenantId);
    }
    
    /**
     * 获取当前租户ID
     */
    public static Integer getCurrentTenant() {
        return CURRENT_TENANT.get();
    }
    
    /**
     * 检查是否设置了租户
     */
    public static boolean hasTenant() {
        return CURRENT_TENANT.get() != null;
    }
    
    /**
     * 清除当前租户信息
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
    
    /**
     * 在指定租户上下文中执行操作
     */
    public static <T> T executeInTenant(Integer tenantId, java.util.function.Supplier<T> operation) {
        Integer originalTenant = getCurrentTenant();
        try {
            setCurrentTenant(tenantId);
            return operation.get();
        } finally {
            if (originalTenant != null) {
                setCurrentTenant(originalTenant);
            } else {
                clear();
            }
        }
    }
    
    /**
     * 在指定租户上下文中执行操作（无返回值）
     */
    public static void executeInTenant(Integer tenantId, Runnable operation) {
        executeInTenant(tenantId, () -> {
            operation.run();
            return null;
        });
    }
}

