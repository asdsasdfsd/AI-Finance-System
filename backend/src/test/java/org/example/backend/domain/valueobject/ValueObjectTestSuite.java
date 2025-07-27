// backend/src/test/java/org/example/backend/domain/valueobject/ValueObjectTestSuite.java
package org.example.backend.domain.valueobject;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.jupiter.api.DisplayName;

/**
 * Test suite for all value object tests
 * 
 * Runs comprehensive tests for all value objects in the domain layer
 */
@Suite
@DisplayName("Value Object Test Suite")
@SelectClasses({
    TenantIdTest.class,
    MoneyTest.class,
    TransactionStatusTest.class,
    CompanyStatusTest.class,
    ReportPeriodTest.class,
    ValueObjectSerializationTest.class
})
public class ValueObjectTestSuite {
    
    /**
     * This test suite ensures comprehensive coverage of all value objects:
     * 
     * 1. TenantIdTest - Tests tenant identification value object
     * 2. MoneyTest - Tests monetary value object with currency support
     * 3. TransactionStatusTest - Tests transaction state management
     * 4. CompanyStatusTest - Tests company operational status
     * 5. ReportPeriodTest - Tests financial reporting period logic
     * 6. ValueObjectSerializationTest - Tests API communication compatibility
     * 
     * Key testing aspects covered:
     * - Object creation and factory methods
     * - Validation and business rules
     * - Equality and hash code contracts
     * - Immutability guarantees
     * - JPA embeddable compatibility
     * - JSON serialization/deserialization
     * - String representation
     * - Edge cases and error handling
     * 
     * Run this suite to verify all value objects meet DDD standards.
     */
    
}