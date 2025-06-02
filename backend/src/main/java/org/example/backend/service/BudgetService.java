// backend/src/main/java/org/example/backend/service/BudgetService.java
package org.example.backend.service;

import org.example.backend.application.service.CompanyApplicationService;
import org.example.backend.application.dto.CompanyDTO;
import org.example.backend.model.Budget;
import org.example.backend.model.BudgetLine;
import org.example.backend.model.Company;
import org.example.backend.model.Department;
import org.example.backend.repository.BudgetLineRepository;
import org.example.backend.repository.BudgetRepository;
import org.example.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Budget Service - DDD适配器版本
 * 
 * 改造策略：
 * 1. 保持原有Service接口不变（向后兼容）
 * 2. 内部使用DDD应用服务进行业务验证
 * 3. 利用CompanyApplicationService验证公司状态
 * 4. 添加预算控制和审批流程功能
 */
@Service
@Transactional
public class BudgetService {
    
    @Autowired
    private BudgetRepository budgetRepository;
    
    @Autowired
    private BudgetLineRepository budgetLineRepository;
    
    @Autowired
    private CompanyApplicationService companyApplicationService;
    
    @Autowired
    private DepartmentService departmentService;

    // ========== 保持原有接口不变 ==========
    
    public List<Budget> findAll() {
        return budgetRepository.findAll();
    }

    public Budget findById(Integer id) {
        return budgetRepository.findById(id).orElse(null);
    }
    
    /**
     * 增强版：使用DDD验证公司状态
     */
    public List<Budget> findByCompany(Company company) {
        validateCompanyActive(company.getCompanyId());
        return budgetRepository.findByCompany(company);
    }
    
    /**
     * 增强版：验证部门和公司状态
     */
    public List<Budget> findByDepartment(Department department) {
        if (department != null && department.getCompany() != null) {
            validateCompanyActive(department.getCompany().getCompanyId());
        }
        return budgetRepository.findByDepartment(department);
    }

    /**
     * 增强版：保存前进行业务验证
     */
    @Transactional
    public Budget save(Budget budget) {
        validateBudgetForSave(budget);
        
        if (budget.getCreatedAt() == null) {
            budget.setCreatedAt(LocalDateTime.now());
        }
        budget.setUpdatedAt(LocalDateTime.now());
        
        return budgetRepository.save(budget);
    }
    
    /**
     * 增强版：保存预算及其明细行
     */
    @Transactional
    public Budget saveWithLines(Budget budget, List<BudgetLine> lines) {
        Budget savedBudget = save(budget);
        
        if (lines != null) {
            // 验证并保存预算明细行
            BigDecimal totalLineAmount = BigDecimal.ZERO;
            
            for (BudgetLine line : lines) {
                validateBudgetLine(line, savedBudget);
                line.setBudget(savedBudget);
                budgetLineRepository.save(line);
                totalLineAmount = totalLineAmount.add(line.getAmount());
            }
            
            // 更新预算总额
            savedBudget.setTotalAmount(totalLineAmount);
            savedBudget = budgetRepository.save(savedBudget);
        }
        
        return savedBudget;
    }

    @Transactional
    public void deleteById(Integer id) {
        Budget budget = findById(id);
        if (budget != null) {
            validateBudgetCanBeDeleted(budget);
        }
        budgetRepository.deleteById(id);
    }
    
    // ========== 新增的DDD业务方法 ==========
    
    /**
     * 根据公司ID获取预算列表（新方法）
     */
    public List<Budget> findByCompanyId(Integer companyId) {
        CompanyDTO company = validateAndGetCompany(companyId);
        Company companyEntity = convertToEntity(company);
        return budgetRepository.findByCompany(companyEntity);
    }
    
    /**
     * 创建新预算（新方法，增强业务验证）
     */
    public Budget createBudget(Integer companyId, String name, String description, 
                              Integer departmentId, Integer fiscalPeriodId, BigDecimal totalAmount) {
        CompanyDTO company = validateAndGetCompany(companyId);
        
        // 验证部门（如果指定）
        Department department = null;
        if (departmentId != null) {
            department = departmentService.findById(departmentId);
            if (department == null) {
                throw new ResourceNotFoundException("Department not found: " + departmentId);
            }
            
            // 验证部门属于同一公司
            if (!department.getCompany().getCompanyId().equals(companyId)) {
                throw new IllegalArgumentException("Department must belong to the same company");
            }
        }
        
        // 创建新预算
        Budget budget = new Budget();
        budget.setName(name);
        budget.setDescription(description);
        budget.setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);
        budget.setCompany(convertToEntity(company));
        budget.setDepartment(department);
        budget.setStatus(Budget.BudgetStatus.DRAFT);
        
        return save(budget);
    }
    
    /**
     * 批准预算（新方法）
     */
    public Budget approveBudget(Integer budgetId) {
        Budget budget = findById(budgetId);
        if (budget == null) {
            throw new ResourceNotFoundException("Budget not found: " + budgetId);
        }
        
        validateCompanyActive(budget.getCompany().getCompanyId());
        
        if (budget.getStatus() != Budget.BudgetStatus.DRAFT) {
            throw new IllegalStateException("Only draft budgets can be approved");
        }
        
        // 验证预算完整性
        validateBudgetCompleteness(budget);
        
        budget.setStatus(Budget.BudgetStatus.APPROVED);
        return save(budget);
    }
    
    /**
     * 关闭预算（新方法）
     */
    public Budget closeBudget(Integer budgetId) {
        Budget budget = findById(budgetId);
        if (budget == null) {
            throw new ResourceNotFoundException("Budget not found: " + budgetId);
        }
        
        validateCompanyActive(budget.getCompany().getCompanyId());
        
        if (budget.getStatus() != Budget.BudgetStatus.APPROVED) {
            throw new IllegalStateException("Only approved budgets can be closed");
        }
        
        budget.setStatus(Budget.BudgetStatus.CLOSED);
        return save(budget);
    }
    
    /**
     * 获取预算执行情况（新方法）
     */
    public BudgetExecution getBudgetExecution(Integer budgetId) {
        Budget budget = findById(budgetId);
        if (budget == null) {
            throw new ResourceNotFoundException("Budget not found: " + budgetId);
        }
        
        validateCompanyActive(budget.getCompany().getCompanyId());
        
        List<BudgetLine> lines = budgetLineRepository.findByBudget(budget);
        
        BigDecimal totalBudget = budget.getTotalAmount();
        BigDecimal totalUsed = lines.stream()
                .map(BudgetLine::getUsedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remaining = totalBudget.subtract(totalUsed);
        
        double executionRate = totalBudget.compareTo(BigDecimal.ZERO) > 0 ? 
                totalUsed.divide(totalBudget, 4, BigDecimal.ROUND_HALF_UP).doubleValue() : 0.0;
        
        return new BudgetExecution(
            budget,
            totalBudget,
            totalUsed,
            remaining,
            executionRate,
            lines.size()
        );
    }
    
    /**
     * 更新预算行使用金额（新方法）
     */
    public BudgetLine updateBudgetLineUsage(Integer lineId, BigDecimal usedAmount) {
        BudgetLine line = budgetLineRepository.findById(lineId).orElse(null);
        if (line == null) {
            throw new ResourceNotFoundException("Budget line not found: " + lineId);
        }
        
        validateCompanyActive(line.getBudget().getCompany().getCompanyId());
        
        if (line.getBudget().getStatus() != Budget.BudgetStatus.APPROVED) {
            throw new IllegalStateException("Cannot update usage for non-approved budget");
        }
        
        if (usedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Used amount cannot be negative");
        }
        
        if (usedAmount.compareTo(line.getAmount()) > 0) {
            throw new IllegalArgumentException("Used amount cannot exceed budgeted amount");
        }
        
        line.setUsedAmount(usedAmount);
        return budgetLineRepository.save(line);
    }
    
    /**
     * 获取公司预算汇总（新方法）
     */
    public BudgetSummary getBudgetSummary(Integer companyId) {
        List<Budget> budgets = findByCompanyId(companyId);
        
        BigDecimal totalBudgetAmount = budgets.stream()
                .map(Budget::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long approvedBudgets = budgets.stream()
                .filter(b -> b.getStatus() == Budget.BudgetStatus.APPROVED)
                .count();
        
        long draftBudgets = budgets.stream()
                .filter(b -> b.getStatus() == Budget.BudgetStatus.DRAFT)
                .count();
        
        return new BudgetSummary(
            totalBudgetAmount,
            (int) approvedBudgets,
            (int) draftBudgets,
            budgets.size()
        );
    }
    
    /**
     * 检查预算超支情况（新方法）
     */
    public List<BudgetOverrunAlert> checkBudgetOverruns(Integer companyId, double thresholdPercentage) {
        List<Budget> budgets = findByCompanyId(companyId).stream()
                .filter(b -> b.getStatus() == Budget.BudgetStatus.APPROVED)
                .collect(Collectors.toList());
        
        return budgets.stream()
                .map(budget -> {
                    BudgetExecution execution = getBudgetExecution(budget.getBudgetId());
                    if (execution.getExecutionRate() >= thresholdPercentage / 100.0) {
                        return new BudgetOverrunAlert(
                            budget,
                            execution.getExecutionRate() * 100,
                            execution.getRemaining()
                        );
                    }
                    return null;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
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
     * 验证预算保存前的业务规则
     */
    private void validateBudgetForSave(Budget budget) {
        if (budget == null) {
            throw new IllegalArgumentException("Budget cannot be null");
        }
        
        if (budget.getName() == null || budget.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Budget name cannot be empty");
        }
        
        if (budget.getCompany() == null || budget.getCompany().getCompanyId() == null) {
            throw new IllegalArgumentException("Budget must belong to a company");
        }
        
        // 使用DDD服务验证公司状态
        validateCompanyActive(budget.getCompany().getCompanyId());
        
        // 验证预算金额
        if (budget.getTotalAmount() != null && budget.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Budget amount cannot be negative");
        }
    }
    
    /**
     * 验证预算明细行
     */
    private void validateBudgetLine(BudgetLine line, Budget budget) {
        if (line == null) {
            throw new IllegalArgumentException("Budget line cannot be null");
        }
        
        if (line.getAmount() == null || line.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Budget line amount must be positive");
        }
        
        if (line.getUsedAmount() != null && line.getUsedAmount().compareTo(line.getAmount()) > 0) {
            throw new IllegalArgumentException("Used amount cannot exceed budgeted amount");
        }
    }
    
    /**
     * 验证预算完整性
     */
    private void validateBudgetCompleteness(Budget budget) {
        List<BudgetLine> lines = budgetLineRepository.findByBudget(budget);
        
        if (lines.isEmpty()) {
            throw new IllegalStateException("Budget must have at least one budget line to be approved");
        }
        
        BigDecimal totalLineAmount = lines.stream()
                .map(BudgetLine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 检查预算总额与明细行总额是否匹配
        if (budget.getTotalAmount().compareTo(totalLineAmount) != 0) {
            throw new IllegalStateException("Budget total amount must match sum of budget lines");
        }
    }
    
    /**
     * 验证预算是否可以删除
     */
    private void validateBudgetCanBeDeleted(Budget budget) {
        validateCompanyActive(budget.getCompany().getCompanyId());
        
        if (budget.getStatus() == Budget.BudgetStatus.APPROVED) {
            throw new IllegalStateException("Cannot delete approved budget");
        }
        
        // 检查是否有已使用的预算行
        List<BudgetLine> lines = budgetLineRepository.findByBudget(budget);
        boolean hasUsedAmount = lines.stream()
                .anyMatch(line -> line.getUsedAmount().compareTo(BigDecimal.ZERO) > 0);
        
        if (hasUsedAmount) {
            throw new IllegalStateException("Cannot delete budget with used amounts");
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
    
    // ========== 内部类 ==========
    
    /**
     * 预算执行情况
     */
    public static class BudgetExecution {
        private final Budget budget;
        private final BigDecimal totalBudget;
        private final BigDecimal totalUsed;
        private final BigDecimal remaining;
        private final double executionRate;
        private final int lineCount;
        
        public BudgetExecution(Budget budget, BigDecimal totalBudget, BigDecimal totalUsed,
                             BigDecimal remaining, double executionRate, int lineCount) {
            this.budget = budget;
            this.totalBudget = totalBudget;
            this.totalUsed = totalUsed;
            this.remaining = remaining;
            this.executionRate = executionRate;
            this.lineCount = lineCount;
        }
        
        public Budget getBudget() { return budget; }
        public BigDecimal getTotalBudget() { return totalBudget; }
        public BigDecimal getTotalUsed() { return totalUsed; }
        public BigDecimal getRemaining() { return remaining; }
        public double getExecutionRate() { return executionRate; }
        public int getLineCount() { return lineCount; }
        
        public boolean isOverBudget() {
            return totalUsed.compareTo(totalBudget) > 0;
        }
        
        public boolean isNearLimit(double thresholdPercentage) {
            return executionRate >= thresholdPercentage / 100.0;
        }
    }
    
    /**
     * 预算汇总信息
     */
    public static class BudgetSummary {
        private final BigDecimal totalBudgetAmount;
        private final int approvedBudgets;
        private final int draftBudgets;
        private final int totalBudgets;
        
        public BudgetSummary(BigDecimal totalBudgetAmount, int approvedBudgets,
                           int draftBudgets, int totalBudgets) {
            this.totalBudgetAmount = totalBudgetAmount;
            this.approvedBudgets = approvedBudgets;
            this.draftBudgets = draftBudgets;
            this.totalBudgets = totalBudgets;
        }
        
        public BigDecimal getTotalBudgetAmount() { return totalBudgetAmount; }
        public int getApprovedBudgets() { return approvedBudgets; }
        public int getDraftBudgets() { return draftBudgets; }
        public int getTotalBudgets() { return totalBudgets; }
    }
    
    /**
     * 预算超支警告
     */
    public static class BudgetOverrunAlert {
        private final Budget budget;
        private final double executionPercentage;
        private final BigDecimal remainingAmount;
        private final LocalDateTime alertTime;
        
        public BudgetOverrunAlert(Budget budget, double executionPercentage, BigDecimal remainingAmount) {
            this.budget = budget;
            this.executionPercentage = executionPercentage;
            this.remainingAmount = remainingAmount;
            this.alertTime = LocalDateTime.now();
        }
        
        public Budget getBudget() { return budget; }
        public double getExecutionPercentage() { return executionPercentage; }
        public BigDecimal getRemainingAmount() { return remainingAmount; }
        public LocalDateTime getAlertTime() { return alertTime; }
        
        public String getAlertLevel() {
            if (executionPercentage >= 100) return "CRITICAL";
            if (executionPercentage >= 90) return "HIGH";
            if (executionPercentage >= 80) return "MEDIUM";
            return "LOW";
        }
    }
}