package org.example.backend.controller;

import org.example.backend.model.Income;
import org.example.backend.service.IncomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incomes")
public class IncomeController {
    @Autowired
    private IncomeService incomeService;

    @GetMapping
    public List<Income> getAll() {
        return incomeService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Income> getById(@PathVariable Integer id) {
        Income income = incomeService.findById(id);
        return income != null ? ResponseEntity.ok(income) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public Income create(@RequestBody Income income) {
        return incomeService.save(income);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        incomeService.deleteById(id);
    }
}