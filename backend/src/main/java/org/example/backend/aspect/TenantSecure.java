// backend/src/main/java/org/example/backend/aspect/TenantSecure.java
package org.example.backend.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 租户安全注解
 * 
 * 标记需要租户安全检查的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantSecure {
    
    /**
     * 需要检查的参数名称
     */
    String[] paramNames() default {"companyId"};
    
    /**
     * 是否允许系统管理员访问所有租户数据
     */
    boolean allowSystemAdmin() default false;
}

