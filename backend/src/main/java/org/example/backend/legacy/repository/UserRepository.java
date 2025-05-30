// src/main/java/org/example/backend/repository/UserRepository.java
package org.example.backend.repository;

import org.example.backend.model.Department;
import org.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    List<User> findByDepartment(Department department);
    Optional<User> findByUsername(String username);
    Optional<User> findByExternalId(String externalId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}