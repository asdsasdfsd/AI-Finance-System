// backend/src/main/java/org/example/backend/model/Company.java
package org.example.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
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
    @Column(name = "company_id")  // 明确使用下划线命名
    private Integer companyId;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;
    
    @Column(name = "address", length = 500)
    private String address;
    
    @Column(name = "city", length = 100)
    private String city;
    
    @Column(name = "state_province", length = 100)
    private String stateProvince;
    
    @Column(name = "postal_code", length = 20)
    private String postalCode;
    
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(name = "website", length = 200)
    private String website;
    
    @Column(name = "registration_number", length = 100)
    private String registrationNumber;
    
    @Column(name = "tax_id", length = 100)
    private String taxId;
    
    @Column(name = "fiscal_year_start", length = 10)
    private String fiscalYearStart;
    
    @Column(name = "default_currency", length = 3)
    private String defaultCurrency;
    
    @Column(name = "status", length = 20)
    private String status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Default values for new fields
    public Company() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "ACTIVE";
        this.fiscalYearStart = "01-01";
        this.defaultCurrency = "CNY";
    }
}