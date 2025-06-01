// backend/src/main/java/org/example/backend/application/service/JournalEntryApplicationService.java
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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Journal Entry Application Service - 修复版本
 * 
 * 协调会计分录的业务用例，确保借贷平衡和会计合规性
 */
@Service
@Transactional
public class JournalEntryApplicationService {
    
    private final JournalEntryAggregateRepository journalEntryRepository;
    private final DomainEventPublisher eventPublisher;
    
    public JournalEntryApplicationService(JournalEntryAggregateRepository journalEntryRepository,
                                        DomainEventPublisher eventPublisher) {
        this.journalEntryRepository = journalEntryRepository;
        this.eventPublisher = eventPublisher;
    }
    
    // ========== Command Handlers ==========
    
    /**
     * Create journal entry from transaction
     */
    public JournalEntryDTO createFromTransaction(TransactionAggregate transaction) {
        validateTransaction(transaction);
        
        TenantId tenantId = transaction.getTenantId();
        
        JournalEntryAggregate journalEntry = JournalEntryAggregate.create(
            tenantId,
            transaction.getTransactionDate(),
            "Auto-generated from transaction: " + transaction.getTransactionId(),
            transaction.getUserId()
        );
        
        // 根据交易类型创建会计分录
        if (transaction.getTransactionType() == TransactionAggregate.TransactionType.INCOME) {
            // 收入：借记现金，贷记收入
            journalEntry.addJournalLine(
                1001, // 现金账户 (假设)
                transaction.getMoney(),
                null,
                "收入 - " + transaction.getDescription()
            );
            journalEntry.addJournalLine(
                4001, // 收入账户 (假设)
                null,
                transaction.getMoney(),
                "收入 - " + transaction.getDescription()
            );
        } else {
            // 支出：借记费用，贷记现金
            journalEntry.addJournalLine(
                5001, // 费用账户 (假设)
                transaction.getMoney(),
                null,
                "支出 - " + transaction.getDescription()
            );
            journalEntry.addJournalLine(
                1001, // 现金账户 (假设)
                null,
                transaction.getMoney(),
                "支出 - " + transaction.getDescription()
            );
        }
        
        // 验证平衡后过账
        journalEntry.validateBalance();
        journalEntry.post();
        
        JournalEntryAggregate savedEntry = journalEntryRepository.save(journalEntry);
        
        // 发布领域事件
        eventPublisher.publishAll(savedEntry.getDomainEvents());
        savedEntry.clearDomainEvents();
        
        return mapToDTO(savedEntry);
    }
    
    /**
     * Create manual journal entry
     */
    public JournalEntryDTO createManualEntry(CreateJournalEntryCommand command) {
        validateCreateCommand(command);
        
        TenantId tenantId = TenantId.of(command.getCompanyId());
        
        JournalEntryAggregate journalEntry = JournalEntryAggregate.create(
            tenantId,
            command.getEntryDate(),
            command.getDescription(),
            command.getCreatedBy()
        );
        
        // 添加分录行
        command.getJournalLines().forEach(line -> {
            Money debitAmount = line.getDebitAmount() != null ? 
                Money.of(line.getDebitAmount(), "CNY") : null;
            Money creditAmount = line.getCreditAmount() != null ? 
                Money.of(line.getCreditAmount(), "CNY") : null;
                
            journalEntry.addJournalLine(
                line.getAccountId(),
                debitAmount,
                creditAmount,
                line.getDescription()
            );
        });
        
        // 验证平衡
        journalEntry.validateBalance();
        
        JournalEntryAggregate savedEntry = journalEntryRepository.save(journalEntry);
        return mapToDTO(savedEntry);
    }
    
    /**
     * Post journal entry
     */
    public JournalEntryDTO postJournalEntry(Integer entryId, Integer companyId) {
        TenantId tenantId = TenantId.of(companyId);
        JournalEntryAggregate journalEntry = findJournalEntryByIdAndTenant(entryId, tenantId);
        
        journalEntry.post();
        
        JournalEntryAggregate savedEntry = journalEntryRepository.save(journalEntry);
        
        // 发布领域事件
        eventPublisher.publishAll(savedEntry.getDomainEvents());
        savedEntry.clearDomainEvents();
        
        return mapToDTO(savedEntry);
    }
    
    // ========== Query Handlers ==========
    
    /**
     * Get journal entry by ID
     */
    @Transactional(readOnly = true)
    public JournalEntryDTO getJournalEntryById(Integer entryId, Integer companyId) {
        TenantId tenantId = TenantId.of(companyId);
        JournalEntryAggregate journalEntry = findJournalEntryByIdAndTenant(entryId, tenantId);
        return mapToDTO(journalEntry);
    }
    
    /**
     * Get journal entries by company
     */
    @Transactional(readOnly = true)
    public List<JournalEntryDTO> getJournalEntriesByCompany(Integer companyId) {
        List<JournalEntryAggregate> entries = journalEntryRepository.findByTenantIdOrderByEntryDateDesc(companyId);
        return entries.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    
    /**
     * Get journal entries by date range
     */
    @Transactional(readOnly = true)
    public List<JournalEntryDTO> getJournalEntriesByDateRange(Integer companyId, 
                                                             LocalDate startDate, 
                                                             LocalDate endDate) {
        List<JournalEntryAggregate> entries = journalEntryRepository.findByTenantIdAndDateRange(
            companyId, startDate, endDate);
        return entries.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    
    // ========== Helper Methods ==========
    
    private JournalEntryAggregate findJournalEntryByIdAndTenant(Integer entryId, TenantId tenantId) {
        return journalEntryRepository.findByIdAndTenant(entryId, tenantId.getValue())
                .orElseThrow(() -> new ResourceNotFoundException("Journal entry not found with ID: " + entryId));
    }
    
    private void validateTransaction(TransactionAggregate transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        if (!transaction.isCompleted()) {
            throw new IllegalArgumentException("Only completed transactions can generate journal entries");
        }
    }
    
    private void validateCreateCommand(CreateJournalEntryCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Create journal entry command cannot be null");
        }
        if (command.getCompanyId() == null) {
            throw new IllegalArgumentException("Company ID cannot be null");
        }
        if (command.getJournalLines() == null || command.getJournalLines().isEmpty()) {
            throw new IllegalArgumentException("Journal entry must have at least one line");
        }
    }
    
    private JournalEntryDTO mapToDTO(JournalEntryAggregate journalEntry) {
        return JournalEntryDTO.builder()
                .entryId(journalEntry.getEntryId())
                .companyId(journalEntry.getTenantId().getValue())
                .entryDate(journalEntry.getEntryDate())
                .description(journalEntry.getDescription())
                .status(journalEntry.getStatus())
                .createdAt(journalEntry.getCreatedAt())
                .isBalanced(journalEntry.isBalanced())
                .journalLines(journalEntry.getJournalLines().stream()
                    .map(line -> JournalEntryDTO.JournalLineDTO.builder()
                        .lineId(null) // JournalLineData没有lineId
                        .accountId(line.getAccountId())
                        .description(line.getDescription())
                        .debitAmount(line.getDebitAmount())
                        .creditAmount(line.getCreditAmount())
                        .build())
                    .collect(Collectors.toList()))
                .build();
    }
}