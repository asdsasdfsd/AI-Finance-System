// src/main/java/org/example/backend/controller/DepartmentController.java
package org.example.backend.controller;

import org.example.backend.model.Company;
import org.example.backend.model.Department;
import org.example.backend.model.User;
import org.example.backend.service.CompanyService;
import org.example.backend.service.DepartmentService;
import org.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<Department> getAll() {
        return departmentService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Department> getById(@PathVariable Integer id) {
        Department department = departmentService.findById(id);
        return department != null ? ResponseEntity.ok(department) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/company/{companyId}")
    public List<Department> getByCompany(@PathVariable Integer companyId) {
        Company company = companyService.findById(companyId);
        return departmentService.findByCompany(company);
    }
    
    @GetMapping("/{id}/subdepartments")
    public List<Department> getSubdepartments(@PathVariable Integer id) {
        Department parent = departmentService.findById(id);
        return departmentService.findByParent(parent);
    }
    
    @GetMapping("/manager/{managerId}")
    public List<Department> getByManager(@PathVariable Integer managerId) {
        User manager = userService.findById(managerId);
        return departmentService.findByManager(manager);
    }

    @PostMapping
    public Department create(@RequestBody Department department) {
        return departmentService.save(department);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Department> update(@PathVariable Integer id, @RequestBody Department department) {
        if (departmentService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        department.setDepartmentId(id);
        return ResponseEntity.ok(departmentService.save(department));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (departmentService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        departmentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}