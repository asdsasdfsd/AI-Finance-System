// src/main/java/org/example/backend/model/Role.java
package org.example.backend.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "Role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roleId;
    
    private String name;
    private String description;

    // Bidirectional relationship
    @JsonIgnore // Preventing loops in JSON serialization
    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();
    
    // Add convenience methods for Optional handling
    public boolean isEmpty() {
        return roleId == null;
    }
    
    public <X extends Throwable> Role orElseThrow(java.util.function.Supplier<? extends X> exceptionSupplier) throws X {
        if (isEmpty()) {
            throw exceptionSupplier.get();
        }
        return this;
    }

    @Override
    public int hashCode() {
        // Use only role IDs and names, do not reference user collections
        return Objects.hash(roleId, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Role role = (Role) obj;
        return Objects.equals(roleId, role.roleId);
    }
}