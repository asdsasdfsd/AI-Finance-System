// backend/src/main/java/org/example/backend/infrastructure/config/DDDServiceRegistry.java
package org.example.backend.infrastructure.config;

import org.example.backend.application.service.TransactionApplicationService;
import org.example.backend.application.service.CompanyApplicationService;
import org.example.backend.application.service.UserApplicationService;
import org.example.backend.service.*;
import org.example.backend.infrastructure.web.TransactionControllerAdapter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * DDD Service Registry Configuration
 * 
 * Manages the transition from ORM-based services to DDD application services
 * allowing gradual migration with feature flags
 */
@Configuration
public class DDDServiceRegistry {
    
    /**
     * Enable DDD services via property: ddd.enabled=true
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "ddd.enabled", havingValue = "true", matchIfMissing = false)
    public TransactionServiceFacade dddTransactionService(TransactionApplicationService transactionApplicationService,
                                                         TransactionService legacyTransactionService) {
        return new DddTransactionServiceFacade(transactionApplicationService, legacyTransactionService);
    }
    
    /**
     * Fallback to legacy services when DDD is disabled
     */
    @Bean
    @ConditionalOnProperty(name = "ddd.enabled", havingValue = "false", matchIfMissing = true)
    public TransactionServiceFacade legacyTransactionService(TransactionService transactionService) {
        return new LegacyTransactionServiceFacade(transactionService);
    }
    
    /**
     * Company service facade
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "ddd.enabled", havingValue = "true", matchIfMissing = false)
    public CompanyServiceFacade dddCompanyService(CompanyApplicationService companyApplicationService,
                                                 CompanyService legacyCompanyService) {
        return new DddCompanyServiceFacade(companyApplicationService, legacyCompanyService);
    }
    
    @Bean
    @ConditionalOnProperty(name = "ddd.enabled", havingValue = "false", matchIfMissing = true)
    public CompanyServiceFacade legacyCompanyService(CompanyService companyService) {
        return new LegacyCompanyServiceFacade(companyService);
    }
}

/**
 * Transaction Service Facade Interface
 */
interface TransactionServiceFacade {
    // Add common interface methods that both legacy and DDD implementations support
}

/**
 * DDD Transaction Service Facade
 */
class DddTransactionServiceFacade implements TransactionServiceFacade {
    private final TransactionApplicationService dddService;
    private final TransactionService legacyService;
    
    public DddTransactionServiceFacade(TransactionApplicationService dddService, 
                                     TransactionService legacyService) {
        this.dddService = dddService;
        this.legacyService = legacyService;
    }
    
    // Implement facade methods that delegate to DDD service
    // Fall back to legacy service for operations not yet migrated
}

/**
 * Legacy Transaction Service Facade
 */
class LegacyTransactionServiceFacade implements TransactionServiceFacade {
    private final TransactionService legacyService;
    
    public LegacyTransactionServiceFacade(TransactionService legacyService) {
        this.legacyService = legacyService;
    }
    
    // Implement facade methods that delegate to legacy service
}

/**
 * Company Service Facade Interface
 */
interface CompanyServiceFacade {
    // Add common interface methods
}

/**
 * DDD Company Service Facade
 */
class DddCompanyServiceFacade implements CompanyServiceFacade {
    private final CompanyApplicationService dddService;
    private final CompanyService legacyService;
    
    public DddCompanyServiceFacade(CompanyApplicationService dddService, 
                                 CompanyService legacyService) {
        this.dddService = dddService;
        this.legacyService = legacyService;
    }
}

/**
 * Legacy Company Service Facade
 */
class LegacyCompanyServiceFacade implements CompanyServiceFacade {
    private final CompanyService legacyService;
    
    public LegacyCompanyServiceFacade(CompanyService legacyService) {
        this.legacyService = legacyService;
    }
}