// backend/src/main/java/org/example/backend/application/service/FixedAssetApplicationService.java
package org.example.backend.application.service;

import org.example.backend.application.dto.FixedAssetDTO;
import org.example.backend.application.dto.CreateFixedAssetCommand;
import org.example.backend.application.dto.UpdateFixedAssetCommand;
import org.example.backend.domain.aggregate.fixedasset.FixedAssetAggregate;
import org.example.backend.domain.aggregate.fixedasset.FixedAssetAggregateRepository;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.Money;
import org.example.backend.domain.event.DomainEventPublisher;
import org.example.backend.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fixed Asset Application Service
 * 
 * 协调固定资产管理的业务用例，包括资产生命周期和折旧计算
 */
@Service
@Transactional
public class FixedAssetApplicationService {
    
    private final FixedAssetAggregateRepository fixedAssetRepository;
    private final DomainEventPublisher eventPublisher;
    
    public FixedAssetApplicationService(FixedAssetAggregateRepository fixedAssetRepository,
                                     DomainEventPublisher eventPublisher) {
        this.fixedAssetRepository = fixedAssetRepository;
        this.eventPublisher = eventPublisher;
    }
    
    // ========== Command Handlers ==========
    
    /**
     * Create new fixed asset
     */
    public FixedAssetDTO createFixedAsset(CreateFixedAssetCommand command) {
        validateCreateCommand(command);
        
        TenantId tenantId = TenantId.of(command.getCompanyId());
        Money acquisitionCost = Money.of(command.getAcquisitionCost(), "CNY");
        
        FixedAssetAggregate fixedAsset = FixedAssetAggregate.create(
            command.getName(),
            command.getDescription(),
            acquisitionCost,
            command.getAcquisitionDate(),
            tenantId,
            command.getDepartmentId()
        );
        
        // 设置可选字段
        if (command.getLocation() != null) {
            fixedAsset.setLocation(command.getLocation());
        }
        if (command.getSerialNumber() != null) {
            fixedAsset.setSerialNumber(command.getSerialNumber());
        }
        
        FixedAssetAggregate savedAsset = fixedAssetRepository.save(fixedAsset);
        
        // 发布领域事件
        eventPublisher.publishAll(savedAsset.getDomainEvents());
        savedAsset.clearDomainEvents();
        
        return mapToDTO(savedAsset);
    }
    
    /**
     * Update fixed asset information
     */
    public FixedAssetDTO updateFixedAsset(Integer assetId, UpdateFixedAssetCommand command) {
        validateUpdateCommand(command);
        
        TenantId tenantId = TenantId.of(command.getCompanyId());
        FixedAssetAggregate fixedAsset = findAssetByIdAndTenant(assetId, tenantId);
        
        fixedAsset.updateAssetInfo(
            command.getName(),
            command.getDescription(),
            command.getLocation()
        );
        
        if (command.getDepartmentId() != null) {
            fixedAsset.transferToDepartment(command.getDepartmentId());
        }
        
        FixedAssetAggregate savedAsset = fixedAssetRepository.save(fixedAsset);
        return mapToDTO(savedAsset);
    }
    
    /**
     * Calculate and record depreciation
     */
    public FixedAssetDTO calculateDepreciation(Integer assetId, Integer companyId, 
                                             BigDecimal depreciationAmount) {
        TenantId tenantId = TenantId.of(companyId);
        FixedAssetAggregate fixedAsset = findAssetByIdAndTenant(assetId, tenantId);
        
        Money depreciation = Money.of(depreciationAmount, "CNY");
        fixedAsset.recordDepreciation(depreciation);
        
        FixedAssetAggregate savedAsset = fixedAssetRepository.save(fixedAsset);
        
        // 发布领域事件
        eventPublisher.publishAll(savedAsset.getDomainEvents());
        savedAsset.clearDomainEvents();
        
        return mapToDTO(savedAsset);
    }
    
    /**
     * Dispose fixed asset
     */
    public FixedAssetDTO disposeAsset(Integer assetId, Integer companyId, 
                                    BigDecimal disposalAmount, String reason) {
        TenantId tenantId = TenantId.of(companyId);
        FixedAssetAggregate fixedAsset = findAssetByIdAndTenant(assetId, tenantId);
        
        Money disposal = Money.of(disposalAmount, "CNY");
        fixedAsset.dispose(disposal, reason);
        
        FixedAssetAggregate savedAsset = fixedAssetRepository.save(fixedAsset);
        
        // 发布领域事件
        eventPublisher.publishAll(savedAsset.getDomainEvents());
        savedAsset.clearDomainEvents();
        
        return mapToDTO(savedAsset);
    }
    
    /**
     * Write off fixed asset
     */
    public FixedAssetDTO writeOffAsset(Integer assetId, Integer companyId, String reason) {
        TenantId tenantId = TenantId.of(companyId);
        FixedAssetAggregate fixedAsset = findAssetByIdAndTenant(assetId, tenantId);
        
        fixedAsset.writeOff(reason);
        
        FixedAssetAggregate savedAsset = fixedAssetRepository.save(fixedAsset);
        
        // 发布领域事件
        eventPublisher.publishAll(savedAsset.getDomainEvents());
        savedAsset.clearDomainEvents();
        
        return mapToDTO(savedAsset);
    }
    
    // ========== Query Handlers ==========
    
    /**
     * Get fixed asset by ID
     */
    @Transactional(readOnly = true)
    public FixedAssetDTO getFixedAssetById(Integer assetId, Integer companyId) {
        TenantId tenantId = TenantId.of(companyId);
        FixedAssetAggregate fixedAsset = findAssetByIdAndTenant(assetId, tenantId);
        return mapToDTO(fixedAsset);
    }
    
    /**
     * Get all fixed assets for company
     */
    @Transactional(readOnly = true)
    public List<FixedAssetDTO> getFixedAssetsByCompany(Integer companyId) {
        TenantId tenantId = TenantId.of(companyId);
        List<FixedAssetAggregate> assets = fixedAssetRepository.findByTenantId(tenantId);
        return assets.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    
    /**
     * Get fixed assets by department
     */
    @Transactional(readOnly = true)
    public List<FixedAssetDTO> getFixedAssetsByDepartment(Integer companyId, Integer departmentId) {
        TenantId tenantId = TenantId.of(companyId);
        List<FixedAssetAggregate> assets = fixedAssetRepository.findByTenantIdAndDepartmentId(
            tenantId, departmentId);
        return assets.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    
    /**
     * Get fixed assets by status
     */
    @Transactional(readOnly = true)
    public List<FixedAssetDTO> getFixedAssetsByStatus(Integer companyId, String status) {
        TenantId tenantId = TenantId.of(companyId);
        List<FixedAssetAggregate> assets = fixedAssetRepository.findByTenantIdAndStatus(
            tenantId, status);
        return assets.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    
    /**
     * Calculate total asset value for company
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAssetValue(Integer companyId) {
        TenantId tenantId = TenantId.of(companyId);
        return fixedAssetRepository.sumCurrentValueByTenant(tenantId);
    }
    
    // ========== Helper Methods ==========
    
    private FixedAssetAggregate findAssetByIdAndTenant(Integer assetId, TenantId tenantId) {
        return fixedAssetRepository.findByIdAndTenant(assetId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Fixed asset not found with ID: " + assetId));
    }
    
    private void validateCreateCommand(CreateFixedAssetCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Create fixed asset command cannot be null");
        }
        if (command.getName() == null || command.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Asset name is required");
        }
        if (command.getAcquisitionCost() == null || 
            command.getAcquisitionCost().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Acquisition cost must be positive");
        }
        if (command.getCompanyId() == null) {
            throw new IllegalArgumentException("Company ID cannot be null");
        }
        if (command.getAcquisitionDate() == null) {
            throw new IllegalArgumentException("Acquisition date cannot be null");
        }
    }
    
    private void validateUpdateCommand(UpdateFixedAssetCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Update fixed asset command cannot be null");
        }
        if (command.getCompanyId() == null) {
            throw new IllegalArgumentException("Company ID cannot be null");
        }
    }
    
    private FixedAssetDTO mapToDTO(FixedAssetAggregate fixedAsset) {
        return FixedAssetDTO.builder()
                .assetId(fixedAsset.getAssetId())
                .name(fixedAsset.getName())
                .description(fixedAsset.getDescription())
                .acquisitionDate(fixedAsset.getAcquisitionDate())
                .acquisitionCost(fixedAsset.getAcquisitionCost().getAmount())
                .currentValue(fixedAsset.getCurrentValue().getAmount())
                .accumulatedDepreciation(fixedAsset.getAccumulatedDepreciation().getAmount())
                .location(fixedAsset.getLocation())
                .serialNumber(fixedAsset.getSerialNumber())
                .status(fixedAsset.getStatus())
                .companyId(fixedAsset.getTenantId().getValue())
                .departmentId(fixedAsset.getDepartmentId())
                .createdAt(fixedAsset.getCreatedAt())
                .updatedAt(fixedAsset.getUpdatedAt())
                .isActive(fixedAsset.isActive())
                .netBookValue(fixedAsset.getNetBookValue().getAmount())
                .build();
    }
}