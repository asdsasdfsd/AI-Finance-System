// backend/src/main/java/org/example/backend/dto/DepartmentDTO.java
package org.example.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class DepartmentDTO {
    private Integer departmentId;
    private String name;
    private String code;
    private BigDecimal budget;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 简化的关联对象信息，避免循环引用
    private CompanyInfo company;
    private DepartmentInfo parentDepartment;
    private ManagerInfo manager;
    
    @Data
    @Builder
    public static class CompanyInfo {
        private Integer companyId;
        private String companyName;
    }
    
    @Data
    @Builder
    public static class DepartmentInfo {
        private Integer departmentId;
        private String name;
        private String code;
    }
    
    @Data
    @Builder
    public static class ManagerInfo {
        private Integer userId;
        private String fullName;
        private String username;
    }
}