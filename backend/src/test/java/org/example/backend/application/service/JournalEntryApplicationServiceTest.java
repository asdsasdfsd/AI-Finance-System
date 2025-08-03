// backend/src/test/java/org/example/backend/application/service/JournalEntryApplicationServiceTest.java
package org.example.backend.application.service;

import org.example.backend.application.dto.CreateJournalEntryCommand;
import org.example.backend.application.dto.JournalEntryDTO;
import org.example.backend.domain.aggregate.journalentry.JournalEntryAggregate;
import org.example.backend.domain.aggregate.journalentry.JournalEntryAggregateRepository;
import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.event.DomainEventPublisher;
import org.example.backend.domain.valueobject.Money;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JournalEntryApplicationService - Fixed Version
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JournalEntryApplicationServiceTest {

    @Mock
    private JournalEntryAggregateRepository journalEntryRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private JournalEntryApplicationService journalEntryApplicationService;

    // Test constants
    private static final Integer TEST_COMPANY_ID = 1;
    private static final Integer TEST_USER_ID = 100;
    private static final Integer TEST_ENTRY_ID = 1001;
    private static final LocalDate TEST_DATE = LocalDate.of(2024, 1, 15);
    private static final String TEST_DESCRIPTION = "Test journal entry";
    private static final String TEST_CURRENCY = "CNY";

    private JournalEntryAggregate testJournalEntry;
    private TransactionAggregate testTransaction;

    @BeforeEach
    void setUp() {
        testJournalEntry = createMockJournalEntry();
        testTransaction = createMockTransaction();
    }

    // ========== Create Manual Entry Tests ==========

    @Test
    @DisplayName("Should create manual journal entry successfully")
    void shouldCreateManualJournalEntrySuccessfully() {
        // Given
        CreateJournalEntryCommand command = createValidCreateCommand();
        when(journalEntryRepository.save(any(JournalEntryAggregate.class))).thenReturn(testJournalEntry);

        // When
        JournalEntryDTO result = journalEntryApplicationService.createManualEntry(command);

        // Then
        assertNotNull(result);
        assertEquals(testJournalEntry.getEntryId(), result.getEntryId());
        assertEquals(testJournalEntry.getTenantId().getValue(), result.getCompanyId());
        assertEquals(testJournalEntry.getDescription(), result.getDescription());

        verify(journalEntryRepository).save(any(JournalEntryAggregate.class));
    }

    @Test
    @DisplayName("Should throw exception when create command is null")
    void shouldThrowExceptionWhenCreateCommandIsNull() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            journalEntryApplicationService.createManualEntry(null);
        });
    }

    @Test
    @DisplayName("Should throw exception when company ID is null")
    void shouldThrowExceptionWhenCompanyIdIsNull() {
        // Given
        CreateJournalEntryCommand command = CreateJournalEntryCommand.builder()
                .companyId(null)
                .entryDate(TEST_DATE)
                .description(TEST_DESCRIPTION)
                .journalLines(createValidJournalLines())
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            journalEntryApplicationService.createManualEntry(command);
        });
    }

    @Test
    @DisplayName("Should throw exception when journal lines are empty")
    void shouldThrowExceptionWhenJournalLinesAreEmpty() {
        // Given
        CreateJournalEntryCommand command = CreateJournalEntryCommand.builder()
                .companyId(TEST_COMPANY_ID)
                .entryDate(TEST_DATE)
                .description(TEST_DESCRIPTION)
                .journalLines(new ArrayList<>())
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            journalEntryApplicationService.createManualEntry(command);
        });
    }

    // ========== Create From Transaction Tests ==========

    @Test
    @DisplayName("Should create journal entry from income transaction")
    void shouldCreateJournalEntryFromIncomeTransaction() {
        // Given
        TransactionAggregate incomeTransaction = createIncomeTransaction();
        when(journalEntryRepository.save(any(JournalEntryAggregate.class))).thenReturn(testJournalEntry);

        // When
        JournalEntryDTO result = journalEntryApplicationService.createFromTransaction(incomeTransaction);

        // Then
        assertNotNull(result);
        verify(journalEntryRepository).save(any(JournalEntryAggregate.class));
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    @DisplayName("Should create journal entry from expense transaction")
    void shouldCreateJournalEntryFromExpenseTransaction() {
        // Given
        TransactionAggregate expenseTransaction = createExpenseTransaction();
        when(journalEntryRepository.save(any(JournalEntryAggregate.class))).thenReturn(testJournalEntry);

        // When
        JournalEntryDTO result = journalEntryApplicationService.createFromTransaction(expenseTransaction);

        // Then
        assertNotNull(result);
        verify(journalEntryRepository).save(any(JournalEntryAggregate.class));
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    @DisplayName("Should throw exception when transaction is null")
    void shouldThrowExceptionWhenTransactionIsNull() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            journalEntryApplicationService.createFromTransaction(null);
        });
    }

    // ========== Post Journal Entry Tests ==========

    @Test
    @DisplayName("Should post journal entry successfully")
    void shouldPostJournalEntrySuccessfully() {
        // Given
        when(journalEntryRepository.findByIdAndTenant(TEST_ENTRY_ID, TEST_COMPANY_ID))
                .thenReturn(Optional.of(testJournalEntry));
        when(journalEntryRepository.save(any(JournalEntryAggregate.class))).thenReturn(testJournalEntry);

        // When
        JournalEntryDTO result = journalEntryApplicationService.postJournalEntry(TEST_ENTRY_ID, TEST_COMPANY_ID);

        // Then
        assertNotNull(result);
        verify(journalEntryRepository).findByIdAndTenant(TEST_ENTRY_ID, TEST_COMPANY_ID);
        verify(journalEntryRepository).save(testJournalEntry);
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    @DisplayName("Should throw exception when journal entry not found for posting")
    void shouldThrowExceptionWhenJournalEntryNotFoundForPosting() {
        // Given
        when(journalEntryRepository.findByIdAndTenant(TEST_ENTRY_ID, TEST_COMPANY_ID))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            journalEntryApplicationService.postJournalEntry(TEST_ENTRY_ID, TEST_COMPANY_ID);
        });
    }

    // ========== Query Tests ==========

    @Test
    @DisplayName("Should get journal entry by ID successfully")
    void shouldGetJournalEntryByIdSuccessfully() {
        // Given
        when(journalEntryRepository.findByIdAndTenant(TEST_ENTRY_ID, TEST_COMPANY_ID))
                .thenReturn(Optional.of(testJournalEntry));

        // When
        JournalEntryDTO result = journalEntryApplicationService.getJournalEntryById(TEST_ENTRY_ID, TEST_COMPANY_ID);

        // Then
        assertNotNull(result);
        assertEquals(testJournalEntry.getEntryId(), result.getEntryId());
        verify(journalEntryRepository).findByIdAndTenant(TEST_ENTRY_ID, TEST_COMPANY_ID);
    }

    @Test
    @DisplayName("Should throw exception when journal entry not found by ID")
    void shouldThrowExceptionWhenJournalEntryNotFoundById() {
        // Given
        when(journalEntryRepository.findByIdAndTenant(TEST_ENTRY_ID, TEST_COMPANY_ID))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            journalEntryApplicationService.getJournalEntryById(TEST_ENTRY_ID, TEST_COMPANY_ID);
        });
    }

    @Test
    @DisplayName("Should get journal entries by company successfully")
    void shouldGetJournalEntriesByCompanySuccessfully() {
        // Given
        List<JournalEntryAggregate> entries = List.of(testJournalEntry);
        when(journalEntryRepository.findByTenantIdOrderByEntryDateDesc(TEST_COMPANY_ID))
                .thenReturn(entries);

        // When
        List<JournalEntryDTO> result = journalEntryApplicationService.getJournalEntriesByCompany(TEST_COMPANY_ID);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(journalEntryRepository).findByTenantIdOrderByEntryDateDesc(TEST_COMPANY_ID);
    }

    @Test
    @DisplayName("Should get journal entries by date range successfully")
    void shouldGetJournalEntriesByDateRangeSuccessfully() {
        // Given
        LocalDate startDate = TEST_DATE.minusDays(7);
        LocalDate endDate = TEST_DATE.plusDays(7);
        List<JournalEntryAggregate> entries = List.of(testJournalEntry);
        
        when(journalEntryRepository.findByTenantIdAndDateRange(TEST_COMPANY_ID, startDate, endDate))
                .thenReturn(entries);

        // When
        List<JournalEntryDTO> result = journalEntryApplicationService.getJournalEntriesByDateRange(
                TEST_COMPANY_ID, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(journalEntryRepository).findByTenantIdAndDateRange(TEST_COMPANY_ID, startDate, endDate);
    }

    // ========== Helper Methods ==========

    private CreateJournalEntryCommand createValidCreateCommand() {
        return CreateJournalEntryCommand.builder()
                .companyId(TEST_COMPANY_ID)
                .entryDate(TEST_DATE)
                .description(TEST_DESCRIPTION)
                .createdBy(TEST_USER_ID)
                .journalLines(createValidJournalLines())
                .build();
    }

    private List<CreateJournalEntryCommand.JournalLineCommand> createValidJournalLines() {
        List<CreateJournalEntryCommand.JournalLineCommand> lines = new ArrayList<>();
        
        // Debit line
        lines.add(CreateJournalEntryCommand.JournalLineCommand.builder()
                .accountId(1001)
                .debitAmount(BigDecimal.valueOf(1000.00))
                .creditAmount(null)
                .description("Cash debit")
                .build());
        
        // Credit line
        lines.add(CreateJournalEntryCommand.JournalLineCommand.builder()
                .accountId(4001)
                .debitAmount(null)
                .creditAmount(BigDecimal.valueOf(1000.00))
                .description("Revenue credit")
                .build());
        
        return lines;
    }

    private JournalEntryAggregate createMockJournalEntry() {
        JournalEntryAggregate entry = mock(JournalEntryAggregate.class);
        
        when(entry.getEntryId()).thenReturn(TEST_ENTRY_ID);
        when(entry.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(entry.getEntryDate()).thenReturn(TEST_DATE);
        when(entry.getDescription()).thenReturn(TEST_DESCRIPTION);
        when(entry.getCreatedBy()).thenReturn(TEST_USER_ID);
        when(entry.getStatus()).thenReturn(JournalEntryAggregate.EntryStatus.DRAFT);
        when(entry.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(entry.isBalanced()).thenReturn(true);
        when(entry.getJournalLines()).thenReturn(new ArrayList<>());
        when(entry.getDomainEvents()).thenReturn(new ArrayList<>());
        
        // Mock post method
        doNothing().when(entry).post();
        doNothing().when(entry).validateBalance();
        doNothing().when(entry).clearDomainEvents();
        
        return entry;
    }

    private TransactionAggregate createMockTransaction() {
        TransactionAggregate transaction = mock(TransactionAggregate.class);
        
        when(transaction.getTransactionId()).thenReturn(1001);
        when(transaction.getTenantId()).thenReturn(TenantId.of(TEST_COMPANY_ID));
        when(transaction.getUserId()).thenReturn(TEST_USER_ID);
        when(transaction.getTransactionDate()).thenReturn(TEST_DATE);
        when(transaction.getDescription()).thenReturn("Test transaction");
        when(transaction.getMoney()).thenReturn(Money.of(BigDecimal.valueOf(1000.00), TEST_CURRENCY));
        when(transaction.getTransactionType()).thenReturn(TransactionAggregate.TransactionType.INCOME);
        when(transaction.isCompleted()).thenReturn(true);
        
        return transaction;
    }

    private TransactionAggregate createIncomeTransaction() {
        TransactionAggregate transaction = createMockTransaction();
        when(transaction.getTransactionType()).thenReturn(TransactionAggregate.TransactionType.INCOME);
        return transaction;
    }

    private TransactionAggregate createExpenseTransaction() {
        TransactionAggregate transaction = createMockTransaction();
        when(transaction.getTransactionType()).thenReturn(TransactionAggregate.TransactionType.EXPENSE);
        return transaction;
    }
}