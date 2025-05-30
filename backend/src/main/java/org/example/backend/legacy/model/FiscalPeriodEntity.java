// src/main/java/org/example/backend/model/FiscalPeriod.java
package org.example.backend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "Fiscal_Period")
public class FiscalPeriod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer periodId;
    
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    private PeriodType periodType;
    
    @Enumerated(EnumType.STRING)
    private PeriodStatus status;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default values
    public FiscalPeriod() {
        this.status = PeriodStatus.OPEN;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Period type enum
    public enum PeriodType {
        MONTH, QUARTER, YEAR
    }
    
    // Period status enum
    public enum PeriodStatus {
        OPEN, CLOSED, LOCKED
    }
}