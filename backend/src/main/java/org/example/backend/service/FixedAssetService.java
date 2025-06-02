// backend/src/main/java/org/example/backend/service/FixedAssetService.java
package org.example.backend.service;

import org.example.backend.application.service.CompanyApplicationService;
import org.example.backend.application.service.UserApplicationService;
import org.example.backend.application.dto.CompanyDTO;
import org.example.backend.application.dto.UserDTO;
import org.example.backend.model.Company;
import org.example.backend.model.Department;
import org.example.backend.model.FixedAsset;
import org.example.backend.model.User;
import org.example.backend.repository.FixedAssetRepository;
import org.example.backend.repository.DepartmentRepository;
import org.example.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Fixed Asset Service - DDD适配器版本
 * 
 * 改造策略：
 * 1. 保持原有Service接口不变（向后兼容）
 * 2. 内部使用DDD应用服务进行业务验证
 * 3. 利用CompanyApplicationService和UserApplicationService验证
 * 4. 仅增强现有方法的业务验证，不添加新方法
 */
@Service
@Transactional
public class FixedAssetService {
    
    @Autowired
    private FixedAssetRepository fixedAssetRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private CompanyApplicationService companyApplicationService;
    
    @Autowired
    private UserApplicationService userApplicationService;

    // ========== 保持原有接口不变，仅增强业务验证 ==========
    
    public List<FixedAsset> findAll() {
        return fixedAssetRepository.findAll();
    }

    public FixedAsset findById(Integer id) {
        return fixedAssetRepository.findById(id).orElse(null);
    }
    
    /**
     * 增强版：使用DDD验证公司状态
     */
    public List<FixedAsset> findByCompany(Company company) {
        if (company != null && company.getCompanyId() != null) {
            validateCompanyActive(company.getCompanyId());
        }
        return fixedAssetRepository.findByCompany(company);
    }
    
    /**
     * 增强版：验证部门归属
     */
    public List<FixedAsset> findByDepartment(Department department) {
        if (department != null) {
            if (department.getCompany() != null && department.getCompany().getCompanyId() != null) {
                validateCompanyActive(department.getCompany().getCompanyId());
            }
            validateDepartmentExists(department.getDepartmentId());
        }
        return fixedAssetRepository.findByDepartment(department);
    }
    
    public List<FixedAsset> findByStatus(FixedAsset.AssetStatus status) {
        return fixedAssetRepository.findByStatus(status);
    }

    /**
     * 增强版：保存前进行业务验证
     */
    public FixedAsset save(FixedAsset fixedAsset) {
        validateFixedAssetForSave(fixedAsset);
        
        if (fixedAsset.getCreatedAt() == null) {
            fixedAsset.setCreatedAt(LocalDateTime.now());
        }
        fixedAsset.setUpdatedAt(LocalDateTime.now());
        
        return fixedAssetRepository.save(fixedAsset);
    }

    /**
     * 增强版：删除前进行业务验证
     */
    public void deleteById(Integer id) {
        FixedAsset fixedAsset = findById(id);
        if (fixedAsset != null) {
            validateFixedAssetCanBeDeleted(fixedAsset);
        }
        fixedAssetRepository.deleteById(id);
    }
    
    // ========== 业务验证方法 ==========
    
    /**
     * 验证公司是否激活
     */
    private void validateCompanyActive(Integer companyId) {
        try {
            CompanyDTO company = companyApplicationService.getCompanyById(companyId);
            if (!company.isActive()) {
                throw new IllegalStateException("Company is not active: " + companyId);
            }
        } catch (Exception e) {
            throw new ResourceNotFoundException("Company not found: " + companyId, e);
        }
    }
    
    /**
     * 验证部门是否存在
     */
    private void validateDepartmentExists(Integer departmentId) {
        if (departmentId != null) {
            Department department = departmentRepository.findById(departmentId).orElse(null);
            if (department == null) {
                throw new ResourceNotFoundException("Department not found: " + departmentId);
            }
        }
    }
    
    /**
     * 验证固定资产保存前的业务规则
     */
    private void validateFixedAssetForSave(FixedAsset fixedAsset) {
        if (fixedAsset == null) {
            throw new IllegalArgumentException("Fixed asset cannot be null");
        }
        
        if (fixedAsset.getName() == null || fixedAsset.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Fixed asset name cannot be empty");
        }
        
        if (fixedAsset.getCompany() == null || fixedAsset.getCompany().getCompanyId() == null) {
            throw new IllegalArgumentException("Fixed asset must belong to a company");
        }
        
        // 使用DDD服务验证公司状态
        validateCompanyActive(fixedAsset.getCompany().getCompanyId());
        
        // 验证部门存在（如果指定了部门）
        if (fixedAsset.getDepartment() != null && fixedAsset.getDepartment().getDepartmentId() != null) {
            validateDepartmentExists(fixedAsset.getDepartment().getDepartmentId());
            
            // 验证部门属于同一公司
            Department department = departmentRepository.findById(fixedAsset.getDepartment().getDepartmentId()).orElse(null);
            if (department != null && !department.getCompany().getCompanyId().equals(fixedAsset.getCompany().getCompanyId())) {
                throw new IllegalArgumentException("Department must belong to the same company as the fixed asset");
            }
        }
        
        // 验证资产数据的基本业务规则
        if (fixedAsset.getAcquisitionCost() != null && fixedAsset.getAcquisitionCost().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Acquisition cost cannot be negative");
        }
        
        if (fixedAsset.getCurrentValue() != null && fixedAsset.getCurrentValue().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Current value cannot be negative");
        }
        
        if (fixedAsset.getAccumulatedDepreciation() != null && fixedAsset.getAccumulatedDepreciation().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Accumulated depreciation cannot be negative");
        }
        
        // 验证折旧不能超过购置成本
        if (fixedAsset.getAcquisitionCost() != null && fixedAsset.getAccumulatedDepreciation() != null) {
            if (fixedAsset.getAccumulatedDepreciation().compareTo(fixedAsset.getAcquisitionCost()) > 0) {
                throw new IllegalArgumentException("Accumulated depreciation cannot exceed acquisition cost");
            }
        }
        
        // 验证购置日期不能是未来
        if (fixedAsset.getAcquisitionDate() != null && fixedAsset.getAcquisitionDate().isAfter(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("Acquisition date cannot be in the future");
        }
    }
    
    /**
     * 验证固定资产是否可以删除
     */
    private void validateFixedAssetCanBeDeleted(FixedAsset fixedAsset) {
        if (fixedAsset.getCompany() != null && fixedAsset.getCompany().getCompanyId() != null) {
            validateCompanyActive(fixedAsset.getCompany().getCompanyId());
        }
        
        // 检查资产状态 - 只有ACTIVE状态的资产可以删除
        if (fixedAsset.getStatus() != FixedAsset.AssetStatus.ACTIVE) {
            throw new IllegalStateException("Cannot delete fixed asset that is not in ACTIVE status: " + fixedAsset.getStatus());
        }
        
        // 可以添加更多业务规则，比如检查是否有相关的交易记录等
        // 这里暂时只做基本验证
    }
}