package org.example.backend.controller;

import org.example.backend.model.Outcome;
import org.example.backend.service.OutcomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/outcomes")
public class OutcomeController {
    @Autowired
    private OutcomeService outcomeService;

    @GetMapping
    public List<Outcome> getAll() { return outcomeService.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Outcome> getById(@PathVariable Integer id) {
        Outcome outcome = outcomeService.findById(id);
        return outcome != null ? ResponseEntity.ok(outcome) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public Outcome create(@RequestBody Outcome outcome) { return outcomeService.save(outcome); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) { outcomeService.deleteById(id); }
}
