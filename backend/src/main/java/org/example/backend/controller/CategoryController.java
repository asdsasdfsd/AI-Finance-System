// src/main/java/org/example/backend/controller/CategoryController.java
package org.example.backend.controller;

import org.example.backend.model.Category;
import org.example.backend.model.Company;
import org.example.backend.service.CategoryService;
import org.example.backend.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private CompanyService companyService;

    @GetMapping
    public List<Category> getAll() {
        return categoryService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(@PathVariable Integer id) {
        Category category = categoryService.findById(id);
        return category != null ? ResponseEntity.ok(category) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/company/{companyId}/type/{type}")
    public List<Category> getByCompanyAndType(
            @PathVariable Integer companyId,
            @PathVariable Category.CategoryType type) {
        Company company = companyService.findById(companyId);
        return categoryService.findByCompanyAndType(company, type);
    }
    
    @GetMapping("/company/{companyId}/top-level")
    public List<Category> getTopLevelCategories(@PathVariable Integer companyId) {
        Company company = companyService.findById(companyId);
        return categoryService.findTopLevelCategories(company);
    }
    
    @GetMapping("/{categoryId}/subcategories")
    public List<Category> getSubcategories(@PathVariable Integer categoryId) {
        Category parentCategory = categoryService.findById(categoryId);
        return categoryService.findSubcategories(parentCategory);
    }

    @PostMapping
    public Category create(@RequestBody Category category) {
        return categoryService.save(category);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Category> update(@PathVariable Integer id, @RequestBody Category category) {
        if (categoryService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        category.setCategoryId(id);
        return ResponseEntity.ok(categoryService.save(category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (categoryService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        categoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}