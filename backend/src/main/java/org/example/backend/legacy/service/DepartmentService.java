// src/main/java/org/example/backend/service/DepartmentService.java
package org.example.backend.service;

import org.example.backend.model.Company;
import org.example.backend.model.Department;
import org.example.backend.model.User;
import org.example.backend.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DepartmentService {
    @Autowired
    private DepartmentRepository departmentRepository;

    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    public Department findById(Integer id) {
        return departmentRepository.findById(id).orElse(null);
    }
    
    public List<Department> findByCompany(Company company) {
        return departmentRepository.findByCompany(company);
    }
    
    public List<Department> findByParent(Department parent) {
        return departmentRepository.findByParentDepartment(parent);
    }
    
    public List<Department> findByManager(User manager) {
        return departmentRepository.findByManager(manager);
    }

    public Department save(Department department) {
        if (department.getCreatedAt() == null) {
            department.setCreatedAt(LocalDateTime.now());
        }
        department.setUpdatedAt(LocalDateTime.now());
        return departmentRepository.save(department);
    }

    public void deleteById(Integer id) {
        departmentRepository.deleteById(id);
    }
}