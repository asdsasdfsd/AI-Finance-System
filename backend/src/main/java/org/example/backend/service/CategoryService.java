// src/main/java/org/example/backend/service/CategoryService.java
package org.example.backend.service;

import org.example.backend.model.Category;
import org.example.backend.model.Company;
import org.example.backend.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> findAll() { 
        return categoryRepository.findAll(); 
    }
    
    public Category findById(Integer id) { 
        return categoryRepository.findById(id).orElse(null); 
    }
    
    public List<Category> findByCompanyAndType(Company company, Category.CategoryType type) {
        return categoryRepository.findByCompanyAndType(company, type);
    }
    
    public List<Category> findTopLevelCategories(Company company) {
        return categoryRepository.findByCompanyAndParentCategoryIsNull(company);
    }
    
    public List<Category> findSubcategories(Category parentCategory) {
        return categoryRepository.findByParentCategory(parentCategory);
    }
    
    public Category save(Category category) { 
        if (category.getCreatedAt() == null) {
            category.setCreatedAt(LocalDateTime.now());
        }
        category.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(category); 
    }
    
    public void deleteById(Integer id) { 
        categoryRepository.deleteById(id); 
    }
}