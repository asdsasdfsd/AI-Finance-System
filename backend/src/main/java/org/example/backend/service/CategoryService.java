// backend/src/main/java/org/example/backend/service/CategoryService.java
package org.example.backend.service;

import org.example.backend.application.service.CompanyApplicationService;
import org.example.backend.application.dto.CompanyDTO;
import org.example.backend.model.Category;
import org.example.backend.model.Company;
import org.example.backend.repository.CategoryRepository;
import org.example.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Category Service - DDD适配器版本
 * 
 * 改造策略：
 * 1. 保持原有Service接口不变（向后兼容）
 * 2. 内部使用DDD应用服务进行业务验证
 * 3. 利用现有的CompanyApplicationService验证公司状态
 * 4. 逐步添加新的业务功能
 */
@Service
@Transactional
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private CompanyApplicationService companyApplicationService;

    // ========== 保持原有接口不变 ==========
    
    public List<Category> findAll() { 
        return categoryRepository.findAll(); 
    }
    
    public Category findById(Integer id) { 
        return categoryRepository.findById(id).orElse(null); 
    }
    
    /**
     * 增强版：使用DDD验证公司状态
     */
    public List<Category> findByCompanyAndType(Company company, Category.CategoryType type) {
        validateCompanyActive(company.getCompanyId());
        return categoryRepository.findByCompanyAndType(company, type);
    }
    
    /**
     * 增强版：使用DDD验证公司状态
     */
    public List<Category> findTopLevelCategories(Company company) {
        validateCompanyActive(company.getCompanyId());
        return categoryRepository.findByCompanyAndParentCategoryIsNull(company);
    }
    
    public List<Category> findSubcategories(Category parentCategory) {
        if (parentCategory != null && parentCategory.getCompany() != null) {
            validateCompanyActive(parentCategory.getCompany().getCompanyId());
        }
        return categoryRepository.findByParentCategory(parentCategory);
    }
    
    /**
     * 增强版：保存前进行业务验证
     */
    public Category save(Category category) {
        validateCategoryForSave(category);
        
        if (category.getCreatedAt() == null) {
            category.setCreatedAt(LocalDateTime.now());
        }
        category.setUpdatedAt(LocalDateTime.now());
        
        return categoryRepository.save(category);
    }
    
    public void deleteById(Integer id) {
        Category category = findById(id);
        if (category != null && category.getCompany() != null) {
            validateCompanyActive(category.getCompany().getCompanyId());
        }
        categoryRepository.deleteById(id);
    }
    
    // ========== 新增的DDD业务方法 ==========
    
    /**
     * 根据公司ID获取分类（新方法）
     */
    public List<Category> findByCompanyId(Integer companyId) {
        CompanyDTO company = validateAndGetCompany(companyId);
        Company companyEntity = convertToEntity(company);
        return categoryRepository.findByCompanyAndParentCategoryIsNull(companyEntity);
    }
    
    /**
     * 根据公司ID和类型获取分类（新方法）
     */
    public List<Category> findByCompanyIdAndType(Integer companyId, Category.CategoryType type) {
        CompanyDTO company = validateAndGetCompany(companyId);
        Company companyEntity = convertToEntity(company);
        return categoryRepository.findByCompanyAndType(companyEntity, type);
    }
    
    /**
     * 创建新分类（新方法，增强业务验证）
     */
    public Category createCategory(Integer companyId, String name, Category.CategoryType type, 
                                 Integer parentCategoryId) {
        CompanyDTO company = validateAndGetCompany(companyId);
        
        // 验证父分类（如果有）
        Category parentCategory = null;
        if (parentCategoryId != null) {
            parentCategory = findById(parentCategoryId);
            if (parentCategory == null) {
                throw new ResourceNotFoundException("Parent category not found: " + parentCategoryId);
            }
            
            // 验证父分类属于同一公司
            if (!parentCategory.getCompany().getCompanyId().equals(companyId)) {
                throw new IllegalArgumentException("Parent category must belong to the same company");
            }
        }
        
        // 创建新分类
        Category category = new Category();
        category.setName(name);
        category.setType(type);
        category.setCompany(convertToEntity(company));
        category.setParentCategory(parentCategory);
        category.setIsActive(true);
        
        return save(category);
    }
    
    /**
     * 检查分类名称是否在公司内重复（新方法）
     */
    public boolean existsByCompanyAndName(Integer companyId, String name) {
        CompanyDTO company = validateAndGetCompany(companyId);
        Company companyEntity = convertToEntity(company);
        
        List<Category> categories = categoryRepository.findByCompanyAndType(companyEntity, null);
        return categories.stream()
                .anyMatch(cat -> name.equals(cat.getName()));
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
     * 验证分类保存前的业务规则
     */
    private void validateCategoryForSave(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        
        if (category.getCompany() == null || category.getCompany().getCompanyId() == null) {
            throw new IllegalArgumentException("Category must belong to a company");
        }
        
        // 使用DDD服务验证公司状态
        validateCompanyActive(category.getCompany().getCompanyId());
        
        // 验证分类名称在公司内不重复（对新建分类）
        if (category.getCategoryId() == null) {
            if (existsByCompanyAndName(category.getCompany().getCompanyId(), category.getName())) {
                throw new IllegalArgumentException("Category name already exists in this company: " + category.getName());
            }
        }
    }
    
    /**
     * 将DDD CompanyDTO转换为传统Company实体
     */
    private Company convertToEntity(CompanyDTO dto) {
        Company company = new Company();
        company.setCompanyId(dto.getCompanyId());
        company.setCompanyName(dto.getCompanyName());
        company.setEmail(dto.getEmail());
        company.setStatus(dto.getStatus().name());
        return company;
    }
    
    // ========== 统计和报表方法（可选） ==========
    
    /**
     * 获取公司的分类统计信息
     */
    public CategoryStats getCategoryStats(Integer companyId) {
        CompanyDTO company = validateAndGetCompany(companyId);
        Company companyEntity = convertToEntity(company);
        
        List<Category> incomeCategories = categoryRepository.findByCompanyAndType(companyEntity, Category.CategoryType.INCOME);
        List<Category> expenseCategories = categoryRepository.findByCompanyAndType(companyEntity, Category.CategoryType.EXPENSE);
        
        return new CategoryStats(
            incomeCategories.size(),
            expenseCategories.size(),
            incomeCategories.size() + expenseCategories.size()
        );
    }
    
    /**
     * 分类统计信息内部类
     */
    public static class CategoryStats {
        private final int incomeCategories;
        private final int expenseCategories;
        private final int totalCategories;
        
        public CategoryStats(int incomeCategories, int expenseCategories, int totalCategories) {
            this.incomeCategories = incomeCategories;
            this.expenseCategories = expenseCategories;
            this.totalCategories = totalCategories;
        }
        
        public int getIncomeCategories() { return incomeCategories; }
        public int getExpenseCategories() { return expenseCategories; }
        public int getTotalCategories() { return totalCategories; }
    }
}