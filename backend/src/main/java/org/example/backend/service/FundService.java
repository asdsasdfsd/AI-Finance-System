// backend/src/main/java/org/example/backend/service/FundService.java
package org.example.backend.service;

import org.example.backend.application.service.CompanyApplicationService;
import org.example.backend.application.dto.CompanyDTO;
import org.example.backend.model.Company;
import org.example.backend.model.Fund;
import org.example.backend.repository.FundRepository;
import org.example.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fund Service - DDD适配器版本
 * 
 * 改造策略：
 * 1. 保持原有Service接口不变（向后兼容）
 * 2. 内部使用DDD应用服务进行业务验证
 * 3. 利用CompanyApplicationService验证公司状态
 * 4. 添加基金管理和财务控制功能
 */
@Service
@Transactional
public class FundService {
    
    @Autowired
    private FundRepository fundRepository;
    
    @Autowired
    private CompanyApplicationService companyApplicationService;

    // ========== 保持原有接口不变 ==========
    
    public List<Fund> findAll() {
        return fundRepository.findAll();
    }

    public Fund findById(Integer id) {
        return fundRepository.findById(id).orElse(null);
    }
    
    /**
     * 增强版：使用DDD验证公司状态
     */
    public List<Fund> findByCompany(Company company) {
        validateCompanyActive(company.getCompanyId());
        return fundRepository.findByCompany(company);
    }

    public List<Fund> findByCompanyId(Integer companyId) {
        validateCompanyActive(companyId);
        return fundRepository.findByCompanyCompanyId(companyId);
    }

    /**
     * 增强版：验证公司状态后返回活跃基金
     */
    public List<Fund> findActiveByCompany(Company company) {
        validateCompanyActive(company.getCompanyId());
        return fundRepository.findByCompanyAndIsActive(company, true);
    }

    /**
     * 增强版：保存前进行业务验证
     */
    public Fund save(Fund fund) {
        validateFundForSave(fund);
        
        if (fund.getCreatedAt() == null) {
            fund.setCreatedAt(LocalDateTime.now());
        }
        fund.setUpdatedAt(LocalDateTime.now());
        
        return fundRepository.save(fund);
    }

    public void deleteById(Integer id) {
        Fund fund = findById(id);
        if (fund != null) {
            validateFundCanBeDeleted(fund);
        }
        fundRepository.deleteById(id);
    }
    
    // ========== 新增的DDD业务方法 ==========
    
    /**
     * 根据公司ID获取活跃基金（新方法）
     */
    public List<Fund> findActiveByCompanyId(Integer companyId) {
        CompanyDTO company = validateAndGetCompany(companyId);
        Company companyEntity = convertToEntity(company);
        return fundRepository.findByCompanyAndIsActive(companyEntity, true);
    }
    
    /**
     * 创建新基金（新方法，增强业务验证）
     */
    public Fund createFund(Integer companyId, String name, String description, 
                          String fundType, BigDecimal initialBalance) {
        CompanyDTO company = validateAndGetCompany(companyId);
        
        // 验证基金名称唯一性
        if (existsByCompanyAndName(companyId, name)) {
            throw new IllegalArgumentException("Fund name already exists in this company: " + name);
        }
        
        // 创建新基金
        Fund fund = new Fund();
        fund.setName(name);
        fund.setDescription(description);
        fund.setFundType(fundType);
        fund.setBalance(initialBalance != null ? initialBalance : BigDecimal.ZERO);
        fund.setCompany(convertToEntity(company));
        fund.setIsActive(true);
        
        return save(fund);
    }
    
    /**
     * 更新基金余额（新方法）
     */
    public Fund updateBalance(Integer fundId, BigDecimal newBalance, String reason) {
        Fund fund = findById(fundId);
        if (fund == null) {
            throw new ResourceNotFoundException("Fund not found: " + fundId);
        }
        
        validateCompanyActive(fund.getCompany().getCompanyId());
        
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Fund balance cannot be negative");
        }
        
        BigDecimal oldBalance = fund.getBalance();
        fund.setBalance(newBalance);
        
        // TODO: 可以在这里记录余额变更的审计日志
        // auditLogService.logFundBalanceChange(fund, oldBalance, newBalance, reason);
        
        return save(fund);
    }
    
    /**
     * 基金间转账（新方法）
     */
    public FundTransferResult transferBetweenFunds(Integer fromFundId, Integer toFundId, 
                                                  BigDecimal amount, String description) {
        Fund fromFund = findById(fromFundId);
        Fund toFund = findById(toFundId);
        
        if (fromFund == null || toFund == null) {
            throw new ResourceNotFoundException("One or both funds not found");
        }
        
        // 验证两个基金属于同一公司
        if (!fromFund.getCompany().getCompanyId().equals(toFund.getCompany().getCompanyId())) {
            throw new IllegalArgumentException("Funds must belong to the same company");
        }
        
        validateCompanyActive(fromFund.getCompany().getCompanyId());
        
        // 验证转账金额
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        
        if (fromFund.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance in source fund");
        }
        
        // 执行转账
        BigDecimal fromOldBalance = fromFund.getBalance();
        BigDecimal toOldBalance = toFund.getBalance();
        
        fromFund.setBalance(fromOldBalance.subtract(amount));
        toFund.setBalance(toOldBalance.add(amount));
        
        Fund updatedFromFund = save(fromFund);
        Fund updatedToFund = save(toFund);
        
        return new FundTransferResult(updatedFromFund, updatedToFund, amount, description);
    }
    
    /**
     * 激活/停用基金（新方法）
     */
    public Fund toggleFundStatus(Integer fundId) {
        Fund fund = findById(fundId);
        if (fund == null) {
            throw new ResourceNotFoundException("Fund not found: " + fundId);
        }
        
        validateCompanyActive(fund.getCompany().getCompanyId());
        
        fund.setIsActive(!fund.getIsActive());
        return save(fund);
    }
    
    /**
     * 获取公司基金汇总信息（新方法）
     */
    public FundSummary getFundSummary(Integer companyId) {
        List<Fund> funds = findByCompanyId(companyId);
        
        BigDecimal totalBalance = funds.stream()
                .map(Fund::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long activeFunds = funds.stream()
                .filter(Fund::getIsActive)
                .count();
        
        BigDecimal activeBalance = funds.stream()
                .filter(Fund::getIsActive)
                .map(Fund::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new FundSummary(
            totalBalance,
            activeBalance,
            (int) activeFunds,
            funds.size()
        );
    }
    
    /**
     * 根据基金类型获取基金（新方法）
     */
    public List<Fund> findByCompanyIdAndType(Integer companyId, String fundType) {
        List<Fund> funds = findByCompanyId(companyId);
        return funds.stream()
                .filter(fund -> fundType.equals(fund.getFundType()))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取基金余额历史（新方法）
     */
    public List<FundBalanceSnapshot> getFundBalanceHistory(Integer fundId) {
        Fund fund = findById(fundId);
        if (fund == null) {
            throw new ResourceNotFoundException("Fund not found: " + fundId);
        }
        
        validateCompanyActive(fund.getCompany().getCompanyId());
        
        // TODO: 实现基金余额历史查询
        // 这里可以从审计日志或者专门的余额历史表中查询
        return List.of(new FundBalanceSnapshot(
            fund.getFundId(),
            fund.getBalance(),
            LocalDateTime.now(),
            "Current Balance"
        ));
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
     * 验证基金保存前的业务规则
     */
    private void validateFundForSave(Fund fund) {
        if (fund == null) {
            throw new IllegalArgumentException("Fund cannot be null");
        }
        
        if (fund.getName() == null || fund.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Fund name cannot be empty");
        }
        
        if (fund.getCompany() == null || fund.getCompany().getCompanyId() == null) {
            throw new IllegalArgumentException("Fund must belong to a company");
        }
        
        // 使用DDD服务验证公司状态
        validateCompanyActive(fund.getCompany().getCompanyId());
        
        // 验证基金名称在公司内不重复（对新建基金）
        if (fund.getFundId() == null) {
            if (existsByCompanyAndName(fund.getCompany().getCompanyId(), fund.getName())) {
                throw new IllegalArgumentException("Fund name already exists in this company: " + fund.getName());
            }
        }
        
        // 验证余额不能为负数
        if (fund.getBalance() != null && fund.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Fund balance cannot be negative");
        }
    }
    
    /**
     * 验证基金是否可以删除
     */
    private void validateFundCanBeDeleted(Fund fund) {
        validateCompanyActive(fund.getCompany().getCompanyId());
        
        // 检查基金余额是否为零
        if (fund.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Cannot delete fund with non-zero balance: " + fund.getBalance());
        }
        
        // TODO: 检查是否有关联的交易记录
        // 可以通过TransactionApplicationService检查是否有使用此基金的交易
    }
    
    /**
     * 检查基金名称是否在公司内重复
     */
    private boolean existsByCompanyAndName(Integer companyId, String name) {
        List<Fund> funds = findByCompanyId(companyId);
        return funds.stream()
                .anyMatch(fund -> name.equals(fund.getName()));
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
     * 基金转账结果
     */
    public static class FundTransferResult {
        private final Fund fromFund;
        private final Fund toFund;
        private final BigDecimal amount;
        private final String description;
        private final LocalDateTime transferTime;
        
        public FundTransferResult(Fund fromFund, Fund toFund, BigDecimal amount, String description) {
            this.fromFund = fromFund;
            this.toFund = toFund;
            this.amount = amount;
            this.description = description;
            this.transferTime = LocalDateTime.now();
        }
        
        public Fund getFromFund() { return fromFund; }
        public Fund getToFund() { return toFund; }
        public BigDecimal getAmount() { return amount; }
        public String getDescription() { return description; }
        public LocalDateTime getTransferTime() { return transferTime; }
    }
    
    /**
     * 基金汇总信息
     */
    public static class FundSummary {
        private final BigDecimal totalBalance;
        private final BigDecimal activeBalance;
        private final int activeFunds;
        private final int totalFunds;
        
        public FundSummary(BigDecimal totalBalance, BigDecimal activeBalance, 
                          int activeFunds, int totalFunds) {
            this.totalBalance = totalBalance;
            this.activeBalance = activeBalance;
            this.activeFunds = activeFunds;
            this.totalFunds = totalFunds;
        }
        
        public BigDecimal getTotalBalance() { return totalBalance; }
        public BigDecimal getActiveBalance() { return activeBalance; }
        public int getActiveFunds() { return activeFunds; }
        public int getTotalFunds() { return totalFunds; }
    }
    
    /**
     * 基金余额快照
     */
    public static class FundBalanceSnapshot {
        private final Integer fundId;
        private final BigDecimal balance;
        private final LocalDateTime snapshotTime;
        private final String description;
        
        public FundBalanceSnapshot(Integer fundId, BigDecimal balance, 
                                 LocalDateTime snapshotTime, String description) {
            this.fundId = fundId;
            this.balance = balance;
            this.snapshotTime = snapshotTime;
            this.description = description;
        }
        
        public Integer getFundId() { return fundId; }
        public BigDecimal getBalance() { return balance; }
        public LocalDateTime getSnapshotTime() { return snapshotTime; }
        public String getDescription() { return description; }
    }
}