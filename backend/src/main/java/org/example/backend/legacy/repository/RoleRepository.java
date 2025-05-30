// src/main/java/org/example/backend/repository/RoleRepository.java
package org.example.backend.repository;

import org.example.backend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Role findByName(String name);
}