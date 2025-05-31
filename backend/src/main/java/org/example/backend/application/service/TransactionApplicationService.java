// backend/src/main/java/org/example/backend/application/service/TransactionApplicationService.java
package org.example.backend.application.service;

import org.example.backend.application.dto.CreateTransactionCommand;
import org.example.backend.application.dto.UpdateTransactionCommand;
import org.example.backend.application.dto.ApproveTransactionCommand;
import org.example.backend.application.dto.TransactionDTO;
import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.aggregate.transaction.TransactionAggregateRepository;
import org.example.backend.domain.valueobject.Money;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.TransactionStatus;
import org.example.backend.domain.event.DomainEventPublisher;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.exception.UnauthorizedException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Transaction Application Service
 * 
 * Orchestrates transaction use cases and coordinates between
 * domain aggregates, ensuring business rules and transaction boundaries
 */
@Service
@Transactional
public class TransactionApplicationService {
    
    private final TransactionAggregateRepository transactionRepository;
    private final DomainEventPublisher eventPublisher;
    
    public TransactionApplicationService(TransactionAggregateRepository transactionRepository,
                                       DomainEventPublisher eventPublisher) {
        this.transactionRepository = transactionRepository;
        this.eventPublisher = eventPublisher;
    }
    
    // ========== Command Handlers ==========
    
    /**
     * Create new income transaction
     */
    public TransactionDTO createIncomeTransaction(CreateTransactionCommand command) {
        validateCreateCommand(command);
        
        Money amount = Money.of(command.getAmount(), command.getCurrency());
        TenantId tenantId = TenantId.of(command.getCompanyId());
        
        TransactionAggregate transaction = TransactionAggregate.createIncome(
            amount, 
            command.getTransactionDate(), 
            command.getDescription(),
            tenantId,
            command.getUserId()
        );
        
        // Set optional fields
        setOptionalFields(transaction, command);
        
        TransactionAggregate savedTransaction = transactionRepository.save(transaction);
        
        // Publish domain events
        eventPublisher.publishAll(savedTransaction.getDomainEvents());
        savedTransaction.clearDomainEvents();
        
        return mapToDTO(savedTransaction);
    }
    
    /**
     * Create new expense transaction
     */
    public TransactionDTO createExpenseTransaction(CreateTransactionCommand command) {
        validateCreateCommand(command);
        
        Money amount = Money.of(command.getAmount(), command.getCurrency());
        TenantId tenantId = TenantId.of(command.getCompanyId());
        
        TransactionAggregate transaction = TransactionAggregate.createExpense(
            amount, 
            command.getTransactionDate(), 
            command.getDescription(),
            tenantId,
            command.getUserId()
        );
        
        // Set optional fields
        setOptionalFields(transaction, command);
        
        TransactionAggregate savedTransaction = transactionRepository.save(transaction);
        
        // Publish domain events
        eventPublisher.publishAll(savedTransaction.getDomainEvents());
        savedTransaction.clearDomainEvents();
        
        return mapToDTO(savedTransaction);
    }
    
    /**
     * Update existing transaction
     */
    public TransactionDTO updateTransaction(Integer transactionId, UpdateTransactionCommand command) {
        validateUpdateCommand(command);
        
        TenantId tenantId = TenantId.of(command.getCompanyId());
        TransactionAggregate transaction = findTransactionByIdAndTenant(transactionId, tenantId);
        
        // Verify user has permission to update
        if (!transaction.getUserId().equals(command.getUserId())) {
            throw new UnauthorizedException("User can only update their own transactions");
        }
        
        Money newAmount = Money.of(command.getAmount(), command.getCurrency());
        transaction.updateTransaction(
            newAmount,
            command.getDescription(),
            command.getPaymentMethod(),
            command.getReferenceNumber()
        );
        
        // Update optional fields
        updateOptionalFields(transaction, command);
        
        TransactionAggregate savedTransaction = transactionRepository.save(transaction);
        return mapToDTO(savedTransaction);
    }
    
    /**
     * Approve transaction
     */
    public TransactionDTO approveTransaction(Integer transactionId, ApproveTransactionCommand command) {
        TenantId tenantId = TenantId.of(command.getCompanyId());
        TransactionAggregate transaction = findTransactionByIdAndTenant(transactionId, tenantId);
        
        if (!transaction.canBeApproved()) {
            throw new IllegalStateException("Transaction cannot be approved in current state");
        }
        
        transaction.approve(command.getApproverUserId());
        
        TransactionAggregate savedTransaction = transactionRepository.save(transaction);
        
        // Publish domain events
        eventPublisher.publishAll(savedTransaction.getDomainEvents());
        savedTransaction.clearDomainEvents();
        
        return mapToDTO(savedTransaction);
    }
    
    /**
     * Cancel transaction
     */
    public TransactionDTO cancelTransaction(Integer transactionId, Integer companyId, Integer userId) {
        TenantId tenantId = TenantId.of(companyId);
        TransactionAggregate transaction = findTransactionByIdAndTenant(transactionId, tenantId);
        
        // Verify user has permission to cancel
        if (!transaction.getUserId().equals(userId)) {
            throw new UnauthorizedException("User can only cancel their own transactions");
        }
        
        transaction.cancel();
        
        TransactionAggregate savedTransaction = transactionRepository.save(transaction);
        
        // Publish domain events
        eventPublisher.publishAll(savedTransaction.getDomainEvents());
        savedTransaction.clearDomainEvents();
        
        return mapToDTO(savedTransaction);
    }
    
    /**
     * Void approved transaction
     */
    public TransactionDTO voidTransaction(Integer transactionId, Integer companyId, Integer voidedBy, String reason) {
        TenantId tenantId = TenantId.of(companyId);
        TransactionAggregate transaction = findTransactionByIdAndTenant(transactionId, tenantId);
        
        transaction.voidTransaction(voidedBy, reason);
        
        TransactionAggregate savedTransaction = transactionRepository.save(transaction);
        
        // Publish domain events
        eventPublisher.publishAll(savedTransaction.getDomainEvents());
        savedTransaction.clearDomainEvents();
        
        return mapToDTO(savedTransaction);
    }
    
    // ========== Query Handlers ==========
    
    /**
     * Get transaction by ID
     */
    @Transactional(readOnly = true)
    public TransactionDTO getTransactionById(Integer transactionId, Integer companyId) {
        TenantId tenantId = TenantId.of(companyId);
        TransactionAggregate transaction = findTransactionByIdAndTenant(transactionId, tenantId);
        return mapToDTO(transaction);
    }
    
    /**
     * Get all transactions for company
     */
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByCompany(Integer companyId) {
        TenantId tenantId = TenantId.of(companyId);
        List<TransactionAggregate> transactions = transactionRepository.findByTenantIdOrderByTransactionDateDesc(tenantId);
        return transactions.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    
    /**
     * Get transactions by type
     */
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByType(Integer companyId, TransactionAggregate.TransactionType type) {
        TenantId tenantId = TenantId.of(companyId);
        List<TransactionAggregate> transactions = transactionRepository.findByTenantIdAndTransactionType(tenantId, type);
        return transactions.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    
    /**
     * Get transactions by date range
     */
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByDateRange(Integer companyId, LocalDate startDate, LocalDate endDate) {
        TenantId tenantId = TenantId.of(companyId);
        List<TransactionAggregate> transactions = transactionRepository.findByTenantIdAndTransactionDateBetween(tenantId, startDate, endDate);
        return transactions.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    
    /**
     * Get transactions pending approval
     */
    @Transactional(readOnly = true)
    public List<TransactionDTO> getPendingApprovalTransactions(Integer companyId) {
        TenantId tenantId = TenantId.of(companyId);
        List<TransactionAggregate> transactions = transactionRepository.findPendingApprovalByTenant(tenantId);
        return transactions.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    
    /**
     * Get user's transactions
     */
    @Transactional(readOnly = true)
    public List<TransactionDTO> getUserTransactions(Integer companyId, Integer userId) {
        TenantId tenantId = TenantId.of(companyId);
        List<TransactionAggregate> transactions = transactionRepository.findByTenantAndUser(tenantId, userId);
        return transactions.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    
    /**
     * Calculate total amount by type and status
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmount(Integer companyId, TransactionAggregate.TransactionType type, TransactionStatus.Status status) {
        TenantId tenantId = TenantId.of(companyId);
        return transactionRepository.sumAmountByTenantTypeAndStatus(tenantId, type, status);
    }
    
    // ========== Helper Methods ==========
    
    private TransactionAggregate findTransactionByIdAndTenant(Integer transactionId, TenantId tenantId) {
        return transactionRepository.findByIdAndTenant(transactionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + transactionId));
    }
    
    private void validateCreateCommand(CreateTransactionCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Create transaction command cannot be null");
        }
        if (command.getAmount() == null || command.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
        if (command.getCompanyId() == null) {
            throw new IllegalArgumentException("Company ID cannot be null");
        }
        if (command.getUserId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (command.getTransactionDate() == null) {
            throw new IllegalArgumentException("Transaction date cannot be null");
        }
    }
    
    private void validateUpdateCommand(UpdateTransactionCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Update transaction command cannot be null");
        }
        if (command.getAmount() == null || command.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
        if (command.getCompanyId() == null) {
            throw new IllegalArgumentException("Company ID cannot be null");
        }
        if (command.getUserId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }
    
    private void setOptionalFields(TransactionAggregate transaction, CreateTransactionCommand command) {
        if (command.getCategoryId() != null) {
            transaction.setCategory(command.getCategoryId());
        }
        if (command.getDepartmentId() != null) {
            transaction.setDepartment(command.getDepartmentId());
        }
        if (command.getFundId() != null) {
            transaction.setFund(command.getFundId());
        }
        if (Boolean.TRUE.equals(command.getIsRecurring())) {
            transaction.markAsRecurring();
        }
        if (Boolean.TRUE.equals(command.getIsTaxable())) {
            transaction.markAsTaxable();
        }
    }
    
    private void updateOptionalFields(TransactionAggregate transaction, UpdateTransactionCommand command) {
        if (command.getCategoryId() != null) {
            transaction.setCategory(command.getCategoryId());
        }
        if (command.getDepartmentId() != null) {
            transaction.setDepartment(command.getDepartmentId());
        }
        if (command.getFundId() != null) {
            transaction.setFund(command.getFundId());
        }
    }
    
    private TransactionDTO mapToDTO(TransactionAggregate transaction) {
        return TransactionDTO.builder()
                .transactionId(transaction.getTransactionId())
                .amount(transaction.getMoney().getAmount())
                .currency(transaction.getMoney().getCurrencyCode())
                .transactionType(transaction.getTransactionType())
                .status(transaction.getTransactionStatus().getStatus())
                .transactionDate(transaction.getTransactionDate())
                .description(transaction.getDescription())
                .paymentMethod(transaction.getPaymentMethod())
                .referenceNumber(transaction.getReferenceNumber())
                .isRecurring(transaction.getIsRecurring())
                .isTaxable(transaction.getIsTaxable())
                .companyId(transaction.getTenantId().getValue())
                .userId(transaction.getUserId())
                .departmentId(transaction.getDepartmentId())
                .fundId(transaction.getFundId())
                .categoryId(transaction.getCategoryId())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .approvedAt(transaction.getApprovedAt())
                .approvedBy(transaction.getApprovedBy())
                .build();
    }
}