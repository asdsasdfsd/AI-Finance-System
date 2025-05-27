// backend/src/main/java/org/example/backend/service/TransactionAdapterService.java
package org.example.backend.service;

import org.example.backend.domain.aggregate.transaction.Transaction;
import org.example.backend.domain.valueobject.Money;
import org.example.backend.model.Company;
import org.example.backend.model.User;
import org.example.backend.model.Department;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Transaction适配器服务
 * 
 * 作为新DDD架构和现有代码之间的桥梁
 * 提供向后兼容性，同时逐步迁移到新架构
 */
@Service
@Transactional
public class TransactionAdapterService {
    
    @Autowired
    private TransactionService originalTransactionService;
    
    @Autowired
    private CompanyService companyService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private DepartmentService departmentService;
    
    // ========== 查询方法适配 ==========
    
    /**
     * 查找所有交易
     */
    @Transactional(readOnly = true)
    public List<org.example.backend.model.Transaction> findAll() {
        return originalTransactionService.findAll();
    }
    
    /**
     * 根据ID查找交易
     */
    @Transactional(readOnly = true)
    public org.example.backend.model.Transaction findById(Integer id) {
        return originalTransactionService.findById(id);
    }
    
    /**
     * 根据公司ID查找交易
     */
    @Transactional(readOnly = true)
    public List<org.example.backend.model.Transaction> findByCompany(Integer companyId) {
        return originalTransactionService.findByCompany(companyService.findById(companyId));
    }
    
    /**
     * 根据公司和类型查找交易
     */
    @Transactional(readOnly = true)
    public List<org.example.backend.model.Transaction> findByCompanyAndType(
            Integer companyId, org.example.backend.model.Transaction.TransactionType type) {
        Company company = companyService.findById(companyId);
        return originalTransactionService.findByCompanyAndType(company, type);
    }
    
    /**
     * 根据日期范围查找交易
     */
    @Transactional(readOnly = true)
    public List<org.example.backend.model.Transaction> findByDateRange(
            Integer companyId, LocalDate startDate, LocalDate endDate) {
        Company company = companyService.findById(companyId);
        return originalTransactionService.findByDateRange(company, startDate, endDate);
    }
    
    /**
     * 根据公司查找排序后的交易
     */
    @Transactional(readOnly = true)
    public List<org.example.backend.model.Transaction> findByCompanySortedByDate(Integer companyId) {
        Company company = companyService.findById(companyId);
        return originalTransactionService.findByCompanySortedByDate(company);
    }
    
    /**
     * 计算交易总额
     */
    @Transactional(readOnly = true)
    public Double getSum(Integer companyId, org.example.backend.model.Transaction.TransactionType type) {
        Company company = companyService.findById(companyId);
        return originalTransactionService.getSum(company, type);
    }
    
    // ========== 创建和更新方法适配 ==========
    
    /**
     * 保存交易（兼容原有API）
     */
    public org.example.backend.model.Transaction save(org.example.backend.model.Transaction transaction) {
        return originalTransactionService.save(transaction);
    }
    
    /**
     * 删除交易
     */
    public void deleteById(Integer id) {
        originalTransactionService.deleteById(id);
    }
    
    // ========== 新DDD方法的简化版本 ==========
    
    /**
     * 创建新格式的交易（用于渐进式迁移）
     */
    public org.example.backend.model.Transaction createTransaction(CreateTransactionRequest request) {
        // 创建老格式的Transaction对象
        org.example.backend.model.Transaction transaction = new org.example.backend.model.Transaction();
        
        // 设置基本字段
        transaction.setTransactionType(convertTransactionType(request.getTransactionType()));
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setDescription(request.getDescription());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setReferenceNumber(request.getReferenceNumber());
        transaction.setIsRecurring(request.getIsRecurring());
        transaction.setIsTaxable(request.getIsTaxable());
        
        // 设置关联对象
        if (request.getCompanyId() != null) {
            Company company = companyService.findById(request.getCompanyId());
            transaction.setCompany(company);
        }
        
        if (request.getUserId() != null) {
            User user = userService.findById(request.getUserId());
            transaction.setUser(user);
        }
        
        if (request.getDepartmentId() != null) {
            Department department = departmentService.findById(request.getDepartmentId());
            transaction.setDepartment(department);
        }
        
        // 保存交易
        return originalTransactionService.save(transaction);
    }
    
    /**
     * 计算现金流汇总（简化版）
     */
    @Transactional(readOnly = true)
    public CashFlowSummary getCashFlowSummary(Integer companyId, LocalDate startDate, 
                                            LocalDate endDate, String currency) {
        Company company = companyService.findById(companyId);
        List<org.example.backend.model.Transaction> transactions = 
            originalTransactionService.findByDateRange(company, startDate, endDate);
        
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        
        for (org.example.backend.model.Transaction transaction : transactions) {
            if (currency.equals(transaction.getCurrency())) {
                if (transaction.getTransactionType() == org.example.backend.model.Transaction.TransactionType.INCOME) {
                    totalIncome = totalIncome.add(transaction.getAmount());
                } else {
                    totalExpense = totalExpense.add(transaction.getAmount());
                }
            }
        }
        
        BigDecimal netCashFlow = totalIncome.subtract(totalExpense);
        
        return new CashFlowSummary(
            Money.of(totalIncome, currency),
            Money.of(totalExpense, currency),
            Money.of(netCashFlow, currency),
            startDate,
            endDate
        );
    }
    
    // ========== 类型转换方法 ==========
    
    private org.example.backend.model.Transaction.TransactionType convertTransactionType(
            Transaction.TransactionType newType) {
        if (newType == null) return null;
        
        switch (newType) {
            case INCOME:
                return org.example.backend.model.Transaction.TransactionType.INCOME;
            case EXPENSE:
                return org.example.backend.model.Transaction.TransactionType.EXPENSE;
            default:
                throw new IllegalArgumentException("不支持的交易类型: " + newType);
        }
    }
    
    private Transaction.TransactionType convertFromOldTransactionType(
            org.example.backend.model.Transaction.TransactionType oldType) {
        if (oldType == null) return null;
        
        switch (oldType) {
            case INCOME:
                return Transaction.TransactionType.INCOME;
            case EXPENSE:
                return Transaction.TransactionType.EXPENSE;
            default:
                throw new IllegalArgumentException("不支持的交易类型: " + oldType);
        }
    }
    
    // ========== 内部DTO类 ==========
    
    public static class CreateTransactionRequest {
        private Transaction.TransactionType transactionType;
        private BigDecimal amount;
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
        public Transaction.TransactionType getTransactionType() { return transactionType; }
        public void setTransactionType(Transaction.TransactionType transactionType) { this.transactionType = transactionType; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
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
    
    public static class CashFlowSummary {
        private final Money totalIncome;
        private final Money totalExpense;
        private final Money netCashFlow;
        private final LocalDate startDate;
        private final LocalDate endDate;
        
        public CashFlowSummary(Money totalIncome, Money totalExpense, Money netCashFlow, 
                              LocalDate startDate, LocalDate endDate) {
            this.totalIncome = totalIncome;
            this.totalExpense = totalExpense;
            this.netCashFlow = netCashFlow;
            this.startDate = startDate;
            this.endDate = endDate;
        }
        
        public Money getTotalIncome() { return totalIncome; }
        public Money getTotalExpense() { return totalExpense; }
        public Money getNetCashFlow() { return netCashFlow; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        
        public boolean isPositive() {
            return netCashFlow.isPositive();
        }
        
        public boolean isNegative() {
            return netCashFlow.isNegative();
        }
        
        @Override
        public String toString() {
            return String.format("CashFlowSummary{income=%s, expense=%s, net=%s, period=%s to %s}", 
                               totalIncome, totalExpense, netCashFlow, startDate, endDate);
        }
    }
}