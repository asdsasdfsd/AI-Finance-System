// backend/src/main/java/org/example/backend/controller/FixedAssetController.java
package org.example.backend.controller;

import org.example.backend.model.Company;
import org.example.backend.model.Department;
import org.example.backend.model.FixedAsset;
import org.example.backend.service.CompanyService;
import org.example.backend.service.DepartmentService;
import org.example.backend.service.FixedAssetService;
import org.example.backend.util.JwtContextUtil;
import org.example.backend.exception.MissingCompanyIdException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fixed-assets")
@CrossOrigin(origins = "http://localhost:3000")
public class FixedAssetController {

    @Autowired
    private FixedAssetService fixedAssetService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private JwtContextUtil jwtContextUtil;

    // Get all assets for current user's company (from JWT)
    @GetMapping
    public ResponseEntity<List<FixedAsset>> getAll() {
        Integer companyId = getCurrentCompanyId();
        Company company = companyService.findById(companyId);
        if (company == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(fixedAssetService.findByCompany(company));
    }

    // Get asset by ID (with company validation)
    @GetMapping("/{id}")
    public ResponseEntity<FixedAsset> getById(@PathVariable Integer id) {
        Integer companyId = getCurrentCompanyId();
        FixedAsset asset = fixedAssetService.findById(id);
        
        if (asset == null || !asset.getCompany().getCompanyId().equals(companyId)) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(asset);
    }

    // Get assets for current user's company
    @GetMapping("/company")
    public ResponseEntity<List<FixedAsset>> getByCompany() {
        Integer companyId = getCurrentCompanyId();
        Company company = companyService.findById(companyId);
        if (company == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(fixedAssetService.findByCompany(company));
    }

    // Backward compatibility - allow companyId parameter but validate against JWT
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<FixedAsset>> getByCompanyId(@PathVariable Integer companyId) {
        Integer jwtCompanyId = getCurrentCompanyId();
        
        // Security check: ensure requested company matches user's company
        if (!jwtCompanyId.equals(companyId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        Company company = companyService.findById(companyId);
        if (company == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(fixedAssetService.findByCompany(company));
    }

    // Get assets by department (within current user's company)
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<FixedAsset>> getByDepartment(@PathVariable Integer departmentId) {
        Integer companyId = getCurrentCompanyId();
        Department department = departmentService.findById(departmentId);
        
        // Validate department belongs to user's company
        if (department == null || !department.getCompany().getCompanyId().equals(companyId)) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(fixedAssetService.findByDepartment(department));
    }

    // Get assets by status (within current user's company)
    @GetMapping("/status/{status}")
    public ResponseEntity<List<FixedAsset>> getByStatus(@PathVariable FixedAsset.AssetStatus status) {
        Integer companyId = getCurrentCompanyId();
        Company company = companyService.findById(companyId);
        if (company == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Filter by status and company
        List<FixedAsset> allStatusAssets = fixedAssetService.findByStatus(status);
        List<FixedAsset> companyStatusAssets = allStatusAssets.stream()
            .filter(asset -> asset.getCompany().getCompanyId().equals(companyId))
            .toList();
            
        return ResponseEntity.ok(companyStatusAssets);
    }

    // Create asset (auto-assign to current user's company)
    @PostMapping
    public ResponseEntity<FixedAsset> create(@RequestBody FixedAsset fixedAsset) {
        Integer companyId = getCurrentCompanyId();
        Company company = companyService.findById(companyId);
        
        if (company == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // Auto-assign company from JWT
        fixedAsset.setCompany(company);
        
        // Validate department belongs to same company if specified
        if (fixedAsset.getDepartment() != null) {
            Department department = departmentService.findById(fixedAsset.getDepartment().getDepartmentId());
            if (department == null || !department.getCompany().getCompanyId().equals(companyId)) {
                return ResponseEntity.badRequest().build();
            }
        }
        
        return ResponseEntity.ok(fixedAssetService.save(fixedAsset));
    }

    // Update asset (with company validation)
    @PutMapping("/{id}")
    public ResponseEntity<FixedAsset> update(@PathVariable Integer id, @RequestBody FixedAsset fixedAsset) {
        Integer companyId = getCurrentCompanyId();
        FixedAsset existing = fixedAssetService.findById(id);
        
        if (existing == null || !existing.getCompany().getCompanyId().equals(companyId)) {
            return ResponseEntity.notFound().build();
        }
        
        fixedAsset.setAssetId(id);
        fixedAsset.setCompany(existing.getCompany()); // Preserve original company
        
        // Validate department belongs to same company if specified
        if (fixedAsset.getDepartment() != null) {
            Department department = departmentService.findById(fixedAsset.getDepartment().getDepartmentId());
            if (department == null || !department.getCompany().getCompanyId().equals(companyId)) {
                return ResponseEntity.badRequest().build();
            }
        }
        
        return ResponseEntity.ok(fixedAssetService.save(fixedAsset));
    }

    // Delete asset (with company validation)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        Integer companyId = getCurrentCompanyId();
        FixedAsset existing = fixedAssetService.findById(id);
        
        if (existing == null || !existing.getCompany().getCompanyId().equals(companyId)) {
            return ResponseEntity.notFound().build();
        }
        
        fixedAssetService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Helper method to get company ID from JWT with proper error handling
    private Integer getCurrentCompanyId() {
        Integer companyId = jwtContextUtil.getCurrentCompanyId();
        if (companyId == null) {
            throw new MissingCompanyIdException(
                "Company ID not found in JWT token. Please ensure you are properly authenticated."
            );
        }
        return companyId;
    }
}