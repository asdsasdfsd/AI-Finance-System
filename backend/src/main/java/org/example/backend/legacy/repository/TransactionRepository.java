// backend/src/main/java/org/example/backend/repository/TransactionRepository.java (扩展)
package org.example.backend.repository;

import org.example.backend.domain.aggregate.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Transaction 仓储接口扩展
 * 添加支持新聚合模型的查询方法
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    
    // 按公司查询
    List<Transaction> findByCompanyId(Integer companyId);
    
    // 按公司和类型查询
    List<Transaction> findByCompanyIdAndTransactionType(Integer companyId, Transaction.TransactionType transactionType);
    
    // 按公司和状态查询
    List<Transaction> findByCompanyIdAndStatus(Integer companyId, Transaction.TransactionStatus status);
    
    // 按公司、类型和状态查询
    List<Transaction> findByCompanyIdAndTransactionTypeAndStatus(
        Integer companyId, Transaction.TransactionType transactionType, Transaction.TransactionStatus status);
    
    // 按日期范围查询
    List<Transaction> findByCompanyIdAndTransactionDateBetween(
        Integer companyId, LocalDate startDate, LocalDate endDate);
    
    // 按日期范围和状态查询
    List<Transaction> findByCompanyIdAndTransactionDateBetweenAndStatus(
        Integer companyId, LocalDate startDate, LocalDate endDate, Transaction.TransactionStatus status);
    
    // 按公司排序查询
    List<Transaction> findByCompanyIdOrderByTransactionDateDesc(Integer companyId);
    
    // 按用户查询
    List<Transaction> findByUserId(Integer userId);
    
    // 按用户和类型查询
    List<Transaction> findByUserIdAndTransactionType(Integer userId, Transaction.TransactionType transactionType);
    
    // 按部门查询
    List<Transaction> findByDepartmentId(Integer departmentId);
    
    // 按基金查询
    List<Transaction> findByFundId(Integer fundId);
    
    // 按分类查询
    List<Transaction> findByCategoryId(Integer categoryId);
    
    // 复杂查询：计算金额总和
    @Query("SELECT SUM(t.money.amount) FROM Transaction t WHERE t.companyId = :companyId AND t.transactionType = :type AND t.status = :status")
    Double sumAmountByCompanyAndTypeAndStatus(
        @Param("companyId") Integer companyId, 
        @Param("type") Transaction.TransactionType type,
        @Param("status") Transaction.TransactionStatus status);
    
    // 统计查询：按月份统计
    @Query("SELECT YEAR(t.transactionDate), MONTH(t.transactionDate), COUNT(t), SUM(t.money.amount) " +
           "FROM Transaction t WHERE t.companyId = :companyId AND t.status = :status " +
           "GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate) " +
           "ORDER BY YEAR(t.transactionDate), MONTH(t.transactionDate)")
    List<Object[]> findMonthlyStatistics(@Param("companyId") Integer companyId, 
                                        @Param("status") Transaction.TransactionStatus status);
}