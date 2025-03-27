// backend/src/main/java/org/example/backend/config/ConfigurationChecker.java
package org.example.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Component that checks critical configuration values at application startup
 * and logs warnings if any required configurations are missing
 */
@Component
public class ConfigurationChecker {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationChecker.class);
    
    @Value("${jwt.secret:}")
    private String jwtSecret;
    
    @Value("${sso.client-id:}")
    private String ssoClientId;
    
    @Value("${sso.client-secret:}")
    private String ssoClientSecret;
    
    /**
     * Check all critical configuration values when the application starts
     */
    @EventListener(ApplicationStartedEvent.class)
    public void checkConfigurations() {
        checkJwtSecret();
        checkSsoConfigurations();
    }
    
    /**
     * Check JWT secret key configuration
     */
    private void checkJwtSecret() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty() || "${JWT_SECRET}".equals(jwtSecret)) {
            logger.warn("⚠️ JWT_SECRET environment variable is not set! Using a default secret which is insecure.");
            logger.warn("Please set JWT_SECRET environment variable for secure JWT token generation.");
        }
    }
    
    /**
     * Check Microsoft SSO configurations
     */
    private void checkSsoConfigurations() {
        boolean ssoConfigured = true;
        
        if (ssoClientId == null || ssoClientId.trim().isEmpty() || "${SSO_CLIENT_ID}".equals(ssoClientId)) {
            logger.warn("⚠️ SSO_CLIENT_ID environment variable is not set! Microsoft SSO login will not work.");
            ssoConfigured = false;
        }
        
        if (ssoClientSecret == null || ssoClientSecret.trim().isEmpty() || "${SSO_CLIENT_SECRET}".equals(ssoClientSecret)) {
            logger.warn("⚠️ SSO_CLIENT_SECRET environment variable is not set! Microsoft SSO login will not work.");
            ssoConfigured = false;
        }
        
        if (ssoConfigured) {
            logger.info("✅ Microsoft SSO configuration detected. SSO login functionality should be available.");
        } else {
            logger.warn("❌ Microsoft SSO is not properly configured. Users will not be able to login with Microsoft accounts.");
            logger.warn("To enable Microsoft SSO, please set the SSO_CLIENT_ID and SSO_CLIENT_SECRET environment variables.");
        }
    }
}