package org.example.backend.controller;

import org.example.backend.model.IncomeCategory;
import org.example.backend.service.IncomeCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/income-categories")
public class IncomeCategoryController {
    @Autowired
    private IncomeCategoryService categoryService;

    @GetMapping
    public List<IncomeCategory> getAll() { return categoryService.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<IncomeCategory> getById(@PathVariable Integer id) {
        IncomeCategory category = categoryService.findById(id);
        return category != null ? ResponseEntity.ok(category) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public IncomeCategory create(@RequestBody IncomeCategory category) { return categoryService.save(category); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) { categoryService.deleteById(id); }
}