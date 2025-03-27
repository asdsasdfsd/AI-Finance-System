// backend/src/main/java/org/example/backend/repository/RoleRepository.java
package org.example.backend.repository;

import java.util.Optional;

import org.example.backend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String name);
}