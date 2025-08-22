// backend/src/main/java/org/example/backend/config/DataInitializer.java
package org.example.backend.config;

import java.util.Arrays;
import java.util.List;

import org.example.backend.model.Role;
import org.example.backend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Initialize database with required data on application startup
 * FIXED: Complete implementation with proper error handling
 */
@Configuration
public class DataInitializer {

    /**
     * Initialize roles in the database
     * 
     * @param roleRepository Repository for roles
     * @param environment Spring environment for profile-specific initialization
     * @return CommandLineRunner bean
     */
    @Bean
    public CommandLineRunner initRoles(RoleRepository roleRepository, Environment environment) {
        return args -> {
            try {
                // Only initialize in development mode or when database is empty
                if (isDevProfile(environment) || roleRepository.count() == 0) {
                    // Define system roles
                    List<Role> defaultRoles = Arrays.asList(
                        createRole("SYSTEM_ADMIN", "System administrator with full access"),
                        createRole("COMPANY_ADMIN", "Company administrator with company-wide access"),
                        createRole("FINANCE_MANAGER", "Finance manager with finance data management access"),
                        createRole("FINANCE_OPERATOR", "Finance operator with basic data entry access"),
                        createRole("REPORT_VIEWER", "User with read-only access to reports"),
                        createRole("USER", "Basic user with minimal access"),
                        createRole("AUDITOR", "Auditor with read-only access to all data")
                    );
                    
                    // Save roles if they don't exist
                    for (Role role : defaultRoles) {
                        // FIXED: Complete the findByName check
                        if (roleRepository.findByName(role.getName()).isEmpty()) {
                            roleRepository.save(role);
                            System.out.println("Created role: " + role.getName());
                        } else {
                            System.out.println("Role already exists: " + role.getName());
                        }
                    }
                    
                    System.out.println("Roles initialized successfully");
                } else {
                    System.out.println("Skipping role initialization - roles already exist or not in dev profile");
                }
            } catch (Exception e) {
                System.err.println("Error initializing roles: " + e.getMessage());
                e.printStackTrace();
                // Don't throw exception to prevent application startup failure
            }
        };
    }
    
    /**
     * Create a role with name and description
     * 
     * @param name Role name
     * @param description Role description
     * @return Role object
     */
    private Role createRole(String name, String description) {
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        return role;
    }
    
    /**
     * Check if application is running in development profile
     * 
     * @param environment Spring environment
     * @return true if dev profile is active
     */
    private boolean isDevProfile(Environment environment) {
        String[] activeProfiles = environment.getActiveProfiles();
        return Arrays.asList(activeProfiles).contains("dev") ||
               Arrays.asList(activeProfiles).contains("development") ||
               activeProfiles.length == 0; // Also initialize in default profile
    }
}