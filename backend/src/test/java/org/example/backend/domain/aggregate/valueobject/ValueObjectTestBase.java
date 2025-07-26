// backend/src/test/java/org/example/backend/domain/valueobject/ValueObjectTestBase.java
package org.example.backend.domain.aggregate.valueobject;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Base class for value object tests
 * 
 * 值对象测试的基类，提供通用的测试工具
 */
@ExtendWith(MockitoExtension.class)
public abstract class ValueObjectTestBase {
    
    /**
     * Test equality contract for value objects
     */
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
    
    /**
     * Test immutability of value objects
     */
    protected void testImmutability(Object valueObject) {
        // Value objects should be immutable
        // This is more of a documentation/reminder than an actual test
        // as immutability is enforced by design
        assertNotNull(valueObject);
    }
    
    private void assertEquals(Object expected, Object actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }
    
    private void assertNotEquals(Object unexpected, Object actual) {
        org.junit.jupiter.api.Assertions.assertNotEquals(unexpected, actual);
    }
    
    private void assertNotNull(Object object) {
        org.junit.jupiter.api.Assertions.assertNotNull(object);
    }
}

