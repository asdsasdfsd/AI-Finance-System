package org.example.backend.model;

import jakarta.persistence.*;
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
}