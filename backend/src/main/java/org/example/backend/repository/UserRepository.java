// backend/src/main/java/org/example/backend/repository/UserRepository.java
package org.example.backend.repository;

import java.util.Optional;

import org.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByExternalId(String externalId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}