package org.example.backend.service;

import org.example.backend.model.OutcomeCategory;
import org.example.backend.repository.OutcomeCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OutcomeCategoryService {
    @Autowired
    private OutcomeCategoryRepository categoryRepository;

    public List<OutcomeCategory> findAll() { return categoryRepository.findAll(); }
    public OutcomeCategory findById(Integer id) { return categoryRepository.findById(id).orElse(null); }
    public OutcomeCategory save(OutcomeCategory category) { return categoryRepository.save(category); }
    public void deleteById(Integer id) { categoryRepository.deleteById(id); }
}