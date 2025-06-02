// backend/src/main/java/org/example/backend/service/FiscalPeriodService.java
package org.example.backend.service;

import org.example.backend.application.service.CompanyApplicationService;
import org.example.backend.application.dto.CompanyDTO;
import org.example.backend.model.Company;
import org.example.backend.model.FiscalPeriod;
import org.example.backend.repository.FiscalPeriodRepository;
import org.example.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FiscalPeriod Service - DDD适配器版本
 * 
 * 改造策略：
 * 1. 保持原有Service接口不变（向后兼容）
 * 2. 内部使用DDD应用服务进行业务验证
 * 3. 添加会计期间管理业务规则
 * 4. 支持自动生成会计期间
 */
@Service
@Transactional
public class FiscalPeriodService {
    
    @Autowired
    private FiscalPeriodRepository fiscalPeriodRepository;
    
    @Autowired
    private CompanyApplicationService companyApplicationService;

    // ========== 保持原有接口不变 ==========
    
    public List<FiscalPeriod> findAll() {
        return fiscalPeriodRepository.findAll();
    }

    public FiscalPeriod findById(Integer id) {
        return fiscalPeriodRepository.findById(id).orElse(null);
    }
    
    /**
     * 增强版：使用DDD验证公司状态
     */
    public List<FiscalPeriod> findByCompany(Company company) {
        validateCompanyActive(company.getCompanyId());
        return fiscalPeriodRepository.findByCompany(company);
    }
    
    /**
     * 增强版：验证公司状态
     */
    public List<FiscalPeriod> findOpenPeriods(Company company) {
        validateCompanyActive(company.getCompanyId());
        return fiscalPeriodRepository.findByCompanyAndStatus(company, FiscalPeriod.PeriodStatus.OPEN);
    }
    
    /**
     * 增强版：验证公司状态和日期有效性
     */
    public FiscalPeriod findPeriodForDate(Company company, LocalDate date) {
        validateCompanyActive(company.getCompanyId());
        
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        
        return fiscalPeriodRepository.findByCompanyAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                company, date, date);
    }

    /**
     * 增强版：保存前进行业务验证
     */
    public FiscalPeriod save(FiscalPeriod fiscalPeriod) {
        validateFiscalPeriodForSave(fiscalPeriod);
        
        if (fiscalPeriod.getCreatedAt() == null) {
            fiscalPeriod.setCreatedAt(LocalDateTime.now());
        }
        fiscalPeriod.setUpdatedAt(LocalDateTime.now());
        
        return fiscalPeriodRepository.save(fiscalPeriod);
    }

    public void deleteById(Integer id) {
        FiscalPeriod period = findById(id);
        if (period != null) {
            validatePeriodCanBeDeleted(period);
        }
        fiscalPeriodRepository.deleteById(id);
    }
    
    // ========== 新增的DDD业务方法 ==========
    
    /**
     * 根据公司ID获取会计期间（新方法）
     */
    public List<FiscalPeriod> findByCompanyId(Integer companyId) {
        CompanyDTO company = validateAndGetCompany(companyId);
        Company companyEntity = convertToEntity(company);
        return fiscalPeriodRepository.findByCompany(companyEntity);
    }
    
    /**
     * 获取当前开放的会计期间（新方法）
     */
    public FiscalPeriod getCurrentOpenPeriod(Integer companyId) {
        List<FiscalPeriod> openPeriods = findOpenPeriodsByCompanyId(companyId);
        LocalDate today = LocalDate.now();
        
        return openPeriods.stream()
                .filter(period -> !today.isBefore(period.getStartDate()) && !today.isAfter(period.getEndDate()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 获取公司的开放期间列表（新方法）
     */
    public List<FiscalPeriod> findOpenPeriodsByCompanyId(Integer companyId) {
        CompanyDTO company = validateAndGetCompany(companyId);
        Company companyEntity = convertToEntity(company);
        return fiscalPeriodRepository.findByCompanyAndStatus(companyEntity, FiscalPeriod.PeriodStatus.OPEN);
    }
    
    /**
     * 创建新的会计期间（新方法）
     */
    public FiscalPeriod createFiscalPeriod(Integer companyId, LocalDate startDate, LocalDate endDate, 
                                         FiscalPeriod.PeriodType periodType) {
        CompanyDTO company = validateAndGetCompany(companyId);
        
        // 验证日期范围
        validateDateRange(startDate, endDate);
        
        // 验证期间不重叠
        validateNoOverlap(companyId, startDate, endDate, null);
        
        // 创建新期间
        FiscalPeriod period = new FiscalPeriod();
        period.setCompany(convertToEntity(company));
        period.setStartDate(startDate);
        period.setEndDate(endDate);
        period.setPeriodType(periodType);
        period.setStatus(FiscalPeriod.PeriodStatus.OPEN);
        
        return save(period);
    }
    
    /**
     * 自动生成年度会计期间（新方法）
     */
    public List<FiscalPeriod> generateYearlyPeriods(Integer companyId, int year, String fiscalYearStart) {
        CompanyDTO company = validateAndGetCompany(companyId);
        
        // 解析财年开始月日（格式：MM-dd）
        String[] parts = fiscalYearStart.split("-");
        int startMonth = Integer.parseInt(parts[0]);
        int startDay = Integer.parseInt(parts[1]);
        
        // 计算财年起始日期
        LocalDate fiscalYearStartDate = LocalDate.of(year, startMonth, startDay);
        LocalDate fiscalYearEndDate = fiscalYearStartDate.plusYears(1).minusDays(1);
        
        // 生成12个月度期间
        List<FiscalPeriod> periods = new java.util.ArrayList<>();
        LocalDate currentStart = fiscalYearStartDate;
        
        for (int i = 0; i < 12; i++) {
            LocalDate currentEnd = currentStart.plusMonths(1).minusDays(1);
            
            // 确保最后一个期间不超过财年结束日期
            if (i == 11) {
                currentEnd = fiscalYearEndDate;
            }
            
            FiscalPeriod period = createFiscalPeriod(companyId, currentStart, currentEnd, 
                                                   FiscalPeriod.PeriodType.MONTH);
            periods.add(period);
            
            currentStart = currentEnd.plusDays(1);
        }
        
        return periods;
    }
    
    /**
     * 关闭会计期间（新方法）
     */
    public FiscalPeriod closeFiscalPeriod(Integer periodId) {
        FiscalPeriod period = findById(periodId);
        if (period == null) {
            throw new ResourceNotFoundException("Fiscal period not found: " + periodId);
        }
        
        validateCompanyActive(period.getCompany().getCompanyId());
        
        if (period.getStatus() != FiscalPeriod.PeriodStatus.OPEN) {
            throw new IllegalStateException("Only open periods can be closed");
        }
        
        // 验证期间内的交易是否都已过账
        validateAllTransactionsPosted(period);
        
        period.setStatus(FiscalPeriod.PeriodStatus.CLOSED);
        return save(period);
    }
    
    /**
     * 锁定会计期间（新方法）
     */
    public FiscalPeriod lockFiscalPeriod(Integer periodId) {
        FiscalPeriod period = findById(periodId);
        if (period == null) {
            throw new ResourceNotFoundException("Fiscal period not found: " + periodId);
        }
        
        validateCompanyActive(period.getCompany().getCompanyId());
        
        if (period.getStatus() != FiscalPeriod.PeriodStatus.CLOSED) {
            throw new IllegalStateException("Only closed periods can be locked");
        }
        
        period.setStatus(FiscalPeriod.PeriodStatus.LOCKED);
        return save(period);
    }
    
    /**
     * 重新开放会计期间（新方法）
     */
    public FiscalPeriod reopenFiscalPeriod(Integer periodId) {
        FiscalPeriod period = findById(periodId);
        if (period == null) {
            throw new ResourceNotFoundException("Fiscal period not found: " + periodId);
        }
        
        validateCompanyActive(period.getCompany().getCompanyId());
        
        if (period.getStatus() == FiscalPeriod.PeriodStatus.LOCKED) {
            throw new IllegalStateException("Locked periods cannot be reopened");
        }
        
        period.setStatus(FiscalPeriod.PeriodStatus.OPEN);
        return save(period);
    }
    
    /**
     * 获取会计期间统计信息（新方法）
     */
    public FiscalPeriodStats getPeriodStats(Integer companyId) {
        List<FiscalPeriod> allPeriods = findByCompanyId(companyId);
        
        long openPeriods = allPeriods.stream()
                .filter(p -> p.getStatus() == FiscalPeriod.PeriodStatus.OPEN)
                .count();
        
        long closedPeriods = allPeriods.stream()
                .filter(p -> p.getStatus() == FiscalPeriod.PeriodStatus.CLOSED)
                .count();
        
        long lockedPeriods = allPeriods.stream()
                .filter(p -> p.getStatus() == FiscalPeriod.PeriodStatus.LOCKED)
                .count();
        
        return new FiscalPeriodStats(
            allPeriods.size(),
            (int) openPeriods,
            (int) closedPeriods,
            (int) lockedPeriods
        );
    }
    
    /**
     * 检查期间是否可以进行交易（新方法）
     */
    public boolean canTransactInPeriod(Integer companyId, LocalDate transactionDate) {
        if (transactionDate == null) {
            return false;
        }
        
        CompanyDTO company = validateAndGetCompany(companyId);
        FiscalPeriod period = findPeriodForDate(convertToEntity(company), transactionDate);
        
        return period != null && period.getStatus() == FiscalPeriod.PeriodStatus.OPEN;
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
     * 验证会计期间保存前的业务规则
     */
    private void validateFiscalPeriodForSave(FiscalPeriod period) {
        if (period == null) {
            throw new IllegalArgumentException("Fiscal period cannot be null");
        }
        
        if (period.getCompany() == null || period.getCompany().getCompanyId() == null) {
            throw new IllegalArgumentException("Fiscal period must belong to a company");
        }
        
        if (period.getStartDate() == null || period.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        
        validateCompanyActive(period.getCompany().getCompanyId());
        validateDateRange(period.getStartDate(), period.getEndDate());
        
        // 验证期间不重叠（对新建期间或日期变更）
        validateNoOverlap(period.getCompany().getCompanyId(), 
                         period.getStartDate(), period.getEndDate(), period.getPeriodId());
    }
    
    /**
     * 验证日期范围
     */
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        if (ChronoUnit.DAYS.between(startDate, endDate) > 366) {
            throw new IllegalArgumentException("Fiscal period cannot exceed 366 days");
        }
    }
    
    /**
     * 验证期间不重叠
     */
    private void validateNoOverlap(Integer companyId, LocalDate startDate, LocalDate endDate, Integer excludePeriodId) {
        List<FiscalPeriod> existingPeriods = findByCompanyId(companyId);
        
        for (FiscalPeriod existing : existingPeriods) {
            // 跳过当前编辑的期间
            if (excludePeriodId != null && existing.getPeriodId().equals(excludePeriodId)) {
                continue;
            }
            
            // 检查重叠
            if (isDateRangeOverlap(startDate, endDate, existing.getStartDate(), existing.getEndDate())) {
                throw new IllegalArgumentException("Fiscal period overlaps with existing period: " + 
                    existing.getStartDate() + " to " + existing.getEndDate());
            }
        }
    }
    
    /**
     * 检查日期范围是否重叠
     */
    private boolean isDateRangeOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }
    
    /**
     * 验证期间是否可以删除
     */
    private void validatePeriodCanBeDeleted(FiscalPeriod period) {
        validateCompanyActive(period.getCompany().getCompanyId());
        
        if (period.getStatus() == FiscalPeriod.PeriodStatus.LOCKED) {
            throw new IllegalStateException("Cannot delete locked fiscal period");
        }
        
        // 检查期间内是否有交易
        if (hasTransactionsInPeriod(period)) {
            throw new IllegalStateException("Cannot delete fiscal period with transactions");
        }
    }
    
    /**
     * 验证期间内所有交易都已过账
     */
    private void validateAllTransactionsPosted(FiscalPeriod period) {
        // TODO: 实现交易过账状态检查
        // 这里可以调用TransactionApplicationService检查期间内的交易状态
    }
    
    /**
     * 检查期间内是否有交易
     */
    private boolean hasTransactionsInPeriod(FiscalPeriod period) {
        // TODO: 实现交易存在性检查
        // 这里可以调用TransactionApplicationService检查期间内是否有交易
        return false;
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
        company.setFiscalYearStart(dto.getFiscalYearStart());
        return company;
    }
    
    // ========== 内部类 ==========
    
    /**
     * 会计期间统计信息
     */
    public static class FiscalPeriodStats {
        private final int totalPeriods;
        private final int openPeriods;
        private final int closedPeriods;
        private final int lockedPeriods;
        
        public FiscalPeriodStats(int totalPeriods, int openPeriods, int closedPeriods, int lockedPeriods) {
            this.totalPeriods = totalPeriods;
            this.openPeriods = openPeriods;
            this.closedPeriods = closedPeriods;
            this.lockedPeriods = lockedPeriods;
        }
        
        public int getTotalPeriods() { return totalPeriods; }
        public int getOpenPeriods() { return openPeriods; }
        public int getClosedPeriods() { return closedPeriods; }
        public int getLockedPeriods() { return lockedPeriods; }
        
        public double getOpenPercentage() {
            return totalPeriods > 0 ? (double) openPeriods / totalPeriods * 100 : 0;
        }
    }
}