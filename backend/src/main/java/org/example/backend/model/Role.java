// src/main/java/org/example/backend/model/Role.java
package org.example.backend.model;

import java.util.HashSet;
import java.util.Set;

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
}