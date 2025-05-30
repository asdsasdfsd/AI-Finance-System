// src/main/java/org/example/backend/service/TransactionBusinessService.java
package org.example.backend.service;

import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.valueobject.Money;
import org.example.backend.model.Transaction; // 传统ORM实体
import org.example.backend.model.Company;
import org.example.backend.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TransactionBusinessService {
    
    @Autowired
    private TransactionService legacyService; // 复用现有服务
    
    @Autowired
    private CompanyService companyService;
    
    @Autowired
    private UserService userService;
    
    /**
     * DDD业务方法：批准交易
     */
    public void approveTransaction(Integer transactionId, Integer approverId) {
        // 1. 获取传统ORM实体
        Transaction ormEntity = legacyService.findById(transactionId);
        if (ormEntity == null) {
            throw new IllegalArgumentException("交易不存在，ID: " + transactionId);
        }
        
        // 2. 转换为DDD聚合根
        TransactionAggregate aggregate = convertToAggregate(ormEntity);
        
        // 3. 执行DDD业务逻辑（这里会有业务规则验证）
        aggregate.approve(approverId);
        
        // 4. 将聚合的状态同步回ORM实体
        syncAggregateToOrmEntity(aggregate, ormEntity);
        
        // 5. 保存更新（使用现有服务）
        legacyService.save(ormEntity);
        
        System.out.println("✅ DDD业务逻辑：交易 " + transactionId + " 已通过业务规则验证并批准");
    }
    
    /**
     * 获取交易的业务信息（展示DDD价值）
     */
    public TransactionBusinessInfo getTransactionBusinessInfo(Integer transactionId) {
        Transaction ormEntity = legacyService.findById(transactionId);
        if (ormEntity == null) {
            throw new IllegalArgumentException("交易不存在");
        }
        
        TransactionAggregate aggregate = convertToAggregate(ormEntity);
        
        return TransactionBusinessInfo.builder()
                .transactionId(aggregate.getTransactionId())
                .money(aggregate.getMoney()) // Money值对象
                .canBeApproved(aggregate.canBeApproved())
                .businessStatus(aggregate.getStatus().getDisplayName())
                .displayAmount(aggregate.getDisplayAmount())
                .isCompleted(aggregate.isCompleted())
                .build();
    }
    
    /**
     * 计算含税总额（展示Money值对象的业务价值）
     */
    public Money calculateTotalWithTax(Integer transactionId, double taxRate) {
        Transaction ormEntity = legacyService.findById(transactionId);
        TransactionAggregate aggregate = convertToAggregate(ormEntity);
        
        return aggregate.calculateTax(taxRate);
    }
    
    /**
     * 取消交易（DDD业务方法）
     */
    public void cancelTransaction(Integer transactionId) {
        Transaction ormEntity = legacyService.findById(transactionId);
        if (ormEntity == null) {
            throw new IllegalArgumentException("交易不存在");
        }
        
        TransactionAggregate aggregate = convertToAggregate(ormEntity);
        aggregate.cancel(); // DDD业务规则验证
        
        syncAggregateToOrmEntity(aggregate, ormEntity);
        legacyService.save(ormEntity);
    }
    
    // ========== 私有转换方法 ==========
    
    /**
     * 传统ORM实体 → DDD聚合根
     */
    private TransactionAggregate convertToAggregate(Transaction ormEntity) {
        Money money = Money.of(ormEntity.getAmount(), 
                              ormEntity.getCurrency() != null ? ormEntity.getCurrency() : "CNY");
        
        TransactionAggregate aggregate;
        if (ormEntity.getTransactionType() == Transaction.TransactionType.INCOME) {
            aggregate = TransactionAggregate.createIncome(
                money,
                ormEntity.getTransactionDate(),
                ormEntity.getDescription(),
                ormEntity.getCompany().getCompanyId(),
                ormEntity.getUser().getUserId()
            );
        } else {
            aggregate = TransactionAggregate.createExpense(
                money,
                ormEntity.getTransactionDate(),
                ormEntity.getDescription(),
                ormEntity.getCompany().getCompanyId(),
                ormEntity.getUser().getUserId()
            );
        }
        
        // 设置其他字段
        aggregate.setTransactionId(ormEntity.getTransactionId());
        // 如果需要设置其他状态，在这里添加
        
        return aggregate;
    }
    
    /**
     * 将聚合的状态同步回ORM实体
     */
    private void syncAggregateToOrmEntity(TransactionAggregate aggregate, Transaction ormEntity) {
        // 同步Money值对象的变化
        ormEntity.setAmount(aggregate.getMoney().getAmount());
        ormEntity.setCurrency(aggregate.getMoney().getCurrencyCode());
        
        // 同步状态变化
        ormEntity.setUpdatedAt(java.time.LocalDateTime.now());
        
        // 如果有其他状态字段需要同步，在这里添加
    }
    
    // ========== 内部DTO类 ==========
    
    public static class TransactionBusinessInfo {
        private Integer transactionId;
        private Money money;
        private Boolean canBeApproved;
        private String businessStatus;
        private String displayAmount;
        private Boolean isCompleted;
        
        // Builder模式
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private TransactionBusinessInfo info = new TransactionBusinessInfo();
            
            public Builder transactionId(Integer transactionId) {
                info.transactionId = transactionId;
                return this;
            }
            
            public Builder money(Money money) {
                info.money = money;
                return this;
            }
            
            public Builder canBeApproved(Boolean canBeApproved) {
                info.canBeApproved = canBeApproved;
                return this;
            }
            
            public Builder businessStatus(String businessStatus) {
                info.businessStatus = businessStatus;
                return this;
            }
            
            public Builder displayAmount(String displayAmount) {
                info.displayAmount = displayAmount;
                return this;
            }
            
            public Builder isCompleted(Boolean isCompleted) {
                info.isCompleted = isCompleted;
                return this;
            }
            
            public TransactionBusinessInfo build() {
                return info;
            }
        }
        
        // Getters
        public Integer getTransactionId() { return transactionId; }
        public Money getMoney() { return money; }
        public Boolean getCanBeApproved() { return canBeApproved; }
        public String getBusinessStatus() { return businessStatus; }
        public String getDisplayAmount() { return displayAmount; }
        public Boolean getIsCompleted() { return isCompleted; }
    }
}
