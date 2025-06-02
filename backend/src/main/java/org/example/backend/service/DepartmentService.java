// backend/src/main/java/org/example/backend/service/DepartmentService.java
package org.example.backend.service;

import org.example.backend.application.service.CompanyApplicationService;
import org.example.backend.application.service.UserApplicationService;
import org.example.backend.application.dto.CompanyDTO;
import org.example.backend.application.dto.UserDTO;
import org.example.backend.model.Company;
import org.example.backend.model.Department;
import org.example.backend.model.User;
import org.example.backend.repository.DepartmentRepository;
import org.example.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Department Service - DDD适配器版本
 * 
 * 改造策略：
 * 1. 保持原有Service接口不变（向后兼容）
 * 2. 内部使用DDD应用服务进行业务验证
 * 3. 利用CompanyApplicationService和UserApplicationService验证
 * 4. 添加部门层级管理和权限验证
 */
@Service
@Transactional
public class DepartmentService {
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private CompanyApplicationService companyApplicationService;
    
    @Autowired
    private UserApplicationService userApplicationService;

    // ========== 保持原有接口不变 ==========
    
    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    public Department findById(Integer id) {
        return departmentRepository.findById(id).orElse(null);
    }
    
    /**
     * 增强版：使用DDD验证公司状态
     */
    public List<Department> findByCompany(Company company) {
        validateCompanyActive(company.getCompanyId());
        return departmentRepository.findByCompany(company);
    }
    
    public List<Department> findByParent(Department parent) {
        if (parent != null && parent.getCompany() != null) {
            validateCompanyActive(parent.getCompany().getCompanyId());
        }
        return departmentRepository.findByParentDepartment(parent);
    }
    
    /**
     * 增强版：验证经理权限
     */
    public List<Department> findByManager(User manager) {
        if (manager != null && manager.getCompany() != null) {
            validateCompanyActive(manager.getCompany().getCompanyId());
            validateUserIsActive(manager.getUserId());
        }
        return departmentRepository.findByManager(manager);
    }

    /**
     * 增强版：保存前进行业务验证
     */
    public Department save(Department department) {
        validateDepartmentForSave(department);
        
        if (department.getCreatedAt() == null) {
            department.setCreatedAt(LocalDateTime.now());
        }
        department.setUpdatedAt(LocalDateTime.now());
        
        return departmentRepository.save(department);
    }

    public void deleteById(Integer id) {
        Department department = findById(id);
        if (department != null) {
            validateDepartmentCanBeDeleted(department);
        }
        departmentRepository.deleteById(id);
    }
    
    // ========== 新增的DDD业务方法 ==========
    
    /**
     * 根据公司ID获取部门列表（新方法）
     */
    public List<Department> findByCompanyId(Integer companyId) {
        CompanyDTO company = validateAndGetCompany(companyId);
        Company companyEntity = convertCompanyToEntity(company);
        return departmentRepository.findByCompany(companyEntity);
    }
    
    /**
     * 获取公司的顶级部门（新方法）
     */
    public List<Department> findTopLevelDepartments(Integer companyId) {
        CompanyDTO company = validateAndGetCompany(companyId);
        Company companyEntity = convertCompanyToEntity(company);
        
        return departmentRepository.findByCompany(companyEntity).stream()
                .filter(dept -> dept.getParentDepartment() == null)
                .collect(Collectors.toList());
    }
    
    /**
     * 创建新部门（新方法，增强业务验证）
     */
    public Department createDepartment(Integer companyId, String name, String code, 
                                     BigDecimal budget, Integer parentDepartmentId, Integer managerId) {
        CompanyDTO company = validateAndGetCompany(companyId);
        
        // 验证部门代码唯一性
        if (existsByCompanyAndCode(companyId, code)) {
            throw new IllegalArgumentException("Department code already exists: " + code);
        }
        
        // 验证父部门（如果有）
        Department parentDepartment = null;
        if (parentDepartmentId != null) {
            parentDepartment = validateAndGetDepartment(parentDepartmentId, companyId);
        }
        
        // 验证经理（如果有）
        User manager = null;
        if (managerId != null) {
            manager = validateAndGetManager(managerId, companyId);
        }
        
        // 创建新部门
        Department department = new Department();
        department.setName(name);
        department.setCode(code);
        department.setBudget(budget != null ? budget : BigDecimal.ZERO);
        department.setCompany(convertCompanyToEntity(company));
        department.setParentDepartment(parentDepartment);
        department.setManager(manager);
        department.setIsActive(true);
        
        return save(department);
    }
    
    /**
     * 设置部门经理（新方法）
     */
    public Department setDepartmentManager(Integer departmentId, Integer managerId) {
        Department department = findById(departmentId);
        if (department == null) {
            throw new ResourceNotFoundException("Department not found: " + departmentId);
        }
        
        validateCompanyActive(department.getCompany().getCompanyId());
        
        User manager = null;
        if (managerId != null) {
            manager = validateAndGetManager(managerId, department.getCompany().getCompanyId());
        }
        
        department.setManager(manager);
        return save(department);
    }
    
    /**
     * 转移部门到新的父部门（新方法）
     */
    public Department transferDepartment(Integer departmentId, Integer newParentId) {
        Department department = findById(departmentId);
        if (department == null) {
            throw new ResourceNotFoundException("Department not found: " + departmentId);
        }
        
        Integer companyId = department.getCompany().getCompanyId();
        validateCompanyActive(companyId);
        
        Department newParent = null;
        if (newParentId != null) {
            newParent = validateAndGetDepartment(newParentId, companyId);
            
            // 防止循环依赖
            if (isCircularDependency(department, newParent)) {
                throw new IllegalArgumentException("Cannot create circular department hierarchy");
            }
        }
        
        department.setParentDepartment(newParent);
        return save(department);
    }
    
    /**
     * 获取部门层级结构（新方法）
     */
    public DepartmentHierarchy getDepartmentHierarchy(Integer companyId) {
        List<Department> allDepartments = findByCompanyId(companyId);
        List<Department> topLevel = allDepartments.stream()
                .filter(dept -> dept.getParentDepartment() == null)
                .collect(Collectors.toList());
        
        return new DepartmentHierarchy(topLevel, buildHierarchyMap(allDepartments));
    }
    
    /**
     * 获取部门预算汇总（新方法）
     */
    public DepartmentBudgetSummary getBudgetSummary(Integer companyId) {
        List<Department> departments = findByCompanyId(companyId);
        
        BigDecimal totalBudget = departments.stream()
                .map(Department::getBudget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long activeDepartments = departments.stream()
                .filter(Department::getIsActive)
                .count();
        
        return new DepartmentBudgetSummary(
            totalBudget,
            (int) activeDepartments,
            departments.size()
        );
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
     * 验证并获取公司信息
     */
    private CompanyDTO validateAndGetCompany(Integer companyId) {
        try {
            CompanyDTO company = companyApplicationService.getCompanyById(companyId);
            if (!company.isActive()) {
                throw new IllegalStateException("Company is not active: " + companyId);
            }
            return company;
        } catch (Exception e) {
            throw new ResourceNotFoundException("Company not found: " + companyId, e);
        }
    }
    
    /**
     * 验证用户是否激活
     */
    private void validateUserIsActive(Integer userId) {
        try {
            UserDTO user = userApplicationService.getUserById(userId);
            if (!user.isActiveAndUnlocked()) {
                throw new IllegalStateException("User is not active: " + userId);
            }
        } catch (Exception e) {
            throw new ResourceNotFoundException("User not found: " + userId, e);
        }
    }
    
    /**
     * 验证并获取部门
     */
    private Department validateAndGetDepartment(Integer departmentId, Integer companyId) {
        Department department = findById(departmentId);
        if (department == null) {
            throw new ResourceNotFoundException("Department not found: " + departmentId);
        }
        
        if (!department.getCompany().getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("Department does not belong to the specified company");
        }
        
        return department;
    }
    
    /**
     * 验证并获取经理用户
     */
    private User validateAndGetManager(Integer managerId, Integer companyId) {
        UserDTO userDTO = userApplicationService.getUserById(managerId);
        
        if (!userDTO.getTenantId().equals(companyId)) {
            throw new IllegalArgumentException("Manager must belong to the same company");
        }
        
        if (!userDTO.isActiveAndUnlocked()) {
            throw new IllegalStateException("Manager user is not active: " + managerId);
        }
        
        return convertUserToEntity(userDTO);
    }
    
    /**
     * 验证部门保存前的业务规则
     */
    private void validateDepartmentForSave(Department department) {
        if (department == null) {
            throw new IllegalArgumentException("Department cannot be null");
        }
        
        if (department.getName() == null || department.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Department name cannot be empty");
        }
        
        if (department.getCompany() == null || department.getCompany().getCompanyId() == null) {
            throw new IllegalArgumentException("Department must belong to a company");
        }
        
        // 使用DDD服务验证公司状态
        validateCompanyActive(department.getCompany().getCompanyId());
        
        // 验证部门代码唯一性（对新建部门）
        if (department.getDepartmentId() == null && department.getCode() != null) {
            if (existsByCompanyAndCode(department.getCompany().getCompanyId(), department.getCode())) {
                throw new IllegalArgumentException("Department code already exists: " + department.getCode());
            }
        }
        
        // 验证经理属于同一公司
        if (department.getManager() != null) {
            validateUserIsActive(department.getManager().getUserId());
        }
    }
    
    /**
     * 验证部门是否可以删除
     */
    private void validateDepartmentCanBeDeleted(Department department) {
        validateCompanyActive(department.getCompany().getCompanyId());
        
        // 检查是否有子部门
        List<Department> children = findByParent(department);
        if (!children.isEmpty()) {
            throw new IllegalStateException("Cannot delete department with child departments");
        }
        
        // 检查是否有员工（这里可以添加更多验证）
        // TODO: 可以通过UserApplicationService检查部门下是否有用户
    }
    
    /**
     * 检查是否存在循环依赖
     */
    private boolean isCircularDependency(Department department, Department newParent) {
        if (newParent == null) {
            return false;
        }
        
        Department current = newParent;
        while (current != null) {
            if (current.getDepartmentId().equals(department.getDepartmentId())) {
                return true;
            }
            current = current.getParentDepartment();
        }
        
        return false;
    }
    
    /**
     * 检查部门代码是否在公司内重复
     */
    private boolean existsByCompanyAndCode(Integer companyId, String code) {
        List<Department> departments = findByCompanyId(companyId);
        return departments.stream()
                .anyMatch(dept -> code.equals(dept.getCode()));
    }
    
    /**
     * 构建层级结构映射
     */
    private java.util.Map<Integer, List<Department>> buildHierarchyMap(List<Department> allDepartments) {
        return allDepartments.stream()
                .filter(dept -> dept.getParentDepartment() != null)
                .collect(Collectors.groupingBy(dept -> dept.getParentDepartment().getDepartmentId()));
    }
    
    // ========== 转换方法 ==========
    
    /**
     * 将DDD CompanyDTO转换为传统Company实体
     */
    private Company convertCompanyToEntity(CompanyDTO dto) {
        Company company = new Company();
        company.setCompanyId(dto.getCompanyId());
        company.setCompanyName(dto.getCompanyName());
        company.setEmail(dto.getEmail());
        company.setStatus(dto.getStatus().name());
        return company;
    }
    
    /**
     * 将DDD UserDTO转换为传统User实体
     */
    private User convertUserToEntity(UserDTO dto) {
        User user = new User();
        user.setUserId(dto.getUserId());
        user.setUsername(dto.getUsername());
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setEnabled(dto.getEnabled());
        return user;
    }
    
    // ========== 内部类 ==========
    
    /**
     * 部门层级结构
     */
    public static class DepartmentHierarchy {
        private final List<Department> topLevelDepartments;
        private final java.util.Map<Integer, List<Department>> childrenMap;
        
        public DepartmentHierarchy(List<Department> topLevelDepartments, 
                                 java.util.Map<Integer, List<Department>> childrenMap) {
            this.topLevelDepartments = topLevelDepartments;
            this.childrenMap = childrenMap;
        }
        
        public List<Department> getTopLevelDepartments() { return topLevelDepartments; }
        public java.util.Map<Integer, List<Department>> getChildrenMap() { return childrenMap; }
    }
    
    /**
     * 部门预算汇总
     */
    public static class DepartmentBudgetSummary {
        private final BigDecimal totalBudget;
        private final int activeDepartments;
        private final int totalDepartments;
        
        public DepartmentBudgetSummary(BigDecimal totalBudget, int activeDepartments, int totalDepartments) {
            this.totalBudget = totalBudget;
            this.activeDepartments = activeDepartments;
            this.totalDepartments = totalDepartments;
        }
        
        public BigDecimal getTotalBudget() { return totalBudget; }
        public int getActiveDepartments() { return activeDepartments; }
        public int getTotalDepartments() { return totalDepartments; }
    }
}