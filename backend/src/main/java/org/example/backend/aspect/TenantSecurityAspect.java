// backend/src/main/java/org/example/backend/aspect/TenantSecurityAspect.java
package org.example.backend.aspect;

import org.example.backend.tenant.TenantContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 租户安全切面
 * 
 * 自动验证方法参数中的租户ID
 */
@Aspect
@Component
public class TenantSecurityAspect {
    
    /**
     * 拦截带有@TenantSecure注解的方法
     */
    @Around("@annotation(tenantSecure)")
    public Object checkTenantAccess(ProceedingJoinPoint joinPoint, TenantSecure tenantSecure) throws Throwable {
        Integer currentTenant = TenantContext.getCurrentTenant();
        
        if (currentTenant == null) {
            throw new SecurityException("未设置租户上下文");
        }
        
        // 检查方法参数中的租户ID
        Object[] args = joinPoint.getArgs();
        String[] paramNames = tenantSecure.paramNames();
        
        for (int i = 0; i < paramNames.length && i < args.length; i++) {
            if (paramNames[i].equals("companyId") || paramNames[i].equals("tenantId")) {
                Integer paramTenantId = (Integer) args[i];
                if (paramTenantId != null && !currentTenant.equals(paramTenantId)) {
                    throw new SecurityException("无权访问其他租户的数据");
                }
            }
        }
        
        return joinPoint.proceed();
    }
}

