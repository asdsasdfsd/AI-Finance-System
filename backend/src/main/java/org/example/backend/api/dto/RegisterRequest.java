// backend/src/main/java/org/example/backend/dto/RegisterRequest.java
package org.example.backend.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String fullName;
    private Integer companyId;
    
    // For company registration
    private String companyName;
    private String address;
    private String city;
    private String stateProvince;
    private String postalCode;
    private String registrationNumber;
    private String taxId;
}