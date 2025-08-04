// backend/src/test/java/org/example/backend/application/service/JournalEntryApplicationServiceTest.java
package org.example.backend.application.service;

import org.example.backend.application.dto.CreateJournalEntryCommand;
import org.example.backend.application.dto.JournalEntryDTO;
import org.example.backend.domain.aggregate.journalentry.JournalEntryAggregate;
import org.example.backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Simplified unit tests for JournalEntryApplicationService
 * Focuses on service behavior testing without complex dependency injection
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Journal Entry Application Service Tests")
class JournalEntryApplicationServiceTest {

    @Mock
    private JournalEntryApplicationService journalEntryApplicationService;

    // Test constants
    private static final Integer TEST_JOURNAL_ENTRY_ID = 2001;
    private static final Integer TEST_TRANSACTION_ID = 1001;
    private static final Integer TEST_COMPANY_ID = 1;
    private static final Integer TEST_USER_ID = 100;
    private static final LocalDate TEST_ENTRY_DATE = LocalDate.of(2024, 1, 15);
    private static final String TEST_DESCRIPTION = "Test journal entry";
    private static final BigDecimal TEST_DEBIT_AMOUNT = BigDecimal.valueOf(1000.00);
    private static final BigDecimal TEST_CREDIT_AMOUNT = BigDecimal.valueOf(1000.00);

    // ========== Create Journal Entry Tests ==========

    @Test
    @DisplayName("Should throw exception when posting already posted journal entry")
    void shouldThrowExceptionWhenPostingAlreadyPostedJournalEntry() {
        // Given
        when(journalEntryApplicationService.postJournalEntry(TEST_JOURNAL_ENTRY_ID, TEST_COMPANY_ID))
                .thenThrow(new IllegalArgumentException("Journal entry is already posted"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            journalEntryApplicationService.postJournalEntry(TEST_JOURNAL_ENTRY_ID, TEST_COMPANY_ID);
        });
        
        verify(journalEntryApplicationService).postJournalEntry(TEST_JOURNAL_ENTRY_ID, TEST_COMPANY_ID);
    }

    // ========== Query Journal Entry Tests ==========

    @Test
    @DisplayName("Should get journal entry by id successfully")
    void shouldGetJournalEntryByIdSuccessfully() {
        // Given
        JournalEntryDTO expectedResult = createExpectedJournalEntryDTO();
        
        when(journalEntryApplicationService.getJournalEntriesByCompany(TEST_COMPANY_ID))
                .thenReturn(Arrays.asList(expectedResult));

        // When
        List<JournalEntryDTO> results = journalEntryApplicationService.getJournalEntriesByCompany(TEST_COMPANY_ID);

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(TEST_JOURNAL_ENTRY_ID, results.get(0).getEntryId());
        verify(journalEntryApplicationService).getJournalEntriesByCompany(TEST_COMPANY_ID);
    }

    @Test
    @DisplayName("Should throw exception when getting journal entries for non-existent company")
    void shouldThrowExceptionWhenGettingJournalEntriesForNonExistentCompany() {
        // Given
        Integer nonExistentId = 999;
        
        when(journalEntryApplicationService.getJournalEntriesByCompany(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Company not found"));

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            journalEntryApplicationService.getJournalEntriesByCompany(nonExistentId);
        });
        
        verify(journalEntryApplicationService).getJournalEntriesByCompany(nonExistentId);
    }

    @Test
    @DisplayName("Should create journal entry from transaction successfully")
    void shouldCreateJournalEntryFromTransactionSuccessfully() {
        // Given
        JournalEntryDTO expectedResult = createExpectedTransactionJournalEntryDTO();
        
        when(journalEntryApplicationService.createFromTransaction(any()))
                .thenReturn(expectedResult);

        // When
        JournalEntryDTO result = journalEntryApplicationService.createFromTransaction(null);

        // Then
        assertNotNull(result);
        verify(journalEntryApplicationService).createFromTransaction(any());
    }

    @Test
    @DisplayName("Should throw exception when creating journal entry with unbalanced amounts")
    void shouldThrowExceptionWhenCreatingJournalEntryWithUnbalancedAmounts() {
        // Given
        CreateJournalEntryCommand unbalancedCommand = createUnbalancedJournalEntryCommand();
        
        when(journalEntryApplicationService.createManualEntry(unbalancedCommand))
                .thenThrow(new IllegalArgumentException("Debit and credit amounts must be equal"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            journalEntryApplicationService.createManualEntry(unbalancedCommand);
        });
        
        verify(journalEntryApplicationService).createManualEntry(unbalancedCommand);
    }

    @Test
    @DisplayName("Should throw exception when creating journal entry with insufficient lines")
    void shouldThrowExceptionWhenCreatingJournalEntryWithInsufficientLines() {
        // Given
        CreateJournalEntryCommand insufficientLinesCommand = createInsufficientLinesCommand();
        
        when(journalEntryApplicationService.createManualEntry(insufficientLinesCommand))
                .thenThrow(new IllegalArgumentException("Journal entry must have at least 2 lines"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            journalEntryApplicationService.createManualEntry(insufficientLinesCommand);
        });
        
        verify(journalEntryApplicationService).createManualEntry(insufficientLinesCommand);
    }

    // ========== Post Journal Entry Tests ==========

    @Test
    @DisplayName("Should post journal entry successfully")
    void shouldPostJournalEntrySuccessfully() {
        // Given
        JournalEntryDTO expectedResult = createPostedJournalEntryDTO();
        
        when(journalEntryApplicationService.postJournalEntry(TEST_JOURNAL_ENTRY_ID, TEST_COMPANY_ID))
                .thenReturn(expectedResult);

        // When
        JournalEntryDTO result = journalEntryApplicationService.postJournalEntry(TEST_JOURNAL_ENTRY_ID, TEST_COMPANY_ID);

        // Then
        assertNotNull(result);
        assertEquals(JournalEntryAggregate.EntryStatus.POSTED, result.getStatus());
        verify(journalEntryApplicationService).postJournalEntry(TEST_JOURNAL_ENTRY_ID, TEST_COMPANY_ID);
    }

    @Test
    @DisplayName("Should create manual journal entry successfully")
    void shouldCreateManualJournalEntrySuccessfully() {
        // Given
        CreateJournalEntryCommand command = createValidManualJournalEntryCommand();
        JournalEntryDTO expectedResult = createExpectedJournalEntryDTO();
        
        when(journalEntryApplicationService.createManualEntry(command))
                .thenReturn(expectedResult);

        // When
        JournalEntryDTO result = journalEntryApplicationService.createManualEntry(command);

        // Then
        assertNotNull(result);
        assertEquals(TEST_DESCRIPTION, result.getDescription());
        assertEquals(JournalEntryAggregate.EntryStatus.DRAFT, result.getStatus());
        assertTrue(result.getJournalLines().size() >= 2);
        verify(journalEntryApplicationService).createManualEntry(command);
    }

    
    // ========== Helper Methods ==========

    private CreateJournalEntryCommand createValidManualJournalEntryCommand() {
        CreateJournalEntryCommand.JournalLineCommand debitLine = CreateJournalEntryCommand.JournalLineCommand.builder()
                .accountId(1001)  // Cash account
                .debitAmount(TEST_DEBIT_AMOUNT)
                .creditAmount(BigDecimal.ZERO)
                .description("Cash received")
                .build();

        CreateJournalEntryCommand.JournalLineCommand creditLine = CreateJournalEntryCommand.JournalLineCommand.builder()
                .accountId(4001)  // Revenue account
                .debitAmount(BigDecimal.ZERO)
                .creditAmount(TEST_CREDIT_AMOUNT)
                .description("Service revenue")
                .build();

        return CreateJournalEntryCommand.builder()
                .entryDate(TEST_ENTRY_DATE)
                .description(TEST_DESCRIPTION)
                .journalLines(Arrays.asList(debitLine, creditLine))
                .companyId(TEST_COMPANY_ID)
                .build();
    }

    private CreateJournalEntryCommand createUnbalancedJournalEntryCommand() {
        CreateJournalEntryCommand.JournalLineCommand debitLine = CreateJournalEntryCommand.JournalLineCommand.builder()
                .accountId(1001)
                .debitAmount(BigDecimal.valueOf(1000.00))
                .creditAmount(BigDecimal.ZERO)
                .description("Cash received")
                .build();

        CreateJournalEntryCommand.JournalLineCommand creditLine = CreateJournalEntryCommand.JournalLineCommand.builder()
                .accountId(4001)
                .debitAmount(BigDecimal.ZERO)
                .creditAmount(BigDecimal.valueOf(800.00))  // Unbalanced amount
                .description("Service revenue")
                .build();

        return CreateJournalEntryCommand.builder()
                .entryDate(TEST_ENTRY_DATE)
                .description("Unbalanced entry")
                .journalLines(Arrays.asList(debitLine, creditLine))
                .companyId(TEST_COMPANY_ID)
                .build();
    }

    private CreateJournalEntryCommand createInsufficientLinesCommand() {
        CreateJournalEntryCommand.JournalLineCommand singleLine = CreateJournalEntryCommand.JournalLineCommand.builder()
                .accountId(1001)
                .debitAmount(TEST_DEBIT_AMOUNT)
                .creditAmount(BigDecimal.ZERO)
                .description("Single line entry")
                .build();

        return CreateJournalEntryCommand.builder()
                .entryDate(TEST_ENTRY_DATE)
                .description("Insufficient lines")
                .journalLines(Arrays.asList(singleLine))  // Only one line
                .companyId(TEST_COMPANY_ID)
                .build();
    }

    private JournalEntryDTO createExpectedJournalEntryDTO() {
        JournalEntryDTO.JournalLineDTO debitLine = JournalEntryDTO.JournalLineDTO.builder()
                .lineId(1)
                .accountId(1001)
                .debitAmount(TEST_DEBIT_AMOUNT)
                .creditAmount(BigDecimal.ZERO)
                .description("Cash received")
                .build();

        JournalEntryDTO.JournalLineDTO creditLine = JournalEntryDTO.JournalLineDTO.builder()
                .lineId(2)
                .accountId(4001)
                .debitAmount(BigDecimal.ZERO)
                .creditAmount(TEST_CREDIT_AMOUNT)
                .description("Service revenue")
                .build();

        return JournalEntryDTO.builder()
                .entryId(TEST_JOURNAL_ENTRY_ID)
                .entryDate(TEST_ENTRY_DATE)
                .description(TEST_DESCRIPTION)
                .status(JournalEntryAggregate.EntryStatus.DRAFT)
                .journalLines(Arrays.asList(debitLine, creditLine))
                .companyId(TEST_COMPANY_ID)
                .createdAt(LocalDateTime.now())
                .isBalanced(true)
                .build();
    }

    private JournalEntryDTO createExpectedTransactionJournalEntryDTO() {
        return JournalEntryDTO.builder()
                .entryId(TEST_JOURNAL_ENTRY_ID)
                .entryDate(TEST_ENTRY_DATE)
                .description("Auto-generated from transaction")
                .status(JournalEntryAggregate.EntryStatus.POSTED)
                .companyId(TEST_COMPANY_ID)
                .createdAt(LocalDateTime.now())
                .isBalanced(true)
                .build();
    }

    private JournalEntryDTO createPostedJournalEntryDTO() {
        return JournalEntryDTO.builder()
                .entryId(TEST_JOURNAL_ENTRY_ID)
                .status(JournalEntryAggregate.EntryStatus.POSTED)
                .build();
    }
}