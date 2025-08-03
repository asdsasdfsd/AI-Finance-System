// backend/src/test/java/org/example/backend/test/BaseServiceTest.java
package org.example.backend.test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Base test class for all service tests
 * 
 * Provides common configuration to avoid Mockito strictness issues
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public abstract class BaseServiceTest {
    
    // Common test utilities can be added here
    
}