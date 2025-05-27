// backend/src/main/java/org/example/backend/controller/TransactionController.java
package org.example.backend.controller;

import org.example.backend.model.Transaction;
import org.example.backend.service.TransactionAdapterService;
import org.example.backend.domain.valueobject.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * TransactionController - 简化版本
 * 
 * 保持与现有前端的兼容性，同时逐步引入DDD概念
 * 使用适配器模式桥接新旧架构
 */
@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:3000")
public class TransactionController {
    
    @Autowired
    private TransactionAdapterService transactionAdapterService;
    
    // ========== 原有API保持不变 ==========
    
    /**
     * 获取所有交易
     */
    @GetMapping
    public List<Transaction> getAll() {
        return transactionAdapterService.findAll();
    }
    
    /**
     * 根据ID获取交易
     */
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getById(@PathVariable Integer id) {
        Transaction transaction = transactionAdapterService.findById(id);
        return transaction != null ? ResponseEntity.ok(transaction) : ResponseEntity.notFound().build();
    }
    
    /**
     * 根据公司和类型获取交易
     */
    @GetMapping("/company/{companyId}/type/{type}")
    public List<Transaction> getByCompanyAndType(
            @PathVariable Integer companyId,
            @PathVariable Transaction.TransactionType type) {
        return transactionAdapterService.findByCompanyAndType(companyId, type);
    }
    
    /**
     * 根据公司ID获取交易
     */
    @GetMapping("/company/{companyId}")
    public List<Transaction> getByCompany(@PathVariable Integer companyId) {
        return transactionAdapterService.findByCompany(companyId);
    }
    
    /**
     * 根据公司ID获取排序后的交易
     */
    @GetMapping("/company/{companyId}/sorted")
    public List<Transaction> getByCompanySorted(@PathVariable Integer companyId) {
        return transactionAdapterService.findByCompanySortedByDate(companyId);
    }
    
    /**
     * 根据日期范围获取交易
     */
    @GetMapping("/company/{companyId}/date-range")
    public List<Transaction> getByDateRange(
            @PathVariable Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return transactionAdapterService.findByDateRange(companyId, startDate, endDate);
    }
    
    /**
     * 计算指定类型交易总额
     */
    @GetMapping("/company/{companyId}/type/{type}/sum")
    public ResponseEntity<Double> getSumByCompanyAndType(
            @PathVariable Integer companyId,
            @PathVariable Transaction.TransactionType type) {
        Double sum = transactionAdapterService.getSum(companyId, type);
        return ResponseEntity.ok(sum != null ? sum : 0.0);
    }
    
    /**
     * 创建交易（兼容原有API）
     */
    @PostMapping
    public Transaction create(@RequestBody Transaction transaction) {
        return transactionAdapterService.save(transaction);
    }
    
    /**
     * 更新交易
     */
    @PutMapping("/{id}")
    public ResponseEntity<Transaction> update(@PathVariable Integer id, @RequestBody Transaction transaction) {
        if (transactionAdapterService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        transaction.setTransactionId(id);
        return ResponseEntity.ok(transactionAdapterService.save(transaction));
    }
    
    /**
     * 删除交易
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (transactionAdapterService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        transactionAdapterService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    // ========== 新增的DDD增强API ==========
    
    /**
     * 获取现金流汇总（新功能）
     */
    @GetMapping("/company/{companyId}/cash-flow-summary")
    public ResponseEntity<TransactionAdapterService.CashFlowSummary> getCashFlowSummary(
            @PathVariable Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "CNY") String currency) {
        
        TransactionAdapterService.CashFlowSummary summary = 
            transactionAdapterService.getCashFlowSummary(companyId, startDate, endDate, currency);
        
        return ResponseEntity.ok(summary);
    }
    
    /**
     * 创建交易（新格式，支持更多验证）
     */
    @PostMapping("/v2")
    public ResponseEntity<TransactionCreationResponse> createTransactionV2(
            @RequestBody CreateTransactionRequest request) {
        
        try {
            TransactionAdapterService.CreateTransactionRequest adapterRequest = 
                convertToAdapterRequest(request);
            
            Transaction transaction = transactionAdapterService.createTransaction(adapterRequest);
            
            TransactionCreationResponse response = new TransactionCreationResponse();
            response.setTransactionId(transaction.getTransactionId());
            response.setMessage("交易创建成功");
            response.setAmount(Money.of(transaction.getAmount(), transaction.getCurrency()));
            response.setSuccess(true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            TransactionCreationResponse errorResponse = new TransactionCreationResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("交易创建失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // ========== 辅助方法 ==========
    
    private TransactionAdapterService.CreateTransactionRequest convertToAdapterRequest(
            CreateTransactionRequest request) {
        
        TransactionAdapterService.CreateTransactionRequest adapterRequest = 
            new TransactionAdapterService.CreateTransactionRequest();
        
        // 转换交易类型
        if (request.getTransactionType() != null) {
            switch (request.getTransactionType()) {
                case "INCOME":
                    adapterRequest.setTransactionType(
                        org.example.backend.domain.aggregate.transaction.Transaction.TransactionType.INCOME);
                    break;
                case "EXPENSE":
                    adapterRequest.setTransactionType(
                        org.example.backend.domain.aggregate.transaction.Transaction.TransactionType.EXPENSE);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的交易类型: " + request.getTransactionType());
            }
        }
        
        // 设置其他字段
        adapterRequest.setAmount(request.getAmount());
        adapterRequest.setCurrency(request.getCurrency());
        adapterRequest.setTransactionDate(request.getTransactionDate());
        adapterRequest.setDescription(request.getDescription());
        adapterRequest.setCompanyId(request.getCompanyId());
        adapterRequest.setUserId(request.getUserId());
        adapterRequest.setDepartmentId(request.getDepartmentId());
        adapterRequest.setCategoryId(request.getCategoryId());
        adapterRequest.setFundId(request.getFundId());
        adapterRequest.setPaymentMethod(request.getPaymentMethod());
        adapterRequest.setReferenceNumber(request.getReferenceNumber());
        adapterRequest.setIsRecurring(request.getIsRecurring());
        adapterRequest.setIsTaxable(request.getIsTaxable());
        
        return adapterRequest;
    }
    
    // ========== DTO类 ==========
    
    public static class CreateTransactionRequest {
        private String transactionType;
        private java.math.BigDecimal amount;
        private String currency = "CNY";
        private LocalDate transactionDate;
        private String description;
        private Integer companyId;
        private Integer userId;
        private Integer departmentId;
        private Integer categoryId;
        private Integer fundId;
        private String paymentMethod;
        private String referenceNumber;
        private Boolean isRecurring = false;
        private Boolean isTaxable = false;
        
        // Getters and Setters
        public String getTransactionType() { return transactionType; }
        public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
        
        public java.math.BigDecimal getAmount() { return amount; }
        public void setAmount(java.math.BigDecimal amount) { this.amount = amount; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public LocalDate getTransactionDate() { return transactionDate; }
        public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Integer getCompanyId() { return companyId; }
        public void setCompanyId(Integer companyId) { this.companyId = companyId; }
        
        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
        
        public Integer getDepartmentId() { return departmentId; }
        public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
        
        public Integer getCategoryId() { return categoryId; }
        public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
        
        public Integer getFundId() { return fundId; }
        public void setFundId(Integer fundId) { this.fundId = fundId; }
        
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        
        public String getReferenceNumber() { return referenceNumber; }
        public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
        
        public Boolean getIsRecurring() { return isRecurring; }
        public void setIsRecurring(Boolean isRecurring) { this.isRecurring = isRecurring; }
        
        public Boolean getIsTaxable() { return isTaxable; }
        public void setIsTaxable(Boolean isTaxable) { this.isTaxable = isTaxable; }
    }
    
    public static class TransactionCreationResponse {
        private Integer transactionId;
        private String message;
        private Money amount;
        private Boolean success;
        
        // Getters and Setters
        public Integer getTransactionId() { return transactionId; }
        public void setTransactionId(Integer transactionId) { this.transactionId = transactionId; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Money getAmount() { return amount; }
        public void setAmount(Money amount) { this.amount = amount; }
        
        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }
    }
}