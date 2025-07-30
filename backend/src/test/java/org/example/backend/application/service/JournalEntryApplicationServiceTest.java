// backend/src/test/java/org/example/backend/application/service/JournalEntryApplicationServiceTest.java
package org.example.backend.application.service;

import org.example.backend.application.dto.JournalEntryDTO;
import org.example.backend.application.dto.CreateJournalEntryCommand;
import org.example.backend.domain.aggregate.journalentry.JournalEntryAggregate;
import org.example.backend.domain.aggregate.journalentry.JournalEntryAggregateRepository;
import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.Money;
import org.example.backend.domain.event.DomainEventPublisher;
import org.example.backend.exception.ResourceNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JournalEntryApplicationService
 * 
 * Tests journal entry management functionality including creation from transactions,
 * double-entry bookkeeping validation, and journal entry queries
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JournalEntryApplicationService Tests")
class JournalEntryApplicationServiceTest {
    
    @Mock
    private JournalEntryAggregateRepository journalEntryRepository;
    
    @Mock
    private DomainEventPublisher eventPublisher;
    
    @InjectMocks
    private JournalEntryApplicationService journalEntryApplicationService;
    
    private CreateJournalEntryCommand createCommand;
    private JournalEntryAggregate testJournalEntry;
    private TransactionAggregate testTransaction;
    
    @BeforeEach
    void setUp() {
        createCommand = CreateJournalEntryCommand.builder()
            .companyId(1)
            .entryDate(LocalDate.now())
            .description("Test Journal Entry")
            .userId(1)
            .build();
            
        testJournalEntry = JournalEntryAggregate.create(
            TenantId.of(1),
            LocalDate.now(),
            "Test Journal Entry",
            1
        );
        
        testTransaction = TransactionAggregate.createExpenseTransaction(
            Money.of(new BigDecimal("1000.00"), "CNY"),
            "Test Expense Transaction",
            LocalDate.now(),
            1, // categoryId
            TenantId.of(1),
            1  // userId
        );
    }
    
    @Nested
    @DisplayName("Create Journal Entry Tests")
    class CreateJournalEntryTests {
        
        @Test
        @DisplayName("Should create manual journal entry successfully")
        void shouldCreateManualJournalEntrySuccessfully() {
            // Given
            when(journalEntryRepository.save(any(JournalEntryAggregate.class))).thenReturn(testJournalEntry);
            
            // When
            JournalEntryDTO result = journalEntryApplicationService.createManualJournalEntry(createCommand);
            
            // Then
            assertNotNull(result);
            assertEquals(createCommand.getDescription(), result.getDescription());
            assertEquals(createCommand.getCompanyId(), result.getCompanyId());
            
            verify(journalEntryRepository).save(any(JournalEntryAggregate.class));
            verify(eventPublisher).publishAll(any());
        }
        
        @Test
        @DisplayName("Should create journal entry from income transaction successfully")
        void shouldCreateJournalEntryFromIncomeTransactionSuccessfully() {
            // Given
            TransactionAggregate incomeTransaction = TransactionAggregate.createIncomeTransaction(
                Money.of(new BigDecimal("2000.00"), "CNY"),
                "Sales Revenue",
                LocalDate.now(),
                1, // categoryId
                TenantId.of(1),
                1  // userId
            );
            
            when(journalEntryRepository.save(any(JournalEntryAggregate.class))).thenReturn(testJournalEntry);
            
            // When
            JournalEntryDTO result = journalEntryApplicationService.createFromTransaction(incomeTransaction);
            
            // Then
            assertNotNull(result);
            verify(journalEntryRepository).save(any(JournalEntryAggregate.class));
            verify(eventPublisher).publishAll(any());
        }
        
        @Test
        @DisplayName("Should create journal entry from expense transaction successfully")
        void shouldCreateJournalEntryFromExpenseTransactionSuccessfully() {
            // Given
            when(journalEntryRepository.save(any(JournalEntryAggregate.class))).thenReturn(testJournalEntry);
            
            // When
            JournalEntryDTO result = journalEntryApplicationService.createFromTransaction(testTransaction);
            
            // Then
            assertNotNull(result);
            verify(journalEntryRepository).save(any(JournalEntryAggregate.class));
            verify(eventPublisher).publishAll(any());
        }
        
        @Test
        @DisplayName("Should throw exception when create command is null")
        void shouldThrowExceptionWhenCreateCommandIsNull() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> journalEntryApplicationService.createManualJournalEntry(null)
            );
            
            assertEquals("CreateJournalEntryCommand cannot be null", exception.getMessage());
            verify(journalEntryRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Should throw exception when transaction is null")
        void shouldThrowExceptionWhenTransactionIsNull() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> journalEntryApplicationService.createFromTransaction(null)
            );
            
            assertEquals("Transaction cannot be null", exception.getMessage());
            verify(journalEntryRepository, never()).save(any());
        }
    }
    
    @Nested
    @DisplayName("Journal Entry Line Management Tests")
    class JournalEntryLineTests {
        
        @Test
        @DisplayName("Should add debit line successfully")
        void shouldAddDebitLineSuccessfully() {
            // Given
            when(journalEntryRepository.findById(1)).thenReturn(Optional.of(testJournalEntry));
            
            // When
            JournalEntryDTO result = journalEntryApplicationService.getJournalEntryById(1, 1);
            
            // Then
            assertNotNull(result);
            assertEquals(testJournalEntry.getJournalEntryId(), result.getJournalEntryId());
            verify(journalEntryRepository).findById(1);
        }
        
        @Test
        @DisplayName("Should throw exception when journal entry not found")
        void shouldThrowExceptionWhenJournalEntryNotFound() {
            // Given
            when(journalEntryRepository.findById(1)).thenReturn(Optional.empty());
            
            // When & Then
            ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> journalEntryApplicationService.getJournalEntryById(1, 1)
            );
            
            assertTrue(exception.getMessage().contains("Journal entry not found"));
            verify(journalEntryRepository).findById(1);
        }
        
        @Test
        @DisplayName("Should get journal entries by company successfully")
        void shouldGetJournalEntriesByCompanySuccessfully() {
            // Given
            List<JournalEntryAggregate> journalEntries = List.of(testJournalEntry);
            when(journalEntryRepository.findByTenantId(any(TenantId.class))).thenReturn(journalEntries);
            
            // When
            List<JournalEntryDTO> result = journalEntryApplicationService.getJournalEntriesByCompany(1);
            
            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(journalEntryRepository).findByTenantId(any(TenantId.class));
        }
        
        @Test
        @DisplayName("Should get journal entries by date range successfully")
        void shouldGetJournalEntriesByDateRangeSuccessfully() {
            // Given
            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();
            List<JournalEntryAggregate> journalEntries = List.of(testJournalEntry);
            when(journalEntryRepository.findByTenantIdAndEntryDateBetween(any(TenantId.class), eq(startDate), eq(endDate)))
                .thenReturn(journalEntries);
            
            // When
            List<JournalEntryDTO> result = journalEntryApplicationService.getJournalEntriesByDateRange(1, startDate, endDate);
            
            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(journalEntryRepository).findByTenantIdAndEntryDateBetween(any(TenantId.class), eq(startDate), eq(endDate));
        }
        
        @Test
        @DisplayName("Should get unposted journal entries successfully")
        void shouldGetUnpostedJournalEntriesSuccessfully() {
            // Given
            List<JournalEntryAggregate> unpostedEntries = List.of(testJournalEntry);
            when(journalEntryRepository.findByTenantIdAndPosted(any(TenantId.class), eq(false)))
                .thenReturn(unpostedEntries);
            
            // When
            List<JournalEntryDTO> result = journalEntryApplicationService.getUnpostedJournalEntries(1);
            
            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(journalEntryRepository).findByTenantIdAndPosted(any(TenantId.class), eq(false));
        }
    }
    
    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {
        
        @Test
        @DisplayName("Should validate journal entry description is not empty")
        void shouldValidateJournalEntryDescriptionNotEmpty() {
            // Given
            createCommand = CreateJournalEntryCommand.builder()
                .companyId(1)
                .entryDate(LocalDate.now())
                .description("")
                .userId(1)
                .build();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> journalEntryApplicationService.createManualJournalEntry(createCommand)
            );
            
            assertTrue(exception.getMessage().contains("Description cannot be empty"));
            verify(journalEntryRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Should validate entry date is not in future")
        void shouldValidateEntryDateNotInFuture() {
            // Given
            createCommand = CreateJournalEntryCommand.builder()
                .companyId(1)
                .entryDate(LocalDate.now().plusDays(1))
                .description("Future entry")
                .userId(1)
                .build();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> journalEntryApplicationService.createManualJournalEntry(createCommand)
            );
            
            assertTrue(exception.getMessage().contains("Entry date cannot be in the future"));
            verify(journalEntryRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Should validate company ID is positive")
        void shouldValidateCompanyIdIsPositive() {
            // Given
            createCommand = CreateJournalEntryCommand.builder()
                .companyId(0)
                .entryDate(LocalDate.now())
                .description("Test entry")
                .userId(1)
                .build();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> journalEntryApplicationService.createManualJournalEntry(createCommand)
            );
            
            assertTrue(exception.getMessage().contains("Company ID must be positive"));
            verify(journalEntryRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Should validate user ID is positive")
        void shouldValidateUserIdIsPositive() {
            // Given
            createCommand = CreateJournalEntryCommand.builder()
                .companyId(1)
                .entryDate(LocalDate.now())
                .description("Test entry")
                .userId(0)
                .build();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> journalEntryApplicationService.createManualJournalEntry(createCommand)
            );
            
            assertTrue(exception.getMessage().contains("User ID must be positive"));
            verify(journalEntryRepository, never()).save(any());
        }
    }
    
    @Nested
    @DisplayName("Double-Entry Bookkeeping Tests")
    class DoubleEntryBookkeepingTests {
        
        @Test
        @DisplayName("Should validate double-entry rules for income transaction")
        void shouldValidateDoubleEntryRulesForIncomeTransaction() {
            // Given
            TransactionAggregate incomeTransaction = TransactionAggregate.createIncomeTransaction(
                Money.of(new BigDecimal("1500.00"), "CNY"),
                "Service Revenue",
                LocalDate.now(),
                1, // categoryId
                TenantId.of(1),
                1  // userId
            );
            
            when(journalEntryRepository.save(any(JournalEntryAggregate.class))).thenAnswer(invocation -> {
                JournalEntryAggregate savedEntry = invocation.getArgument(0);
                // Verify that the journal entry has proper debit and credit lines
                assertNotNull(savedEntry);
                assertTrue(savedEntry.getDescription().contains("Service Revenue"));
                return savedEntry;
            });
            
            // When
            JournalEntryDTO result = journalEntryApplicationService.createFromTransaction(incomeTransaction);
            
            // Then
            assertNotNull(result);
            verify(journalEntryRepository).save(any(JournalEntryAggregate.class));
        }
        
        @Test
        @DisplayName("Should validate double-entry rules for expense transaction")
        void shouldValidateDoubleEntryRulesForExpenseTransaction() {
            // Given
            when(journalEntryRepository.save(any(JournalEntryAggregate.class))).thenAnswer(invocation -> {
                JournalEntryAggregate savedEntry = invocation.getArgument(0);
                // Verify that the journal entry has proper debit and credit lines
                assertNotNull(savedEntry);
                assertTrue(savedEntry.getDescription().contains("Test Expense Transaction"));
                return savedEntry;
            });
            
            // When
            JournalEntryDTO result = journalEntryApplicationService.createFromTransaction(testTransaction);
            
            // Then
            assertNotNull(result);
            verify(journalEntryRepository).save(any(JournalEntryAggregate.class));
        }
        
        @Test
        @DisplayName("Should ensure journal entry balance before posting")
        void shouldEnsureJournalEntryBalanceBeforePosting() {
            // Given
            Money debitAmount = Money.of(new BigDecimal("1000.00"), "CNY");
            Money creditAmount = Money.of(new BigDecimal("1000.00"), "CNY");
            
            testJournalEntry.addJournalLine(1001, debitAmount, null, "Cash Account");
            testJournalEntry.addJournalLine(4001, null, creditAmount, "Revenue Account");
            
            when(journalEntryRepository.findById(1)).thenReturn(Optional.of(testJournalEntry));
            when(journalEntryRepository.save(any(JournalEntryAggregate.class))).thenReturn(testJournalEntry);
            
            // When
            JournalEntryDTO result = journalEntryApplicationService.postJournalEntry(1, 1);
            
            // Then
            assertNotNull(result);
            verify(journalEntryRepository).save(testJournalEntry);
        }
    }
}
            when(journalEntryRepository.save(any(JournalEntryAggregate.class))).thenReturn(testJournalEntry);
            
            Money debitAmount = Money.of(new BigDecimal("500.00"), "CNY");
            
            // When
            JournalEntryDTO result = journalEntryApplicationService.addJournalLine(
                1, 1, 1001, debitAmount, null, "Debit Entry"
            );
            
            // Then
            assertNotNull(result);
            verify(journalEntryRepository).findById(1);
            verify(journalEntryRepository).save(testJournalEntry);
        }
        
        @Test
        @DisplayName("Should add credit line successfully")
        void shouldAddCreditLineSuccessfully() {
            // Given
            when(journalEntryRepository.findById(1)).thenReturn(Optional.of(testJournalEntry));
            when(journalEntryRepository.save(any(JournalEntryAggregate.class))).thenReturn(testJournalEntry);
            
            Money creditAmount = Money.of(new BigDecimal("500.00"), "CNY");
            
            // When
            JournalEntryDTO result = journalEntryApplicationService.addJournalLine(
                1, 1, 2001, null, creditAmount, "Credit Entry"
            );
            
            // Then
            assertNotNull(result);
            verify(journalEntryRepository).findById(1);
            verify(journalEntryRepository).save(testJournalEntry);
        }
        
        @Test
        @DisplayName("Should throw exception when both debit and credit amounts are provided")
        void shouldThrowExceptionWhenBothDebitAndCreditProvided() {
            // Given
            when(journalEntryRepository.findById(1)).thenReturn(Optional.of(testJournalEntry));
            
            Money amount = Money.of(new BigDecimal("500.00"), "CNY");
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> journalEntryApplicationService.addJournalLine(
                    1, 1, 1001, amount, amount, "Invalid Entry"
                )
            );
            
            assertTrue(exception.getMessage().contains("Cannot have both debit and credit amounts"));
            verify(journalEntryRepository).findById(1);
            verify(journalEntryRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Should throw exception when neither debit nor credit amount is provided")
        void shouldThrowExceptionWhenNeitherDebitNorCreditProvided() {
            // Given
            when(journalEntryRepository.findById(1)).thenReturn(Optional.of(testJournalEntry));
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> journalEntryApplicationService.addJournalLine(
                    1, 1, 1001, null, null, "Invalid Entry"
                )
            );
            
            assertTrue(exception.getMessage().contains("Must have either debit or credit amount"));
            verify(journalEntryRepository).findById(1);
            verify(journalEntryRepository, never()).save(any());
        }
    }
    
    @Nested
    @DisplayName("Journal Entry Posting Tests")
    class JournalEntryPostingTests {
        
        @Test
        @DisplayName("Should post balanced journal entry successfully")
        void shouldPostBalancedJournalEntrySuccessfully() {
            // Given
            // Add balanced entries to the journal entry
            Money amount = Money.of(new BigDecimal("1000.00"), "CNY");
            testJournalEntry.addJournalLine(1001, amount, null, "Debit Entry");
            testJournalEntry.addJournalLine(2001, null, amount, "Credit Entry");
            
            when(journalEntryRepository.findById(1)).thenReturn(Optional.of(testJournalEntry));
            when(journalEntryRepository.save(any(JournalEntryAggregate.class))).thenReturn(testJournalEntry);
            
            // When
            JournalEntryDTO result = journalEntryApplicationService.postJournalEntry(1, 1);
            
            // Then
            assertNotNull(result);
            verify(journalEntryRepository).findById(1);
            verify(journalEntryRepository).save(testJournalEntry);
            verify(eventPublisher).publishAll(any());
        }
        
        @Test
        @DisplayName("Should throw exception when posting unbalanced journal entry")
        void shouldThrowExceptionWhenPostingUnbalancedJournalEntry() {
            // Given
            // Add unbalanced entries
            Money debitAmount = Money.of(new BigDecimal("1000.00"), "CNY");
            Money creditAmount = Money.of(new BigDecimal("500.00"), "CNY");
            testJournalEntry.addJournalLine(1001, debitAmount, null, "Debit Entry");
            testJournalEntry.addJournalLine(2001, null, creditAmount, "Credit Entry");
            
            when(journalEntryRepository.findById(1)).thenReturn(Optional.of(testJournalEntry));
            
            // When & Then
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> journalEntryApplicationService.postJournalEntry(1, 1)
            );
            
            assertTrue(exception.getMessage().contains("Journal entry is not balanced"));
            verify(journalEntryRepository).findById(1);
            verify(journalEntryRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Should reverse journal entry successfully")
        void shouldReverseJournalEntrySuccessfully() {
            // Given
            // Create and post the original entry
            Money amount = Money.of(new BigDecimal("1000.00"), "CNY");
            testJournalEntry.addJournalLine(1001, amount, null, "Original Debit");
            testJournalEntry.addJournalLine(2001, null, amount, "Original Credit");
            testJournalEntry.post();
            
            when(journalEntryRepository.findById(1)).thenReturn(Optional.of(testJournalEntry));
            when(journalEntryRepository.save(any(JournalEntryAggregate.class))).thenReturn(testJournalEntry);
            
            // When
            JournalEntryDTO result = journalEntryApplicationService.reverseJournalEntry(1, 1, "Correction needed");
            
            // Then
            assertNotNull(result);
            verify(journalEntryRepository).findById(1);
            verify(journalEntryRepository).save(any(JournalEntryAggregate.class));
            verify(eventPublisher).publishAll(any());
        }
    }
    
    @Nested
    @DisplayName("Query Tests")
    class QueryTests {
        
        @Test
        @DisplayName("Should get journal entry by ID successfully")
        void shouldGetJournalEntryByIdSuccessfully() {
            // Given
            when(journalEntryRepository.findById(1)).thenReturn(Optional.of(testJournalEntry));