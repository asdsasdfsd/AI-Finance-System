// backend/src/test/java/org/example/backend/domain/valueobject/ValueObjectTestBase.java
package org.example.backend.domain.valueobject;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Base class for value object unit tests
 * 
 * Provides common utilities and factory methods for value object testing
 */
@ExtendWith(MockitoExtension.class)
public abstract class ValueObjectTestBase {

    // Common test constants
    protected static final String DEFAULT_CURRENCY = "CNY";
    protected static final String USD_CURRENCY = "USD";
    protected static final String EUR_CURRENCY = "EUR";
    protected static final Integer DEFAULT_TENANT_ID = 1;
    protected static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("100.00");
    
    // Factory methods for common value objects
    protected TenantId createDefaultTenantId() {
        return TenantId.of(DEFAULT_TENANT_ID);
    }
    
    protected TenantId createTenantId(Integer id) {
        return TenantId.of(id);
    }
    
    protected Money createDefaultMoney() {
        return Money.of(DEFAULT_AMOUNT, DEFAULT_CURRENCY);
    }
    
    protected Money createMoney(String amount) {
        return Money.of(new BigDecimal(amount), DEFAULT_CURRENCY);
    }
    
    protected Money createMoney(String amount, String currency) {
        return Money.of(new BigDecimal(amount), currency);
    }
    
    protected Money createMoney(BigDecimal amount, String currency) {
        return Money.of(amount, currency);
    }
    
    protected Money createZeroMoney() {
        return Money.zero(DEFAULT_CURRENCY);
    }
    
    protected Money createZeroMoney(String currency) {
        return Money.zero(currency);
    }
    
    protected TransactionStatus createDraftStatus() {
        return TransactionStatus.draft();
    }
    
    protected TransactionStatus createApprovedStatus() {
        return TransactionStatus.of(TransactionStatus.Status.APPROVED);
    }
    
    protected TransactionStatus createPendingStatus() {
        return TransactionStatus.of(TransactionStatus.Status.PENDING_APPROVAL);
    }
    
    // Validation helper methods
    protected void assertValidCurrency(String currencyCode) {
        try {
            Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException e) {
            throw new AssertionError("Invalid currency code: " + currencyCode, e);
        }
    }
    
    /**
     * Generic equality contract test for value objects
     * Tests reflexive, symmetric, transitive properties and null handling
     */
    protected <T> void assertEqualsContract(T object1, T object2, T object3) {
        // Reflexive: x.equals(x) should return true
        assertEquals(object1, object1);
        
        // Symmetric: x.equals(y) should return the same as y.equals(x)
        assertEquals(object1, object2);
        assertEquals(object2, object1);
        
        // Transitive: if x.equals(y) and y.equals(z), then x.equals(z)
        assertEquals(object2, object3);
        assertEquals(object1, object3);
        
        // Consistent hash codes
        assertEquals(object1.hashCode(), object2.hashCode());
        assertEquals(object2.hashCode(), object3.hashCode());
        
        // Null handling
        assertNotEquals(object1, null);
    }
    
    /**
     * Generic immutability test for value objects
     * Verifies that operations return new instances rather than modifying existing ones
     */
    protected <T> void assertImmutability(T original, T modified) {
        assertNotSame(original, modified);
        // Original should remain unchanged (specific checks depend on value object type)
    }
    
    /**
     * Test that value object properly implements toString
     */
    protected void assertMeaningfulToString(Object valueObject, String... expectedContents) {
        String toString = valueObject.toString();
        assertNotNull(toString);
        assertFalse(toString.trim().isEmpty());
        
        for (String expectedContent : expectedContents) {
            assertTrue(toString.contains(expectedContent), 
                "toString should contain: " + expectedContent + ", but was: " + toString);
        }
    }
    
    // Import static assertions for convenience
    protected static void assertEquals(Object expected, Object actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }
    
    protected static void assertNotEquals(Object unexpected, Object actual) {
        org.junit.jupiter.api.Assertions.assertNotEquals(unexpected, actual);
    }
    
    protected static void assertNotNull(Object object) {
        org.junit.jupiter.api.Assertions.assertNotNull(object);
    }
    
    protected static void assertTrue(boolean condition) {
        org.junit.jupiter.api.Assertions.assertTrue(condition);
    }
    
    protected static void assertTrue(boolean condition, String message) {
        org.junit.jupiter.api.Assertions.assertTrue(condition, message);
    }
    
    protected static void assertFalse(boolean condition) {
        org.junit.jupiter.api.Assertions.assertFalse(condition);
    }
    
    protected static void assertNotSame(Object unexpected, Object actual) {
        org.junit.jupiter.api.Assertions.assertNotSame(unexpected, actual);
    }
    
    protected static <T extends Throwable> T assertThrows(Class<T> expectedType, 
                                                          org.junit.jupiter.api.function.Executable executable) {
        return org.junit.jupiter.api.Assertions.assertThrows(expectedType, executable);
    }
    
    protected static void assertDoesNotThrow(org.junit.jupiter.api.function.Executable executable) {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(executable);
    }
}