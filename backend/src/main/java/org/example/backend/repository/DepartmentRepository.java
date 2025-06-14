// backend/src/main/java/org/example/backend/repository/DepartmentRepository.java
package org.example.backend.repository;

import org.example.backend.model.Company;
import org.example.backend.model.Department;
import org.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    
    // ========== Existing Methods ==========
    
    List<Department> findByCompany(Company company);
    List<Department> findByParentDepartment(Department parentDepartment);
    List<Department> findByManager(User manager);
    
    /**
     * Find departments by company ID
     */
    List<Department> findByCompanyCompanyId(Integer companyId);

    // ========== FIXED: Added Missing Methods ==========
    
    /**
     * Find departments by ID set and company ID
     */
    @Query("SELECT d FROM Department d WHERE d.company.companyId = :companyId AND d.departmentId IN :departmentIds")
    List<Department> findByIdInAndCompanyId(@Param("departmentIds") Set<Integer> departmentIds, 
                                           @Param("companyId") Integer companyId);
    
    /**
     * Alternative method using company ID and department IDs
     */
    @Query("SELECT d FROM Department d WHERE d.company.companyId = :companyId AND d.departmentId IN :ids")
    List<Department> findByCompanyIdAndDepartmentIdIn(@Param("companyId") Integer companyId, 
                                                     @Param("ids") Set<Integer> ids);
    
    /**
     * Find active departments by company ID
     */
    @Query("SELECT d FROM Department d WHERE d.company.companyId = :companyId AND d.isActive = true")
    List<Department> findActiveByCompanyId(@Param("companyId") Integer companyId);
    
    /**
     * Find root departments (no parent) by company
     */
    @Query("SELECT d FROM Department d WHERE d.company.companyId = :companyId AND d.parentDepartment IS NULL")
    List<Department> findRootDepartmentsByCompanyId(@Param("companyId") Integer companyId);
    
    /**
     * Find departments by manager ID
     */
    @Query("SELECT d FROM Department d WHERE d.manager.userId = :managerId")
    List<Department> findByManagerId(@Param("managerId") Integer managerId);
    
    /**
     * Count departments by company
     */
    @Query("SELECT COUNT(d) FROM Department d WHERE d.company.companyId = :companyId")
    long countByCompanyId(@Param("companyId") Integer companyId);
    
    /**
     * Check if department exists in company
     */
    @Query("SELECT COUNT(d) > 0 FROM Department d WHERE d.company.companyId = :companyId AND d.departmentId = :departmentId")
    boolean existsByCompanyIdAndDepartmentId(@Param("companyId") Integer companyId, 
                                           @Param("departmentId") Integer departmentId);
    
    /**
     * FIXED: Find departments with budget limits - corrected field name from budgetLimit to budget
     */
    @Query("SELECT d FROM Department d WHERE d.company.companyId = :companyId AND d.budget IS NOT NULL AND d.budget > 0")
    List<Department> findDepartmentsWithBudgetByCompanyId(@Param("companyId") Integer companyId);
}