// src/main/java/org/example/backend/repository/DepartmentRepository.java
package org.example.backend.repository;

import org.example.backend.model.Company;
import org.example.backend.model.Department;
import org.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    List<Department> findByCompany(Company company);
    List<Department> findByParentDepartment(Department parentDepartment);
    List<Department> findByManager(User manager);
}