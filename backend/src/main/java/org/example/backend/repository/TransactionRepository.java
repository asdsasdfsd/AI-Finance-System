// backend/src/main/java/org/example/backend/repository/TransactionRepository.java
package org.example.backend.repository;

import org.example.backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 传统Transaction Repository - 与DDD TransactionAggregateRepository共存
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    // 基本CRUD方法即可，复杂业务使用DDD的Repository
}