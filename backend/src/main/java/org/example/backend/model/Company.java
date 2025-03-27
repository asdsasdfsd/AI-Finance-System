package org.example.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "Company")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer companyId;

    private String companyName;
    private String address;
    private String city;
    private String stateProvince;
    private String postalCode;
    private String email;
    private String website;
    private String registrationNumber;
    private String taxId;
    private String fiscalYearStart;
    private String defaultCurrency;
    private String createdAt;
    private String updatedAt;
    private String status;
    
    /**
     * Automatically set creation timestamp when entity is first persisted
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now.toString();
        updatedAt = now.toString();
        
        // Set default values if not provided
        if (status == null) {
            status = "ACTIVE";
        }
        
        if (defaultCurrency == null) {
            defaultCurrency = "USD";
        }
        
        if (fiscalYearStart == null) {
            fiscalYearStart = "01-01"; // Default fiscal year starts January 1
        }
    }
    
    /**
     * Automatically update the timestamp when entity is updated
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now().toString();
    }
}