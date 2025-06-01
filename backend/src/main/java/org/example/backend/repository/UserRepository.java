// backend/src/main/java/org/example/backend/repository/UserRepository.java
package org.example.backend.repository;

import org.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 传统User Repository - 与DDD UserAggregateRepository共存
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
    User findByEmail(String email);
    Optional<User> findByExternalId(String externalId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}

