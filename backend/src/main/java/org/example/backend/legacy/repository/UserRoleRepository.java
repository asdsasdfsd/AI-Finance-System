// src/main/java/org/example/backend/repository/UserRoleRepository.java
package org.example.backend.repository;

import org.example.backend.model.Role;
import org.example.backend.model.User;
import org.example.backend.model.UserRole;
import org.example.backend.model.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    List<UserRole> findByUser(User user);
    List<UserRole> findByRole(Role role);
}