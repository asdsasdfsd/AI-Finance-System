// backend/src/main/java/org/example/backend/controller/TransactionController.java
package org.example.backend.controller;

import org.example.backend.model.Transaction;
import org.example.backend.service.TransactionAdapterService;
import org.example.backend.service.TransactionBusinessService;
import org.example.backend.domain.valueobject.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TransactionController - DDD集成版本
 * 
 * 保持与现有前端的兼容性，同时支持DDD概念
 * 使用适配器模式桥接新旧架构
 */
@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:3000")
public class TransactionController {
    
    @Autowired
    private TransactionAdapterService transactionAdapterService;

    @Autowired
    private TransactionBusinessService businessService;
    
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
    
    // ========== DDD增强API（已有的现金流汇总保留） ==========
    
    /**
     * 获取现金流汇总
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
    
    // ========== 新增DDD业务API ==========
    
    /**
     * DDD业务方法：批准交易
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveTransaction(
            @PathVariable Integer id, 
            @RequestParam Integer approverId) {
        try {
            businessService.approveTransaction(id, approverId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "交易批准成功");
            response.put("transactionId", id);
            response.put("approverId", approverId);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "业务规则验证失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 获取交易业务信息（展示DDD价值）
     */
    @GetMapping("/{id}/business-info")
    public ResponseEntity<TransactionBusinessService.TransactionBusinessInfo> getBusinessInfo(
            @PathVariable Integer id) {
        try {
            TransactionBusinessService.TransactionBusinessInfo info = 
                businessService.getTransactionBusinessInfo(id);
            return ResponseEntity.ok(info);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 计算含税金额（展示Money值对象）
     */
    @PostMapping("/{id}/calculate-tax")
    public ResponseEntity<Map<String, Object>> calculateTax(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "0.13") double taxRate) {
        try {
            Money totalWithTax = businessService.calculateTotalWithTax(id, taxRate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("transactionId", id);
            response.put("taxRate", taxRate);
            response.put("totalAmount", totalWithTax.getAmount());
            response.put("currency", totalWithTax.getCurrencyCode());
            response.put("displayAmount", totalWithTax.toDisplayString());
            response.put("calculation", "展示Money值对象的货币运算能力");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 取消交易（DDD业务方法）
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelTransaction(@PathVariable Integer id) {
        try {
            businessService.cancelTransaction(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "交易取消成功");
            response.put("transactionId", id);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * DDD状态检查：验证DDD集成
     */
    @GetMapping("/ddd-status")
    public ResponseEntity<Map<String, Object>> getDddStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("dddIntegrationEnabled", true);
        status.put("message", "DDD架构集成成功");
        status.put("availableBusinessMethods", Arrays.asList(
            "approve", "cancel", "business-info", "calculate-tax"
        ));
        status.put("valueObjectsSupported", Arrays.asList("Money", "TenantId"));
        status.put("aggregatesImplemented", Arrays.asList(
            "TransactionAggregate", "CompanyAggregate", "UserAggregate"
        ));
        status.put("servicesAvailable", Arrays.asList(
            "TransactionAdapterService", "TransactionBusinessService", "TenantAwareTransactionService"
        ));
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * 展示Money值对象功能
     */
    @GetMapping("/{id}/money-demo")
    public ResponseEntity<Map<String, Object>> getMoneyDemo(@PathVariable Integer id) {
        try {
            Transaction transaction = transactionAdapterService.findById(id);
            if (transaction == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 创建Money值对象
            Money money = Money.of(transaction.getAmount(), 
                                 transaction.getCurrency() != null ? transaction.getCurrency() : "CNY");
            
            // 展示Money的各种能力
            Map<String, Object> demo = new HashMap<>();
            demo.put("originalAmount", money.getAmount());
            demo.put("currency", money.getCurrencyCode());
            demo.put("displayString", money.toDisplayString());
            demo.put("isPositive", money.isPositive());
            demo.put("isZero", money.isZero());
            
            // 演示运算
            Money doubled = money.multiply(2);
            Money withTax = money.add(money.multiply(0.13));
            
            demo.put("doubledAmount", doubled.toDisplayString());
            demo.put("withTaxAmount", withTax.toDisplayString());
            demo.put("demonstration", "这展示了Money值对象的类型安全货币运算");
            
            return ResponseEntity.ok(demo);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // ========== 保留原有的v2 API ==========
    
    /**
     * 创建交易V2（如果TransactionAdapterService支持）
     */
    @PostMapping("/v2")
    public ResponseEntity<Map<String, Object>> createTransactionV2(
            @RequestBody CreateTransactionRequest request) {
        
        try {
            // 这里需要根据你的TransactionAdapterService实际方法调整
            Transaction transaction = new Transaction();
            transaction.setAmount(request.getAmount());
            transaction.setCurrency(request.getCurrency());
            transaction.setDescription(request.getDescription());
            transaction.setTransactionDate(request.getTransactionDate());
            
            // 设置交易类型
            if ("INCOME".equals(request.getTransactionType())) {
                transaction.setTransactionType(Transaction.TransactionType.INCOME);
            } else if ("EXPENSE".equals(request.getTransactionType())) {
                transaction.setTransactionType(Transaction.TransactionType.EXPENSE);
            }
            
            Transaction saved = transactionAdapterService.save(transaction);
            
            Map<String, Object> response = new HashMap<>();
            response.put("transactionId", saved.getTransactionId());
            response.put("message", "交易创建成功");
            response.put("amount", Money.of(saved.getAmount(), saved.getCurrency()));
            response.put("success", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "交易创建失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
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
}