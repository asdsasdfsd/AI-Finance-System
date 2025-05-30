// backend/src/main/java/org/example/backend/service/TenantAwareTransactionService.java
package org.example.backend.service;

import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.valueobject.Money;
import org.example.backend.repository.TenantAwareTransactionRepository;
import org.example.backend.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 租户感知的Transaction服务
 * 
 * 自动应用租户隔离，确保数据安全
 */
@Service
@Transactional
public class TenantAwareTransactionService {
    
    @Autowired
    private TenantAwareTransactionRepository transactionRepository;
    
    /**
     * 创建交易（自动设置租户）
     */
    public TransactionAggregate createTransaction(TransactionService.CreateTransactionCommand command) {
        // 确保命令中的companyId与当前租户一致
        Integer currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant == null) {
            throw new IllegalStateException("未设置租户上下文");
        }
        
        if (!currentTenant.equals(command.getCompanyId())) {
            throw new IllegalArgumentException("公司ID与当前租户不匹配");
        }
        
        Money amount = Money.of(command.getAmount(), command.getCurrency());
        TransactionAggregate transaction;
        
        if (command.getTransactionType() == TransactionAggregate.TransactionType.INCOME) {
            transaction = TransactionAggregate.createIncome(
                amount, command.getTransactionDate(), command.getDescription(),
                command.getCompanyId(), command.getUserId()
            );
        } else {
            transaction = TransactionAggregate.createExpense(
                amount, command.getTransactionDate(), command.getDescription(),
                command.getCompanyId(), command.getUserId()
            );
        }
        
        return transactionRepository.save(transaction);
    }
    
    /**
     * 查找所有交易（当前租户）
     */
    @Transactional(readOnly = true)
    public List<TransactionAggregate> findAll() {
        ensureTenantSet();
        return transactionRepository.findAllByTenant();
    }
    
    /**
     * 按ID查找交易（当前租户）
     */
    @Transactional(readOnly = true)
    public Optional<TransactionAggregate> findById(Integer id) {
        ensureTenantSet();
        return transactionRepository.findByIdAndTenant(id);
    }
    
    /**
     * 按类型查找交易（当前租户）
     */
    @Transactional(readOnly = true)
    public List<TransactionAggregate> findByType(TransactionAggregate.TransactionType type) {
        ensureTenantSet();
        return transactionRepository.findByTenantAndType(type);
    }
    
    /**
     * 按日期范围查找交易（当前租户）
     */
    @Transactional(readOnly = true)
    public List<TransactionAggregate> findByDateRange(LocalDate startDate, LocalDate endDate) {
        ensureTenantSet();
        return transactionRepository.findByTenantAndDateRange(startDate, endDate);
    }
    
    /**
     * 更新交易（租户安全检查）
     */
    public TransactionAggregate updateTransaction(Integer id, TransactionService.UpdateTransactionCommand command) {
        ensureTenantSet();
        
        TransactionAggregate transaction = transactionRepository.findByIdAndTenant(id)
                .orElseThrow(() -> new IllegalArgumentException("交易不存在或无权访问"));
        
        Money newAmount = Money.of(command.getAmount(), command.getCurrency());
        transaction.updateTransaction(
            newAmount, command.getDescription(),
            command.getPaymentMethod(), command.getReferenceNumber()
        );
        
        return transactionRepository.save(transaction);
    }
    
    /**
     * 删除交易（租户安全检查）
     */
    public void deleteById(Integer id) {
        ensureTenantSet();
        
        TransactionAggregate transaction = transactionRepository.findByIdAndTenant(id)
                .orElseThrow(() -> new IllegalArgumentException("交易不存在或无权访问"));
        
        if (!transaction.canModify()) {
            throw new IllegalStateException("只有草稿状态的交易才能删除");
        }
        
        transactionRepository.deleteByIdAndTenant(id);
    }
    
    /**
     * 统计当前租户的交易数量
     */
    @Transactional(readOnly = true)
    public long countTransactions() {
        ensureTenantSet();
        return transactionRepository.countByTenant();
    }
    
    private void ensureTenantSet() {
        if (!TenantContext.hasTenant()) {
            throw new IllegalStateException("未设置租户上下文");
        }
    }
}

