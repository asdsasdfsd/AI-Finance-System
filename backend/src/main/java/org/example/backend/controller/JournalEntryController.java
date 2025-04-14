// src/main/java/org/example/backend/controller/JournalEntryController.java
package org.example.backend.controller;

import org.example.backend.model.Company;
import org.example.backend.model.Fund;
import org.example.backend.model.JournalEntry;
import org.example.backend.model.JournalLine;
import org.example.backend.service.CompanyService;
import org.example.backend.service.FundService;
import org.example.backend.service.JournalEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/journal-entries")
public class JournalEntryController {
    @Autowired
    private JournalEntryService journalEntryService;
    
    @Autowired
    private CompanyService companyService;
    
    @Autowired
    private FundService fundService;

    @GetMapping
    public List<JournalEntry> getAll() {
        return journalEntryService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<JournalEntry> getById(@PathVariable Integer id) {
        JournalEntry journalEntry = journalEntryService.findById(id);
        return journalEntry != null ? ResponseEntity.ok(journalEntry) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/company/{companyId}")
    public List<JournalEntry> getByCompany(@PathVariable Integer companyId) {
        Company company = companyService.findById(companyId);
        return journalEntryService.findByCompany(company);
    }
    
    @GetMapping("/company/{companyId}/date-range")
    public List<JournalEntry> getByDateRange(
            @PathVariable Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Company company = companyService.findById(companyId);
        return journalEntryService.findByDateRange(company, startDate, endDate);
    }
    
    @GetMapping("/fund/{fundId}")
    public List<JournalEntry> getByFund(@PathVariable Integer fundId) {
        Fund fund = fundService.findById(fundId);
        return journalEntryService.findByFund(fund);
    }

    @PostMapping
    public JournalEntry create(@RequestBody JournalEntry journalEntry) {
        return journalEntryService.save(journalEntry);
    }
    
    @PostMapping("/with-lines")
    public JournalEntry createWithLines(@RequestBody Map<String, Object> payload) {
        JournalEntry journalEntry = new JournalEntry();
        // Map payload fields to journalEntry
        
        List<JournalLine> lines = null;
        // Extract lines from payload
        
        return journalEntryService.saveWithLines(journalEntry, lines);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<JournalEntry> update(@PathVariable Integer id, @RequestBody JournalEntry journalEntry) {
        if (journalEntryService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        journalEntry.setEntryId(id);
        return ResponseEntity.ok(journalEntryService.save(journalEntry));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (journalEntryService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        journalEntryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}