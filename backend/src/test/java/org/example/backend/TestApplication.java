// backend/src/test/java/org/example/backend/TestApplication.java
package org.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;

/**
 * Test Application for Spring Boot Tests
 * 
 * 提供测试环境的特殊配置
 */
@TestConfiguration
@SpringBootApplication
@Profile("test")
public class TestApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}

