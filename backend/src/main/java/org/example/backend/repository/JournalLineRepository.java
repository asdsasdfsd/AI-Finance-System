// src/main/java/org/example/backend/repository/JournalLineRepository.java
package org.example.backend.repository;

import org.example.backend.model.Account;
import org.example.backend.model.JournalEntry;
import org.example.backend.model.JournalLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalLineRepository extends JpaRepository<JournalLine, Integer> {
    List<JournalLine> findByJournalEntry(JournalEntry journalEntry);
    List<JournalLine> findByAccount(Account account);
}