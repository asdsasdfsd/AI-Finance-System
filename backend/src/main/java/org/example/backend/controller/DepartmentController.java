// src/main/java/org/example/backend/controller/DepartmentController.java
package org.example.backend.controller;

import org.example.backend.dto.DepartmentDTO;
import org.example.backend.model.Company;
import org.example.backend.model.Department;
import org.example.backend.model.User;
import org.example.backend.service.CompanyService;
import org.example.backend.service.DepartmentService;
import org.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private CompanyService companyService;
    
    @Autowired
    private UserService userService;

    @GetMapping
    public List<DepartmentDTO> getAll() {
        List<Department> departments = departmentService.findAll();
        return departments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDTO> getById(@PathVariable Integer id) {
        Department department = departmentService.findById(id);
        return department != null ? 
            ResponseEntity.ok(convertToDTO(department)) : 
            ResponseEntity.notFound().build();
    }
    
    @GetMapping("/company/{companyId}")
    public List<DepartmentDTO> getByCompany(@PathVariable Integer companyId) {
        Company company = companyService.findById(companyId);
        List<Department> departments = departmentService.findByCompany(company);
        return departments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/{id}/subdepartments")
    public List<DepartmentDTO> getSubdepartments(@PathVariable Integer id) {
        Department parent = departmentService.findById(id);
        List<Department> departments = departmentService.findByParent(parent);
        return departments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/manager/{managerId}")
    public List<DepartmentDTO> getByManager(@PathVariable Integer managerId) {
        User manager = userService.findById(managerId);
        List<Department> departments = departmentService.findByManager(manager);
        return departments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public DepartmentDTO create(@RequestBody Map<String, Object> departmentData) {
        Department department = mapRequestDataToDepartment(departmentData, null);
        Department savedDepartment = departmentService.save(department);
        return convertToDTO(savedDepartment);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DepartmentDTO> update(@PathVariable Integer id, @RequestBody Map<String, Object> departmentData) {
        Department existingDepartment = departmentService.findById(id);
        if (existingDepartment == null) {
            return ResponseEntity.notFound().build();
        }
        
        Department department = mapRequestDataToDepartment(departmentData, existingDepartment);
        department.setDepartmentId(id);
        department.setCreatedAt(existingDepartment.getCreatedAt());
        department.setUpdatedAt(LocalDateTime.now());
        
        Department updatedDepartment = departmentService.save(department);
        return ResponseEntity.ok(convertToDTO(updatedDepartment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (departmentService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        departmentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 将Department实体转换为DTO，避免循环引用
     */
    private DepartmentDTO convertToDTO(Department department) {
        DepartmentDTO.DepartmentDTOBuilder builder = DepartmentDTO.builder()
                .departmentId(department.getDepartmentId())
                .name(department.getName())
                .code(department.getCode())
                .budget(department.getBudget())
                .isActive(department.getIsActive())
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt());
        
        // 安全地处理关联对象
        if (department.getCompany() != null) {
            builder.company(DepartmentDTO.CompanyInfo.builder()
                    .companyId(department.getCompany().getCompanyId())
                    .companyName(department.getCompany().getCompanyName())
                    .build());
        }
        
        if (department.getParentDepartment() != null) {
            builder.parentDepartment(DepartmentDTO.DepartmentInfo.builder()
                    .departmentId(department.getParentDepartment().getDepartmentId())
                    .name(department.getParentDepartment().getName())
                    .code(department.getParentDepartment().getCode())
                    .build());
        }
        
        if (department.getManager() != null) {
            builder.manager(DepartmentDTO.ManagerInfo.builder()
                    .userId(department.getManager().getUserId())
                    .fullName(department.getManager().getFullName())
                    .username(department.getManager().getUsername())
                    .build());
        }
        
        return builder.build();
    }
    
    /**
     * 将前端请求数据转换为Department对象
     */
    private Department mapRequestDataToDepartment(Map<String, Object> data, Department existingDepartment) {
        Department department = new Department();
        
        // 基本字段
        department.setName((String) data.get("name"));
        department.setCode((String) data.get("code"));
        
        // 处理预算
        if (data.containsKey("budget") && data.get("budget") != null) {
            Object budgetObj = data.get("budget");
            if (budgetObj instanceof Number) {
                department.setBudget(new java.math.BigDecimal(budgetObj.toString()));
            } else if (budgetObj instanceof String) {
                try {
                    department.setBudget(new java.math.BigDecimal((String) budgetObj));
                } catch (NumberFormatException e) {
                    department.setBudget(java.math.BigDecimal.ZERO);
                }
            }
        } else {
            department.setBudget(java.math.BigDecimal.ZERO);
        }
        
        // 处理状态
        if (data.containsKey("isActive") && data.get("isActive") != null) {
            department.setIsActive((Boolean) data.get("isActive"));
        } else {
            department.setIsActive(true);
        }
        
        // 处理公司关联
        if (data.containsKey("companyId") && data.get("companyId") != null) {
            Integer companyId = (Integer) data.get("companyId");
            Company company = companyService.findById(companyId);
            department.setCompany(company);
        }
        
        // 处理经理关联
        if (data.containsKey("managerId") && data.get("managerId") != null) {
            Integer managerId = (Integer) data.get("managerId");
            User manager = userService.findById(managerId);
            department.setManager(manager);
        }
        
        // 处理父部门关联
        if (data.containsKey("parentDepartmentId") && data.get("parentDepartmentId") != null) {
            Integer parentId = (Integer) data.get("parentDepartmentId");
            Department parentDepartment = departmentService.findById(parentId);
            department.setParentDepartment(parentDepartment);
        }
        
        return department;
    }
}