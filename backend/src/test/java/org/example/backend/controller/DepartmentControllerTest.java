// backend/src/test/java/org/example/backend/controller/DepartmentControllerTest.java
package org.example.backend.controller;

import org.example.backend.dto.DepartmentDTO;
import org.example.backend.model.Company;
import org.example.backend.model.Department;
import org.example.backend.model.User;
import org.example.backend.service.CompanyService;
import org.example.backend.service.DepartmentService;
import org.example.backend.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DepartmentController - Testing Real Controller with Mocked Dependencies
 * 
 * This test class creates a REAL instance of DepartmentController and mocks its dependencies,
 * following the proper unit testing approach for testing HTTP layer business logic.
 * 
 * Coverage Target: From 0% to 80%+ for all Controller methods
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Department Controller Tests - Real Controller Implementation")
class DepartmentControllerTest {

    // Mock dependencies (not the controller itself!)
    @Mock
    private DepartmentService departmentService;
    
    @Mock
    private CompanyService companyService;
    
    @Mock
    private UserService userService;

    // Real controller instance under test
    private DepartmentController departmentController;

    // Test constants
    private static final Integer TEST_DEPARTMENT_ID = 1;
    private static final Integer TEST_COMPANY_ID = 100;
    private static final Integer TEST_USER_ID = 200;
    private static final Integer TEST_PARENT_ID = 50;
    private static final String TEST_DEPT_NAME = "IT Department";
    private static final String TEST_DEPT_CODE = "IT001";
    private static final BigDecimal TEST_BUDGET = new BigDecimal("50000.00");

    @BeforeEach
    void setUp() {
        // Create real controller instance with mocked dependencies
        // Since the fields are private, we need to use constructor injection or reflection
        // For this test, we'll use a different approach - creating the controller properly
        departmentController = new DepartmentController();
        
        // Use reflection to set private fields since @Autowired fields are not accessible
        try {
            java.lang.reflect.Field deptServiceField = DepartmentController.class.getDeclaredField("departmentService");
            deptServiceField.setAccessible(true);
            deptServiceField.set(departmentController, departmentService);
            
            java.lang.reflect.Field companyServiceField = DepartmentController.class.getDeclaredField("companyService");
            companyServiceField.setAccessible(true);
            companyServiceField.set(departmentController, companyService);
            
            java.lang.reflect.Field userServiceField = DepartmentController.class.getDeclaredField("userService");
            userServiceField.setAccessible(true);
            userServiceField.set(departmentController, userService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up test dependencies", e);
        }
    }

    // ========== HTTP GET Methods Tests ==========

    @Test
    @DisplayName("Should get all departments successfully via GET")
    void shouldGetAllDepartmentsSuccessfully() {
        // Given
        List<Department> expectedDepartments = Arrays.asList(
            createMockDepartment(),
            createMockDepartment("HR Department", "HR001")
        );
        
        when(departmentService.findAll()).thenReturn(expectedDepartments);
        
        // When
        List<DepartmentDTO> response = departmentController.getAll();
        
        // Then
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(TEST_DEPT_NAME, response.get(0).getName());
        assertEquals(TEST_DEPT_CODE, response.get(0).getCode());
        
        verify(departmentService).findAll();
    }

    @Test
    @DisplayName("Should get department by ID successfully via GET")
    void shouldGetDepartmentByIdSuccessfully() {
        // Given
        Department expectedDepartment = createMockDepartment();
        when(departmentService.findById(TEST_DEPARTMENT_ID)).thenReturn(expectedDepartment);
        
        // When
        ResponseEntity<DepartmentDTO> response = departmentController.getById(TEST_DEPARTMENT_ID);
        
        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(TEST_DEPARTMENT_ID, response.getBody().getDepartmentId());
        assertEquals(TEST_DEPT_NAME, response.getBody().getName());
        
        verify(departmentService).findById(TEST_DEPARTMENT_ID);
    }

    @Test
    @DisplayName("Should return 404 when department not found via GET")
    void shouldReturn404WhenDepartmentNotFound() {
        // Given
        when(departmentService.findById(TEST_DEPARTMENT_ID)).thenReturn(null);
        
        // When
        ResponseEntity<DepartmentDTO> response = departmentController.getById(TEST_DEPARTMENT_ID);
        
        // Then
        assertNotNull(response);
        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
        
        verify(departmentService).findById(TEST_DEPARTMENT_ID);
    }

    @Test
    @DisplayName("Should get departments by company successfully via GET")
    void shouldGetDepartmentsByCompanySuccessfully() {
        // Given
        Company mockCompany = createMockCompany();
        List<Department> expectedDepartments = Arrays.asList(createMockDepartment());
        
        when(companyService.findById(TEST_COMPANY_ID)).thenReturn(mockCompany);
        when(departmentService.findByCompany(mockCompany)).thenReturn(expectedDepartments);
        
        // When
        List<DepartmentDTO> response = departmentController.getByCompany(TEST_COMPANY_ID);
        
        // Then
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(TEST_COMPANY_ID, response.get(0).getCompany().getCompanyId());
        
        verify(companyService).findById(TEST_COMPANY_ID);
        verify(departmentService).findByCompany(mockCompany);
    }

    @Test
    @DisplayName("Should get subdepartments successfully via GET")
    void shouldGetSubdepartmentsSuccessfully() {
        // Given
        Department parentDepartment = createMockDepartment();
        List<Department> subdepartments = Arrays.asList(
            createMockDepartmentWithParent("Sub Department 1", "SUB001", parentDepartment)
        );
        
        when(departmentService.findById(TEST_DEPARTMENT_ID)).thenReturn(parentDepartment);
        when(departmentService.findByParent(parentDepartment)).thenReturn(subdepartments);
        
        // When
        List<DepartmentDTO> response = departmentController.getSubdepartments(TEST_DEPARTMENT_ID);
        
        // Then
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Sub Department 1", response.get(0).getName());
        assertNotNull(response.get(0).getParentDepartment());
        
        verify(departmentService).findById(TEST_DEPARTMENT_ID);
        verify(departmentService).findByParent(parentDepartment);
    }

    @Test
    @DisplayName("Should get departments by manager successfully via GET")
    void shouldGetDepartmentsByManagerSuccessfully() {
        // Given
        User mockManager = createMockUser();
        List<Department> expectedDepartments = Arrays.asList(createMockDepartment());
        
        when(userService.findById(TEST_USER_ID)).thenReturn(mockManager);
        when(departmentService.findByManager(mockManager)).thenReturn(expectedDepartments);
        
        // When
        List<DepartmentDTO> response = departmentController.getByManager(TEST_USER_ID);
        
        // Then
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(TEST_USER_ID, response.get(0).getManager().getUserId());
        
        verify(userService).findById(TEST_USER_ID);
        verify(departmentService).findByManager(mockManager);
    }

    // ========== HTTP POST Methods Tests ==========

    @Nested
    @DisplayName("Create Department Operations")
    class CreateDepartmentOperations {

        @Test
        @DisplayName("Should create department successfully via POST")
        void shouldCreateDepartmentSuccessfully() {
            // Given
            Map<String, Object> request = createValidDepartmentRequest();
            Department savedDepartment = createMockDepartment();
            
            when(companyService.findById(TEST_COMPANY_ID)).thenReturn(createMockCompany());
            when(userService.findById(TEST_USER_ID)).thenReturn(createMockUser());
            when(departmentService.save(any(Department.class))).thenReturn(savedDepartment);
            
            // When
            DepartmentDTO response = departmentController.create(request);
            
            // Then
            assertNotNull(response);
            assertEquals(TEST_DEPT_NAME, response.getName());
            assertEquals(TEST_DEPT_CODE, response.getCode());
            assertEquals(TEST_BUDGET, response.getBudget());
            
            verify(departmentService).save(any(Department.class));
        }

        @Test
        @DisplayName("Should map request to Department correctly during creation")
        void shouldMapRequestToDepartmentCorrectlyDuringCreation() {
            // Given
            Map<String, Object> request = createValidDepartmentRequest();
            Department savedDepartment = createMockDepartment();
            
            ArgumentCaptor<Department> departmentCaptor = ArgumentCaptor.forClass(Department.class);
            
            when(companyService.findById(TEST_COMPANY_ID)).thenReturn(createMockCompany());
            when(userService.findById(TEST_USER_ID)).thenReturn(createMockUser());
            when(departmentService.save(departmentCaptor.capture())).thenReturn(savedDepartment);
            
            // When
            departmentController.create(request);
            
            // Then - Verify mapping is correct
            Department capturedDepartment = departmentCaptor.getValue();
            assertEquals(TEST_DEPT_NAME, capturedDepartment.getName());
            assertEquals(TEST_DEPT_CODE, capturedDepartment.getCode());
            assertEquals(TEST_BUDGET, capturedDepartment.getBudget());
            assertEquals(true, capturedDepartment.getIsActive());
            assertNotNull(capturedDepartment.getCompany());
            assertNotNull(capturedDepartment.getManager());
        }

        @Test
        @DisplayName("Should handle budget as String during creation")
        void shouldHandleBudgetAsStringDuringCreation() {
            // Given
            Map<String, Object> request = createValidDepartmentRequest();
            request.put("budget", "75000.50"); // String instead of Number
            
            Department savedDepartment = createMockDepartment();
            ArgumentCaptor<Department> departmentCaptor = ArgumentCaptor.forClass(Department.class);
            
            when(companyService.findById(TEST_COMPANY_ID)).thenReturn(createMockCompany());
            when(departmentService.save(departmentCaptor.capture())).thenReturn(savedDepartment);
            
            // When
            departmentController.create(request);
            
            // Then
            Department capturedDepartment = departmentCaptor.getValue();
            assertEquals(new BigDecimal("75000.50"), capturedDepartment.getBudget());
        }

        @Test
        @DisplayName("Should handle invalid budget string during creation")
        void shouldHandleInvalidBudgetStringDuringCreation() {
            // Given
            Map<String, Object> request = createValidDepartmentRequest();
            request.put("budget", "invalid_budget"); // Invalid string
            
            Department savedDepartment = createMockDepartment();
            ArgumentCaptor<Department> departmentCaptor = ArgumentCaptor.forClass(Department.class);
            
            when(companyService.findById(TEST_COMPANY_ID)).thenReturn(createMockCompany());
            when(departmentService.save(departmentCaptor.capture())).thenReturn(savedDepartment);
            
            // When
            departmentController.create(request);
            
            // Then
            Department capturedDepartment = departmentCaptor.getValue();
            assertEquals(BigDecimal.ZERO, capturedDepartment.getBudget());
        }
    }

    // ========== HTTP PUT Methods Tests ==========

    @Nested
    @DisplayName("Update Department Operations")
    class UpdateDepartmentOperations {

        @Test
        @DisplayName("Should update department successfully via PUT")
        void shouldUpdateDepartmentSuccessfully() {
            // Given
            Department existingDepartment = createMockDepartment();
            Map<String, Object> request = createValidUpdateRequest();
            Department updatedDepartment = createMockDepartment();
            updatedDepartment.setName("Updated IT Department");
            
            when(departmentService.findById(TEST_DEPARTMENT_ID)).thenReturn(existingDepartment);
            when(companyService.findById(TEST_COMPANY_ID)).thenReturn(createMockCompany());
            when(departmentService.save(any(Department.class))).thenReturn(updatedDepartment);
            
            // When
            ResponseEntity<DepartmentDTO> response = departmentController.update(TEST_DEPARTMENT_ID, request);
            
            // Then
            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertEquals("Updated IT Department", response.getBody().getName());
            
            verify(departmentService).findById(TEST_DEPARTMENT_ID);
            verify(departmentService).save(any(Department.class));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent department")
        void shouldReturn404WhenUpdatingNonExistentDepartment() {
            // Given
            Map<String, Object> request = createValidUpdateRequest();
            when(departmentService.findById(TEST_DEPARTMENT_ID)).thenReturn(null);
            
            // When
            ResponseEntity<DepartmentDTO> response = departmentController.update(TEST_DEPARTMENT_ID, request);
            
            // Then
            assertEquals(404, response.getStatusCode().value());
            assertNull(response.getBody());
            
            verify(departmentService).findById(TEST_DEPARTMENT_ID);
            verify(departmentService, never()).save(any(Department.class));
        }

        @Test
        @DisplayName("Should preserve creation time during update")
        void shouldPreserveCreationTimeDuringUpdate() {
            // Given
            LocalDateTime originalCreatedAt = LocalDateTime.now().minusDays(30);
            Department existingDepartment = createMockDepartment();
            existingDepartment.setCreatedAt(originalCreatedAt);
            
            Map<String, Object> request = createValidUpdateRequest();
            
            ArgumentCaptor<Department> departmentCaptor = ArgumentCaptor.forClass(Department.class);
            
            when(departmentService.findById(TEST_DEPARTMENT_ID)).thenReturn(existingDepartment);
            when(companyService.findById(TEST_COMPANY_ID)).thenReturn(createMockCompany());
            when(departmentService.save(departmentCaptor.capture())).thenReturn(existingDepartment);
            
            // When
            departmentController.update(TEST_DEPARTMENT_ID, request);
            
            // Then
            Department capturedDepartment = departmentCaptor.getValue();
            assertEquals(TEST_DEPARTMENT_ID, capturedDepartment.getDepartmentId());
            assertEquals(originalCreatedAt, capturedDepartment.getCreatedAt());
            assertNotNull(capturedDepartment.getUpdatedAt());
        }
    }

    // ========== HTTP DELETE Methods Tests ==========

    @Test
    @DisplayName("Should delete department successfully via DELETE")
    void shouldDeleteDepartmentSuccessfully() {
        // Given
        Department existingDepartment = createMockDepartment();
        when(departmentService.findById(TEST_DEPARTMENT_ID)).thenReturn(existingDepartment);
        doNothing().when(departmentService).deleteById(TEST_DEPARTMENT_ID);
        
        // When
        ResponseEntity<Void> response = departmentController.delete(TEST_DEPARTMENT_ID);
        
        // Then
        assertNotNull(response);
        assertEquals(204, response.getStatusCode().value()); // No Content
        assertNull(response.getBody());
        
        verify(departmentService).findById(TEST_DEPARTMENT_ID);
        verify(departmentService).deleteById(TEST_DEPARTMENT_ID);
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent department")
    void shouldReturn404WhenDeletingNonExistentDepartment() {
        // Given
        when(departmentService.findById(TEST_DEPARTMENT_ID)).thenReturn(null);
        
        // When
        ResponseEntity<Void> response = departmentController.delete(TEST_DEPARTMENT_ID);
        
        // Then
        assertEquals(404, response.getStatusCode().value());
        
        verify(departmentService).findById(TEST_DEPARTMENT_ID);
        verify(departmentService, never()).deleteById(any());
    }

    // ========== Data Conversion Tests ==========

    @Nested
    @DisplayName("Data Conversion Operations")
    class DataConversionOperations {

        @Test
        @DisplayName("Should convert Department to DTO correctly with all relationships")
        void shouldConvertDepartmentToDtoCorrectlyWithAllRelationships() {
            // Given
            Department department = createMockDepartmentWithAllRelations();
            when(departmentService.findById(TEST_DEPARTMENT_ID)).thenReturn(department);
            
            // When
            ResponseEntity<DepartmentDTO> response = departmentController.getById(TEST_DEPARTMENT_ID);
            
            // Then
            DepartmentDTO dto = response.getBody();
            assertNotNull(dto);
            
            // Basic fields
            assertEquals(TEST_DEPARTMENT_ID, dto.getDepartmentId());
            assertEquals(TEST_DEPT_NAME, dto.getName());
            assertEquals(TEST_DEPT_CODE, dto.getCode());
            assertEquals(TEST_BUDGET, dto.getBudget());
            assertTrue(dto.getIsActive());
            
            // Company info
            assertNotNull(dto.getCompany());
            assertEquals(TEST_COMPANY_ID, dto.getCompany().getCompanyId());
            assertEquals("Test Company", dto.getCompany().getCompanyName());
            
            // Manager info
            assertNotNull(dto.getManager());
            assertEquals(TEST_USER_ID, dto.getManager().getUserId());
            assertEquals("John Doe", dto.getManager().getFullName());
            assertEquals("john.doe", dto.getManager().getUsername());
            
            // Parent department info
            assertNotNull(dto.getParentDepartment());
            assertEquals(TEST_PARENT_ID, dto.getParentDepartment().getDepartmentId());
        }

        @Test
        @DisplayName("Should handle Department with null relationships in DTO conversion")
        void shouldHandleDepartmentWithNullRelationshipsInDtoConversion() {
            // Given
            Department department = createMockDepartmentWithoutRelations();
            when(departmentService.findById(TEST_DEPARTMENT_ID)).thenReturn(department);
            
            // When
            ResponseEntity<DepartmentDTO> response = departmentController.getById(TEST_DEPARTMENT_ID);
            
            // Then
            DepartmentDTO dto = response.getBody();
            assertNotNull(dto);
            assertEquals(TEST_DEPARTMENT_ID, dto.getDepartmentId());
            assertEquals(TEST_DEPT_NAME, dto.getName());
            
            // All relationships should be null
            assertNull(dto.getCompany());
            assertNull(dto.getManager());
            assertNull(dto.getParentDepartment());
        }
    }

    // ========== Edge Cases and Error Scenarios ==========

    @Nested
    @DisplayName("Edge Cases and Error Scenarios")
    class EdgeCasesAndErrorScenarios {

        @Test
        @DisplayName("Should handle empty request data gracefully")
        void shouldHandleEmptyRequestDataGracefully() {
            // Given
            Map<String, Object> emptyRequest = new HashMap<>();
            Department savedDepartment = createMockDepartment();
            
            when(departmentService.save(any(Department.class))).thenReturn(savedDepartment);
            
            // When
            DepartmentDTO response = departmentController.create(emptyRequest);
            
            // Then
            assertNotNull(response);
            verify(departmentService).save(any(Department.class));
        }

        @Test
        @DisplayName("Should handle null values in request data")
        void shouldHandleNullValuesInRequestData() {
            // Given
            Map<String, Object> requestWithNulls = new HashMap<>();
            requestWithNulls.put("name", null);
            requestWithNulls.put("code", null);
            requestWithNulls.put("budget", null);
            requestWithNulls.put("isActive", null);
            requestWithNulls.put("companyId", null);
            requestWithNulls.put("managerId", null);
            
            Department savedDepartment = createMockDepartment();
            ArgumentCaptor<Department> departmentCaptor = ArgumentCaptor.forClass(Department.class);
            
            when(departmentService.save(departmentCaptor.capture())).thenReturn(savedDepartment);
            
            // When
            departmentController.create(requestWithNulls);
            
            // Then
            Department capturedDepartment = departmentCaptor.getValue();
            assertNull(capturedDepartment.getName());
            assertNull(capturedDepartment.getCode());
            assertEquals(BigDecimal.ZERO, capturedDepartment.getBudget());
            assertTrue(capturedDepartment.getIsActive()); // Default to true
            assertNull(capturedDepartment.getCompany());
            assertNull(capturedDepartment.getManager());
        }

        @Test
        @DisplayName("Should handle budget as Number type correctly")
        void shouldHandleBudgetAsNumberTypeCorrectly() {
            // Given
            Map<String, Object> request = createValidDepartmentRequest();
            request.put("budget", 85000); // Integer number
            
            Department savedDepartment = createMockDepartment();
            ArgumentCaptor<Department> departmentCaptor = ArgumentCaptor.forClass(Department.class);
            
            when(companyService.findById(TEST_COMPANY_ID)).thenReturn(createMockCompany());
            when(departmentService.save(departmentCaptor.capture())).thenReturn(savedDepartment);
            
            // When
            departmentController.create(request);
            
            // Then
            Department capturedDepartment = departmentCaptor.getValue();
            assertEquals(new BigDecimal("85000"), capturedDepartment.getBudget());
        }

        @Test
        @DisplayName("Should handle department with parent department correctly")
        void shouldHandleDepartmentWithParentDepartmentCorrectly() {
            // Given
            Map<String, Object> request = createValidDepartmentRequest();
            request.put("parentDepartmentId", TEST_PARENT_ID);
            
            Department parentDepartment = createMockDepartment();
            parentDepartment.setDepartmentId(TEST_PARENT_ID);
            parentDepartment.setName("Parent Department");
            
            Department savedDepartment = createMockDepartment();
            ArgumentCaptor<Department> departmentCaptor = ArgumentCaptor.forClass(Department.class);
            
            when(companyService.findById(TEST_COMPANY_ID)).thenReturn(createMockCompany());
            when(userService.findById(TEST_USER_ID)).thenReturn(createMockUser());
            when(departmentService.findById(TEST_PARENT_ID)).thenReturn(parentDepartment);
            when(departmentService.save(departmentCaptor.capture())).thenReturn(savedDepartment);
            
            // When
            departmentController.create(request);
            
            // Then
            Department capturedDepartment = departmentCaptor.getValue();
            assertNotNull(capturedDepartment.getParentDepartment());
            assertEquals(TEST_PARENT_ID, capturedDepartment.getParentDepartment().getDepartmentId());
        }
    }

    // ========== Test Data Helper Methods ==========

    private Map<String, Object> createValidDepartmentRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("name", TEST_DEPT_NAME);
        request.put("code", TEST_DEPT_CODE);
        request.put("budget", TEST_BUDGET);
        request.put("isActive", true);
        request.put("companyId", TEST_COMPANY_ID);
        request.put("managerId", TEST_USER_ID);
        return request;
    }

    private Map<String, Object> createValidUpdateRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Updated IT Department");
        request.put("code", "UIT001");
        request.put("budget", new BigDecimal("60000.00"));
        request.put("isActive", true);
        request.put("companyId", TEST_COMPANY_ID);
        return request;
    }

    private Department createMockDepartment() {
        return createMockDepartment(TEST_DEPT_NAME, TEST_DEPT_CODE);
    }

    private Department createMockDepartment(String name, String code) {
        Department department = new Department();
        department.setDepartmentId(TEST_DEPARTMENT_ID);
        department.setName(name);
        department.setCode(code);
        department.setBudget(TEST_BUDGET);
        department.setIsActive(true);
        department.setCreatedAt(LocalDateTime.now().minusDays(10));
        department.setUpdatedAt(LocalDateTime.now().minusDays(1));
        department.setCompany(createMockCompany());
        department.setManager(createMockUser());
        return department;
    }

    private Department createMockDepartmentWithParent(String name, String code, Department parent) {
        Department department = createMockDepartment(name, code);
        department.setParentDepartment(parent);
        return department;
    }

    private Department createMockDepartmentWithAllRelations() {
        Department department = createMockDepartment();
        
        // Add parent department
        Department parentDept = new Department();
        parentDept.setDepartmentId(TEST_PARENT_ID);
        parentDept.setName("Parent Department");
        parentDept.setCode("PAR001");
        department.setParentDepartment(parentDept);
        
        return department;
    }

    private Department createMockDepartmentWithoutRelations() {
        Department department = new Department();
        department.setDepartmentId(TEST_DEPARTMENT_ID);
        department.setName(TEST_DEPT_NAME);
        department.setCode(TEST_DEPT_CODE);
        department.setBudget(TEST_BUDGET);
        department.setIsActive(true);
        department.setCreatedAt(LocalDateTime.now().minusDays(10));
        department.setUpdatedAt(LocalDateTime.now().minusDays(1));
        // No relationships set
        return department;
    }

    private Company createMockCompany() {
        Company company = new Company();
        company.setCompanyId(TEST_COMPANY_ID);
        company.setCompanyName("Test Company");
        return company;
    }

    private User createMockUser() {
        User user = new User();
        user.setUserId(TEST_USER_ID);
        user.setFullName("John Doe");
        user.setUsername("john.doe");
        user.setEmail("john.doe@test.com");
        return user;
    }
}