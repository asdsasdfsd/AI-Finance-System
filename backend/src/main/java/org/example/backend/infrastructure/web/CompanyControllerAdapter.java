// backend/src/main/java/org/example/backend/infrastructure/web/CompanyControllerAdapter.java
package org.example.backend.infrastructure.web;

import org.example.backend.application.service.CompanyApplicationService;
import org.example.backend.application.dto.CreateCompanyCommand;
import org.example.backend.application.dto.UpdateCompanyCommand;
import org.example.backend.application.dto.CompanyDTO;
import org.example.backend.application.dto.CompanyStatsDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Company Controller Adapter
 * 
 * 适配REST API调用到DDD应用服务命令
 * 保持与现有前端API的100%向后兼容性
 */
@RestController
@RequestMapping("/api/companies")
@CrossOrigin(origins = "http://localhost:3000")
@Profile("ddd")
public class CompanyControllerAdapter {
    
    private final CompanyApplicationService companyApplicationService;
    
    public CompanyControllerAdapter(CompanyApplicationService companyApplicationService) {
        this.companyApplicationService = companyApplicationService;
    }
    
    // ========== Create Operations ==========
    
    /**
     * 创建新公司 (向后兼容)
     */
    @PostMapping
    public ResponseEntity<CompanyDTO> createCompany(@RequestBody Map<String, Object> request) {
        CreateCompanyCommand command = mapToCreateCommand(request);
        CompanyDTO result = companyApplicationService.createCompany(command);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 创建公司和管理员用户 (新功能)
     */
    @PostMapping("/with-admin")
    public ResponseEntity<Map<String, Object>> createCompanyWithAdmin(@RequestBody Map<String, Object> request) {
        try {
            // 创建公司
            CreateCompanyCommand companyCommand = mapToCreateCommand(request);
            CompanyDTO company = companyApplicationService.createCompany(companyCommand);
            
            // TODO: 创建管理员用户（需要UserApplicationService）
            // CreateUserCommand userCommand = mapToCreateAdminUserCommand(request, company.getCompanyId());
            // UserDTO adminUser = userApplicationService.createUser(userCommand);
            
            Map<String, Object> response = Map.of(
                "company", company,
                "message", "公司创建成功"
                // "adminUser", adminUser
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "status", "error",
                "message", "创建公司失败: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            );
            return ResponseEntity.status(400).body(errorResponse);
        }
    }
    
    // ========== Update Operations ==========
    
    /**
     * 更新公司信息 (向后兼容)
     */
    @PutMapping("/{id}")
    public ResponseEntity<CompanyDTO> updateCompany(@PathVariable Integer id, 
                                                  @RequestBody Map<String, Object> request) {
        try {
            UpdateCompanyCommand command = mapToUpdateCommand(request);
            CompanyDTO result = companyApplicationService.updateCompany(id, command);
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * 激活公司
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<CompanyDTO> activateCompany(@PathVariable Integer id) {
        try {
            CompanyDTO result = companyApplicationService.activateCompany(id);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }
    
    /**
     * 暂停公司
     */
    @PostMapping("/{id}/suspend")
    public ResponseEntity<CompanyDTO> suspendCompany(@PathVariable Integer id,
                                                   @RequestBody Map<String, Object> request) {
        try {
            String reason = (String) request.getOrDefault("reason", "管理员操作");
            CompanyDTO result = companyApplicationService.suspendCompany(id, reason);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }
    
    /**
     * 更新订阅
     */
    @PostMapping("/{id}/subscription")
    public ResponseEntity<CompanyDTO> updateSubscription(@PathVariable Integer id,
                                                       @RequestBody Map<String, Object> request) {
        try {
            String expiresAtStr = (String) request.get("expiresAt");
            LocalDateTime expiresAt = LocalDateTime.parse(expiresAtStr);
            
            CompanyDTO result = companyApplicationService.updateSubscription(id, expiresAt);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }
    
    // ========== Query Operations (向后兼容) ==========
    
    /**
     * 获取所有公司 (向后兼容)
     */
    @GetMapping
    public ResponseEntity<List<CompanyDTO>> getAllCompanies() {
        List<CompanyDTO> companies = companyApplicationService.getAllCompanies();
        return ResponseEntity.ok(companies);
    }
    
    /**
     * 根据ID获取公司 (向后兼容)
     */
    @GetMapping("/{id}")
    public ResponseEntity<CompanyDTO> getCompanyById(@PathVariable Integer id) {
        try {
            CompanyDTO company = companyApplicationService.getCompanyById(id);
            return ResponseEntity.ok(company);
            
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 获取活跃公司
     */
    @GetMapping("/active")
    public ResponseEntity<List<CompanyDTO>> getActiveCompanies() {
        List<CompanyDTO> companies = companyApplicationService.getActiveCompanies();
        return ResponseEntity.ok(companies);
    }
    
    /**
     * 搜索公司
     */
    @GetMapping("/search")
    public ResponseEntity<List<CompanyDTO>> searchCompanies(@RequestParam String name) {
        List<CompanyDTO> companies = companyApplicationService.searchCompaniesByName(name);
        return ResponseEntity.ok(companies);
    }
    
    /**
     * 获取公司统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<CompanyStatsDTO> getCompanyStatistics() {
        CompanyStatsDTO stats = companyApplicationService.getCompanyStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 获取订阅即将过期的公司
     */
    @GetMapping("/expiring-subscriptions")
    public ResponseEntity<List<CompanyDTO>> getExpiringSubscriptions(
            @RequestParam(defaultValue = "30") Integer warningDays) {
        List<CompanyDTO> companies = companyApplicationService.getCompaniesWithExpiringSubscriptions(warningDays);
        return ResponseEntity.ok(companies);
    }
    
    /**
     * 检查公司是否可以添加用户
     */
    @GetMapping("/{id}/can-add-user")
    public ResponseEntity<Map<String, Object>> canAddUser(@PathVariable Integer id,
                                                         @RequestParam Integer currentUserCount) {
        try {
            boolean canAdd = companyApplicationService.canAddUser(id, currentUserCount);
            
            Map<String, Object> response = Map.of(
                "canAdd", canAdd,
                "companyId", id,
                "currentUserCount", currentUserCount
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }
    
    // ========== Delete Operations ==========
    
    /**
     * 删除公司 (软删除, 向后兼容)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Integer id) {
        try {
            companyApplicationService.deleteCompany(id);
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // ========== Helper Methods ==========
    
    private CreateCompanyCommand mapToCreateCommand(Map<String, Object> request) {
        return CreateCompanyCommand.builder()
                .companyName(getRequiredString(request, "companyName"))
                .address(getString(request, "address"))
                .city(getString(request, "city"))
                .stateProvince(getString(request, "stateProvince"))
                .postalCode(getString(request, "postalCode"))
                .email(getRequiredString(request, "email"))
                .website(getString(request, "website"))
                .registrationNumber(getString(request, "registrationNumber"))
                .taxId(getString(request, "taxId"))
                .fiscalYearStart(getString(request, "fiscalYearStart", "01-01"))
                .defaultCurrency(getString(request, "defaultCurrency", "CNY"))
                .maxUsers(getInteger(request, "maxUsers", 100))
                .createdBy(getInteger(request, "createdBy", 1))
                .build();
    }
    
    private UpdateCompanyCommand mapToUpdateCommand(Map<String, Object> request) {
        return UpdateCompanyCommand.builder()
                .companyName(getString(request, "companyName"))
                .address(getString(request, "address"))
                .city(getString(request, "city"))
                .stateProvince(getString(request, "stateProvince"))
                .postalCode(getString(request, "postalCode"))
                .website(getString(request, "website"))
                .registrationNumber(getString(request, "registrationNumber"))
                .taxId(getString(request, "taxId"))
                .fiscalYearStart(getString(request, "fiscalYearStart"))
                .defaultCurrency(getString(request, "defaultCurrency"))
                .maxUsers(getInteger(request, "maxUsers"))
                .build();
    }
    
    // 工具方法：安全地从Map中获取值
    private String getRequiredString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null || value.toString().trim().isEmpty()) {
            throw new IllegalArgumentException("Required field '" + key + "' is missing or empty");
        }
        return value.toString().trim();
    }
    
    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString().trim() : null;
    }
    
    private String getString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString().trim() : defaultValue;
    }
    
    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        
        if (value instanceof Integer) {
            return (Integer) value;
        }
        
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private Integer getInteger(Map<String, Object> map, String key, Integer defaultValue) {
        Integer value = getInteger(map, key);
        return value != null ? value : defaultValue;
    }
}