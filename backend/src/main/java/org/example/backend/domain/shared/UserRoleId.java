// backend/src/main/java/org/example/backend/domain/shared/UserRoleId.java
package org.example.backend.domain.shared;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserRoleId - DDD模式专用
 */
@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleId implements Serializable {
    private Integer userId;
    private Integer roleId;
}