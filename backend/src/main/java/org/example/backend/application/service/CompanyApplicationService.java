// backend/src/main/java/org/example/backend/application/service/CompanyApplicationService.java
package org.example.backend.application.service;

import org.example.backend.application.dto.CreateCompanyCommand;
import org.example.backend.application.dto.UpdateCompanyCommand;
import org.example.backend.application.dto.CompanyDTO;
import org.example.backend.application.dto.CompanyStatsDTO;
import org.example.backend.domain.aggregate.company.CompanyAggregate;
import org.example.backend.domain.aggregate.company.CompanyAggregateRepository;
import org.example.backend.domain.valueobject.CompanyStatus;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.event.DomainEventPublisher;
import org.example.backend.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Company Application Service
 * 
 * Orchestrates company management use cases and coordinates
 * multi-tenant operations, subscription management, and business rules
 */
@Service
@Transactional
public class CompanyApplicationService {
    
    private final CompanyAggregateRepository companyRepository;
    private final DomainEventPublisher eventPublisher;
    
    public CompanyApplicationService(CompanyAggregateRepository companyRepository,
                                   DomainEventPublisher eventPublisher) {
        this.companyRepository = companyRepository;
        this.eventPublisher = eventPublisher;
    }
    
    // ========== Command Handlers ==========
    
    /**
     * Create new company
     */
    public CompanyDTO createCompany(CreateCompanyCommand command) {
        validateCreateCommand(command);
        
        // Check for duplicate company name or email
        if (companyRepository.existsByCompanyName(command.getCompanyName())) {
            throw new IllegalArgumentException("Company name already exists: " + command.getCompanyName());
        }
        
        if (companyRepository.existsByEmail(command.getEmail())) {
            throw new IllegalArgumentException("Company email already exists: " + command.getEmail());
        }
        
        CompanyAggregate company = CompanyAggregate.create(
            command.getCompanyName(),
            command.getEmail(),
            command.getAddress(),
            command.getCity(),
            command.getStateProvince(),
            command.getPostalCode(),
            command.getCreatedBy()
        );
        
        // Set optional fields
        setOptionalFields(company, command);
        
        CompanyAggregate savedCompany = companyRepository.save(company);
        
        // Publish domain events
        eventPublisher.publishAll(savedCompany.getDomainEvents());
        savedCompany.clearDomainEvents();
        
        return mapToDTO(savedCompany);
    }
    
    /**
     * Update company information
     */
    public CompanyDTO updateCompany(Integer companyId, UpdateCompanyCommand command) {
        validateUpdateCommand(command);
        
        CompanyAggregate company = findCompanyById(companyId);
        
        // Update basic information
        company.updateBasicInfo(
            command.getCompanyName(),
            command.getAddress(),
            command.getCity(),
            command.getStateProvince(),
            command.getPostalCode(),
            command.getWebsite()
        );
        
        // Update registration information if provided
        if (command.getRegistrationNumber() != null || command.getTaxId() != null) {
            company.updateRegistrationInfo(
                command.getRegistrationNumber(),
                command.getTaxId()
            );
        }
        
        // Update financial settings if provided
        if (command.getFiscalYearStart() != null || command.getDefaultCurrency() != null) {
            company.updateFinancialSettings(
                command.getFiscalYearStart(),
                command.getDefaultCurrency()
            );
        }
        
        // Update user limit if provided
        if (command.getMaxUsers() != null) {
            company.updateUserLimit(command.getMaxUsers());
        }
        
        CompanyAggregate savedCompany = companyRepository.save(company);
        return mapToDTO(savedCompany);
    }
    
    /**
     * Activate company
     */
    public CompanyDTO activateCompany(Integer companyId) {
        CompanyAggregate company = findCompanyById(companyId);
        company.activate();
        
        CompanyAggregate savedCompany = companyRepository.save(company);
        return mapToDTO(savedCompany);
    }
    
    /**
     * Suspend company operations
     */
    public CompanyDTO suspendCompany(Integer companyId, String reason) {
        CompanyAggregate company = findCompanyById(companyId);
        company.suspend();
        
        CompanyAggregate savedCompany = companyRepository.save(company);
        
        // TODO: Add suspension event with reason
        // eventPublisher.publish(new CompanySuspendedEvent(companyId, reason));
        
        return mapToDTO(savedCompany);
    }
    
    /**
     * Delete company (soft delete)
     */
    public void deleteCompany(Integer companyId) {
        CompanyAggregate company = findCompanyById(companyId);
        company.delete();
        
        companyRepository.save(company);
    }
    
    /**
     * Update subscription
     */
    public CompanyDTO updateSubscription(Integer companyId, LocalDateTime expiresAt) {
        CompanyAggregate company = findCompanyById(companyId);
        company.updateSubscription(expiresAt);
        
        CompanyAggregate savedCompany = companyRepository.save(company);
        return mapToDTO(savedCompany);
    }
    
    /**
     * Check if company can add new user
     */
    public boolean canAddUser(Integer companyId, int currentUserCount) {
        CompanyAggregate company = findCompanyById(companyId);
        return company.canAddUser(currentUserCount);
    }
    
    // ========== Query Handlers ==========
    
    /**
     * Get company by ID
     */
    @Transactional(readOnly = true)
    public CompanyDTO getCompanyById(Integer companyId) {
        CompanyAggregate company = findCompanyById(companyId);
        return mapToDTO(company);
    }
    
    /**
     * Get all companies
     */
    @Transactional(readOnly = true)
    public List<CompanyDTO> getAllCompanies() {
        List<CompanyAggregate> companies = companyRepository.findAll();
        return companies.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    
    /**
     * Get active companies only
     */
    @Transactional(readOnly = true)
    public List<CompanyDTO> getActiveCompanies() {
        List<CompanyAggregate> companies = companyRepository.findActiveCompanies();
        return companies.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    
    /**
     * Find company by email domain (for SSO)
     */
    @Transactional(readOnly = true)
    public Optional<CompanyDTO> findByEmailDomain(String domain) {
        Optional<CompanyAggregate> company = companyRepository.findByEmailDomain(domain);
        return company.map(this::mapToDTO);
    }
    
    /**
     * Search companies by name
     */
    @Transactional(readOnly = true)
    public List<CompanyDTO> searchCompaniesByName(String name) {
        List<CompanyAggregate> companies = companyRepository.searchByNameContaining(name);
        return companies.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    
    /**
     * Get companies with expired subscriptions
     */
    @Transactional(readOnly = true)
    public List<CompanyDTO> getCompaniesWithExpiredSubscriptions() {
        List<CompanyAggregate> companies = companyRepository.findWithExpiredSubscriptions(LocalDateTime.now());
        return companies.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    
    /**
     * Get companies with expiring subscriptions (warning)
     */
    @Transactional(readOnly = true)
    public List<CompanyDTO> getCompaniesWithExpiringSubscriptions(int warningDays) {
        LocalDateTime warningDate = LocalDateTime.now().plusDays(warningDays);
        List<CompanyAggregate> companies = companyRepository.findWithExpiringSubscriptions(
            LocalDateTime.now(), warningDate);
        return companies.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    
    /**
     * Get company statistics
     */
    @Transactional(readOnly = true)
    public CompanyStatsDTO getCompanyStatistics() {
        long totalCompanies = companyRepository.count();
        long activeCompanies = companyRepository.countActiveCompanies();
        long suspendedCompanies = companyRepository.countByStatus(CompanyStatus.Status.SUSPENDED);
        long deletedCompanies = companyRepository.countByStatus(CompanyStatus.Status.DELETED);
        
        return CompanyStatsDTO.builder()
                .totalCompanies(totalCompanies)
                .activeCompanies(activeCompanies)
                .suspendedCompanies(suspendedCompanies)
                .deletedCompanies(deletedCompanies)
                .build();
    }
    
    // ========== Helper Methods ==========
    
    private CompanyAggregate findCompanyById(Integer companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + companyId));
    }
    
    private void validateCreateCommand(CreateCompanyCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Create company command cannot be null");
        }
        if (command.getCompanyName() == null || command.getCompanyName().trim().isEmpty()) {
            throw new IllegalArgumentException("Company name is required");
        }
        if (command.getEmail() == null || command.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Company email is required");
        }
        if (command.getCreatedBy() == null) {
            throw new IllegalArgumentException("Created by user ID is required");
        }
    }
    
    private void validateUpdateCommand(UpdateCompanyCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Update company command cannot be null");
        }
    }
    
    private void setOptionalFields(CompanyAggregate company, CreateCompanyCommand command) {
        if (command.getRegistrationNumber() != null || command.getTaxId() != null) {
            company.updateRegistrationInfo(
                command.getRegistrationNumber(),
                command.getTaxId()
            );
        }
        
        if (command.getFiscalYearStart() != null || command.getDefaultCurrency() != null) {
            company.updateFinancialSettings(
                command.getFiscalYearStart(),
                command.getDefaultCurrency()
            );
        }
        
        if (command.getWebsite() != null) {
            company.updateBasicInfo(
                company.getCompanyName(),
                company.getAddress(),
                company.getCity(),
                company.getStateProvince(),
                company.getPostalCode(),
                command.getWebsite()
            );
        }
        
        if (command.getMaxUsers() != null) {
            company.updateUserLimit(command.getMaxUsers());
        }
    }
    
    private CompanyDTO mapToDTO(CompanyAggregate company) {
        return CompanyDTO.builder()
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .address(company.getAddress())
                .city(company.getCity())
                .stateProvince(company.getStateProvince())
                .postalCode(company.getPostalCode())
                .email(company.getEmail())
                .website(company.getWebsite())
                .registrationNumber(company.getRegistrationNumber())
                .taxId(company.getTaxId())
                .fiscalYearStart(company.getFiscalYearStart())
                .defaultCurrency(company.getDefaultCurrency())
                .status(company.getStatus())
                .maxUsers(company.getMaxUsers())
                .subscriptionExpiresAt(company.getSubscriptionExpiresAt())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .createdBy(company.getCreatedBy())
                .tenantId(company.getTenantId().getValue())
                .isActive(company.isActive())
                .canBeModified(company.canBeModified())
                .subscriptionValid(company.isSubscriptionValid())
                .build();
    }
}