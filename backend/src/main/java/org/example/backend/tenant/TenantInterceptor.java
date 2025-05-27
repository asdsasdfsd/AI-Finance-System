// backend/src/main/java/org/example/backend/tenant/TenantInterceptor.java
package org.example.backend.tenant;

import org.example.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 租户拦截器
 * 
 * 从JWT Token中提取租户信息并设置到TenantContext
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // 从请求头中获取JWT token
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                // 从token中提取用户信息和租户信息
                String username = jwtUtil.extractUsername(token);
                
                // 这里需要根据用户名查询用户的租户信息
                // 为了简化，假设从token的claims中直接获取
                // 在实际项目中，可能需要查询数据库
                Integer tenantId = extractTenantFromToken(token);
                
                if (tenantId != null) {
                    TenantContext.setCurrentTenant(tenantId);
                }
            }
            
            return true;
        } catch (Exception e) {
            // 如果提取租户信息失败，继续处理请求
            // 让后续的安全机制处理认证问题
            System.err.println("提取租户信息失败: " + e.getMessage());
            return true;
        }
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                              Object handler, Exception ex) {
        // 清除租户上下文，避免内存泄漏
        TenantContext.clear();
    }
    
    /**
     * 从JWT token中提取租户ID
     * 在实际项目中，这个方法需要根据具体的token结构实现
     */
    private Integer extractTenantFromToken(String token) {
        try {
            // 这里需要根据实际的JWT结构实现
            // 可能需要修改JwtUtil来支持租户信息
            // 目前返回默认值
            return 1; // 临时实现
        } catch (Exception e) {
            return null;
        }
    }
}

