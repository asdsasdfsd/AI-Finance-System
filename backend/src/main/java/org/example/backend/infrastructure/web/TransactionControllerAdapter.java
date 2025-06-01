// backend/src/main/java/org/example/backend/infrastructure/web/TransactionControllerAdapter.java
package org.example.backend.infrastructure.web;

import org.example.backend.application.service.TransactionApplicationService;
import org.example.backend.application.dto.CreateTransactionCommand;
import org.example.backend.application.dto.UpdateTransactionCommand;
import org.example.backend.application.dto.ApproveTransactionCommand;
import org.example.backend.application.dto.TransactionDTO;
import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.valueobject.TransactionStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Transaction Controller Adapter
 * 
 * Adapts REST API calls to DDD Application Service commands
 * while maintaining backward compatibility with existing frontend
 */
@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:3000")
@Profile("ddd")
public class TransactionControllerAdapter {
    
    private final TransactionApplicationService transactionApplicationService;
    
    public TransactionControllerAdapter(TransactionApplicationService transactionApplicationService) {
        this.transactionApplicationService = transactionApplicationService;
    }
    
    // ========== Create Operations ==========
    
    /**
     * Create new transaction (backward compatible)
     */
    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@RequestBody Map<String, Object> request) {
        CreateTransactionCommand command = mapToCreateCommand(request);
        
        // Determine transaction type from request or default to expense
        String typeStr = (String) request.getOrDefault("transactionType", "EXPENSE");
        
        TransactionDTO result;
        if ("INCOME".equals(typeStr)) {
            result = transactionApplicationService.createIncomeTransaction(command);
        } else {
            result = transactionApplicationService.createExpenseTransaction(command);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Create income transaction
     */
    @PostMapping("/income")
    public ResponseEntity<TransactionDTO> createIncomeTransaction(@RequestBody Map<String, Object> request) {
        CreateTransactionCommand command = mapToCreateCommand(request);
        TransactionDTO result = transactionApplicationService.createIncomeTransaction(command);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Create expense transaction
     */
    @PostMapping("/expense")
    public ResponseEntity<TransactionDTO> createExpenseTransaction(@RequestBody Map<String, Object> request) {
        CreateTransactionCommand command = mapToCreateCommand(request);
        TransactionDTO result = transactionApplicationService.createExpenseTransaction(command);
        return ResponseEntity.ok(result);
    }
    
    // ========== Update Operations ==========
    
    /**
     * Update transaction (backward compatible)
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionDTO> updateTransaction(@PathVariable Integer id, 
                                                          @RequestBody Map<String, Object> request) {
        UpdateTransactionCommand command = mapToUpdateCommand(request);
        TransactionDTO result = transactionApplicationService.updateTransaction(id, command);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Approve transaction
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<TransactionDTO> approveTransaction(@PathVariable Integer id,
                                                           @RequestBody Map<String, Object> request) {
        ApproveTransactionCommand command = ApproveTransactionCommand.builder()
                .companyId((Integer) request.get("companyId"))
                .approverUserId((Integer) request.get("approverUserId"))
                .approvalNote((String) request.get("approvalNote"))
                .build();
        
        TransactionDTO result = transactionApplicationService.approveTransaction(id, command);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Cancel transaction
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<TransactionDTO> cancelTransaction(@PathVariable Integer id,
                                                          @RequestBody Map<String, Object> request) {
        Integer companyId = (Integer) request.get("companyId");
        Integer userId = (Integer) request.get("userId");
        
        TransactionDTO result = transactionApplicationService.cancelTransaction(id, companyId, userId);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Void transaction
     */
    @PostMapping("/{id}/void")
    public ResponseEntity<TransactionDTO> voidTransaction(@PathVariable Integer id,
                                                        @RequestBody Map<String, Object> request) {
        Integer companyId = (Integer) request.get("companyId");
        Integer voidedBy = (Integer) request.get("voidedBy");
        String reason = (String) request.get("reason");
        
        TransactionDTO result = transactionApplicationService.voidTransaction(id, companyId, voidedBy, reason);
        return ResponseEntity.ok(result);
    }
    
    // ========== Query Operations (Backward Compatible) ==========
    
    /**
     * Get all transactions (backward compatible)
     */
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions(@RequestParam(required = false) Integer companyId) {
        if (companyId == null) {
            throw new IllegalArgumentException("Company ID is required");
        }
        
        List<TransactionDTO> transactions = transactionApplicationService.getTransactionsByCompany(companyId);
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Get transaction by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable Integer id,
                                                           @RequestParam Integer companyId) {
        TransactionDTO transaction = transactionApplicationService.getTransactionById(id, companyId);
        return ResponseEntity.ok(transaction);
    }
    
    /**
     * Get transactions by company (backward compatible)
     */
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByCompany(@PathVariable Integer companyId) {
        List<TransactionDTO> transactions = transactionApplicationService.getTransactionsByCompany(companyId);
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Get transactions by company sorted by date (backward compatible)
     */
    @GetMapping("/company/{companyId}/sorted")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByCompanySorted(@PathVariable Integer companyId) {
        List<TransactionDTO> transactions = transactionApplicationService.getTransactionsByCompany(companyId);
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Get transactions by company and type (backward compatible)
     */
    @GetMapping("/company/{companyId}/type/{type}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByCompanyAndType(@PathVariable Integer companyId,
                                                                              @PathVariable String type) {
        TransactionAggregate.TransactionType transactionType = TransactionAggregate.TransactionType.valueOf(type.toUpperCase());
        List<TransactionDTO> transactions = transactionApplicationService.getTransactionsByType(companyId, transactionType);
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Get transactions by date range (backward compatible)
     */
    @GetMapping("/company/{companyId}/date-range")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByDateRange(
            @PathVariable Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<TransactionDTO> transactions = transactionApplicationService.getTransactionsByDateRange(companyId, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Get sum by company and type (backward compatible)
     */
    @GetMapping("/company/{companyId}/type/{type}/sum")
    public ResponseEntity<BigDecimal> getSumByCompanyAndType(@PathVariable Integer companyId,
                                                           @PathVariable String type) {
        TransactionAggregate.TransactionType transactionType = TransactionAggregate.TransactionType.valueOf(type.toUpperCase());
        BigDecimal sum = transactionApplicationService.getTotalAmount(companyId, transactionType, TransactionStatus.Status.APPROVED);
        return ResponseEntity.ok(sum != null ? sum : BigDecimal.ZERO);
    }
    
    /**
     * Get user's transactions (backward compatible)
     */
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<TransactionDTO>> getUserTransactionsByType(@PathVariable Integer userId,
                                                                         @PathVariable String type,
                                                                         @RequestParam Integer companyId) {
        List<TransactionDTO> transactions = transactionApplicationService.getUserTransactions(companyId, userId);
        
        // Filter by type if needed
        TransactionAggregate.TransactionType transactionType = TransactionAggregate.TransactionType.valueOf(type.toUpperCase());
        List<TransactionDTO> filteredTransactions = transactions.stream()
                .filter(t -> t.getTransactionType() == transactionType)
                .toList();
        
        return ResponseEntity.ok(filteredTransactions);
    }
    
    /**
     * Get pending approval transactions
     */
    @GetMapping("/company/{companyId}/pending-approval")
    public ResponseEntity<List<TransactionDTO>> getPendingApprovalTransactions(@PathVariable Integer companyId) {
        List<TransactionDTO> transactions = transactionApplicationService.getPendingApprovalTransactions(companyId);
        return ResponseEntity.ok(transactions);
    }
    
    // ========== Delete Operations (Soft Delete) ==========
    
    /**
     * Delete transaction (backward compatible - actually cancels)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Integer id,
                                                @RequestParam Integer companyId,
                                                @RequestParam Integer userId) {
        // In DDD, we don't delete but cancel transactions
        transactionApplicationService.cancelTransaction(id, companyId, userId);
        return ResponseEntity.noContent().build();
    }
    
    // ========== Helper Methods ==========
    
    private CreateTransactionCommand mapToCreateCommand(Map<String, Object> request) {
        return CreateTransactionCommand.builder()
                .amount(new BigDecimal(request.get("amount").toString()))
                .currency((String) request.getOrDefault("currency", "CNY"))
                .transactionDate(parseDate(request.get("transactionDate")))
                .description((String) request.get("description"))
                .paymentMethod((String) request.get("paymentMethod"))
                .referenceNumber((String) request.get("referenceNumber"))
                .isRecurring((Boolean) request.get("isRecurring"))
                .isTaxable((Boolean) request.get("isTaxable"))
                .companyId(getCompanyId(request))
                .userId(getUserId(request))
                .departmentId((Integer) request.get("departmentId"))
                .fundId((Integer) request.get("fundId"))
                .categoryId((Integer) request.get("categoryId"))
                .build();
    }
    
    private UpdateTransactionCommand mapToUpdateCommand(Map<String, Object> request) {
        return UpdateTransactionCommand.builder()
                .amount(new BigDecimal(request.get("amount").toString()))
                .currency((String) request.getOrDefault("currency", "CNY"))
                .description((String) request.get("description"))
                .paymentMethod((String) request.get("paymentMethod"))
                .referenceNumber((String) request.get("referenceNumber"))
                .companyId(getCompanyId(request))
                .userId(getUserId(request))
                .departmentId((Integer) request.get("departmentId"))
                .fundId((Integer) request.get("fundId"))
                .categoryId((Integer) request.get("categoryId"))
                .build();
    }
    
    private LocalDate parseDate(Object dateObj) {
        if (dateObj == null) {
            return LocalDate.now();
        }
        
        if (dateObj instanceof String) {
            return LocalDate.parse((String) dateObj);
        }
        
        if (dateObj instanceof LocalDate) {
            return (LocalDate) dateObj;
        }
        
        throw new IllegalArgumentException("Invalid date format: " + dateObj);
    }
    
    private Integer getCompanyId(Map<String, Object> request) {
        // Try different field names for backward compatibility
        Object companyId = request.get("companyId");
        if (companyId == null) {
            companyId = request.get("company_id");
        }
        if (companyId == null) {
            // Extract from company object if present
            Object company = request.get("company");
            if (company instanceof Map) {
                companyId = ((Map<?, ?>) company).get("companyId");
            }
        }
        
        if (companyId == null) {
            throw new IllegalArgumentException("Company ID is required");
        }
        
        return (Integer) companyId;
    }
    
    private Integer getUserId(Map<String, Object> request) {
        // Try different field names for backward compatibility
        Object userId = request.get("userId");
        if (userId == null) {
            userId = request.get("user_id");
        }
        if (userId == null) {
            // Extract from user object if present
            Object user = request.get("user");
            if (user instanceof Map) {
                userId = ((Map<?, ?>) user).get("userId");
            }
        }
        
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        return (Integer) userId;
    }
}