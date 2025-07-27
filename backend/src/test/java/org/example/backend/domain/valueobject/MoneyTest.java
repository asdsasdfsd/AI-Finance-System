// backend/src/test/java/org/example/backend/domain/valueobject/MoneyTest.java
package org.example.backend.domain.valueobject;

import org.example.backend.domain.valueobject.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Money value object
 */
class MoneyTest extends ValueObjectTestBase {
    
    @Test
    @DisplayName("Should create money with valid amount and currency")
    void shouldCreateMoneySuccessfully() {
        // Given
        BigDecimal amount = new BigDecimal("100.50");
        String currency = "USD";
        
        // When
        Money money = Money.of(amount, currency);
        
        // Then
        assertEquals(amount, money.getAmount());
        assertEquals(currency, money.getCurrencyCode());
    }
    
    @Test
    @DisplayName("Should create zero money")
    void shouldCreateZeroMoney() {
        // Given
        String currency = "CNY";
        
        // When
        Money money = Money.zero(currency);
        
        // Then
        // Use compareTo for BigDecimal comparison to handle precision differences
        assertTrue(money.getAmount().compareTo(BigDecimal.ZERO) == 0);
        assertEquals(currency, money.getCurrencyCode());
    }
    
    @Test
    @DisplayName("Should add money with same currency")
    void shouldAddMoneyWithSameCurrency() {
        // Given
        Money money1 = Money.of(new BigDecimal("100.00"), "USD");
        Money money2 = Money.of(new BigDecimal("50.00"), "USD");
        
        // When
        Money result = money1.add(money2);
        
        // Then
        assertEquals(new BigDecimal("150.00"), result.getAmount());
        assertEquals("USD", result.getCurrencyCode());
    }
    
    @Test
    @DisplayName("Should check if money is positive")
    void shouldCheckIfMoneyIsPositive() {
        // Given
        Money positiveMoney = Money.of(new BigDecimal("100.00"), "USD");
        Money zeroMoney = Money.zero("USD");
        Money negativeMoney = Money.of(new BigDecimal("-50.00"), "USD");
        
        // When & Then
        assertTrue(positiveMoney.isPositive());
        assertFalse(zeroMoney.isPositive());
        assertFalse(negativeMoney.isPositive());
    }
    
    @Test
    @DisplayName("Should have correct equality behavior")
    void shouldHaveCorrectEqualityBehavior() {
        // Given
        Money money1 = Money.of(new BigDecimal("100.00"), "USD");
        Money money2 = Money.of(new BigDecimal("100.00"), "USD");
        Money differentMoney = Money.of(new BigDecimal("200.00"), "USD");
        
        // When & Then
        testEqualityContract(money1, money2, differentMoney);
    }

    protected void testEqualityContract(Object obj1, Object obj2, Object differentObj) {
        // Reflexive
        assertEquals(obj1, obj1);
        
        // Symmetric
        assertEquals(obj1, obj2);
        assertEquals(obj2, obj1);
        
        // Not equal to different object
        assertNotEquals(obj1, differentObj);
        assertNotEquals(obj1, null);
        
        // Hash code consistency
        assertEquals(obj1.hashCode(), obj2.hashCode());
    }
}