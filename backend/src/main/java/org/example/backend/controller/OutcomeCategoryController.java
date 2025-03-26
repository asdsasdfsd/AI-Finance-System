package org.example.backend.controller;

import org.example.backend.model.OutcomeCategory;
import org.example.backend.service.OutcomeCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/outcome-categories")
public class OutcomeCategoryController {
    @Autowired
    private OutcomeCategoryService categoryService;

    @GetMapping
    public List<OutcomeCategory> getAll() { return categoryService.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<OutcomeCategory> getById(@PathVariable Integer id) {
        OutcomeCategory category = categoryService.findById(id);
        return category != null ? ResponseEntity.ok(category) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public OutcomeCategory create(@RequestBody OutcomeCategory category) { return categoryService.save(category); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) { categoryService.deleteById(id); }
}