// src/main/java/org/example/backend/model/Department.java
package org.example.backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "Department")
@ToString(exclude = {"company", "parentDepartment", "manager"})
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer departmentId;
    
    @ManyToOne
    @JoinColumn(name = "company_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Company company;
    
    private String name;
    private String code;
    
    @ManyToOne
    @JoinColumn(name = "parent_department_id")
    // 关键修复：忽略parentDepartment的parentDepartment字段，避免循环引用
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "parentDepartment", "company", "manager"})
    private Department parentDepartment;
    
    @ManyToOne
    @JoinColumn(name = "manager_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "department", "company", "roles"})
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