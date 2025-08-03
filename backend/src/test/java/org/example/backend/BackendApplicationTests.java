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
 * 1. Added @ActiveProfiles("test") to use test configuration
 * 2. Uses test profile to avoid production database dependencies
 * 3. Should work with H2 in-memory database configured in test profile
 * 4. Uses existing DomainEventTestConfig for mock beans
 */
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {BackendApplication.class, DomainEventTestConfig.class})
class BackendApplicationTests {

    @Test
    void contextLoads() {
        // This test ensures that the Spring application context loads successfully
        // with all beans properly configured and wired together.
        // Using test profile to avoid database connection issues.
    }
}