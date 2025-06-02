// backend/src/main/java/org/example/backend/service/AccountService.java
package org.example.backend.service;

import org.example.backend.application.service.CompanyApplicationService;
import org.example.backend.application.dto.CompanyDTO;
import org.example.backend.model.Account;
import org.example.backend.model.Company;
import org.example.backend.repository.AccountRepository;
import org.example.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Account Service - DDD适配器版本
 * 
 * 改造策略：
 * 1. 保持原有Service接口不变（向后兼容）
 * 2. 使用DDD应用服务验证公司状态
 * 3. 添加会计科目的业务验证和层级管理
 * 4. 增强科目编码和命名规则验证
 */
@Service
@Transactional
public class AccountService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private CompanyApplicationService companyApplicationService;

    // ========== 保持原有接口不变 ==========
    
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public Account findById(Integer id) {
        return accountRepository.findById(id).orElse(null);
    }
    
    /**
     * 增强版：验证公司状态
     */
    public List<Account> findByCompany(Company company) {
        validateCompanyActive(company.getCompanyId());
        return accountRepository.findByCompany(company);
    }
    
    /**
     * 增强版：验证公司状态和科目类型
     */
    public List<Account> findByCompanyAndType(Company company, Account.AccountType type) {
        validateCompanyActive(company.getCompanyId());
        validateAccountType(type);
        return accountRepository.findByCompanyAndAccountType(company, type);
    }
    
    public List<Account> findSubaccounts(Account parentAccount) {
        if (parentAccount != null && parentAccount.getCompany() != null) {
            validateCompanyActive(parentAccount.getCompany().getCompanyId());
        }
        return accountRepository.findByParentAccount(parentAccount);
    }

    /**
     * 增强版：保存前进行业务验证
     */
    public Account save(Account account) {
        validateAccountForSave(account);
        
        if (account.getCreatedAt() == null) {
            account.setCreatedAt(LocalDateTime.now());
        }
        account.setUpdatedAt(LocalDateTime.now());
        
        return accountRepository.save(account);
    }

    public void deleteById(Integer id) {
        Account account = findById(id);
        if (account != null) {
            validateAccountCanBeDeleted(account);
        }
        accountRepository.deleteById(id);
    }
    
    // ========== 新增的DDD业务方法 ==========
    
    /**
     * 根据公司ID获取科目列表（新方法）
     */
    public List<Account> findByCompanyId(Integer companyId) {
        CompanyDTO company = validateAndGetCompany(companyId);
        Company companyEntity = convertCompanyToEntity(company);
        return accountRepository.findByCompany(companyEntity);
    }
    
    /**
     * 根据公司ID和科目类型获取科目（新方法）
     */
    public List<Account> findByCompanyIdAndType(Integer companyId, Account.AccountType type) {
        CompanyDTO company = validateAndGetCompany(companyId);
        validateAccountType(type);
        Company companyEntity = convertCompanyToEntity(company);
        return accountRepository.findByCompanyAndAccountType(companyEntity, type);
    }
    
    /**
     * 获取公司的顶级科目（新方法）
     */
    public List<Account> findTopLevelAccounts(Integer companyId) {
        List<Account> allAccounts = findByCompanyId(companyId);
        return allAccounts.stream()
                .filter(account -> account.getParentAccount() == null)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据科目代码查找科目（新方法）
     */
    public Account findByCompanyIdAndCode(Integer companyId, String accountCode) {
        List<Account> accounts = findByCompanyId(companyId);
        return accounts.stream()
                .filter(account -> accountCode.equals(account.getAccountCode()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 创建新科目（新方法，增强业务验证）
     */
    public Account createAccount(Integer companyId, String accountCode, String name, 
                               Account.AccountType accountType, Account.BalanceDirection balanceDirection,
                               Integer parentAccountId) {
        CompanyDTO company = validateAndGetCompany(companyId);
        
        // 验证科目代码唯一性
        if (existsByCompanyAndCode(companyId, accountCode)) {
            throw new IllegalArgumentException("Account code already exists: " + accountCode);
        }
        
        // 验证父科目（如果有）
        Account parentAccount = null;
        if (parentAccountId != null) {
            parentAccount = validateAndGetParentAccount(parentAccountId, companyId, accountType);
        }
        
        // 验证科目类型和借贷方向的一致性
        validateAccountTypeAndDirection(accountType, balanceDirection);
        
        // 创建新科目
        Account account = new Account();
        account.setAccountCode(accountCode);
        account.setName(name);
        account.setAccountType(accountType);
        account.setBalanceDirection(balanceDirection);
        account.setCompany(convertCompanyToEntity(company));
        account.setParentAccount(parentAccount);
        account.setIsActive(true);
        
        return save(account);
    }
    
    /**
     * 移动科目到新的父科目（新方法）
     */
    public Account moveAccount(Integer accountId, Integer newParentId) {
        Account account = findById(accountId);
        if (account == null) {
            throw new ResourceNotFoundException("Account not found: " + accountId);
        }
        
        Integer companyId = account.getCompany().getCompanyId();
        validateCompanyActive(companyId);
        
        Account newParent = null;
        if (newParentId != null) {
            newParent = validateAndGetParentAccount(newParentId, companyId, account.getAccountType());
            
            // 防止循环依赖
            if (isCircularDependency(account, newParent)) {
                throw new IllegalArgumentException("Cannot create circular account hierarchy");
            }
        }
        
        account.setParentAccount(newParent);
        return save(account);
    }
    
    /**
     * 获取科目层级结构（新方法）
     */
    public AccountHierarchy getAccountHierarchy(Integer companyId) {
        List<Account> allAccounts = findByCompanyId(companyId);
        
        // 按科目类型分组
        java.util.Map<Account.AccountType, List<Account>> accountsByType = allAccounts.stream()
                .collect(Collectors.groupingBy(Account::getAccountType));
        
        // 构建层级结构
        java.util.Map<Account.AccountType, List<Account>> topLevelByType = accountsByType.entrySet().stream()
                .collect(Collectors.toMap(
                    java.util.Map.Entry::getKey,
                    entry -> entry.getValue().stream()
                            .filter(account -> account.getParentAccount() == null)
                            .collect(Collectors.toList())
                ));
        
        return new AccountHierarchy(topLevelByType, buildHierarchyMap(allAccounts));
    }
    
    /**
     * 获取科目余额试算表数据结构（新方法）
     */
    public TrialBalanceStructure getTrialBalanceStructure(Integer companyId) {
        List<Account> allAccounts = findByCompanyId(companyId);
        
        java.util.Map<Account.AccountType, List<Account>> accountsByType = allAccounts.stream()
                .filter(Account::getIsActive)
                .collect(Collectors.groupingBy(Account::getAccountType));
        
        return new TrialBalanceStructure(accountsByType);
    }
    
    /**
     * 批量创建标准会计科目（新方法）
     */
    @Transactional
    public List<Account> createStandardChartOfAccounts(Integer companyId) {
        CompanyDTO company = validateAndGetCompany(companyId);
        
        // 检查是否已有科目
        List<Account> existingAccounts = findByCompanyId(companyId);
        if (!existingAccounts.isEmpty()) {
            throw new IllegalStateException("Company already has chart of accounts");
        }
        
        return createBasicChartOfAccounts(company);
    }
    
    /**
     * 启用/禁用科目（新方法）
     */
    public Account toggleAccountStatus(Integer accountId) {
        Account account = findById(accountId);
        if (account == null) {
            throw new ResourceNotFoundException("Account not found: " + accountId);
        }
        
        validateCompanyActive(account.getCompany().getCompanyId());
        
        // 如果要禁用，检查是否有子科目或交易记录
        if (account.getIsActive()) {
            validateAccountCanBeDisabled(account);
        }
        
        account.setIsActive(!account.getIsActive());
        return save(account);
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
     * 验证科目类型
     */
    private void validateAccountType(Account.AccountType type) {
        if (type == null) {
            throw new IllegalArgumentException("Account type cannot be null");
        }
    }
    
    /**
     * 验证并获取父科目
     */
    private Account validateAndGetParentAccount(Integer parentAccountId, Integer companyId, Account.AccountType accountType) {
        Account parentAccount = findById(parentAccountId);
        if (parentAccount == null) {
            throw new ResourceNotFoundException("Parent account not found: " + parentAccountId);
        }
        
        if (!parentAccount.getCompany().getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("Parent account must belong to the same company");
        }
        
        if (!parentAccount.getAccountType().equals(accountType)) {
            throw new IllegalArgumentException("Parent account must be the same type as child account");
        }
        
        return parentAccount;
    }
    
    /**
     * 验证科目类型和借贷方向的一致性
     */
    private void validateAccountTypeAndDirection(Account.AccountType accountType, Account.BalanceDirection balanceDirection) {
        boolean isValidCombination = false;
        
        switch (accountType) {
            case ASSET:
            case EXPENSE:
                isValidCombination = (balanceDirection == Account.BalanceDirection.DEBIT);
                break;
            case LIABILITY:
            case EQUITY:
            case REVENUE:
                isValidCombination = (balanceDirection == Account.BalanceDirection.CREDIT);
                break;
        }
        
        if (!isValidCombination) {
            throw new IllegalArgumentException("Invalid combination of account type and balance direction");
        }
    }
    
    /**
     * 验证科目保存前的业务规则
     */
    private void validateAccountForSave(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        
        if (account.getAccountCode() == null || account.getAccountCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Account code cannot be empty");
        }
        
        if (account.getName() == null || account.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Account name cannot be empty");
        }
        
        if (account.getAccountType() == null) {
            throw new IllegalArgumentException("Account type cannot be null");
        }
        
        if (account.getBalanceDirection() == null) {
            throw new IllegalArgumentException("Balance direction cannot be null");
        }
        
        if (account.getCompany() == null || account.getCompany().getCompanyId() == null) {
            throw new IllegalArgumentException("Account must belong to a company");
        }
        
        // 验证公司状态
        validateCompanyActive(account.getCompany().getCompanyId());
        
        // 验证科目类型和借贷方向一致性
        validateAccountTypeAndDirection(account.getAccountType(), account.getBalanceDirection());
        
        // 验证科目代码唯一性（对新建科目）
        if (account.getAccountId() == null) {
            if (existsByCompanyAndCode(account.getCompany().getCompanyId(), account.getAccountCode())) {
                throw new IllegalArgumentException("Account code already exists: " + account.getAccountCode());
            }
        }
        
        // 验证科目代码格式
        if (!isValidAccountCode(account.getAccountCode())) {
            throw new IllegalArgumentException("Invalid account code format: " + account.getAccountCode());
        }
    }
    
    /**
     * 验证科目是否可以删除
     */
    private void validateAccountCanBeDeleted(Account account) {
        validateCompanyActive(account.getCompany().getCompanyId());
        
        // 检查是否有子科目
        List<Account> children = findSubaccounts(account);
        if (!children.isEmpty()) {
            throw new IllegalStateException("Cannot delete account with sub-accounts");
        }
        
        // TODO: 检查是否有交易记录
        // 这里可以添加检查会计分录、交易等的逻辑
    }
    
    /**
     * 验证科目是否可以禁用
     */
    private void validateAccountCanBeDisabled(Account account) {
        // 检查是否有活跃的子科目
        List<Account> children = findSubaccounts(account);
        long activeChildren = children.stream()
                .filter(Account::getIsActive)
                .count();
        
        if (activeChildren > 0) {
            throw new IllegalStateException("Cannot disable account with active sub-accounts");
        }
        
        // TODO: 检查是否有未结账的交易记录
    }
    
    /**
     * 检查是否存在循环依赖
     */
    private boolean isCircularDependency(Account account, Account newParent) {
        if (newParent == null) {
            return false;
        }
        
        Account current = newParent;
        while (current != null) {
            if (current.getAccountId().equals(account.getAccountId())) {
                return true;
            }
            current = current.getParentAccount();
        }
        
        return false;
    }
    
    /**
     * 检查科目代码是否在公司内重复
     */
    private boolean existsByCompanyAndCode(Integer companyId, String accountCode) {
        List<Account> accounts = findByCompanyId(companyId);
        return accounts.stream()
                .anyMatch(account -> accountCode.equals(account.getAccountCode()));
    }
    
    /**
     * 验证科目代码格式
     */
    private boolean isValidAccountCode(String accountCode) {
        // 基本格式验证：数字或数字+字母组合，长度2-10位
        return accountCode.matches("^[0-9A-Za-z]{2,10}$");
    }
    
    /**
     * 构建层级结构映射
     */
    private java.util.Map<Integer, List<Account>> buildHierarchyMap(List<Account> allAccounts) {
        return allAccounts.stream()
                .filter(account -> account.getParentAccount() != null)
                .collect(Collectors.groupingBy(account -> account.getParentAccount().getAccountId()));
    }
    
    /**
     * 创建基础会计科目
     */
    private List<Account> createBasicChartOfAccounts(CompanyDTO company) {
        Company companyEntity = convertCompanyToEntity(company);
        List<Account> accounts = new java.util.ArrayList<>();
        
        // 资产类科目
        accounts.add(createBasicAccount("1001", "库存现金", Account.AccountType.ASSET, 
                                      Account.BalanceDirection.DEBIT, companyEntity, null));
        accounts.add(createBasicAccount("1002", "银行存款", Account.AccountType.ASSET, 
                                      Account.BalanceDirection.DEBIT, companyEntity, null));
        accounts.add(createBasicAccount("1122", "应收账款", Account.AccountType.ASSET, 
                                      Account.BalanceDirection.DEBIT, companyEntity, null));
        
        // 负债类科目
        accounts.add(createBasicAccount("2001", "短期借款", Account.AccountType.LIABILITY, 
                                      Account.BalanceDirection.CREDIT, companyEntity, null));
        accounts.add(createBasicAccount("2202", "应付账款", Account.AccountType.LIABILITY, 
                                      Account.BalanceDirection.CREDIT, companyEntity, null));
        
        // 所有者权益类科目
        accounts.add(createBasicAccount("3001", "实收资本", Account.AccountType.EQUITY, 
                                      Account.BalanceDirection.CREDIT, companyEntity, null));
        accounts.add(createBasicAccount("3103", "未分配利润", Account.AccountType.EQUITY, 
                                      Account.BalanceDirection.CREDIT, companyEntity, null));
        
        // 收入类科目
        accounts.add(createBasicAccount("6001", "主营业务收入", Account.AccountType.REVENUE, 
                                      Account.BalanceDirection.CREDIT, companyEntity, null));
        
        // 费用类科目
        accounts.add(createBasicAccount("6401", "主营业务成本", Account.AccountType.EXPENSE, 
                                      Account.BalanceDirection.DEBIT, companyEntity, null));
        accounts.add(createBasicAccount("6602", "销售费用", Account.AccountType.EXPENSE, 
                                      Account.BalanceDirection.DEBIT, companyEntity, null));
        accounts.add(createBasicAccount("6603", "管理费用", Account.AccountType.EXPENSE, 
                                      Account.BalanceDirection.DEBIT, companyEntity, null));
        
        // 保存所有科目
        return accounts.stream()
                .map(this::save)
                .collect(Collectors.toList());
    }
    
    /**
     * 创建基础科目实例
     */
    private Account createBasicAccount(String code, String name, Account.AccountType type, 
                                     Account.BalanceDirection direction, Company company, Account parent) {
        Account account = new Account();
        account.setAccountCode(code);
        account.setName(name);
        account.setAccountType(type);
        account.setBalanceDirection(direction);
        account.setCompany(company);
        account.setParentAccount(parent);
        account.setIsActive(true);
        return account;
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
    
    // ========== 内部类 ==========
    
    /**
     * 科目层级结构
     */
    public static class AccountHierarchy {
        private final java.util.Map<Account.AccountType, List<Account>> topLevelAccountsByType;
        private final java.util.Map<Integer, List<Account>> childrenMap;
        
        public AccountHierarchy(java.util.Map<Account.AccountType, List<Account>> topLevelAccountsByType,
                               java.util.Map<Integer, List<Account>> childrenMap) {
            this.topLevelAccountsByType = topLevelAccountsByType;
            this.childrenMap = childrenMap;
        }
        
        public java.util.Map<Account.AccountType, List<Account>> getTopLevelAccountsByType() { 
            return topLevelAccountsByType; 
        }
        public java.util.Map<Integer, List<Account>> getChildrenMap() { 
            return childrenMap; 
        }
    }
    
    /**
     * 试算表结构
     */
    public static class TrialBalanceStructure {
        private final java.util.Map<Account.AccountType, List<Account>> accountsByType;
        
        public TrialBalanceStructure(java.util.Map<Account.AccountType, List<Account>> accountsByType) {
            this.accountsByType = accountsByType;
        }
        
        public java.util.Map<Account.AccountType, List<Account>> getAccountsByType() { 
            return accountsByType; 
        }
        
        public List<Account> getAssetAccounts() { 
            return accountsByType.getOrDefault(Account.AccountType.ASSET, java.util.Collections.emptyList()); 
        }
        
        public List<Account> getLiabilityAccounts() { 
            return accountsByType.getOrDefault(Account.AccountType.LIABILITY, java.util.Collections.emptyList()); 
        }
        
        public List<Account> getEquityAccounts() { 
            return accountsByType.getOrDefault(Account.AccountType.EQUITY, java.util.Collections.emptyList()); 
        }
        
        public List<Account> getRevenueAccounts() { 
            return accountsByType.getOrDefault(Account.AccountType.REVENUE, java.util.Collections.emptyList()); 
        }
        
        public List<Account> getExpenseAccounts() { 
            return accountsByType.getOrDefault(Account.AccountType.EXPENSE, java.util.Collections.emptyList()); 
        }
    }
}