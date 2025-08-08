// backend/src/test/java/org/example/backend/BackendApplicationTests.java
package org.example.backend;

import org.example.backend.domain.event.config.DomainEventTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * Integration test for Spring Boot application context loading
 * 
 * Fixed Issues:
 * 1. Uses enhanced DomainEventTestConfig with all required beans
 * 2. Uses NONE web environment to avoid web context loading issues
 * 3. Uses test profile with proper H2 database configuration
 * 4. Enhanced DomainEventTestConfig now includes OpenAI and other missing beans
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@ContextConfiguration(classes = {BackendApplication.class, DomainEventTestConfig.class})
class BackendApplicationTests {

    @Test
    void contextLoads() {
        // This test ensures that the Spring application context loads successfully
        // with all beans properly configured and wired together.
        // Using test profile and enhanced DomainEventTestConfig to avoid dependency issues.
    }
}
