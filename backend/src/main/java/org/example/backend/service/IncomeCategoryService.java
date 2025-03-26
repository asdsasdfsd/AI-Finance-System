package org.example.backend.service;

import org.example.backend.model.IncomeCategory;
import org.example.backend.repository.IncomeCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncomeCategoryService {
    @Autowired
    private IncomeCategoryRepository categoryRepository;

    public List<IncomeCategory> findAll() { return categoryRepository.findAll(); }
    public IncomeCategory findById(Integer id) { return categoryRepository.findById(id).orElse(null); }
    public IncomeCategory save(IncomeCategory category) { return categoryRepository.save(category); }
    public void deleteById(Integer id) { categoryRepository.deleteById(id); }
}
