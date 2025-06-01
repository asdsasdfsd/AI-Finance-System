// backend/src/main/java/org/example/backend/infrastructure/web/DDDTestController.java
package org.example.backend.infrastructure.web;

import org.example.backend.application.service.CompanyApplicationService;
import org.example.backend.application.service.UserApplicationService;
import org.example.backend.application.service.TransactionApplicationService;
import org.example.backend.application.dto.CreateCompanyCommand;
import org.example.backend.application.dto.CreateUserCommand;
import org.example.backend.application.dto.CreateTransactionCommand;
import org.example.backend.application.dto.CompanyDTO;
import org.example.backend.application.dto.UserDTO;
import org.example.backend.application.dto.TransactionDTO;
import org.example.backend.domain.event.DomainEventPublisher;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * DDD Test Controller
 * 
 * 用于测试DDD模式是否正常工作的控制器
 * 提供端到端测试功能
 */
@RestController
@RequestMapping("/api/ddd-test")
@CrossOrigin(origins = "http://localhost:3000")
@Profile("ddd")
public class DDDTestController {
    
    private final CompanyApplicationService companyApplicationService;
    private final UserApplicationService userApplicationService;
    private final TransactionApplicationService transactionApplicationService;
    private final DomainEventPublisher eventPublisher;
    
    public DDDTestController(CompanyApplicationService companyApplicationService,
                           UserApplicationService userApplicationService,
                           TransactionApplicationService transactionApplicationService,
                           DomainEventPublisher eventPublisher) {
        this.companyApplicationService = companyApplicationService;
        this.userApplicationService = userApplicationService;
        this.transactionApplicationService = transactionApplicationService;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * 基础健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("status", "healthy");
            response.put("mode", "DDD");
            response.put("timestamp", LocalDateTime.now());
            response.put("message", "DDD模式正常运行");
            response.put("services", Map.of(
                "CompanyApplicationService", companyApplicationService != null,
                "UserApplicationService", userApplicationService != null,
                "TransactionApplicationService", transactionApplicationService != null,
                "DomainEventPublisher", eventPublisher != null
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "DDD模式异常: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 创建测试数据
     */
    @PostMapping("/create-test-data")
    public ResponseEntity<Map<String, Object>> createTestData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. 创建测试公司
            CreateCompanyCommand companyCommand = CreateCompanyCommand.builder()
                    .companyName("DDD测试公司")
                    .email("test@dddcompany.com")
                    .address("北京市朝阳区")
                    .city("北京")
                    .stateProvince("北京")
                    .postalCode("100000")
                    .website("https://dddcompany.com")
                    .fiscalYearStart("01-01")
                    .defaultCurrency("CNY")
                    .maxUsers(50)
                    .createdBy(1)
                    .build();
            
            CompanyDTO company = companyApplicationService.createCompany(companyCommand);
            
            // 2. 创建测试用户
            CreateUserCommand userCommand = CreateUserCommand.builder()
                    .username("ddd_test_user")
                    .email("test.user@dddcompany.com")
                    .password("Test123456!")
                    .fullName("DDD测试用户")
                    .enabled(true)
                    .companyId(company.getCompanyId())
                    .preferredLanguage("zh-CN")
                    .timezone("Asia/Shanghai")
                    .roleNames(Set.of("USER", "FINANCE_OPERATOR"))
                    .build();
            
            UserDTO user = userApplicationService.createUser(userCommand);
            
            // 3. 创建测试收入交易
            CreateTransactionCommand incomeCommand = CreateTransactionCommand.builder()
                    .amount(new BigDecimal("5000.00"))
                    .currency("CNY")
                    .transactionDate(LocalDate.now())
                    .description("DDD测试收入")
                    .paymentMethod("银行转账")
                    .referenceNumber("DDD-INC-001")
                    .isRecurring(false)
                    .isTaxable(true)
                    .companyId(company.getCompanyId())
                    .userId(user.getUserId())
                    .build();
            
            TransactionDTO incomeTransaction = transactionApplicationService.createIncomeTransaction(incomeCommand);
            
            // 4. 创建测试支出交易
            CreateTransactionCommand expenseCommand = CreateTransactionCommand.builder()
                    .amount(new BigDecimal("1500.00"))
                    .currency("CNY")
                    .transactionDate(LocalDate.now())
                    .description("DDD测试支出")
                    .paymentMethod("现金")
                    .referenceNumber("DDD-EXP-001")
                    .isRecurring(false)
                    .isTaxable(false)
                    .companyId(company.getCompanyId())
                    .userId(user.getUserId())
                    .build();
            
            TransactionDTO expenseTransaction = transactionApplicationService.createExpenseTransaction(expenseCommand);
            
            response.put("status", "success");
            response.put("message", "测试数据创建成功");
            response.put("data", Map.of(
                "company", Map.of(
                    "id", company.getCompanyId(),
                    "name", company.getCompanyName(),
                    "email", company.getEmail(),
                    "tenantId", company.getTenantId(),
                    "isActive", company.isActive()
                ),
                "user", Map.of(
                    "id", user.getUserId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "fullName", user.getFullName(),
                    "tenantId", user.getTenantId(),
                    "roles", user.getRoleNames(),
                    "isActive", user.isActiveAndUnlocked()
                ),
                "transactions", Map.of(
                    "income", Map.of(
                        "id", incomeTransaction.getTransactionId(),
                        "amount", incomeTransaction.getDisplayAmount(),
                        "type", incomeTransaction.getTransactionType(),
                        "status", incomeTransaction.getStatus(),
                        "canModify", incomeTransaction.isCanModify()
                    ),
                    "expense", Map.of(
                        "id", expenseTransaction.getTransactionId(),
                        "amount", expenseTransaction.getDisplayAmount(),
                        "type", expenseTransaction.getTransactionType(),
                        "status", expenseTransaction.getStatus(),
                        "canModify", expenseTransaction.isCanModify()
                    )
                )
            ));
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "创建测试数据失败: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            response.put("timestamp", LocalDateTime.now());
            
            e.printStackTrace(); // 打印详细错误信息用于调试
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 测试聚合根业务操作
     */
    @PostMapping("/test-business-operations")
    public ResponseEntity<Map<String, Object>> testBusinessOperations() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取所有公司和用户
            var companies = companyApplicationService.getAllCompanies();
            var allUsers = companies.isEmpty() ? null : 
                userApplicationService.getUsersByCompany(companies.get(0).getCompanyId());
            
            if (companies.isEmpty() || allUsers == null || allUsers.isEmpty()) {
                response.put("status", "warning");
                response.put("message", "请先创建测试数据");
                return ResponseEntity.ok(response);
            }
            
            CompanyDTO company = companies.get(0);
            UserDTO user = allUsers.get(0);
            
            // 测试公司操作
            // 1. 激活/暂停公司
            CompanyDTO suspendedCompany = companyApplicationService.suspendCompany(company.getCompanyId(), "测试暂停");
            CompanyDTO reactivatedCompany = companyApplicationService.activateCompany(company.getCompanyId());
            
            // 2. 测试用户操作
            UserDTO disabledUser = userApplicationService.disableUser(user.getUserId());
            UserDTO enabledUser = userApplicationService.enableUser(user.getUserId());
            
            // 3. 分配新角色
            UserDTO userWithNewRole = userApplicationService.assignRole(user.getUserId(), "FINANCE_MANAGER");
            
            // 4. 测试交易查询
            var transactions = transactionApplicationService.getTransactionsByCompany(company.getCompanyId());
            var pendingTransactions = transactionApplicationService.getPendingApprovalTransactions(company.getCompanyId());
            
            response.put("status", "success");
            response.put("message", "业务操作测试完成");
            response.put("results", Map.of(
                "companyOperations", Map.of(
                    "suspendTest", !suspendedCompany.isActive(),
                    "activateTest", reactivatedCompany.isActive()
                ),
                "userOperations", Map.of(
                    "disableTest", !disabledUser.isActiveAndUnlocked(),
                    "enableTest", enabledUser.isActiveAndUnlocked(),
                    "roleAssignmentTest", userWithNewRole.getRoleNames().contains("FINANCE_MANAGER")
                ),
                "transactionQueries", Map.of(
                    "totalTransactions", transactions.size(),
                    "pendingTransactions", pendingTransactions.size()
                )
            ));
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "业务操作测试失败: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            response.put("timestamp", LocalDateTime.now());
            
            e.printStackTrace();
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 测试领域事件发布
     */
    @PostMapping("/test-domain-events")
    public ResponseEntity<Map<String, Object>> testDomainEvents() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 创建一个新公司触发领域事件
            CreateCompanyCommand command = CreateCompanyCommand.builder()
                    .companyName("事件测试公司 " + System.currentTimeMillis())
                    .email("event.test." + System.currentTimeMillis() + "@example.com")
                    .address("事件测试地址")
                    .city("测试城市")
                    .createdBy(1)
                    .build();
            
            CompanyDTO company = companyApplicationService.createCompany(command);
            
            // 手动发布一个测试事件
            eventPublisher.publish(new TestDomainEvent("事件发布测试", company.getCompanyId()));
            
            response.put("status", "success");
            response.put("message", "领域事件测试完成");
            response.put("eventInfo", Map.of(
                "companyCreated", company.getCompanyId(),
                "customEventPublished", true
            ));
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "领域事件测试失败: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 清理测试数据
     */
    @DeleteMapping("/cleanup-test-data")
    public ResponseEntity<Map<String, Object>> cleanupTestData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            var companies = companyApplicationService.getAllCompanies();
            int deletedCompanies = 0;
            
            for (CompanyDTO company : companies) {
                if (company.getCompanyName().contains("测试") || 
                    company.getCompanyName().contains("DDD") ||
                    company.getEmail().contains("test")) {
                    
                    companyApplicationService.deleteCompany(company.getCompanyId());
                    deletedCompanies++;
                }
            }
            
            response.put("status", "success");
            response.put("message", "测试数据清理完成");
            response.put("deletedCompanies", deletedCompanies);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "清理测试数据失败: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取DDD模式统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            var companyStats = companyApplicationService.getCompanyStatistics();
            var companies = companyApplicationService.getAllCompanies();
            
            int totalUsers = 0;
            int totalTransactions = 0;
            
            for (CompanyDTO company : companies) {
                var users = userApplicationService.getUsersByCompany(company.getCompanyId());
                var transactions = transactionApplicationService.getTransactionsByCompany(company.getCompanyId());
                
                totalUsers += users.size();
                totalTransactions += transactions.size();
            }
            
            response.put("status", "success");
            response.put("statistics", Map.of(
                "companies", Map.of(
                    "total", companyStats.getTotalCompanies(),
                    "active", companyStats.getActiveCompanies(),
                    "suspended", companyStats.getSuspendedCompanies(),
                    "deleted", companyStats.getDeletedCompanies(),
                    "activePercentage", String.format("%.2f%%", companyStats.getActivePercentage())
                ),
                "users", Map.of(
                    "total", totalUsers
                ),
                "transactions", Map.of(
                    "total", totalTransactions
                )
            ));
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "获取统计信息失败: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 测试领域事件类
     */
    public static class TestDomainEvent {
        private final String eventType = "TestDomainEvent";
        private final String message;
        private final Integer companyId;
        private final LocalDateTime occurredOn;
        
        public TestDomainEvent(String message, Integer companyId) {
            this.message = message;
            this.companyId = companyId;
            this.occurredOn = LocalDateTime.now();
        }
        
        public String getEventType() { return eventType; }
        public String getMessage() { return message; }
        public Integer getCompanyId() { return companyId; }
        public LocalDateTime getOccurredOn() { return occurredOn; }
        
        @Override
        public String toString() {
            return String.format("TestDomainEvent{message='%s', companyId=%d, occurredOn=%s}", 
                               message, companyId, occurredOn);
        }
    }
}