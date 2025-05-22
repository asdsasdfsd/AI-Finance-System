// src/main/java/org/example/backend/controller/FixedAssetController.java
package org.example.backend.controller;

import org.example.backend.model.Company;
import org.example.backend.model.Department;
import org.example.backend.model.FixedAsset;
import org.example.backend.service.CompanyService;
import org.example.backend.service.DepartmentService;
import org.example.backend.service.FixedAssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fixed-assets")
public class FixedAssetController {

    @Autowired
    private FixedAssetService fixedAssetService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public List<FixedAsset> getAll() {
        return fixedAssetService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FixedAsset> getById(@PathVariable Integer id) {
        FixedAsset fixedAsset = fixedAssetService.findById(id);
        return fixedAsset != null ? ResponseEntity.ok(fixedAsset) : ResponseEntity.notFound().build();
    }

    @GetMapping("/company/{companyId}")
    public List<FixedAsset> getByCompany(@PathVariable Integer companyId) {
        Company company = companyService.findById(companyId);
        return fixedAssetService.findByCompany(company);
    }

    @GetMapping("/department/{departmentId}")
    public List<FixedAsset> getByDepartment(@PathVariable Integer departmentId) {
        Department department = departmentService.findById(departmentId);
        return fixedAssetService.findByDepartment(department);
    }

    @GetMapping("/status/{status}")
    public List<FixedAsset> getByStatus(@PathVariable FixedAsset.AssetStatus status) {
        return fixedAssetService.findByStatus(status);
    }

    @PostMapping
    public FixedAsset create(@RequestBody FixedAsset fixedAsset) {
        return fixedAssetService.save(fixedAsset);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FixedAsset> update(@PathVariable Integer id, @RequestBody FixedAsset fixedAsset) {
        if (fixedAssetService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        fixedAsset.setAssetId(id);
        return ResponseEntity.ok(fixedAssetService.save(fixedAsset));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (fixedAssetService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        fixedAssetService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
