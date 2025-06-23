// backend/src/test/java/org/example/backend/config/TestConfig.java
package org.example.backend.config;

import org.example.backend.domain.event.DomainEventPublisher;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Test configuration for unit and integration tests
 * Provides mock beans and test-specific configurations
 */
@TestConfiguration
@Profile("test")
public class TestConfig {

    /**
     * Mock DomainEventPublisher for testing
     * Prevents actual event publishing during tests
     */
    @Bean
    @Primary
    public DomainEventPublisher mockDomainEventPublisher() {
        return mock(DomainEventPublisher.class);
    }
    
    /**
     * Test data builder utility
     */
    @Bean
    public TestDataBuilder testDataBuilder() {
        return new TestDataBuilder();
    }
}

/**
 * Test data builder utility class
 * Provides factory methods for creating test data
 */
class TestDataBuilder {
    
    public static final Integer DEFAULT_COMPANY_ID = 1;  // 修改为1
    public static final Integer DEFAULT_USER_ID = 1;
    public static final String DEFAULT_CURRENCY = "CNY";
    
    // 简化的测试数据构建方法，避免复杂的依赖
    public static CreateTransactionCommandBuilder defaultCreateTransactionCommand() {
        return new CreateTransactionCommandBuilder()
                .companyId(DEFAULT_COMPANY_ID)
                .amount(new BigDecimal("1000.00"))
                .currency(DEFAULT_CURRENCY)
                .description("Test transaction")
                .transactionDate(LocalDate.now())
                .categoryId(1)
                .departmentId(1);
    }
    
    public static UpdateTransactionCommandBuilder defaultUpdateTransactionCommand() {
        return new UpdateTransactionCommandBuilder()
                .companyId(DEFAULT_COMPANY_ID)
                .amount(new BigDecimal("2000.00"))
                .description("Updated transaction");
    }
    
    // 简化的Builder类，避免复杂依赖
    public static class CreateTransactionCommandBuilder {
        private Integer companyId;
        private BigDecimal amount;
        private String currency;
        private String description;
        private LocalDate transactionDate;
        private Integer categoryId;
        private Integer departmentId;
        
        public CreateTransactionCommandBuilder companyId(Integer companyId) {
            this.companyId = companyId;
            return this;
        }
        
        public CreateTransactionCommandBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }
        
        public CreateTransactionCommandBuilder currency(String currency) {
            this.currency = currency;
            return this;
        }
        
        public CreateTransactionCommandBuilder description(String description) {
            this.description = description;
            return this;
        }
        
        public CreateTransactionCommandBuilder transactionDate(LocalDate transactionDate) {
            this.transactionDate = transactionDate;
            return this;
        }
        
        public CreateTransactionCommandBuilder categoryId(Integer categoryId) {
            this.categoryId = categoryId;
            return this;
        }
        
        public CreateTransactionCommandBuilder departmentId(Integer departmentId) {
            this.departmentId = departmentId;
            return this;
        }
        
        // 构建方法暂时返回Map，避免依赖具体的DTO类
        public java.util.Map<String, Object> build() {
            java.util.Map<String, Object> command = new java.util.HashMap<>();
            command.put("companyId", companyId);
            command.put("amount", amount);
            command.put("currency", currency);
            command.put("description", description);
            command.put("transactionDate", transactionDate);
            command.put("categoryId", categoryId);
            command.put("departmentId", departmentId);
            return command;
        }
    }
    
    public static class UpdateTransactionCommandBuilder {
        private Integer companyId;
        private BigDecimal amount;
        private String description;
        
        public UpdateTransactionCommandBuilder companyId(Integer companyId) {
            this.companyId = companyId;
            return this;
        }
        
        public UpdateTransactionCommandBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }
        
        public UpdateTransactionCommandBuilder description(String description) {
            this.description = description;
            return this;
        }
        
        public java.util.Map<String, Object> build() {
            java.util.Map<String, Object> command = new java.util.HashMap<>();
            command.put("companyId", companyId);
            command.put("amount", amount);
            command.put("description", description);
            return command;
        }
    }
}