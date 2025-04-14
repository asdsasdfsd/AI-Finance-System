// src/main/java/org/example/backend/model/Company.java
package org.example.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default values for new fields
    public Company() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "ACTIVE";
    }
}