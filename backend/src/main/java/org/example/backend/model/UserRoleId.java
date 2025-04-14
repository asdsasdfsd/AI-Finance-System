// src/main/java/org/example/backend/model/UserRoleId.java
package org.example.backend.model;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleId implements Serializable {
    private Integer userId;
    private Integer roleId;
}