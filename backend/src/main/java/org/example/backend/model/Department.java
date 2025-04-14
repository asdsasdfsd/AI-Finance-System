// src/main/java/org/example/backend/model/Department.java
package org.example.backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "Department")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer departmentId;
    
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
    
    private String name;
    private String code;
    
    @ManyToOne
    @JoinColumn(name = "parent_department_id")
    private Department parentDepartment;
    
    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User manager;
    
    private BigDecimal budget;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default values
    public Department() {
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.budget = BigDecimal.ZERO;
    }
}