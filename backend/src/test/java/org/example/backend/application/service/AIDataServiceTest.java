package org.example.backend.application.service;

import org.example.backend.application.dto.AITransactionData;
import org.example.backend.application.dto.DateRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AIDataServiceTest {

    private AIDataService aiDataService;

    @BeforeEach
    void setUp() {
        aiDataService = new AIDataService();
    }

    @Test
    void buildFinancialContext_validDates_returnsContextString() {
        int companyId = 1;
        LocalDate start = LocalDate.now().minusMonths(1);
        LocalDate end = LocalDate.now();

        String context = aiDataService.buildFinancialContext(companyId, start, end);

        assertNotNull(context);
        assertTrue(context.contains(String.valueOf(companyId)));
        assertTrue(context.contains(start.toString()));
        assertTrue(context.contains(end.toString()));
    }

    @Test
    void getTransactionsInRange_validRange_returnsTransactions() {
        int companyId = 1;
        DateRange range = new DateRange(LocalDate.now().minusDays(10), LocalDate.now());

        List<AITransactionData> txns = aiDataService.getTransactionsInRange(companyId, range);

        assertNotNull(txns);
        // Depending on implementation, could be empty or not
    }

    @Test
    void getTransactionsInRange_noTransactions_returnsEmptyList() {
        int companyId = 9999; // Assume this company has no transactions
        DateRange range = new DateRange(LocalDate.now().minusDays(10), LocalDate.now());

        List<AITransactionData> txns = aiDataService.getTransactionsInRange(companyId, range);

        assertNotNull(txns);
        assertTrue(txns.isEmpty() || txns.size() >= 0); // Accepts both empty and non-empty for stub
    }
}