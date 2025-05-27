// backend/src/main/java/org/example/backend/service/TransactionService.java
package org.example.backend.service;

import org.example.backend.domain.aggregate.transaction.Transaction;
import org.example.backend.domain.valueobject.Money;
import org.example.backend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Transaction 领域服务
 * 
 * 职责：
 * 1. 协调Transaction聚合的业务操作
 * 2. 实现跨聚合的业务规则
 * 3. 管理事务边界
 * 4. 触发领域事件
 */
@Service
@Transactional
public class TransactionService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private CompanyService companyService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private FundService fundService;
    
    // ========== 创建操作 ==========
    
    /**
     * 创建收入交易
     */
    public Transaction createIncome(CreateTransactionCommand command) {
        validateTransactionCreationCommand(command);
        
        Money amount = Money.of(command.getAmount(), command.getCurrency());
        Transaction transaction = Transaction.createIncome(
            amount, 
            command.getTransactionDate(), 
            command.getDescription(),
            command.getCompanyId(), 
            command.getUserId()
        );
        
        // 设置可选字段
        setOptionalFields(transaction, command);
        
        // 保存聚合（会自动发布领域事件）
        return transactionRepository.save(transaction);
    }
    
    /**
     * 创建支出交易
     */
    public Transaction createExpense(CreateTransactionCommand command) {
        validateTransactionCreationCommand(command);
        
        Money amount = Money.of(command.getAmount(), command.getCurrency());
        Transaction transaction = Transaction.createExpense(
            amount, 
            command.getTransactionDate(), 
            command.getDescription(),
            command.getCompanyId(), 
            command.getUserId()
        );
        
        setOptionalFields(transaction, command);
        
        return transactionRepository.save(transaction);
    }
    
    /**
     * 批量创建交易（例如导入场景）
     */
    public List<Transaction> createTransactionsBatch(List<CreateTransactionCommand> commands) {
        // 验证批量数据
        if (commands == null || commands.isEmpty()) {
            throw new IllegalArgumentException("批量交易数据不能为空");
        }
        
        if (commands.size() > 1000) {
            throw new IllegalArgumentException("单次批量导入不能超过1000条记录");
        }
        
        return commands.stream()
                .map(cmd -> {
                    if (cmd.getTransactionType() == Transaction.TransactionType.INCOME) {
                        return createIncome(cmd);
                    } else {
                        return createExpense(cmd);
                    }
                })
                .toList();
    }
    
    // ========== 更新操作 ==========
    
    /**
     * 更新交易信息
     */
    public Transaction updateTransaction(Integer transactionId, UpdateTransactionCommand command) {
        Transaction transaction = findTransactionById(transactionId);
        validateTransactionUpdateCommand(command);
        
        Money newAmount = Money.of(command.getAmount(), command.getCurrency());
        transaction.updateTransaction(
            newAmount,
            command.getDescription(),
            command.getPaymentMethod(),
            command.getReferenceNumber()
        );
        
        // 更新可选字段
        if (command.getDepartmentId() != null) {
            validateDepartmentExists(command.getDepartmentId());
            transaction.setDepartment(command.getDepartmentId());
        }
        
        if (command.getCategoryId() != null) {
            validateCategoryExists(command.getCategoryId());
            transaction.setCategory(command.getCategoryId());
        }
        
        if (command.getFundId() != null) {
            validateFundExists(command.getFundId());
            transaction.setFund(command.getFundId());
        }
        
        return transactionRepository.save(transaction);
    }
    
    /**
     * 批准交易
     */
    public Transaction approveTransaction(Integer transactionId, Integer approverUserId) {
        Transaction transaction = findTransactionById(transactionId);
        validateApprover(approverUserId, transaction.getCompanyId());
        
        transaction.approve(approverUserId);
        return transactionRepository.save(transaction);
    }
    
    /**
     * 取消交易
     */
    public Transaction cancelTransaction(Integer transactionId) {
        Transaction transaction = findTransactionById(transactionId);
        transaction.cancel();
        return transactionRepository.save(transaction);
    }
    
    /**
     * 作废交易
     */
    public Transaction voidTransaction(Integer transactionId, Integer voidedBy, String reason) {
        Transaction transaction = findTransactionById(transactionId);
        validateVoidPermission(voidedBy, transaction.getCompanyId());
        
        transaction.void_(voidedBy, reason);
        return transactionRepository.save(transaction);
    }
    
    // ========== 查询操作 ==========
    
    @Transactional(readOnly = true)
    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Transaction findById(Integer id) {
        return findTransactionById(id);
    }
    
    @Transactional(readOnly = true)
    public List<Transaction> findByCompany(Integer companyId) {
        validateCompanyExists(companyId);
        return transactionRepository.findByCompanyId(companyId);
    }
    
    @Transactional(readOnly = true)
    public List<Transaction> findByCompanyAndType(Integer companyId, Transaction.TransactionType type) {
        validateCompanyExists(companyId);
        return transactionRepository.findByCompanyIdAndTransactionType(companyId, type);
    }
    
    @Transactional(readOnly = true)
    public List<Transaction> findByDateRange(Integer companyId, LocalDate startDate, LocalDate endDate) {
        validateCompanyExists(companyId);
        validateDateRange(startDate, endDate);
        return transactionRepository.findByCompanyIdAndTransactionDateBetween(companyId, startDate, endDate);
    }
    
    @Transactional(readOnly = true)
    public List<Transaction> findByCompanySortedByDate(Integer companyId) {
        validateCompanyExists(companyId);
        return transactionRepository.findByCompanyIdOrderByTransactionDateDesc(companyId);
    }
    
    /**
     * 计算公司某类型交易的总金额
     */
    @Transactional(readOnly = true)
    public Money calculateTotalAmount(Integer companyId, Transaction.TransactionType type, String currency) {
        validateCompanyExists(companyId);
        
        List<Transaction> transactions = transactionRepository.findByCompanyIdAndTransactionTypeAndStatus(
            companyId, type, Transaction.TransactionStatus.APPROVED);
        
        Money total = Money.zero(currency);
        for (Transaction transaction : transactions) {
            if (transaction.getMoney().getCurrencyCode().equals(currency)) {
                total = total.add(transaction.getMoney());
            }
        }
        
        return total;
    }
    
    /**
     * 获取公司的现金流概览
     */
    @Transactional(readOnly = true)
    public CashFlowSummary getCashFlowSummary(Integer companyId, LocalDate startDate, LocalDate endDate, String currency) {
        validateCompanyExists(companyId);
        validateDateRange(startDate, endDate);
        
        List<Transaction> transactions = transactionRepository.findByCompanyIdAndTransactionDateBetweenAndStatus(
            companyId, startDate, endDate, Transaction.TransactionStatus.APPROVED);
        
        Money totalIncome = Money.zero(currency);
        Money totalExpense = Money.zero(currency);
        
        for (Transaction transaction : transactions) {
            if (transaction.getMoney().getCurrencyCode().equals(currency)) {
                if (transaction.getTransactionType() == Transaction.TransactionType.INCOME) {
                    totalIncome = totalIncome.add(transaction.getMoney());
                } else {
                    totalExpense = totalExpense.add(transaction.getMoney());
                }
            }
        }
        
        Money netCashFlow = totalIncome.subtract(totalExpense);
        
        return new CashFlowSummary(totalIncome, totalExpense, netCashFlow, startDate, endDate);
    }
    
    // ========== 删除操作 ==========
    
    public void deleteById(Integer id) {
        Transaction transaction = findTransactionById(id);
        
        // 业务规则：只有草稿状态的交易才能删除
        if (!transaction.canModify()) {
            throw new IllegalStateException("只有草稿状态的交易才能删除");
        }
        
        transactionRepository.deleteById(id);
    }
    
    // ========== 验证方法 ==========
    
    private Transaction findTransactionById(Integer id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("交易不存在，ID: " + id));
    }
    
    private void validateTransactionCreationCommand(CreateTransactionCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("创建交易命令不能为空");
        }
        
        if (command.getAmount() == null || command.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("交易金额必须大于0");
        }
        
        if (command.getCurrency() == null || command.getCurrency().trim().isEmpty()) {
            throw new IllegalArgumentException("货币代码不能为空");
        }
        
        if (command.getTransactionDate() == null) {
            throw new IllegalArgumentException("交易日期不能为空");
        }
        
        if (command.getCompanyId() == null) {
            throw new IllegalArgumentException("公司ID不能为空");
        }
        
        if (command.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        // 验证关联实体存在
        validateCompanyExists(command.getCompanyId());
        validateUserExists(command.getUserId());
        
        if (command.getDepartmentId() != null) {
            validateDepartmentExists(command.getDepartmentId());
        }
        
        if (command.getCategoryId() != null) {
            validateCategoryExists(command.getCategoryId());
        }
        
        if (command.getFundId() != null) {
            validateFundExists(command.getFundId());
        }
    }
    
    private void validateTransactionUpdateCommand(UpdateTransactionCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("更新交易命令不能为空");
        }
        
        if (command.getAmount() != null && command.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("交易金额必须大于0");
        }
    }
    
    private void validateCompanyExists(Integer companyId) {
        if (companyService.findById(companyId) == null) {
            throw new IllegalArgumentException("公司不存在，ID: " + companyId);
        }
    }
    
    private void validateUserExists(Integer userId) {
        if (userService.findById(userId) == null) {
            throw new IllegalArgumentException("用户不存在，ID: " + userId);
        }
    }
    
    private void validateDepartmentExists(Integer departmentId) {
        if (departmentService.findById(departmentId) == null) {
            throw new IllegalArgumentException("部门不存在，ID: " + departmentId);
        }
    }
    
    private void validateCategoryExists(Integer categoryId) {
        if (categoryService.findById(categoryId) == null) {
            throw new IllegalArgumentException("分类不存在，ID: " + categoryId);
        }
    }
    
    private void validateFundExists(Integer fundId) {
        if (fundService.findById(fundId) == null) {
            throw new IllegalArgumentException("基金不存在，ID: " + fundId);
        }
    }
    
    private void validateApprover(Integer approverUserId, Integer companyId) {
        validateUserExists(approverUserId);
        // 可以添加权限检查：approver必须有批准权限
        // 可以添加公司检查：approver必须属于同一公司
    }
    
    private void validateVoidPermission(Integer voidedBy, Integer companyId) {
        validateUserExists(voidedBy);
        // 可以添加权限检查：用户必须有作废权限
    }
    
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("起始日期和结束日期不能为空");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("起始日期不能晚于结束日期");
        }
    }
    
    private void setOptionalFields(Transaction transaction, CreateTransactionCommand command) {
        if (command.getPaymentMethod() != null) {
            // transaction.setPaymentMethod() - 需要添加到聚合中
        }
        
        if (command.getReferenceNumber() != null) {
            // transaction.setReferenceNumber() - 需要添加到聚合中
        }
        
        if (command.getDepartmentId() != null) {
            transaction.setDepartment(command.getDepartmentId());
        }
        
        if (command.getCategoryId() != null) {
            transaction.setCategory(command.getCategoryId());
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
    
    // ========== 内部类：命令对象 ==========
    
    public static class CreateTransactionCommand {
        private Transaction.TransactionType transactionType;
        private BigDecimal amount;
        private String currency;
        private LocalDate transactionDate;
        private String description;
        private Integer companyId;
        private Integer userId;
        private Integer departmentId;
        private Integer categoryId;
        private Integer fundId;
        private String paymentMethod;
        private String referenceNumber;
        private Boolean isRecurring;
        private Boolean isTaxable;
        
        // 构造函数和getter/setter方法...
        public CreateTransactionCommand(Transaction.TransactionType transactionType, BigDecimal amount, 
                                      String currency, LocalDate transactionDate, String description,
                                      Integer companyId, Integer userId) {
            this.transactionType = transactionType;
            this.amount = amount;
            this.currency = currency;
            this.transactionDate = transactionDate;
            this.description = description;
            this.companyId = companyId;
            this.userId = userId;
        }
        
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
    
    public static class UpdateTransactionCommand {
        private BigDecimal amount;
        private String currency;
        private String description;
        private String paymentMethod;
        private String referenceNumber;
        private Integer departmentId;
        private Integer categoryId;
        private Integer fundId;
        
        // Getters and Setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        
        public String getReferenceNumber() { return referenceNumber; }
        public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
        
        public Integer getDepartmentId() { return departmentId; }
        public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
        
        public Integer getCategoryId() { return categoryId; }
        public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
        
        public Integer getFundId() { return fundId; }
        public void setFundId(Integer fundId) { this.fundId = fundId; }
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