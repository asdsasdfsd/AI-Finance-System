// backend/src/main/java/org/example/backend/domain/aggregate/transaction/Transaction.java
package org.example.backend.domain.aggregate.transaction;

import org.example.backend.domain.valueobject.Money;
import org.example.backend.domain.event.TransactionCreatedEvent;
import org.example.backend.domain.event.TransactionApprovedEvent;
import org.example.backend.domain.event.TransactionCancelledEvent;
import org.example.backend.model.User;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

import jakarta.persistence.*;

import org.example.backend.model.Company;
import org.example.backend.model.Department;
import org.example.backend.model.Fund;
import org.example.backend.model.Category;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Transaction 聚合根
 * 
 * 聚合边界：
 * - Transaction (聚合根)
 * - TransactionLine (内部实体)
 * - 外部引用：Company, User, Department等通过ID引用
 * 
 * 业务不变量：
 * 1. 交易金额必须大于0
 * 2. 交易必须有有效的分类
 * 3. 已批准的交易不能修改
 * 4. 取消的交易不能再次操作
 */
@Entity
@Table(name = "Transaction")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transactionId;
    
    // 使用Money值对象
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "amount")),
        @AttributeOverride(name = "currencyCode", column = @Column(name = "currency"))
    })
    private Money money;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;
    
    @Column(nullable = false)
    private LocalDate transactionDate;
    
    @Column(length = 500)
    private String description;
    
    private String paymentMethod;
    private String referenceNumber;
    private Boolean isRecurring;
    private Boolean isTaxable;
    
    // 外部聚合引用 - 只保存ID，避免聚合边界泄露
    @Column(name = "company_id", nullable = false)
    private Integer companyId;
    
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    @Column(name = "department_id")
    private Integer departmentId;
    
    @Column(name = "fund_id")
    private Integer fundId;
    
    @Column(name = "category_id")
    private Integer categoryId;
    
    // 审计字段
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime approvedAt;
    private Integer approvedBy;
    
    // 领域事件
    @Transient
    private List<Object> domainEvents = new ArrayList<>();
    
    // ========== 构造函数 ==========
    
    protected Transaction() {
        // JPA需要
    }
    
    private Transaction(TransactionType type, Money money, LocalDate transactionDate, 
                       String description, Integer companyId, Integer userId) {
        // 业务规则验证
        validateTransactionCreation(type, money, transactionDate, companyId, userId);
        
        this.transactionType = type;
        this.money = money;
        this.transactionDate = transactionDate;
        this.description = description;
        this.companyId = companyId;
        this.userId = userId;
        this.status = TransactionStatus.DRAFT;
        this.isRecurring = false;
        this.isTaxable = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // 发布领域事件
        addDomainEvent(new TransactionCreatedEvent(this.transactionId, type, money, companyId));
    }
    
    // ========== 工厂方法 ==========
    
    /**
     * 创建收入交易
     */
    public static Transaction createIncome(Money amount, LocalDate date, String description,
                                         Integer companyId, Integer userId) {
        return new Transaction(TransactionType.INCOME, amount, date, description, companyId, userId);
    }
    
    /**
     * 创建支出交易
     */
    public static Transaction createExpense(Money amount, LocalDate date, String description,
                                          Integer companyId, Integer userId) {
        return new Transaction(TransactionType.EXPENSE, amount, date, description, companyId, userId);
    }
    
    // ========== 业务方法 ==========
    
    /**
     * 更新交易信息
     * 业务规则：只有草稿状态的交易才能修改
     */
    public void updateTransaction(Money newMoney, String newDescription, 
                                String newPaymentMethod, String newReferenceNumber) {
        ensureInDraftStatus();
        validateMoney(newMoney);
        
        this.money = newMoney;
        this.description = newDescription;
        this.paymentMethod = newPaymentMethod;
        this.referenceNumber = newReferenceNumber;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 设置分类
     */
    public void setCategory(Integer categoryId) {
        ensureNotCancelled();
        this.categoryId = categoryId;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 设置部门
     */
    public void setDepartment(Integer departmentId) {
        ensureNotCancelled();
        this.departmentId = departmentId;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 设置基金
     */
    public void setFund(Integer fundId) {
        ensureNotCancelled();
        this.fundId = fundId;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 标记为周期性交易
     */
    public void markAsRecurring() {
        ensureNotCancelled();
        this.isRecurring = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 标记为应税交易
     */
    public void markAsTaxable() {
        ensureNotCancelled();
        this.isTaxable = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 批准交易
     * 业务规则：只有草稿状态的交易才能批准
     */
    public void approve(Integer approverUserId) {
        ensureInDraftStatus();
        validateApprover(approverUserId);
        
        this.status = TransactionStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
        this.approvedBy = approverUserId;
        this.updatedAt = LocalDateTime.now();
        
        // 发布批准事件
        addDomainEvent(new TransactionApprovedEvent(this.transactionId, this.money, 
                                                   this.companyId, approverUserId));
    }
    
    /**
     * 取消交易
     * 业务规则：已批准的交易不能取消，只能作废
     */
    public void cancel() {
        if (status == TransactionStatus.APPROVED) {
            throw new IllegalStateException("已批准的交易不能取消，请使用作废功能");
        }
        ensureNotCancelled();
        
        this.status = TransactionStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
        
        // 发布取消事件
        addDomainEvent(new TransactionCancelledEvent(this.transactionId, this.companyId));
    }
    
    /**
     * 作废交易
     * 业务规则：已批准的交易可以作废，但需要特殊权限
     */
    public void void_(Integer voidedBy, String reason) {
        if (status != TransactionStatus.APPROVED) {
            throw new IllegalStateException("只有已批准的交易才能作废");
        }
        
        this.status = TransactionStatus.VOIDED;
        this.updatedAt = LocalDateTime.now();
        // 可以添加作废原因等字段
        
        // 发布作废事件
        addDomainEvent(new TransactionCancelledEvent(this.transactionId, this.companyId, reason));
    }
    
    /**
     * 检查是否可以修改
     */
    public boolean canModify() {
        return status == TransactionStatus.DRAFT;
    }
    
    /**
     * 检查是否已完成
     */
    public boolean isCompleted() {
        return status == TransactionStatus.APPROVED;
    }
    
    /**
     * 检查是否已取消或作废
     */
    public boolean isCancelledOrVoided() {
        return status == TransactionStatus.CANCELLED || status == TransactionStatus.VOIDED;
    }
    
    /**
     * 获取显示用的金额字符串
     */
    public String getDisplayAmount() {
        return money != null ? money.toDisplayString() : "0.00";
    }
    
    /**
     * 计算税额（如果应税）
     */
    public Money calculateTax(double taxRate) {
        if (!isTaxable || money == null) {
            return Money.zero(money != null ? money.getCurrencyCode() : "CNY");
        }
        return money.multiply(taxRate);
    }
    
    // ========== 验证方法 ==========
    
    private void validateTransactionCreation(TransactionType type, Money money, 
                                           LocalDate transactionDate, Integer companyId, Integer userId) {
        if (type == null) {
            throw new IllegalArgumentException("交易类型不能为空");
        }
        validateMoney(money);
        if (transactionDate == null) {
            throw new IllegalArgumentException("交易日期不能为空");
        }
        if (transactionDate.isAfter(LocalDate.now().plusDays(1))) {
            throw new IllegalArgumentException("交易日期不能是未来日期");
        }
        if (companyId == null) {
            throw new IllegalArgumentException("公司ID不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
    }
    
    private void validateMoney(Money money) {
        if (money == null) {
            throw new IllegalArgumentException("交易金额不能为空");
        }
        if (!money.isPositive()) {
            throw new IllegalArgumentException("交易金额必须大于0");
        }
    }
    
    private void validateApprover(Integer approverUserId) {
        if (approverUserId == null) {
            throw new IllegalArgumentException("批准人不能为空");
        }
        if (approverUserId.equals(this.userId)) {
            throw new IllegalArgumentException("不能自己批准自己的交易");
        }
    }
    
    private void ensureInDraftStatus() {
        if (status != TransactionStatus.DRAFT) {
            throw new IllegalStateException("只有草稿状态的交易才能执行此操作");
        }
    }
    
    private void ensureNotCancelled() {
        if (status == TransactionStatus.CANCELLED || status == TransactionStatus.VOIDED) {
            throw new IllegalStateException("已取消或作废的交易不能执行此操作");
        }
    }
    
    // ========== 领域事件管理 ==========
    
    private void addDomainEvent(Object event) {
        this.domainEvents.add(event);
    }
    
    @DomainEvents
    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    @AfterDomainEventPublication
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
    
    // ========== Getter方法 ==========
    
    public Integer getTransactionId() {
        return transactionId;
    }
    
    public Money getMoney() {
        return money;
    }
    
    public TransactionType getTransactionType() {
        return transactionType;
    }
    
    public TransactionStatus getStatus() {
        return status;
    }
    
    public LocalDate getTransactionDate() {
        return transactionDate;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public String getReferenceNumber() {
        return referenceNumber;
    }
    
    public Boolean getIsRecurring() {
        return isRecurring;
    }
    
    public Boolean getIsTaxable() {
        return isTaxable;
    }
    
    public Integer getCompanyId() {
        return companyId;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public Integer getDepartmentId() {
        return departmentId;
    }
    
    public Integer getFundId() {
        return fundId;
    }
    
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }
    
    public Integer getApprovedBy() {
        return approvedBy;
    }
    
    // ========== 枚举定义 ==========
    
    public enum TransactionType {
        INCOME("收入"),
        EXPENSE("支出");
        
        private final String displayName;
        
        TransactionType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum TransactionStatus {
        DRAFT("草稿"),
        APPROVED("已批准"),
        CANCELLED("已取消"),
        VOIDED("已作废");
        
        private final String displayName;
        
        TransactionStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // ========== Object方法重写 ==========
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Transaction that = (Transaction) obj;
        return Objects.equals(transactionId, that.transactionId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }
    
    @Override
    public String toString() {
        return String.format("Transaction{id=%d, type=%s, amount=%s, status=%s, date=%s}", 
                           transactionId, transactionType, money, status, transactionDate);
    }
}