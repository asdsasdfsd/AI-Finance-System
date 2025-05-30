// src/main/java/org/example/backend/repository/JournalEntryRepository.java
package org.example.backend.repository;

import org.example.backend.model.Company;
import org.example.backend.model.Fund;
import org.example.backend.model.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Integer> {
    List<JournalEntry> findByCompany(Company company);
    List<JournalEntry> findByCompanyAndEntryDateBetween(Company company, LocalDate startDate, LocalDate endDate);
    List<JournalEntry> findByFund(Fund fund);
}