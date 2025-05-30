// src/main/java/org/example/backend/repository/CategoryRepository.java
package org.example.backend.repository;

import org.example.backend.model.Category;
import org.example.backend.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findByCompanyAndType(Company company, Category.CategoryType type);
    List<Category> findByCompanyAndParentCategoryIsNull(Company company);
    List<Category> findByParentCategory(Category parentCategory);
}