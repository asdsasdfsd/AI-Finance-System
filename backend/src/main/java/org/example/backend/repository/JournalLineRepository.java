// backend/src/main/java/org/example/backend/repository/JournalLineRepository.java
package org.example.backend.repository;

import org.example.backend.model.JournalLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JournalLine Repository - 修复版本
 * 
 * 适配新的外键关联方式，不再依赖实体关联
 */
@Repository
public interface JournalLineRepository extends JpaRepository<JournalLine, Integer> {
    
    /**
     * 根据分录ID查找分录行
     */
    List<JournalLine> findByEntryId(Integer entryId);
    
    /**
     * 根据科目ID查找分录行
     */
    List<JournalLine> findByAccountId(Integer accountId);
    
    /**
     * 根据分录ID删除分录行
     */
    void deleteByEntryId(Integer entryId);
    
    /**
     * 计算分录的借方总额
     */
    @Query("SELECT COALESCE(SUM(jl.debitAmount), 0) FROM JournalLine jl WHERE jl.entryId = :entryId")
    java.math.BigDecimal sumDebitAmountByEntryId(@Param("entryId") Integer entryId);
    
    /**
     * 计算分录的贷方总额
     */
    @Query("SELECT COALESCE(SUM(jl.creditAmount), 0) FROM JournalLine jl WHERE jl.entryId = :entryId")
    java.math.BigDecimal sumCreditAmountByEntryId(@Param("entryId") Integer entryId);
    
    /**
     * 检查分录是否平衡
     */
    @Query("SELECT (COALESCE(SUM(jl.debitAmount), 0) - COALESCE(SUM(jl.creditAmount), 0)) = 0 " +
           "FROM JournalLine jl WHERE jl.entryId = :entryId")
    boolean isBalanced(@Param("entryId") Integer entryId);
}