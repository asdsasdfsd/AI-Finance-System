// backend/src/main/java/org/example/backend/infrastructure/config/DDDConfiguration.java
package org.example.backend.infrastructure.config;

import org.example.backend.domain.event.DomainEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * DDD Infrastructure Configuration
 * 
 * Configures DDD-specific beans and infrastructure components
 */
@Configuration
public class DDDConfiguration {
    
    /**
     * Domain Event Publisher bean
     */
    @Bean
    public DomainEventPublisher domainEventPublisher() {
        return new DomainEventPublisher();
    }
    
    /**
     * Enable DDD repositories when DDD mode is active
     */
    @Configuration
    @Profile("ddd")
    static class DDDRepositoryConfiguration {
        // DDD-specific repository configurations
    }
    
    /**
     * Legacy mode configuration
     */
    @Configuration
    @Profile("!ddd")
    static class LegacyConfiguration {
        // Legacy configurations when DDD is disabled
    }
}

