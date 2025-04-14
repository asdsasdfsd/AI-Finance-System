// src/main/java/org/example/backend/service/JournalEntryService.java
package org.example.backend.service;

import org.example.backend.model.Company;
import org.example.backend.model.Fund;
import org.example.backend.model.JournalEntry;
import org.example.backend.model.JournalLine;
import org.example.backend.repository.JournalEntryRepository;
import org.example.backend.repository.JournalLineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class JournalEntryService {
    @Autowired
    private JournalEntryRepository journalEntryRepository;
    
    @Autowired
    private JournalLineRepository journalLineRepository;

    public List<JournalEntry> findAll() {
        return journalEntryRepository.findAll();
    }

    public JournalEntry findById(Integer id) {
        return journalEntryRepository.findById(id).orElse(null);
    }
    
    public List<JournalEntry> findByCompany(Company company) {
        return journalEntryRepository.findByCompany(company);
    }
    
    public List<JournalEntry> findByDateRange(Company company, LocalDate startDate, LocalDate endDate) {
        return journalEntryRepository.findByCompanyAndEntryDateBetween(company, startDate, endDate);
    }
    
    public List<JournalEntry> findByFund(Fund fund) {
        return journalEntryRepository.findByFund(fund);
    }

    @Transactional
    public JournalEntry save(JournalEntry journalEntry) {
        if (journalEntry.getCreatedAt() == null) {
            journalEntry.setCreatedAt(LocalDateTime.now());
        }
        journalEntry.setUpdatedAt(LocalDateTime.now());
        return journalEntryRepository.save(journalEntry);
    }
    
    @Transactional
    public JournalEntry saveWithLines(JournalEntry journalEntry, List<JournalLine> lines) {
        JournalEntry savedEntry = save(journalEntry);
        
        if (lines != null) {
            lines.forEach(line -> {
                line.setJournalEntry(savedEntry);
                journalLineRepository.save(line);
            });
        }
        
        return savedEntry;
    }

    @Transactional
    public void deleteById(Integer id) {
        journalEntryRepository.deleteById(id);
    }
}