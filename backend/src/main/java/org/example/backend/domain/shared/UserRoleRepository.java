// backend/src/main/java/org/example/backend/domain/shared/UserRoleRepository.java
package org.example.backend.domain.shared;

import org.example.backend.domain.aggregate.user.UserAggregate;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * UserRole Repository - DDD模式专用
 */
@Repository
@Profile("ddd")
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    List<UserRole> findByUser(UserAggregate user);
    List<UserRole> findByRole(Role role);
}