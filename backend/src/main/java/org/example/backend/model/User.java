package org.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer user_Id;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    private String fullname;
    private String username;
    private String email;
    private String role;
}
