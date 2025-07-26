// backend/src/test/java/org/example/backend/domain/aggregate/journalentry/JournalEntryAggregateTest.java - 修复版本
package org.example.backend.domain.aggregate.journalentry;

import org.example.backend.domain.aggregate.AggregateTestBase;
import org.example.backend.domain.event.JournalEntryPostedEvent;
import org.example.backend.domain.valueobject.Money;
import org.example.backend.domain.valueobject.TenantId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JournalEntryAggregate - 修复版本
 */
public class JournalEntryAggregateTest extends AggregateTestBase {
    
    @Test
    @DisplayName("Should create journal entry successfully")
    void shouldCreateJournalEntrySuccessfully() {
        // Given
        TenantId tenantId = createTestTenantId();
        LocalDate entryDate = TEST_DATE;
        String description = "Test journal entry";
        Integer createdBy = TEST_USER_ID;
        
        // When
        JournalEntryAggregate entry = JournalEntryAggregate.create(
            tenantId, entryDate, description, createdBy
        );
        
        // Then
        assertNotNull(entry);
        assertEquals(tenantId, entry.getTenantId());
        assertEquals(entryDate, entry.getEntryDate());
        assertEquals(description, entry.getDescription());
        assertEquals(createdBy, entry.getCreatedBy());
        assertEquals(JournalEntryAggregate.EntryStatus.DRAFT, entry.getStatus());
        assertTrue(entry.getJournalLines().isEmpty());
        
        // Verify audit fields
        assertValidCreationTime(entry.getCreatedAt());
    }
    
    @Test
    @DisplayName("Should add journal line successfully")
    void shouldAddJournalLineSuccessfully() {
        // Given
        JournalEntryAggregate entry = createTestJournalEntry();
        Integer accountId = 1001;
        Money debitAmount = createMoney("500.00");
        Money creditAmount = null;
        String lineDescription = "Cash account debit";
        
        // When
        entry.addJournalLine(accountId, debitAmount, creditAmount, lineDescription);
        
        // Then
        assertEquals(1, entry.getJournalLines().size());
    }
    
    @Test
    @DisplayName("Should post journal entry successfully when balanced")
    void shouldPostJournalEntrySuccessfullyWhenBalanced() {
        // Given
        JournalEntryAggregate entry = createTestJournalEntry();
        
        // Add balanced journal lines
        entry.addJournalLine(1001, createMoney("1000.00"), null, "Cash debit");
        entry.addJournalLine(4001, null, createMoney("1000.00"), "Revenue credit");
        
        // When
        entry.post();
        
        // Then
        assertEquals(JournalEntryAggregate.EntryStatus.POSTED, entry.getStatus());
        assertNotNull(entry.getPostedAt());
        
        // Verify domain event
        assertEventPublished(entry.getDomainEvents(), JournalEntryPostedEvent.class);
    }
    
    @Test
    @DisplayName("Should check if entry is balanced correctly")
    void shouldCheckIfEntryIsBalancedCorrectly() {
        // Given
        JournalEntryAggregate entry = createTestJournalEntry();
        
        // When & Then - Empty entry is balanced (zero = zero)
        assertTrue(entry.isBalanced());
        
        // Add unbalanced lines
        entry.addJournalLine(1001, createMoney("1000.00"), null, "Cash debit");
        assertFalse(entry.isBalanced());
        
        // Balance the entry
        entry.addJournalLine(4001, null, createMoney("1000.00"), "Revenue credit");
        assertTrue(entry.isBalanced());
    }
    
    // Helper method to create test journal entry
    private JournalEntryAggregate createTestJournalEntry() {
        return JournalEntryAggregate.create(
            createTestTenantId(),
            TEST_DATE,
            "Test journal entry",
            TEST_USER_ID
        );
    }
}

